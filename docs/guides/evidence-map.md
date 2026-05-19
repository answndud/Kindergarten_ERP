# Evidence Map

기준일: 2026-05-19

이 문서는 README와 면접 답변에서 하는 강한 주장을 코드, 테스트, 문서, 화면 경로와 연결하기 위한 증거 지도입니다.

## 1. 핵심 주장별 증거

| 주장 | 코드 증거 | 테스트 증거 | 문서/시연 증거 | 면접에서 보여줄 순서 |
| --- | --- | --- | --- | --- |
| 역할/tenant 경계를 API와 service에서 함께 검증했다. | `src/main/java/com/erp/global/security/access/AccessPolicyService.java`, `src/main/java/com/erp/domain/*/service/*Service.java` | `src/test/java/com/erp/api/*IntegrationTest.java` | `README.md` 핵심 문제와 해결, `docs/guides/demo-scenario.md` | 권한 질문이 나오면 `AccessPolicyService`부터 열고 통합 테스트로 이동 |
| 세션은 cookie JWT와 Redis refresh/session registry로 revoke 가능하게 설계했다. | `src/main/java/com/erp/global/security/jwt/*`, `src/main/java/com/erp/domain/auth/*` | `src/test/java/com/erp/api/AuthApiIntegrationTest.java` | `README.md` 아키텍처 요약, `docs/guides/interview-guide.md` | 로그인/refresh/session 종료 API 흐름 설명 |
| 입력 오류를 500이 아니라 400 계약으로 닫았다. | `src/main/java/com/erp/global/exception/GlobalExceptionHandler.java`, `src/main/java/com/erp/global/exception/ErrorCode.java` | 출석 월 조회 invalid/missing/type 통합 테스트 | `docs/COMPLETED.md#archive-020` | 예외 처리 질문에서 global handler와 테스트를 함께 제시 |
| 캘린더 긴 조회 범위를 제한했다. | `src/main/java/com/erp/domain/calendar/*`, `RecurrenceExpander` | calendar fast/integration tests | `docs/COMPLETED.md#archive-020` | 성능/안전장치 질문에서 366일 cap 설명 |
| Notification Outbox dead-letter를 운영자가 확인/재시도할 수 있다. | `src/main/java/com/erp/domain/notification/controller/NotificationOutboxOpsController.java`, `src/main/java/com/erp/domain/notification/*` | `src/test/java/com/erp/api/NotificationOutboxOpsApiIntegrationTest.java`, `src/test/java/com/erp/integration/ViewEndpointTest.java` | `/notification-outbox`, `README.md` 화면 섹션 | demo에서 outbox 화면을 열고 principal-only 테스트로 이동 |
| Swagger/OpenAPI와 app-port Prometheus는 기본 공개가 아니라 opt-in이다. | `src/main/java/com/erp/global/config/SecurityConfig.java`, `src/main/java/com/erp/global/config/StartupSafetyValidator.java`, `src/main/java/com/erp/global/monitoring/PrometheusScrapeController.java` | `src/test/java/com/erp/integration/ObservabilityIntegrationTest.java`, `src/test/java/com/erp/integration/ManagementSurfaceOptInIntegrationTest.java`, `src/test/java/com/erp/global/config/StartupSafetyValidatorTest.java` | `docs/guides/env-contract.md` | 보안/운영 질문에서 default deny와 explicit opt-in 테스트를 제시 |
| prod에서는 seed, Swagger, app-port Prometheus, 약한 JWT secret을 막는다. | `src/main/java/com/erp/global/config/StartupSafetyValidator.java`, `src/main/resources/application-prod.yml` | `src/test/java/com/erp/global/config/StartupSafetyValidatorTest.java` | `docs/guides/env-contract.md` | prod safety 질문에서 validator와 env contract를 함께 제시 |
| Notepad/Dashboard 성능 개선은 수치로 검증했다. | `src/main/java/com/erp/domain/notepad/*`, `src/main/java/com/erp/domain/dashboard/*` | `src/test/java/com/erp/performance/*PerformanceStoryTest.java`, k6 결과 | `README.md` 수치로 검증한 개선, `docs/COMPLETED.md` archive | 성능 질문에서 README 표와 performance smoke test를 함께 제시 |
| CI는 혼자 운영하는 main 프로젝트에 맞춰 quick/heavy를 분리했다. | `.github/workflows/ci.yml`, `.github/workflows/backend-quality.yml`, `.github/workflows/cd.yml` | GitHub Actions run history | `README.md` 테스트 & CI, `docs/COMPLETED.md#archive-018` | push CI는 빠른 실패 신호, heavy는 수동 검증이라고 설명 |

## 2. 화면별 증거

| 화면 | 경로 | 보여주는 것 | 관련 API/테스트 |
| --- | --- | --- | --- |
| 원장 대시보드 | `/dashboard` | 통계, 캐시/집계 개선 스토리 | `/api/v1/dashboard/statistics`, dashboard performance tests |
| 입학 신청 큐 | `/applications/pending` | 승인, waitlist, offer 상태 전이 | kid application integration tests |
| 출결 요청 | `/attendance-requests` | 학부모 요청과 교사/원장 처리 경계 | attendance request integration tests |
| 인증 감사 로그 | `/audit-logs` | 로그인/refresh/security event archive/export | auth audit API/tests |
| 업무 감사 로그 | `/domain-audit-logs` | 상태 전이 archive/export | domain audit API/tests |
| 알림 Outbox 운영 | `/notification-outbox` | dead-letter summary/list/retry | `NotificationOutboxOpsApiIntegrationTest`, `ViewEndpointTest` |
| Swagger UI | `/swagger-ui.html` | local/demo API 계약 확인 | management surface opt-in tests |

## 3. 검증 명령 증거

| 목적 | 명령 | 쓰임 |
| --- | --- | --- |
| 빠른 컴파일 검증 | `./gradlew compileJava compileTestJava` | 작은 코드/annotation 변경 후 import/type 오류 확인 |
| release packaging | `./gradlew bootJar` | 면접/릴리스 직전 jar 패키징 확인 |
| push quick CI와 동일 계열 | `./gradlew fastTest` | unit/fast slice 중심의 빠른 실패 신호 |
| heavy 품질 검증 | `./gradlew integrationTest` | MySQL/Redis Testcontainers 기반 통합 검증 |
| 성능 smoke | `./gradlew performanceSmokeTest` | query count/elapsed time 회귀 확인 |
| GitHub 최신 CI | `gh run list --repo answndud/Kindergarten_ERP --branch main --limit 5` | 최신 main push 상태 확인 |

## 4. 주의할 점

- README에는 변동 가능한 최신 CI 시간을 고정값으로 남기지 않는다.
- 대표 개선값은 archive와 README에 남기되, 최신 상태는 GitHub Actions 배지와 run history를 기준으로 확인한다.
- 증거가 코드에 없거나 테스트가 없는 주장은 README에서 강하게 말하지 않는다.
