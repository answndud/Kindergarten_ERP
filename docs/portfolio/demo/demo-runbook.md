# Demo Runbook

이 문서는 실제 시연 순서 SSOT입니다.

## 0-1. 데모 모드 전제

- 앱은 `SPRING_PROFILES_ACTIVE=demo`로 실행합니다.
- `demo`는 seed data를 켜고 Swagger/OpenAPI와 app-port Prometheus를 시연용으로 의도적으로 공개합니다.
- 운영 환경에서는 이 경로들이 기본 공개가 아니라는 점을 함께 설명합니다.

## 0. 기본 계정

- principal: `principal@test.com / test1234!`
- teacher: `teacher1@test.com / test1234!`
- parent: `parent1@test.com / test1234!`

## 1. 5분 시연 흐름

1. Swagger/OpenAPI로 실행 중인 API 계약을 보여줍니다.
2. parent 계정으로 요청을 하나 만들고, 별도 브라우저나 시크릿 창의 principal 계정으로 승인 화면을 엽니다.
3. waitlist/offer 입학 워크플로우와 출결 변경 요청 승인 흐름으로 상태 전이를 설명합니다.
4. auth/domain audit console과 CSV export로 운영 증적을 보여줍니다.
5. readiness, Prometheus, Grafana, split CI로 운영/검증 체계를 마무리합니다.

## 2. 장면별 멘트

### API 계약

- `정적 문서가 아니라 코드와 같이 배포되는 계약 문서이고, 노출을 의도한 local/demo 환경에서만 공개합니다.`

### 입학 워크플로우

- `입학 신청을 승인/거절 버튼으로 끝내지 않고, 정원과 대기열이 있는 상태 머신으로 바꿨습니다.`

### 출결 요청 승인

- `학부모가 최종 Attendance를 직접 바꾸지 못하게 하고, 요청 aggregate를 별도로 둬서 승인 흔적을 남겼습니다.`

### 감사 로그

- `보안 사건(auth audit)과 업무 상태 전이(domain audit)를 분리해서 조회 목적을 명확히 했습니다.`

### 운영/테스트

- `readiness는 DB/Redis 상태를 반영하고, CI는 fast/integration/performance smoke를 분리해 회귀를 잡습니다.`
- `문서와 메트릭도 기본 공개가 아니라 환경별로 열고 닫습니다.`

## 3. 추천 클릭 순서

1. `principal@test.com`로 로그인 후 `/swagger-ui.html`
2. 별도 브라우저나 시크릿 창에서 `parent1@test.com` 세션으로 입학 신청 또는 출결 요청 1건 생성
3. `principal@test.com` 세션으로 `/applications/pending`
4. 이어서 `/attendance-requests`
5. `/domain-audit-logs`
6. `/audit-logs`
7. `/actuator/health/readiness`
8. GitHub Actions `Backend CI`

## 4. 장면별 기대 상태

- `/swagger-ui.html`
  - 문서가 코드와 같이 배포된다는 점이 보입니다.
- `/applications/pending`
  - 신청이 단순 CRUD가 아니라 상태 전이 대상으로 보입니다.
- `/attendance-requests`
  - 요청 aggregate와 확정 aggregate가 분리돼 보입니다.
- `/domain-audit-logs`, `/audit-logs`
  - 업무 증적과 인증 증적이 분리돼 보입니다.
- `/actuator/health/readiness`
  - DB/Redis 의존성을 반영한 readiness가 보입니다.

## 5. 절대 빠뜨리지 말 것

- 멀티테넌시 권한 경계는 `서비스 계층 requester 검증`이라고 설명할 것
- `AttendanceChangeRequest`와 `Attendance`를 분리한 이유를 말할 것
- `notification_outbox`와 감사 로그 분리 이유를 말할 것
- Testcontainers와 suite-based CI를 실제 운영 스택 검증 관점에서 설명할 것
