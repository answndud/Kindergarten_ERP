# Demo Runbook

이 문서는 실제 시연 순서 SSOT입니다.

## 1. 5분 시연 흐름

1. Swagger/OpenAPI로 실행 중인 API 계약을 보여줍니다.
2. waitlist/offer 입학 워크플로우로 상태 전이 설계를 설명합니다.
3. 출결 변경 요청 승인 흐름으로 요청 aggregate와 확정 aggregate 분리를 설명합니다.
4. auth/domain audit console과 CSV export로 운영 증적을 보여줍니다.
5. readiness, Prometheus, Grafana, split CI로 운영/검증 체계를 마무리합니다.

## 2. 장면별 멘트

### API 계약

- `정적 문서가 아니라 코드와 같이 배포되는 계약 문서입니다.`

### 입학 워크플로우

- `입학 신청을 승인/거절 버튼으로 끝내지 않고, 정원과 대기열이 있는 상태 머신으로 바꿨습니다.`

### 출결 요청 승인

- `학부모가 최종 Attendance를 직접 바꾸지 못하게 하고, 요청 aggregate를 별도로 둬서 승인 흔적을 남겼습니다.`

### 감사 로그

- `보안 사건(auth audit)과 업무 상태 전이(domain audit)를 분리해서 조회 목적을 명확히 했습니다.`

### 운영/테스트

- `readiness는 DB/Redis 상태를 반영하고, CI는 fast/integration/performance smoke를 분리해 회귀를 잡습니다.`

## 3. 추천 클릭 순서

1. `/swagger-ui.html`
2. `/attendance-requests`
3. `/domain-audit-logs`
4. `/audit-logs`
5. `/actuator/health/readiness`
6. GitHub Actions `Backend CI`

## 4. 절대 빠뜨리지 말 것

- 멀티테넌시 권한 경계는 `서비스 계층 requester 검증`이라고 설명할 것
- `AttendanceChangeRequest`와 `Attendance`를 분리한 이유를 말할 것
- `notification_outbox`와 감사 로그 분리 이유를 말할 것
- Testcontainers와 suite-based CI를 실제 운영 스택 검증 관점에서 설명할 것
