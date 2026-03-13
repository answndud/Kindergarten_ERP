# 3분 데모 시나리오

## 목적

짧은 시간 안에
"기능이 많다"가 아니라
"운영 감각 있는 백엔드다"를 보여주는 순서입니다.

## 사전 준비

- `./gradlew bootRun --args='--spring.profiles.active=demo'` 실행 상태
- `principal@test.com / test1234!` 로그인 가능 상태
- 유치원, 반, 원생, 공지, 알림장 기본 데이터 준비
- settings 화면에서 Google/Kakao 연결 상태가 보이는 계정 준비
- `/swagger-ui.html`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/prometheus` 호출 가능한 환경 준비

## 0:00 ~ 0:25 API 계약 가시화

1. `/swagger-ui.html` 진입
2. `/v3/api-docs` 또는 login endpoint 스키마 잠깐 확인
3. “정적 표가 아니라 실행 중인 계약 문서”라고 설명

말할 포인트:
- "백엔드 포트폴리오라서 API 계약을 코드와 같이 보여주는 게 중요하다고 봤습니다."
- "Swagger UI와 OpenAPI JSON을 같이 열어 두면 설명과 검증이 빨라집니다."

## 0:25 ~ 0:50 인증/운영 관측성

1. `/actuator/health` 호출
2. `/actuator/health/readiness` 호출
3. `/actuator/prometheus`에서 `erp_auth_events_total` 확인
4. 응답 헤더의 `X-Correlation-Id` 설명

말할 포인트:
- "운영에서는 살아 있는지보다, 트래픽 받을 준비가 됐는지까지 분리해서 봅니다."
- "로그만 남긴 게 아니라 Prometheus로 보안 이벤트를 숫자로도 관측합니다."
- "모든 요청에 correlation id를 붙여 로그 추적이 가능하게 했습니다."

## 0:50 ~ 1:35 멀티테넌시/권한 경계

1. 원장으로 로그인
2. 원생/공지/알림장 조회 화면 또는 API 호출
3. 다른 유치원 데이터는 접근 차단되는 점 설명

말할 포인트:
- "초기에는 `id`만 알면 다른 유치원 데이터에 접근 가능한 IDOR 성격의 문제가 있었습니다."
- "지금은 requester 기반으로 같은 유치원/본인 소유 여부를 서비스 계층에서 검증합니다."

## 1:35 ~ 2:10 소셜 로그인 lifecycle

1. settings 화면 진입
2. 현재 연결된 social provider 표시
3. local password bootstrap / unlink policy / provider 교체 금지 설명

말할 포인트:
- "자동 연결은 금지하고, 사용자가 settings에서 명시적으로 연결합니다."
- "같은 provider의 다른 계정으로 교체는 막았습니다. 로그인 식별자는 자기 수정 불가로 봤기 때문입니다."

## 2:10 ~ 2:40 감사 로그 조회

1. `/audit-logs` 화면 진입
2. eventType/result/provider/email/date 필터 예시 시연
3. 같은 유치원 소속 로그만 보이고 익명 실패 로그는 principal 조회에서 제외되는 점 설명
4. 필요하면 같은 화면에서 `/swagger-ui.html`로 넘어가 API 계약과 연결

말할 포인트:
- "보안 이벤트를 저장만 하는 게 아니라 운영자가 실제로 조회할 수 있게 닫았습니다."
- "단, tenant 경계를 위해 principal은 자기 유치원 소속 member 기반 로그만 조회할 수 있습니다."

## 2:40 ~ 3:00 테스트와 CI

1. README 또는 GitHub Actions 결과 화면 제시
2. Testcontainers 기반 MySQL/Redis 통합 테스트와 split CI 설명

말할 포인트:
- "H2가 아니라 MySQL/Redis Testcontainers로 운영 환경과 최대한 비슷하게 검증합니다."
- "fast/integration job을 분리해서 개발 속도와 신뢰성을 같이 가져갔습니다."

## 마무리 한 문장

"이 프로젝트는 CRUD를 많이 만든 프로젝트가 아니라, 권한 경계, 인증 수명, 소셜 계정 lifecycle, 운영 관측성, 실환경형 테스트까지 실제 운영 관점으로 고도화한 백엔드 포트폴리오입니다."
