# Kindergarten ERP Interview One Pager

## 1. 프로젝트 한 줄 소개

유치원 운영 관리 시스템을 만들면서,
"기능 구현"보다 **운영 가능한 백엔드로 어떻게 다듬었는가**를 보여주는 포트폴리오 프로젝트입니다.

## 2. 내가 강조할 핵심 역량 5가지

### 1) 멀티테넌시 권한 경계 하드닝

- 처음에는 여러 API가 `id`만 알면 다른 유치원 데이터에 접근 가능한 구조였습니다.
- `requesterId` 기반 서비스 검증과 공통 접근 정책으로 같은 유치원/본인 소유/학부모-자녀 관계를 강제했습니다.
- 결과적으로 단순 CRUD를 넘어 **데이터 경계 설계**를 보여줄 수 있게 만들었습니다.

### 2) JWT 세션 설계와 인증 남용 방어

- refresh token을 이메일 단위 단일 키에서 **세션 단위 Redis 저장**으로 바꿨습니다.
- refresh rotation, 로그아웃/탈퇴 시 revoke, 로그인/refresh rate limit, trusted proxy 기준 client IP 해석까지 반영했습니다.
- 마지막에는 access token도 Redis 세션 레지스트리에 묶어, 세션 종료 시 다른 기기의 access token이 남아 있어도 즉시 인증이 끊기도록 만들었습니다.
- 면접에서는 “JWT를 썼다”보다 **세션 수명과 남용 방어를 어떻게 설계했는지**를 설명할 수 있습니다.

### 3) 소셜 로그인 lifecycle 설계

- OAuth2 자동 계정 연결을 금지하고, 충돌은 명시적으로 안내하도록 UX를 정리했습니다.
- settings 화면 기반 명시적 social link, local password bootstrap, unlink safeguards, multi-provider linking까지 확장했습니다.
- 마지막에는 같은 provider의 다른 계정으로 교체되지 않도록 **로그인 식별자 불변 정책**도 넣었습니다.

### 4) 상태 전이 워크플로우 설계

- 입학 신청을 `승인/거절`만 있는 단순 흐름이 아니라 **반 정원(capacity) + waitlist + offer + offer 만료**가 있는 상태 전이로 바꿨습니다.
- 좌석 계산은 현재 원생 수만이 아니라 active offer까지 예약으로 계산하도록 설계했습니다.
- 출결도 학부모가 직접 `Attendance`를 바꾸지 못하게 하고, **AttendanceChangeRequest -> 교사/원장 승인 -> Attendance 반영** 구조로 분리했습니다.
- 여기에 `domain_audit_log`를 붙여 입학과 출결의 상태 변경을 중앙에서 추적할 수 있게 했습니다.
- 면접에서는 “CRUD를 만들었다”보다 **운영 현실을 상태 전이 모델로 어떻게 옮겼는지**를 설명할 수 있습니다.

### 5) 실환경형 테스트와 운영 관측성/감사 추적

- H2/Mock Redis를 버리고 **MySQL/Redis Testcontainers + Flyway** 기반으로 통합 테스트를 전환했습니다.
- GitHub Actions에서 fast/integration job을 분리했고, Node24 네이티브 action으로 runner 경고도 제거했습니다.
- 즉 “돌아가는 테스트”가 아니라 **운영 스택을 닮은 테스트**를 만들었습니다.
- Swagger/OpenAPI live contract, Actuator health/info/prometheus, liveness/readiness probe, correlation id, structured request logging을 추가했습니다.
- local/demo에서는 Swagger와 Prometheus를 바로 열고, prod에서는 Swagger를 비활성화하고 management port를 분리해 운영 노출면을 줄였습니다.
- 로그인/refresh/social link/unlink는 인증 감사 로그로 남기고, 입학/출결/공지 상태 변경은 별도 업무 감사 로그로 분리했습니다.
- 반복 로그인 실패는 원장 시스템 알림으로 연결했고, 이후 `notification_outbox`로 외부 전달을 분리해 retry/dead-letter와 incident webhook까지 붙였습니다.
- Prometheus metric은 Grafana 대시보드까지 바로 보이게 구성했고, 이로써 **계약 문서 -> 이벤트 저장 -> 운영 조회 -> 외부 incident 전파 -> 메트릭 관측 -> 사후 분석** 흐름을 설명할 수 있게 됐습니다.

### 6) 상태 전이가 있는 운영형 워크플로우

- `Classroom.capacity`를 도입하고, 입학 신청을 `PENDING -> WAITLISTED -> OFFERED -> APPROVED / OFFER_EXPIRED` 상태 머신으로 확장했습니다.
- 학부모 출결 변경은 `Attendance`를 직접 수정하지 않고 `AttendanceChangeRequest` aggregate로 분리해 `PENDING -> APPROVED / REJECTED / CANCELLED` 흐름으로 만들었습니다.
- 입학 처리, 출결 요청 승인, 공지 수정/삭제는 별도 `domain_audit_log`에 기록하고, 원장이 화면/CSV로 직접 확인할 수 있게 했습니다.
- 면접에서는 “CRUD를 만들었다”보다 **상태 전이, 승인 흐름, 좌석 제약, 감사 증적을 어떻게 설계했는가**를 설명할 수 있습니다.

## 3. 성능 개선 포인트

- Notepad 목록: queries `22 -> 4`, `15ms -> 4ms`
- Dashboard 통계: queries `13 -> 5`, `30ms -> 9ms`
- Dashboard 반복 조회: queries `5 -> 0`, `12ms -> 0ms`

핵심은 숫자 자체보다,
재현 시나리오를 먼저 정의하고 개선 전/후를 같은 조건에서 다시 측정했다는 점입니다.

## 4. 아키텍처 키워드

- Java 17 / Spring Boot 3.5.9
- JPA + QueryDSL
- Spring Security + JWT + OAuth2
- MySQL 8 / Redis
- Flyway
- Thymeleaf + HTMX + Alpine.js
- OSIV OFF / `default_batch_fetch_size=100`

## 5. 면접에서 이렇게 말하면 좋다

- "단순히 기능을 늘리기보다 권한 경계, 인증 세션, 테스트 현실성, 운영 관측성을 먼저 보강했습니다."
- "포트폴리오를 기능 시연이 아니라 운영 가능한 백엔드 설계 이야기로 바꾸는 데 집중했습니다."
- "특히 보안/권한/감사 로그/테스트는 면접에서 깊게 물어볼 지점이라, 실제 코드와 문서로 모두 닫았습니다."

## 6. 대표 트레이드오프

- 감사 로그는 FK 없이 저장했습니다.
  - 이유: 회원 lifecycle과 독립적으로 운영 증적을 남기기 위해서입니다.

- principal API에서는 tenant에 안전하게 귀속된 감사 로그만 조회합니다.
  - 이유: known email 실패는 `kindergarten_id`로 귀속하지만, 완전히 익명인 로그인 실패는 여전히 tenant를 특정할 수 없기 때문입니다.

- notification outbox worker는 현재 단일 인스턴스 기준으로 설계했습니다.
  - 이유: 이번 단계에서는 전달 신뢰성(retry/dead-letter) 확보를 먼저 닫고, 멀티 인스턴스 lock 전략은 다음 단계 확장 포인트로 남겼기 때문입니다.

- 소셜 계정은 “교체”를 허용하지 않았습니다.
  - 이유: 소셜 `providerId`를 비밀번호가 아니라 로그인 식별자로 봤기 때문입니다.

## 7. 시작 문장 템플릿

"이 프로젝트는 유치원 ERP 자체보다, 기능이 많아질수록 생기는 권한 경계, 인증 세션, 테스트 현실성, 운영 관측성 문제를 실제로 보완한 백엔드 포트폴리오입니다."
