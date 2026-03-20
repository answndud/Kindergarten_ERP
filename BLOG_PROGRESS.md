# BLOG_PROGRESS.md

## 작업명
- 취업용 개발 블로그 시리즈 기획 2차 (전체 코드 재스캔 + 코드 기반 마스터 플랜 고도화)

## 진행 로그

| 시간 (KST) | 상태 | 수행 내용 | 다음 액션 |
|---|---|---|---|
| 2026-03-20 15:05 | DONE | 사용자 요청에 따라 블로그 작업은 기존 `PLAN.md`, `PROGRESS.md`와 분리하기로 결정 | 블로그 전용 SSOT 파일 생성 |
| 2026-03-20 15:09 | DONE | 루트 `blog/` 폴더와 `blog/README.md`, `blog/00_series_plan.md`, `blog/_post_template.md`를 생성해 작업 공간을 만들었다 | 블로그 전용 계획/진행 파일 생성 |
| 2026-03-20 15:12 | DONE | 기존 개발 SSOT인 `PLAN.md`, `PROGRESS.md`를 HEAD 기준으로 복구했다 | `BLOG_PLAN.md`, `BLOG_PROGRESS.md` 작성 |
| 2026-03-20 15:15 | DONE | 블로그 작업 전용 SSOT로 `BLOG_PLAN.md`, `BLOG_PROGRESS.md`를 작성하고 `blog/README.md`에 새 경로를 연결했다 | 실제 블로그 본문 작성 우선순위에 따라 `blog/01-*` 문서부터 집필 시작 |
| 2026-03-20 15:31 | DONE | 전체 코드와 문서 체계를 다시 스캔했다. `build.gradle`, `docker/*`, `application*.yml`, `global/config`, `global/security`, `domain/*`, `src/test/java`, `docs/decisions`, `docs/portfolio`를 기준으로 블로그 근거 지도를 다시 만들었다 | 얕은 개요형 계획을 실제 코드 기반 마스터 플랜으로 전면 재작성 |
| 2026-03-20 15:43 | DONE | `BLOG_PLAN.md`를 글 단위 마스터 플랜으로 재작성했다. 각 글마다 핵심 질문, 실제 파일, 클래스/메서드, 테스트/문서 근거, 입문자 포인트, 취업 포인트를 고정했다 | `blog/00_series_plan.md`를 마스터 플랜의 공개용 요약 인덱스로 재정렬 |
| 2026-03-20 15:47 | DONE | `blog/00_series_plan.md`를 마스터 플랜과 일치하도록 요약 인덱스로 다시 작성했고, 남아 있던 이전 초안 내용을 제거했다 | 포맷 검증 후 커밋/푸시 |
| 2026-03-20 16:02 | DONE | 사용자 피드백을 반영해 `BLOG_PLAN.md`를 다시 강화했다. 코드베이스 스냅샷, 소스 맵, 개발 연대기, 26개 글 구성, 집필 운영 규칙과 우선순위를 보강해 블로그 집필용 SSOT로 재정의했다 | 포맷 검증 후 커밋/푸시 |
| 2026-03-20 16:18 | DONE | 첫 본문 주제를 `settings.gradle`, `build.gradle`, `ErpApplication`으로 고정하고 관련 코드, CI 설정, 결정 로그를 다시 읽었다 | `blog/02-gradle-spring-boot-bootstrap.md` 초안 작성 |
| 2026-03-20 16:31 | DONE | `blog/02-gradle-spring-boot-bootstrap.md` 초안을 작성했다. 빌드 뼈대, 의존성 설계, Java 17 선택, 테스트 태스크 진화, CI 연결까지 입문자용 설명과 취업 포인트를 함께 정리했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 16:43 | DONE | 두 번째 본문 주제를 Docker 개발 환경으로 고정하고 `docker-compose.yml`, `docker-compose.monitoring.yml`, `docker/.env`, `application-local.yml`, `application.yml`, `demo-preflight.md`를 다시 읽었다 | `blog/03-docker-mysql-redis-dev-environment.md` 초안 작성 |
| 2026-03-20 16:57 | DONE | `blog/03-docker-mysql-redis-dev-environment.md` 초안을 작성했다. MySQL/Redis 기본 스택, monitoring overlay 분리, 앱은 호스트에서 실행하고 인프라만 컨테이너화하는 구조, `application-local.yml`과의 연결을 입문자용 설명과 취업 포인트로 정리했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 17:05 | DONE | 세 번째 본문 주제를 프로파일 전략으로 고정하고 `application.yml`, `application-local.yml`, `application-demo.yml`, `application-prod.yml`, `logback-spring.xml`, `DataLoader`, `OpenApiConfig`, `SecurityConfig`, 관련 결정 로그를 다시 읽었다 | `blog/04-application-yml-and-profile-strategy.md` 초안 작성 |
| 2026-03-20 17:18 | DONE | `blog/04-application-yml-and-profile-strategy.md` 초안을 작성했다. 공통 설정과 환경별 차이, `demo=local` profile group, `@Profile`/`@ConditionalOnProperty`/`SecurityConfig.buildPublicEndpoints()`까지 연결해 입문자용 설명과 취업 포인트를 함께 정리했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 17:24 | DONE | `git diff --check`로 포맷 오류가 없는지 확인하고, 하위 제목 레벨과 진행 로그를 정리했다 | add/commit/push |
| 2026-03-20 17:31 | DONE | 네 번째 본문 주제를 JPA/Flyway/QueryDSL/Redis/Cache 공통 설정으로 고정하고 `build.gradle`, `application*.yml`, `V1/V5/V8/V13` 마이그레이션, `JpaConfig`, `BaseEntity`, `QuerydslConfig`, `RedisConfig`, `CacheConfig`, `TestcontainersSupport`, 관련 결정 로그를 다시 읽었다 | `blog/05-jpa-flyway-querydsl-redis-cache-foundation.md` 초안 작성 |
| 2026-03-20 17:46 | DONE | `blog/05-jpa-flyway-querydsl-redis-cache-foundation.md` 초안을 작성했다. JPA와 Flyway의 역할 분리, QueryDSL/Redis/Cache의 초기 공통 설정 의도, Testcontainers 기반 검증까지 입문자용 설명과 취업 포인트로 정리했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 17:51 | DONE | `git diff --check`로 문서 포맷을 검증하고 섹션 구조를 재확인했다 | add/commit/push |
| 2026-03-20 18:12 | DONE | 블로그 시리즈 후반부 작성을 위해 `domain/*`, `global/security`, `global/monitoring`, `src/test/java`, `docs/decisions`, `docs/portfolio`를 다시 스캔했다. 특히 Part C~F 보강용으로 도메인 모델, JWT 세션, AccessPolicy, OAuth2 lifecycle 관련 코드와 테스트를 재정리했다 | Part A/C/D/E/F 초안(01, 06~18) 작성 |
| 2026-03-20 18:44 | DONE | `blog/01-why-kindergarten-erp-domain.md`, `blog/06-global-and-domain-package-structure.md`, `blog/07-member-kindergarten-classroom-modeling.md`, `blog/08-kid-parentkid-attendance-aggregate.md`, `blog/09-notepad-announcement-notification-expansion.md`, `blog/10-calendar-dashboard-and-application-workflows.md`, `blog/11-securityconfig-signup-login-basics.md`, `blog/12-jwt-cookie-auth-flow.md`, `blog/13-multitenant-access-hardening.md`, `blog/14-why-testcontainers-over-h2.md`, `blog/15-github-actions-and-tagged-test-suites.md`, `blog/16-refresh-rotation-and-active-session-control.md`, `blog/17-rate-limit-and-client-ip-trust-model.md`, `blog/18-oauth2-social-account-lifecycle.md` 초안을 작성했다 | 운영/감사/포트폴리오 구간(19~26) 집필 |
| 2026-03-20 19:12 | DONE | auth audit, domain audit, outbox, observability, retention, performance, demo/hiring pack 관련 코드와 결정 로그를 다시 읽었다. `AuthAuditLogService`, `AuthAuditRetentionService`, `NotificationDispatchService`, `CriticalDependenciesHealthIndicator`, `AuditConsolePerformanceSmokeTest`, `DataLoader`, performance/interview 문서를 Part G~I 근거로 정리했다 | `blog/19~26` 초안 작성 |
| 2026-03-20 19:39 | DONE | `blog/19-auth-audit-log-and-operations-console.md`, `blog/20-openapi-management-plane-and-observability.md`, `blog/21-notification-outbox-and-incident-channel.md`, `blog/22-classroom-capacity-and-waitlist-workflow.md`, `blog/23-attendance-change-request-and-domain-audit.md`, `blog/24-audit-logs-as-operations-tools.md`, `blog/25-performance-story-as-portfolio.md`, `blog/26-demo-architecture-and-interview-pack.md` 초안을 작성했다 | 블로그 인덱스와 진행 기록 정리 |
| 2026-03-20 19:47 | DONE | `blog/README.md`와 `blog/00_series_plan.md`를 전체 글 링크가 보이는 인덱스로 갱신했다. 블로그 범위를 벗어난 임시 `PLAN.md`, `PROGRESS.md` 수정은 원복하기로 확정했다 | 개발용 SSOT 원복 후 포맷 검증 |
| 2026-03-20 19:56 | DONE | 개발용 `PROGRESS.md`까지 원복했고, 블로그 시리즈 작업 범위만 남도록 워크트리를 정리했다. `git diff --check` 기준 포맷 오류 없이 26편 전체 초안 + 인덱스가 완성된 상태다 | add/commit/push |
| 2026-03-20 20:11 | DONE | 사용자 피드백을 반영해 블로그 재현성 보강 작업에 착수했다. `BLOG_PLAN.md`, `blog/_post_template.md`, 기존 본문 `02`~`05`를 다시 읽고 “설명형 -> 재현형” 전환 기준을 정의했다 | 공통 재현성 가이드와 템플릿 작성 |
| 2026-03-20 20:24 | DONE | `BLOG_PLAN.md`에 재현성 강화 목표/배치 계획을 추가하고, `blog/00_rebuild_guide.md`, `blog/_rebuild_template.md`를 새로 작성했다. `blog/README.md`도 재현형 사용 순서를 반영했다 | 우선순위 구간 `02`~`05`에 체크포인트 섹션 보강 |
| 2026-03-20 20:36 | DONE | `blog/02-gradle-spring-boot-bootstrap.md`, `blog/03-docker-mysql-redis-dev-environment.md`, `blog/04-application-yml-and-profile-strategy.md`, `blog/05-jpa-flyway-querydsl-redis-cache-foundation.md`에 시작 상태, 변경 파일, 구현 체크리스트, 실행/검증 명령, 글 종료 체크포인트, 자주 막히는 지점을 추가해 첫 재현형 보강 배치를 적용했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 20:43 | DONE | `git diff --check`로 문서 포맷을 다시 검증했고, 공통 규칙 문서 + 초반 4개 글 재현형 보강 배치가 커밋 가능한 상태임을 확인했다 | add/commit/push |
| 2026-03-20 21:02 | DONE | 재현성 보강 2차 배치 범위를 `11`~`15`로 고정하고, 현재 본문 말미와 관련 auth/test/CI 파일명을 다시 점검했다. Security/JWT/AccessPolicy/Testcontainers/tagged CI를 따라 하기 위해 독자가 실제로 무엇을 준비해야 하는지 기준을 정리했다 | `11`~`15`에 재현형 섹션 추가 |
| 2026-03-20 21:17 | DONE | `blog/11-securityconfig-signup-login-basics.md`, `blog/12-jwt-cookie-auth-flow.md`, `blog/13-multitenant-access-hardening.md`, `blog/14-why-testcontainers-over-h2.md`, `blog/15-github-actions-and-tagged-test-suites.md`에 시작 상태, 변경 파일, 구현 체크리스트, 실행/검증 명령, 글 종료 체크포인트, 자주 막히는 지점을 추가했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 21:23 | DONE | `git diff --check` 기준 2차 배치 문서 포맷이 정상이었고, 인증/테스트 구간 `11`~`15`의 재현형 보강이 커밋 가능한 상태임을 확인했다 | add/commit/push |
| 2026-03-20 21:39 | DONE | 재현성 보강 3차 배치 범위를 `16`~`21`로 고정하고, 인증 고도화/운영성 구간에서 실제 구현 파일과 테스트 매핑을 다시 점검했다. `AuthService`, `AuthSessionRegistryService`, `ClientIpResolver`, `OAuth2AuthenticationSuccessHandler`, `AuthAuditLogService`, `CriticalDependenciesHealthIndicator`, `NotificationDispatchService`를 기준으로 글별 체크포인트 기준을 정리했다 | `16`~`21`에 재현형 섹션 추가 |
| 2026-03-20 21:52 | DONE | `blog/16-refresh-rotation-and-active-session-control.md`, `blog/17-rate-limit-and-client-ip-trust-model.md`, `blog/18-oauth2-social-account-lifecycle.md`, `blog/19-auth-audit-log-and-operations-console.md`, `blog/20-openapi-management-plane-and-observability.md`, `blog/21-notification-outbox-and-incident-channel.md`에 시작 상태, 변경 파일, 구현 체크리스트, 실행/검증 명령, 글 종료 체크포인트, 자주 막히는 지점을 추가했다 | 관련 테스트 재실행 및 포맷 검증 |
| 2026-03-20 22:08 | DONE | 검증 명령을 현재 저장소의 태그 기반 테스트 전략에 맞춰 `test --tests`에서 `fastTest`/`integrationTest` 중심으로 정리했다. 특히 Outbox 테스트는 `test --tests`로 좁혀 돌릴 때 결과 XML 쓰기 충돌이 재현돼, 재현형 문서에는 `integrationTest` 실행 경로를 SSOT로 반영했다 | 최종 검증 명령 실행 및 포맷 검사 |
| 2026-03-20 22:16 | DONE | `./gradlew --no-daemon fastTest --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`와 `./gradlew --no-daemon integrationTest --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.api.AuthAuditApiIntegrationTest" --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.integration.NotificationOutboxIntegrationTest" --tests "com.erp.integration.NotificationOutboxRetryIntegrationTest"`를 통과시켜 `16`~`21` 재현형 문서의 실행 예시를 실제 저장소 기준으로 검증했다 | `git diff --check`, add/commit/push |

## 현재 상태 요약
- 현재 단계: `DONE`
- 활성 작업: 없음
- 블로커: 없음
