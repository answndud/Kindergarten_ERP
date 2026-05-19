# Backend Portfolio Interview Guide

기준일: 2026-05-19

이 문서는 면접관이 Kindergarten ERP를 빠르게 평가할 때 볼 수 있는 백엔드 포트폴리오 설명 가이드입니다.

## 1. 5분 설명 루트

1. `README.md`의 "핵심 문제와 해결"과 "수치로 검증한 개선"을 먼저 봅니다.
2. `docs/guides/demo-scenario.md`에서 5분/10분 시연 순서를 확인합니다.
3. `docs/COMPLETED.md`에서 작업 archive를 확인합니다.
4. 코드에서는 `global/security`, `global/exception`, `domain/notification`, `domain/calendar`, `domain/attendance`를 확인합니다.
5. 테스트에서는 `src/test/java/com/erp/api/*IntegrationTest.java`, `src/test/java/com/erp/integration/*`, `src/test/java/com/erp/performance/*`를 확인합니다.

## 2. 10분 시연 루트

1. `demo` 프로파일로 실행하고 원장 계정으로 로그인합니다.
2. `/applications/pending`에서 입학 신청/대기열/offer 흐름을 설명합니다.
3. `/attendance-requests`에서 학부모 요청과 교사/원장 승인 경계를 설명합니다.
4. `/notification-outbox`에서 dead-letter summary, 목록, retry 버튼을 보여줍니다.
5. `/audit-logs`, `/domain-audit-logs`에서 인증 이벤트와 업무 상태 전이가 archive되는 방식을 보여줍니다.
6. README의 Notepad/Dashboard query count 개선표와 CI `5m 28s -> 1m 14s` 지표로 성능/검증 스토리를 마무리합니다.

## 3. 면접 질문 대응 포인트

| 질문 | 답변 방향 | 확인 위치 |
| --- | --- | --- |
| 왜 cookie JWT와 Redis를 같이 썼나요? | access token은 HTTP-only cookie로 노출면을 줄이고, refresh/session revoke는 Redis TTL과 active session registry로 제어합니다. | `global/security`, `domain/auth` |
| tenant 경계는 어디서 보장하나요? | Controller 권한, service requester 검증, repository 조회 조건, 통합 테스트를 같이 둡니다. | `AccessPolicyService`, `*ApiIntegrationTest` |
| 운영 중 알림 전송 실패는 어떻게 보나요? | outbox 상태 전이, dead-letter 목록, 원장 전용 retry API/화면으로 관측과 재처리를 분리합니다. | `/notification-outbox`, `domain/notification` |
| 배포 전 CORS는 어떻게 바꾸나요? | `CORS_ALLOWED_ORIGINS`로 환경별 origin을 주입하고 credentialed CORS에서 wildcard를 쓰지 않습니다. | `SecurityConfig`, `env-contract.md` |
| 큰 service는 어떻게 관리했나요? | `KidApplicationService`는 orchestration만 남기고 admission, notification, audit 보조 책임을 분리했습니다. | `domain/kidapplication/service` |
| 성능 개선은 어떻게 증명했나요? | query count/elapsed time을 전후 측정하고 performance smoke test와 archive에 남겼습니다. | README, `performance/*`, `docs/COMPLETED.md` |

## 4. 최근 강화 작업의 의도

### 입력/예외 하드닝

- 문제: 잘못된 query/path parameter, 필수 파라미터 누락, 날짜 변환 오류가 generic 500으로 떨어질 수 있었습니다.
- 결정: Spring MVC 입력 경계 예외를 `GlobalExceptionHandler`에서 명시적으로 400 `ApiResponse.error`로 닫았습니다.
- 검증: 출석 월 조회 invalid month, missing year, invalid year type 통합 테스트를 추가했습니다.
- 트레이드오프: 모든 `IllegalArgumentException`을 400으로 잡지 않고, MVC 입력 오류와 날짜 오류 중심으로 제한했습니다.

### 캘린더 반복/범위 안전장치

- 문제: 반복 일정 확장 로직이 `CalendarEventService`에 섞여 있었고, 긴 조회 범위를 제한하지 않았습니다.
- 결정: `RecurrenceExpander`로 반복 occurrence 확장을 분리하고, 일정 목록 조회 기간을 최대 366일로 제한했습니다.
- 검증: fast unit test로 반복 확장을 고정하고, 통합 테스트로 366일 초과 조회가 400인지 확인했습니다.
- 트레이드오프: 신규 반복 규칙을 추가하지 않고 기존 `DAILY/WEEKLY/MONTHLY` 동작 보존에 집중했습니다.

### Notification Outbox 운영 API

- 문제: outbox는 retry/backoff/dead-letter 상태 전이를 갖췄지만, 운영자가 실패 건을 확인하고 재시도하는 API가 없었습니다.
- 결정: 원장 전용 `/api/v1/notification-outbox` 운영 API와 `/notification-outbox` 화면을 추가해 summary, dead-letter 목록, 수동 retry를 제공합니다.
- 검증: 원장 성공, 교사 접근 차단, dead-letter 재시도 상태 전이, view endpoint 접근을 통합 테스트로 확인했습니다.
- 트레이드오프: 실제 외부 채널 발송 정책은 건드리지 않고, 실패한 outbox를 다시 `PENDING`으로 돌리는 운영면만 확장했습니다.

### CORS 운영 설정

- 문제: allowed origin이 `http://localhost:8080`으로 하드코딩되어 실제 배포 도메인 전환 시 코드 변경이 필요했습니다.
- 결정: `app.security.cors.allowed-origins`와 `CORS_ALLOWED_ORIGINS`로 분리했습니다.
- 검증: property override가 `CorsConfigurationSource`에 반영되는지 context 테스트로 확인했습니다.
- 트레이드오프: credentialed CORS를 유지하므로 wildcard origin은 허용하지 않습니다.

### KidApplication workflow 분리

- 문제: 입학 신청 service에 상태 전이, 원생 생성, 학부모 활성화, 알림, audit 기록이 함께 섞여 있었습니다.
- 결정: admission, notification, audit 보조 service를 추출하고 `KidApplicationService`는 orchestration 중심으로 남겼습니다.
- 검증: 기존 `KidApplicationApiIntegrationTest`로 approve/waitlist/offer/accept/권한 동작을 보존했습니다.
- 트레이드오프: public API와 DB schema는 건드리지 않는 보존형 리팩토링으로 제한했습니다.

### 코드 탐색 노이즈 제거

- 문제: 비어 있는 `MemberRepositoryCustom/Impl` 스텁과 로컬 swap/backup 파일이 코드 탐색을 방해했습니다.
- 결정: 사용되지 않는 custom repository 스텁을 제거하고 로컬 ignored 작업파일을 삭제했습니다.
- 검증: 컴파일과 targeted tests로 repository wiring 회귀를 확인합니다.

## 5. 면접에서 강조할 답변 포인트

- 보안: HTTP-only cookie JWT, Redis refresh session, active session revoke, fail-closed profile.
- 권한: `PRINCIPAL`, `TEACHER`, `PARENT` 역할과 유치원 tenant 경계를 API/service/test에서 함께 검증.
- 운영성: audit log, outbox retry/dead-letter, Prometheus/Grafana, readiness, structured logging.
- 성능: Notepad/Dashboard N+1 제거, query count와 응답 시간 전후 비교, push CI 경량화.
- 테스트: Testcontainers 기반 MySQL/Redis 통합 테스트, fast/integration/performance smoke 분리.
- 문서화: `docs/PLAN.md`, `docs/PROGRESS.md`, `docs/COMPLETED.md`로 active/archive 상태 관리.

## 6. 남은 리스크와 후속 개선

- 실제 클라우드 배포는 비용 문제로 아직 실행하지 않았고, 배포 자산과 runbook 중심으로 준비된 상태입니다.
- Notification Outbox 운영 화면은 dead-letter 중심입니다. 전체 outbox timeline이나 channel별 drill-down은 후속 개선입니다.
- `KidApplicationService`는 보조 책임을 분리했지만, 더 큰 규모에서는 상태 전이 전용 workflow component까지 분리할 수 있습니다.
- 실제 운영 도메인이 생기면 `CORS_ALLOWED_ORIGINS`, OAuth redirect URI, secure cookie, reverse proxy 설정을 함께 검증해야 합니다.
