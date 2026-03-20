# Phase 40. Notification Outbox와 외부 Incident Channel

## 1. 배경

이전 구조에서 알림은 `notification` row를 저장한 직후 `NotificationDispatchService`가 외부 채널을 동기 호출하는 방식이었다.

이 방식은 두 가지 한계를 갖고 있었다.

1. 외부 webhook/email/push 전송 실패가 애플리케이션 로그에만 남고, 재시도/운영 추적 지점이 없었다.
2. 반복 로그인 실패 같은 보안 이벤트는 앱 내 알림까지만 닫혀 있었고, 운영자가 별도 incident 채널로 받는 흐름이 없었다.

포트폴리오 관점에서 이 상태는 "알림 기능은 있지만 전달 신뢰성과 incident 대응은 약하다"는 인상을 줄 수 있다.

## 1-1. Before / After

| 구분 | Before | After |
|---|---|---|
| in-app 알림 | `notification` 저장 | 동일 |
| 외부 전달 | 저장 직후 동기 호출 | `notification_outbox` 적재 후 worker 비동기 전달 |
| 실패 처리 | warn 로그 | retry / backoff / dead-letter |
| auth anomaly | principal in-app 알림 | principal in-app 알림 + incident webhook fan-out |
| 운영 확인 | 감사 로그/그래프는 별도 | 감사 로그 -> outbox 상태 -> incident webhook 설명 흐름 가능 |

## 2. 이번 결정

### 2-1. in-app 알림 저장과 외부 전달을 분리

- 기존 `notification` 테이블은 그대로 유지한다.
- 앱 내부 알림 저장은 기존처럼 즉시 완료한다.
- 외부 채널 전달만 `notification_outbox`에 적재해 별도 worker가 비동기로 처리한다.

즉 "사용자에게 알림이 생겼는가"와 "외부 시스템 전달이 성공했는가"를 같은 트랜잭션 책임으로 묶지 않았다.

### 2-2. outbox는 선택된 알림 타입만 fan-out

- `notification.delivery.external-types`
  - 수신자 대상 외부 채널(email/push/app)로 fan-out 할 알림 타입
- `notification.delivery.incident-types`
  - 운영 incident webhook까지 fan-out 할 알림 타입

이번 배치에서는 `AUTH_ANOMALY_DETECTED`를 기본 critical type으로 두었다.

즉 반복 로그인 실패 경보는

1. 원장에게 앱 내 알림을 저장하고
2. 필요하면 동일 알림을 외부 receiver channel로 fan-out 하고
3. 운영 incident webhook으로도 별도 전달한다.

### 2-3. 전달 실패는 retry/dead-letter로 관리

- `PENDING -> PROCESSING -> DELIVERED`
- 실패 시 `PENDING`으로 되돌리며 backoff retry
- 최대 시도 횟수를 넘기면 `DEAD_LETTER`
- `processing_started_at`이 timeout을 넘긴 row는 stale processing으로 보고 다시 claim

즉 "실패했는가"만 남기지 않고, 언제 다시 보낼지와 최종 포기 상태까지 데이터로 남긴다.

### 2-4. auth incident 대응 흐름을 하나의 스토리로 연결

이번 배치 이후 인증 이상 징후 흐름은 아래처럼 설명할 수 있다.

1. 로그인 실패가 누적되면 `auth_audit_log`에 기록된다.
2. threshold를 넘으면 principal에게 `AUTH_ANOMALY_DETECTED` in-app 알림이 생성된다.
3. 같은 알림이 `notification_outbox`에 적재된다.
4. worker가 retry/dead-letter 정책으로 incident webhook 전달을 시도한다.
5. 운영자는 감사 로그 화면/CSV/Grafana를 통해 사건을 조회하고 추적할 수 있다.

## 3. 구현 포인트

### schema / entity

- `V12__add_notification_outbox.sql`
  - `notification_outbox` 테이블 추가
  - `(notification_id, channel)` unique constraint
  - `(status, next_attempt_at, id)`, `(status, processing_started_at, id)` 인덱스 추가
- `NotificationOutbox`
  - 수신자/제목/내용/링크/타입 snapshot 저장
  - attempt count, retry 시각, dead-letter 시각, last error 저장

### dispatch / policy

- `NotificationDeliveryPolicyService`
  - 알림 타입별 receiver channel / incident channel 결정
- `NotificationDispatchService`
  - 알림 생성 시 outbox 적재
  - scheduled worker로 ready batch claim
  - sender 예외를 흡수하지 않고 retry/dead-letter 상태로 전이

### channel

- `NotificationChannel`
  - `INCIDENT_WEBHOOK` 추가
- `IncidentWebhookNotificationSender`
  - Slack-compatible webhook payload 전송
- `AppNotificationSender`, `PushNotificationSender`, `EmailNotificationSender`
  - 실패를 warn-only로 삼키지 않고 예외를 surface

### auth anomaly 연동

- `NotificationType.AUTH_ANOMALY_DETECTED` 추가
- `AuthAnomalyAlertService`
  - 반복 로그인 실패 임계치 도달 시 `AUTH_ANOMALY_DETECTED` 알림 생성
  - outbox policy가 receiver channel + incident webhook fan-out을 담당

## 4. 트레이드오프

### 장점

- 외부 채널 전달 실패를 재시도/포기 상태로 운영 추적할 수 있다.
- 앱 내 알림 저장은 빠르게 끝내고, 느리거나 불안정한 외부 호출은 비동기로 분리할 수 있다.
- 보안 이벤트를 앱 내부 UI와 외부 incident 채널 양쪽으로 전달할 수 있다.

### 비용

- 현재 claim 전략은 DB lock 기반이 아니라 조회 후 상태 전이 방식이라 단일 앱 인스턴스 기준에 더 적합하다.
- 멀티 인스턴스/클러스터에서 완전한 중복 방지를 원하면 `FOR UPDATE SKIP LOCKED`나 전용 큐를 추가 검토해야 한다.
- outbox snapshot을 유지하므로 저장 비용과 스키마 복잡도가 조금 늘어난다.

이번 배치에서는 "실서비스 감각을 보여주는 최소 운영형 구조"를 우선했고, cluster-safe locking은 다음 단계로 남겼다.

### 지금 운영자가 볼 수 있는 것

- 감사 로그 화면과 CSV export
- principal in-app 이상 징후 알림
- Prometheus/Grafana의 auth event 지표

### 아직 없는 것

- outbox 전용 운영 콘솔
- 멀티 인스턴스 안전한 claim/중복 방지 lock

## 5. 검증

실행 명령:

```bash
./gradlew --stop
pkill -f 'Gradle Test Executor|gradle-wrapper.jar test|worker.org.gradle' || true
./gradlew --no-daemon compileJava compileTestJava
./gradlew --no-daemon cleanTest test --tests "com.erp.integration.NotificationOutboxIntegrationTest" --tests "com.erp.integration.NotificationOutboxRetryIntegrationTest" --tests "com.erp.integration.AuthAnomalyIncidentChannelIntegrationTest" --tests "com.erp.integration.AuthAnomalyIncidentChannelRetryIntegrationTest"
./gradlew --no-daemon cleanTest test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest"
```

검증한 내용:

- `SYSTEM` 알림이 outbox에 적재되고 app webhook 성공 시 `DELIVERED`
- 외부 webhook 실패 시 retry 후 `DEAD_LETTER`
- 반복 로그인 실패 임계치 도달 시 `AUTH_ANOMALY_DETECTED` 알림이 incident webhook outbox에 적재
- auth anomaly cooldown 동안 중복 incident row가 쌓이지 않음
- 기존 알림 조회 API는 새 타입(`AUTH_ANOMALY_DETECTED`)으로 정상 노출

## 6. 면접에서 말할 포인트

- "앱 내 알림 저장과 외부 전달을 분리해서, 느린 외부 채널 때문에 핵심 트랜잭션이 흔들리지 않게 했습니다."
- "로그만 남기고 끝내지 않고 retry/dead-letter를 DB 상태로 남겨 운영자가 실패를 추적할 수 있게 했습니다."
- "반복 로그인 실패는 앱 내 원장 알림 + incident webhook으로 fan-out 되도록 설계해 보안 이벤트 대응 흐름을 닫았습니다."
- "현재는 단일 인스턴스 기준 outbox worker이고, 멀티 인스턴스 중복 claim 문제는 다음 단계에서 lock 전략으로 확장할 수 있다고 설명할 수 있습니다."
