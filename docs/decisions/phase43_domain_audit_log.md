# Phase 43. 업무 감사 로그(`domain_audit_log`) 도입

## 1. 배경

이전까지 감사 로그는 로그인/refresh/소셜 연결 같은 인증 사건에 집중돼 있었다.

하지만 운영 관점에서 실제로 책임 추적이 필요한 건

- 입학 신청 waitlist/offer/승인/만료
- 출결 요청 승인/거절/취소
- 공지 수정/삭제
- 교사 지원 승인/거절

같은 비즈니스 상태 변경이다.

즉 "누가 언제 로그인했는가"와
"누가 언제 어떤 업무 상태를 바꿨는가"는 목적이 다르다.

## 1-1. Before / After

| 구분 | Before | After |
|---|---|---|
| 감사 대상 | 인증 사건 중심 | 인증 사건 + 업무 상태 전이 분리 |
| 저장 모델 | `auth_audit_log` | `auth_audit_log` + `domain_audit_log` |
| principal 조회 | 인증 감사 화면만 존재 | 인증 감사 + 업무 감사 화면 분리 |
| export | 인증 감사 CSV | 업무 감사 CSV 추가 |

## 2. 이번 결정

### 2-1. 인증 감사와 업무 감사를 분리한다

- `auth_audit_log`
  - 로그인, refresh, 소셜 연결/해제, rate limit, anomaly 대응
- `domain_audit_log`
  - 입학/출결/공지/지원 등 비즈니스 상태 전이

즉 보안 사건 분석과 운영 책임 추적의 목적을 같은 테이블에 섞지 않았다.

### 2-2. 업무 감사 로그는 비즈니스 트랜잭션과 함께 커밋한다

초기 구현에서는 `REQUIRES_NEW`를 시도했지만, 테스트 트랜잭션/foreign key lock과 충돌해 lock wait timeout이 발생했다.

이번 배치에서는 업무 감사 로그를 원래 비즈니스 트랜잭션에 참여시키도록 바꿨다.

이렇게 해서
- 상태 변경과 감사 로그가 원자적으로 커밋되고
- "업무는 성공했는데 로그만 빠지는" 불일치를 줄였다.

### 2-3. principal 전용 조회/CSV/UI를 제공한다

- `/api/v1/domain-audit-logs`
- `/api/v1/domain-audit-logs/export`
- `/domain-audit-logs`

즉 저장만 하는 로그가 아니라,
운영자가 실제로 볼 수 있는 콘솔까지 같이 닫았다.

## 3. 구현 포인트

### schema / entity

- `domain_audit_log`
  - `kindergarten_id`
  - `actor_id`, `actor_name`, `actor_role`
  - `action`, `target_type`, `target_id`
  - `summary`, `metadata_json`
- `DomainAuditAction`
  - 입학 신청 waitlist/offer/승인/만료
  - 교사 지원 승인/거절
  - 공지 수정/삭제
  - 출결 요청 제출/승인/거절/취소
- `DomainAuditTargetType`
  - `KID_APPLICATION`
  - `KINDERGARTEN_APPLICATION`
  - `ANNOUNCEMENT`
  - `ATTENDANCE_CHANGE_REQUEST`

### write path

- `KidApplicationService`
- `AttendanceChangeRequestService`
- `KindergartenApplicationService`
- `AnnouncementService`

에서 각 상태 전이 직후 `DomainAuditLogService.record(...)` 호출

### read path

- `DomainAuditLogQueryService`
  - action / targetType / actorName / 기간 필터
  - principal의 kindergarten 기준 pagination
  - CSV export
- `domainaudit/audit-logs.html`
  - filter / table / export 버튼

## 4. 트레이드오프

### 장점

- 입학/출결/공지 같은 핵심 업무 변경을 중앙에서 추적할 수 있다.
- 인증 감사 로그와 분리해 각 로그의 목적이 명확해졌다.
- CSV export와 UI까지 있어 데모/면접에서 바로 보여주기 좋다.

### 비용

- 이벤트 수가 늘어나면서 문서화와 액션 enum 관리가 필요하다.
- 현재는 summary + metadata JSON 구조라, 고정 스키마 분석용 DW 모델은 아니다.
- principal 전용 조회만 제공하고 teacher용 부분 조회는 아직 열지 않았다.

## 5. 검증

실행 명령:

```bash
./gradlew --no-daemon compileJava compileTestJava
./gradlew --no-daemon test --tests "com.erp.api.DomainAuditApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"
```

검증한 내용:

- waitlist 액션이 업무 감사 로그에 저장되고 principal이 필터 조회 가능
- CSV export 정상 동작
- teacher의 업무 감사 로그 조회 차단
- principal `/domain-audit-logs` 화면 렌더링

## 6. 면접에서 말할 포인트

- "인증 감사 로그와 업무 감사 로그는 목적이 달라서 분리했습니다."
- "업무 감사 로그는 비즈니스 트랜잭션에 참여시켜 상태 변경과 원자적으로 남기도록 했습니다."
- "입학 waitlist/offer, 출결 요청 승인/거절, 공지 수정/삭제 같은 운영 책임 추적을 principal 화면과 CSV export까지 닫았습니다."
