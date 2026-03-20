# 3분 데모 시나리오

실행 준비와 실제 시연 순서는 각각 `docs/portfolio/demo/demo-preflight.md`, `docs/portfolio/demo/demo-runbook.md`를 SSOT로 사용합니다.

## 목적

짧은 시간 안에
"기능이 많다"가 아니라
"운영 감각 있는 백엔드다"를 보여주는 순서입니다.

## 사전 준비

- `./gradlew bootRun --args='--spring.profiles.active=demo'` 실행 상태
- `principal@test.com / test1234!` 로그인 가능 상태
- `parent1@test.com / test1234!` 로그인 가능 상태
- 유치원, 반, 원생, 공지, 알림장 기본 데이터 준비
- 정원이 찬 반과 대기열/offer 전환용 신청 데이터 확인 가능 상태
- `/swagger-ui.html`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/prometheus`, `http://localhost:3000` 호출 가능한 환경 준비

## 0:00 ~ 0:20 API 계약 가시화

1. `/swagger-ui.html` 진입
2. `/v3/api-docs` 또는 login endpoint 스키마 잠깐 확인
3. “정적 표가 아니라 실행 중인 계약 문서”라고 설명

말할 포인트:
- "백엔드 포트폴리오라서 API 계약을 코드와 같이 보여주는 게 중요하다고 봤습니다."
- "Swagger UI와 OpenAPI JSON을 같이 열어 두면 설명과 검증이 빨라집니다."

## 0:20 ~ 0:40 멀티테넌시/권한 경계

1. 원장으로 로그인
2. 원생/공지/알림장 조회 화면 또는 API 호출
3. 다른 유치원 데이터는 접근 차단되는 점 설명

말할 포인트:
- "초기에는 `id`만 알면 다른 유치원 데이터에 접근 가능한 IDOR 성격의 문제가 있었습니다."
- "지금은 requester 기반으로 같은 유치원/본인 소유 여부를 서비스 계층에서 검증합니다."

## 0:40 ~ 1:20 입학 waitlist/offer 워크플로우

1. 반 정원(capacity)과 현재 좌석 상태 설명
2. 정원이 찬 상태에서 입학 신청을 `WAITLISTED`로 보내기
3. 원장으로 `OFFERED` 전환
4. 학부모 계정으로 offer 수락
5. 수락 이후 실제 `Kid` 생성/배정이 닫히는 점 설명

말할 포인트:
- "입학 신청을 승인 버튼 하나로 끝내지 않고, 정원/대기열/offer/만료가 있는 상태 전이 문제로 바꿨습니다."
- "좌석은 현재 원생 수만이 아니라 active offer까지 예약으로 계산합니다."
- "이렇게 해서 oversell 가능성을 줄였습니다."

## 1:20 ~ 1:50 출결 변경 요청 승인 워크플로우

1. 학부모로 `/attendance-requests` 진입
2. 자기 자녀의 결석/병결 요청 생성
3. 교사 또는 원장으로 같은 화면에 들어가 승인
4. 실제 `Attendance`가 승인 시점에만 반영되는 점 설명

말할 포인트:
- "학부모가 최종 출결 record를 직접 바꾸지 못하게 하고, 요청 aggregate를 분리했습니다."
- "요청과 최종 출결 snapshot을 분리해서 권한 경계와 이력 보존을 동시에 만족시켰습니다."

## 1:50 ~ 2:15 업무 감사 로그 조회

1. `/domain-audit-logs` 화면 진입
2. `KID_APPLICATION_WAITLISTED` 또는 `ATTENDANCE_CHANGE_REQUEST_APPROVED` 필터 예시 시연
3. `CSV Export` 버튼 시연
4. 인증 감사 로그(`/audit-logs`)와 역할을 구분해 설명

말할 포인트:
- "인증 감사 로그와 업무 감사 로그는 목적이 달라서 분리했습니다."
- "업무 감사 로그는 입학/출결/공지 상태 전이를 principal이 직접 조회할 수 있게 닫았습니다."

## 2:15 ~ 2:40 인증/운영 관측성

1. `/actuator/health` 호출
2. `/actuator/health/readiness` 호출
3. `/actuator/prometheus`에서 `erp_auth_events_total` 확인
4. Grafana `Kindergarten ERP Observability` 대시보드 잠깐 확인
5. 응답 헤더의 `X-Correlation-Id` 설명

말할 포인트:
- "운영에서는 살아 있는지보다, 트래픽 받을 준비가 됐는지까지 분리해서 봅니다."
- "이 readiness는 단순 endpoint 노출이 아니라 DB/Redis `criticalDependencies`를 직접 반영합니다."
- "로그만 남긴 게 아니라 Prometheus로 보안 이벤트를 숫자로도 관측합니다."
- "Prometheus metric을 Grafana 대시보드로 연결해서 운영자가 바로 볼 수 있게 했습니다."
- "모든 요청에 correlation id를 붙여 로그 추적이 가능하게 했습니다."

## 2:40 ~ 3:00 테스트와 CI

1. README 또는 GitHub Actions 결과 화면 제시
2. Testcontainers 기반 MySQL/Redis 통합 테스트와 split CI 설명

말할 포인트:
- "H2가 아니라 MySQL/Redis Testcontainers로 운영 환경과 최대한 비슷하게 검증합니다."
- "CI는 fast/integration/performance smoke를 suite 기반으로 분리해서 개발 속도와 운영 회귀 검증을 같이 가져갑니다."

## 마무리 한 문장

"이 프로젝트는 CRUD를 많이 만든 프로젝트가 아니라, 권한 경계, 상태 전이 워크플로우, 감사 추적, 운영 관측성, 실환경형 테스트까지 실제 운영 관점으로 고도화한 백엔드 포트폴리오입니다."
