# PROGRESS.md

## 작업명
- 후속 고도화 15차 (인터뷰 패키징 + 운영 관측성 + 감사 로그 조회 API)

## 진행 로그

| 시간 (KST) | 상태 | 수행 내용 | 다음 액션 |
|---|---|---|---|
| 2026-03-13 21:54 | DONE | `docs/interview/interview_one_pager.md`, `docs/interview/demo_3min_scenario.md`를 추가하고 README 문서 섹션을 인터뷰 바로 보기 + 상세 결정 로그 구조로 재정리 | 운영 관측성/감사 로그 조회 구현 최종 검증 |
| 2026-03-13 21:54 | DONE | `spring-boot-starter-actuator`, health/info 노출, liveness/readiness probe, `CorrelationIdFilter`, `RequestLoggingFilter`, logback correlation id 패턴을 반영해 운영 관측성 baseline을 추가 | 감사 로그 조회 API와 테스트 최종 검증 |
| 2026-03-13 21:54 | DONE | 원장 전용 `/api/v1/auth/audit-logs` API와 `AuthAuditLogQueryService`/repository query를 추가. 같은 유치원 소속 member 기반 로그만 조회하고 eventType/result/provider/email/date 필터를 지원하도록 정리 | phase 문서와 최종 로컬 검증 로그 기록 |
| 2026-03-13 21:54 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "*ObservabilityIntegrationTest" --tests "*AuthAuditApiIntegrationTest"`, `./gradlew test --tests "*AuthApiIntegrationTest" --tests "*ViewEndpointTest"`, `./gradlew test`, `git diff --check` 통과 | add/commit/push 및 GitHub Actions run 확인 |
| 2026-03-13 21:45 | IN_PROGRESS | 새 후속 배치 시작. 이전 단계까지 보안/인증 설계와 감사 로그 저장은 닫혔지만, 면접에서 바로 말할 1장 요약본, 운영 관측성 baseline, 실제 조회 가능한 감사 로그 API, 데모 시나리오 문서가 비어 있는 상태 확인 | `PLAN.md`를 인터뷰 패키징 + 운영 관측성 + 감사 로그 조회 API 기준으로 갱신 |
| 2026-03-13 21:46 | IN_PROGRESS | `PLAN.md`를 후속 고도화 15차 기준으로 갱신. 이번 배치는 인터뷰 요약본, 3분 데모 스크립트, actuator/health/readiness/correlation id/request logging, 원장 전용 auth audit log 조회 API, 테스트/phase 문서화까지 포함 | 기존 설정/보안/API 패턴 확인 후 코드 및 문서 반영 |
| 2026-03-13 21:39 | DONE | `219def3` (`feat: add auth and social audit logging`)를 `origin/main`에 push 완료 | GitHub Actions run 결과 기록 후 배치 종료 |
| 2026-03-13 21:39 | DONE | GitHub Actions run `23051105269` 성공 확인. `Fast Checks` 1m46s, `Integration Suite` 2m43s, artifact 업로드 정상 확인 | 후속 과제 정리 |
| 2026-03-13 21:35 | DONE | `auth_audit_log` 마이그레이션, `AuthAuditLog` 엔티티/리포지토리/서비스를 추가하고 login/refresh/social link/unlink 성공·실패를 DB 감사 로그로 기록하도록 연결. 저장은 `REQUIRES_NEW` + 내부 예외 swallow/warn 처리로 분리 | 테스트 가시성 문제 정리 및 문서화 |
| 2026-03-13 21:35 | DONE | `BaseIntegrationTest.readCommitted(...)` 헬퍼를 추가하고 `AuthApiIntegrationTest`, `MemberApiIntegrationTest`, `OAuth2AuthenticationSuccessHandlerTest`에 감사 로그 회귀 검증을 반영. 기존 테스트 트랜잭션과 별도 감사 로그 트랜잭션의 가시성 차이를 새 트랜잭션 조회로 해소 | README/phase 문서 및 최종 로컬 검증 |
| 2026-03-13 21:35 | DONE | `README.md`, `docs/phase/phase33_auth_social_audit_log.md`에 감사 로그 도입 이유, 저장 필드, FK 미사용 이유, `REQUIRES_NEW` 선택 배경, 인터뷰 포인트를 기록 | add/commit/push 및 원격 CI 확인 |
| 2026-03-13 21:35 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`, `git diff --check` 통과 | add/commit/push 및 GitHub Actions run 확인 |
| 2026-03-13 21:24 | IN_PROGRESS | 새 후속 배치 시작. 인증/소셜 흐름을 점검한 결과 로그인 실패, refresh 실패, 소셜 연결/해제 같은 보안 이벤트가 DB에 전혀 남지 않아 면접에서 운영·감사 추적 이야기를 하기 어려운 공백 확인 | `PLAN.md`를 인증/소셜 감사 로그 도입 기준으로 갱신하고 저장 필드/트랜잭션 경계 설계 |
| 2026-03-13 21:25 | IN_PROGRESS | `PLAN.md`를 후속 고도화 14차(인증/소셜 감사 로그 도입) 기준으로 갱신. 이번 배치는 `auth_audit_log` 모델 추가, login/refresh/social link/unlink 이벤트 저장, 테스트/문서화까지 포함 | 감사 로그 저장 경로와 회귀 테스트 구현 |
| 2026-03-13 21:17 | DONE | `V9__preserve_social_account_history.sql`과 `MemberSocialAccount.unlinkedAt`를 추가하고, unlink를 삭제가 아닌 비활성화로 전환. `Member`는 active/historical social account를 구분해 linked 상태, summary, legacy provider sync를 active 기준으로 재계산하도록 정리 | same-provider replacement 정책 및 UI 메시지 마감 |
| 2026-03-13 21:17 | DONE | `SocialAccountLinkService`/`MemberRepository`를 보강해 active social lookup과 any-history lookup을 분리하고, 같은 provider에 과거 이력이 있는데 다른 `providerId`를 연결하려 하면 `A011`로 차단하도록 반영. settings 화면은 재연결 상태와 교체 금지 안내를 노출 | 서비스/OAuth2/view 테스트 보강 |
| 2026-03-13 21:17 | DONE | `SocialAccountLinkServiceTest`를 추가하고 `MemberApiIntegrationTest`, `ViewEndpointTest`, `OAuth2AuthenticationSuccessHandlerTest`를 보강해 same-provider relink 허용, replacement 차단, settings 재연결 문구, callback error reason 매핑을 검증. `README.md`, `docs/phase/phase32_social_provider_identity_immutability.md` 문서화 완료 | 최종 검증 로그 기록 후 add/commit/push |
| 2026-03-13 21:17 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest" --tests "com.erp.domain.auth.service.SocialAccountLinkServiceTest"`, `git diff --check` 통과 | add/commit/push 및 원격 CI 확인 |
| 2026-03-13 21:16 | IN_PROGRESS | 새 후속 배치 시작. 소셜 계정 정규화 이후 흐름을 재점검한 결과, 현재 구조는 unlink 후 다른 같은 provider 계정으로 재연결할 수 있어 "로그인 식별자 불변" 정책이 실제로는 비어 있는 상태임을 확인 | `PLAN.md`를 소셜 provider 식별자 불변 정책 기준으로 갱신하고, unlink를 삭제가 아닌 이력 보존 구조로 전환 |
| 2026-03-13 21:17 | IN_PROGRESS | `PLAN.md`를 후속 고도화 13차(소셜 provider 식별자 불변 정책 도입) 기준으로 갱신. 이번 배치는 same-provider replacement 차단, active/historical link 분리, settings/OAuth2 메시지 정리, 테스트/문서화까지 포함 | social account lifecycle 모델과 link/relink 정책 구현 |
| 2026-03-13 21:03 | DONE | `member_social_account` 마이그레이션과 `MemberSocialAccount` 엔티티를 추가하고, `Member`/`MemberRepository`/`AuthenticatedMemberResolver`/`OAuth2AuthenticationSuccessHandler`/`SocialAccountLinkService`를 새 social account 구조 기준으로 전환. legacy `auth_provider/provider_id`는 primary provider sync 용도로만 유지 | settings UI와 테스트/문서 마감 |
| 2026-03-13 21:03 | DONE | `settings.html`과 `AuthViewController`를 provider별 카드 UI로 재구성. Google/Kakao 동시 연결 표시, provider별 unlink 허용/차단 사유, unlink 성공 메시지를 다중 provider 정책에 맞춰 정리 | 통합 테스트 및 phase 문서 검증 |
| 2026-03-13 21:03 | DONE | `MemberApiIntegrationTest`, `ViewEndpointTest`, `OAuth2AuthenticationSuccessHandlerTest`, `BaseIntegrationTest`, `TestData`를 보강해 다중 provider unlink, settings 렌더링, OAuth2 social lookup, `member_social_account` cleanup/reset 회귀를 반영. `README.md`, `docs/phase/phase31_member_social_account_normalization.md` 문서화 완료 | 최종 검증 로그 기록 후 add/commit/push |
| 2026-03-13 21:04 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`, `git diff --check` 통과 | add/commit/push 및 원격 CI 확인 |
| 2026-03-13 20:54 | IN_PROGRESS | 새 후속 배치 시작. 기존 소셜 계정 플로우를 재점검한 결과 `member.auth_provider/provider_id` 단일 슬롯 구조 때문에 Google/Kakao 동시 연결이 불가능하고 settings 화면도 단일 provider 가정에 묶여 있는 구조적 한계 확인 | `PLAN.md`를 다중 소셜 연결 정규화 기준으로 갱신하고, 새 `member_social_account` 테이블을 SSOT로 삼는 방향으로 코드/테스트 전환 |
| 2026-03-13 20:55 | IN_PROGRESS | `PLAN.md`를 후속 고도화 12차(소셜 계정 다중 연결 구조 정규화) 기준으로 갱신. 이번 배치는 마이그레이션/backfill, `Member` social account 컬렉션, OAuth2/login/link/unlink/settings 다중 provider 정렬, 테스트/문서화까지 한 번에 닫는 범위로 확정 | settings 템플릿과 남은 테스트/문서/검증 작업 마무리 |
| 2026-03-13 20:46 | DONE | `SocialAccountLinkService.unlinkSocialAccount`와 `DELETE /api/v1/members/social-link/{provider}`를 추가해 소셜 연결 해제 경로를 구현. 로컬 비밀번호가 없는 계정은 `A010`으로 차단하고, 성공 시 provider 슬롯을 `LOCAL/null` 상태로 되돌리도록 정리 | settings unlink UI 및 테스트 보강 |
| 2026-03-13 20:46 | DONE | `settings.html`에 연결 해제 버튼/차단 사유를 추가하고, `MemberApiIntegrationTest`/`ViewEndpointTest`에 unlink 성공/차단/UI 회귀 테스트를 반영 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:46 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"` 통과. `README.md`, `docs/phase/phase30_social_account_unlink_policy.md` 반영 | add/commit/push 진행 |
| 2026-03-13 20:44 | IN_PROGRESS | 새 후속 배치 시작. 소셜 계정의 로컬 비밀번호 설정 이후 흐름을 점검한 결과, 연결 해제 기능이 없어 provider lifecycle이 반쪽 상태이며 로컬 비밀번호 없는 계정 잠금 방지 정책도 명시되지 않은 공백 확인 | `PLAN.md` 갱신 후 unlink 정책/API/UI 설계 |
| 2026-03-13 20:44 | IN_PROGRESS | `PLAN.md`를 후속 고도화 11차(소셜 계정 연결 해제 정책 추가) 기준으로 갱신. 이번 배치는 unlink endpoint, 잠금 방지 정책, settings 상태/UI 보강에 집중 | SocialAccountLinkService/Member API/settings/view test 수정 |
| 2026-03-13 20:42 | DONE | `MemberService.setInitialPassword`와 `POST /api/v1/members/password/bootstrap`를 추가해 소셜 전용 계정의 초기 로컬 비밀번호 설정 경로를 구현. 이미 로컬 비밀번호가 있는 계정은 `M005`로 차단 | settings 화면/회원 테스트 보강 |
| 2026-03-13 20:42 | DONE | `settings.html`을 로컬 비밀번호 보유 여부에 따라 "비밀번호 변경"과 "로컬 비밀번호 설정"으로 분기. `MemberApiIntegrationTest`와 `ViewEndpointTest`에 password bootstrap 성공/차단/UI 회귀 테스트 추가 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:42 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"` 통과. `README.md`, `docs/phase/phase29_social_password_bootstrap.md` 반영 | add/commit/push 진행 |
| 2026-03-13 20:39 | IN_PROGRESS | 새 후속 배치 시작. explicit social link 이후 흐름을 점검한 결과, 소셜 전용 계정이 settings에서 로컬 비밀번호를 설정할 정상 경로가 없고 화면도 막힌 안내 문구에서 끝나는 공백 확인 | `PLAN.md` 갱신 후 password bootstrap 정책/API/UI 설계 |
| 2026-03-13 20:39 | IN_PROGRESS | `PLAN.md`를 후속 고도화 10차(소셜 전용 계정의 로컬 비밀번호 설정 추가) 기준으로 갱신. 이번 배치는 password bootstrap endpoint, settings 폼 분기, 회원/뷰 테스트 보강에 집중 | Member API/service/settings/view test 수정 |
| 2026-03-13 20:24 | DONE | `/auth/social/link/{provider}`와 `OAuth2LinkSessionService`를 추가해 settings 기반 명시적 소셜 연결 시작점을 구현. `OAuth2AuthenticationSuccessHandler`는 link intent 존재 시 현재 회원 provider 연결로 분기하고 성공/실패 모두 세션을 정리하도록 정리 | settings 상태/UI 및 테스트 보강 |
| 2026-03-13 20:24 | DONE | `settings.html`에 소셜 연결 카드, linked provider 표시, 소셜 전용 계정의 비밀번호 변경 불가 안내를 반영. `ViewEndpointTest`와 `OAuth2AuthenticationSuccessHandlerTest`에 link 시작/성공/화면 상태 회귀 테스트 추가 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:24 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"` 통과. `README.md`, `docs/phase/phase28_explicit_social_account_linking.md` 반영 | add/commit/push 진행 |
| 2026-03-13 20:19 | IN_PROGRESS | 새 후속 배치 시작. OAuth2 충돌 차단 이후 흐름을 재점검한 결과, 기존 계정에 소셜 로그인 연결을 명시적으로 수행할 정상 경로가 없고 settings 화면도 소셜 전용 계정 상태를 반영하지 못하는 공백 확인 | `PLAN.md` 갱신 후 link intent/session 기반 연결 플로우 설계 |
| 2026-03-13 20:19 | IN_PROGRESS | `PLAN.md`를 후속 고도화 9차(명시적 소셜 계정 연결 플로우 추가) 기준으로 갱신. 이번 배치는 link 시작점, OAuth2 callback 분기, settings 상태/메시지 정합화에 집중 | Member/Auth/OAuth2/settings/view test 수정 |
| 2026-03-13 20:12 | DONE | `OAuth2AuthenticationSuccessHandler`에 social account conflict 분기와 실패 시 임시 세션 정리 로직을 추가. `AuthViewController`/`login.html`은 충돌과 일반 실패를 구분해 제목/메시지/다음 행동을 렌더링하도록 정리 | handler/view 테스트 및 문서 반영 |
| 2026-03-13 20:12 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"` 통과. `README.md`, `docs/phase/phase27_oauth2_account_conflict_policy.md` 반영 | add/commit/push 진행 |
| 2026-03-13 20:08 | IN_PROGRESS | 새 후속 배치 시작. OAuth2 성공 핸들러를 재점검한 결과, 기존 이메일 충돌 시 `IllegalStateException`을 일반 실패로 삼켜 로그인 화면에 모호한 오류만 노출하는 정책/UX 공백 확인 | `PLAN.md` 갱신 후 충돌 사유 분리와 로그인 뷰 메시지 정책 구현 |
| 2026-03-13 20:09 | IN_PROGRESS | `PLAN.md`를 후속 고도화 8차(OAuth2 계정 충돌 정책/UX 정합화) 기준으로 갱신. 이번 배치는 social account conflict 분기, 로그인 오류 메시지 개선, handler/view 테스트 추가에 집중 | AuthViewController/login template/success handler/test 수정 |
| 2026-03-13 20:45 | DONE | `AuthenticatedMemberResolver` 추가 후 `RoleRedirectInterceptor`, `GlobalControllerAdvice`, `AuthViewController`를 principal 타입 독립적으로 동작하도록 정리. `CustomUserDetails` 고정 캐스팅 제거 | OAuth2 성공 후 세션 정리 및 view 테스트 추가 |
| 2026-03-13 20:48 | DONE | `OAuth2AuthenticationSuccessHandler`에서 JWT cookie 발급 후 SecurityContext/session 정리 반영. `ViewEndpointTest`에 OAuth2 principal 기반 `/profile` 접근 성공 시나리오 추가 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:49 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.integration.ViewEndpointTest"`, `git diff --check` 통과. `README.md`, `docs/phase/phase26_oauth2_principal_runtime_safety.md` 반영. 직전 배치 원격 CI run `23047606163`도 성공 확인 | 사용자 승인 시 add/commit/push |
| 2026-03-13 20:37 | IN_PROGRESS | 새 후속 배치 시작. 점검 결과 `RoleRedirectInterceptor`가 인증 principal을 `CustomUserDetails`로 고정 캐스팅하고 있어 OAuth2/session principal이 남는 경우 view 요청에서 `ClassCastException`이 날 수 있는 런타임 리스크 확인 | `PLAN.md` 갱신 후 공통 member resolver 설계/구현 |
| 2026-03-13 20:39 | IN_PROGRESS | `PLAN.md`를 후속 고도화 7차(OAuth2 Principal 런타임 안전성 보강) 기준으로 갱신. 이번 배치는 principal member resolver, OAuth2 성공 후 세션 정리, 뷰 통합 테스트 보강에 집중 | interceptor/controller advice/success handler/view test 수정 |
| 2026-03-13 20:28 | DONE | `AuthRateLimitService`를 사전 확인/실패 기록/성공 초기화 구조로 분리하고, `AuthService`에서 로그인 성공은 이메일 실패 카운터를 지우고 실패만 누적하도록 정책 정교화 | Auth 통합 테스트 및 문서화 |
| 2026-03-13 20:31 | DONE | `AuthApiIntegrationTest`에 반복 성공 로그인 비차단, 성공 후 이메일 실패 카운터 초기화 시나리오를 추가 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:32 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`, `git diff --check` 통과. `README.md`, `docs/phase/phase25_login_rate_limit_policy_refinement.md` 반영. 직전 배치 원격 CI run `23047455397`도 성공 확인 | 사용자 승인 시 add/commit/push |
| 2026-03-13 20:21 | IN_PROGRESS | 새 후속 배치 시작. 인증 rate limit 정책 재점검 결과 로그인 성공도 실패 예산에 포함돼 같은 사용자가 짧은 시간에 여러 번 정상 로그인해도 차단될 수 있는 설계 문제 확인 | `PLAN.md` 갱신 후 Auth rate limit 정책 조정 |
| 2026-03-13 20:22 | IN_PROGRESS | `PLAN.md`를 후속 고도화 6차(로그인 Rate Limit 정책 정교화) 기준으로 갱신. 이번 배치는 실패만 누적, 성공 시 이메일 실패 카운터 초기화, auth 테스트 보강에 집중 | AuthService/AuthRateLimitService/AuthApiIntegrationTest 수정 |
| 2026-03-13 20:14 | DONE | `ClientIpResolver`/`ClientIpProperties` 추가 후 로그인/refresh API를 trusted proxy 기준 client IP 해석으로 전환. untrusted remote는 `remoteAddr`만 사용하고, loopback 및 설정된 proxy만 전달 헤더를 신뢰하도록 정리 | Auth 통합 테스트 및 문서화 |
| 2026-03-13 20:17 | DONE | `AuthApiIntegrationTest`에 spoofed `X-Forwarded-For` 무시, trusted loopback proxy에서 전달 헤더 반영 시나리오를 추가 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:18 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`, `git diff --check` 통과. `README.md`, `docs/phase/phase24_auth_client_ip_trust_model.md` 반영 | 사용자 승인 시 add/commit/push |
| 2026-03-13 20:07 | IN_PROGRESS | 새 후속 배치 시작. 인증 rate limit 재점검 결과 `AuthApiController`가 `X-Forwarded-For`, `X-Real-IP`를 무조건 신뢰해 임의 클라이언트도 헤더 조작으로 IP 제한을 우회할 수 있는 문제 확인 | `PLAN.md` 갱신 후 client IP resolver 설계/구현 |
| 2026-03-13 20:09 | IN_PROGRESS | `PLAN.md`를 후속 고도화 5차(인증 Client IP 신뢰 모델 하드닝) 기준으로 갱신. 이번 배치는 trusted proxy 기준 정립, auth 테스트 보강, 인터뷰용 문서화에 집중 | resolver/properties 추가 및 Auth controller/test 수정 |
| 2026-03-13 19:58 | DONE | `CalendarEventRepository`/`CalendarEventService`에 반복 일정 후보 조회와 occurrence 확장 로직을 반영하고, 학부모의 유치원 전체 일정 조회를 허용하도록 권한 정합화 | 캘린더 통합 테스트 및 문서화 |
| 2026-03-13 20:01 | DONE | `CalendarApiIntegrationTest`를 재작성해 반복 일정 조회 성공, 학부모 유치원 일정 조회 성공, 교차 유치원 상세 차단, 잘못된 반복 입력 실패를 검증 | README/phase 문서 반영 및 최종 검증 |
| 2026-03-13 20:03 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.CalendarApiIntegrationTest"`, `git diff --check` 통과. `README.md`, `docs/phase/phase23_calendar_recurrence_access_alignment.md` 반영 | 사용자 승인 시 add/commit/push |
| 2026-03-13 19:50 | IN_PROGRESS | 새 후속 배치 시작. 캘린더 영역 재점검 결과, 반복 일정이 문서와 달리 실제 조회에서 전혀 확장되지 않고, 학부모가 유치원 전체 일정을 보지 못하는 정합성 문제 확인 | `PLAN.md` 갱신 후 캘린더 서비스/테스트 수정 |
| 2026-03-13 19:52 | IN_PROGRESS | `PLAN.md`를 후속 고도화 4차(캘린더 반복 일정/권한 정합성 보강) 기준으로 갱신. 이번 배치는 캘린더 구현-문서 불일치 해소와 회귀 테스트 보강에 집중 | Calendar repository/service/controller/test 수정 |
| 2026-03-13 19:41 | DONE | `ede8ceb` push 후 GitHub Actions run `23046823391` 최종 통과 확인. `Fast Checks` 57초, `Integration Suite` 3분 3초, artifact 업로드 정상 확인. 이전에 남던 Node20 deprecation annotation 섹션이 사라져 Node24 네이티브 action 전환 효과 확인 | 후속 개선 후보 선정 또는 배치 마감 |
| 2026-03-13 19:36 | DONE | `.github/workflows/ci.yml`의 action major를 `checkout@v5`, `setup-java@v5`, `setup-gradle@v5`, `upload-artifact@v6`로 상향하고 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24`를 제거 | README/phase 문서와 검증 결과 반영 |
| 2026-03-13 19:37 | DONE | `README.md`, `docs/phase/phase22_github_actions_node24_native_actions.md` 반영. 검증 완료: `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml')"` 및 `git diff --check` 통과 | 사용자 승인 시 add/commit/push |
| 2026-03-13 19:31 | DONE | `4915d4c` 기준 GitHub Actions run `23046637472` 통과 확인. `Fast Checks` 54초, `Integration Suite` 2분 17초, artifact 업로드 정상 확인. 다만 `checkout/setup-java/setup-gradle/upload-artifact`가 여전히 Node20 deprecation annotation을 남기는 점 확인 | action major 업그레이드 범위 확인 및 workflow 갱신 |
| 2026-03-13 19:33 | IN_PROGRESS | `PLAN.md`를 후속 고도화 3차(Node24 네이티브 전환) 기준으로 갱신. 이번 배치는 GitHub Actions action 버전 업그레이드와 문서화에 집중 | workflow 수정 및 README/phase 문서 반영 |
| 2026-03-13 19:21 | IN_PROGRESS | `2b3f858` push 후 GitHub Actions run `23046452039` 확인 시작. 병행해서 다음 개선 후보를 점검한 결과 인증 rate limit 부재 확인 | 원격 run 완료 확인 후 auth rate limit 설계/구현 |
| 2026-03-13 19:23 | DONE | GitHub Actions run `23046452039` 통과 확인. `Fast Checks` 1m11s, `Integration Suite` 2m29s, artifact 업로드 정상 확인. 다만 Node20 deprecation annotation은 여전히 남아 추후 action 버전 업그레이드 검토 필요 | 인증 rate limit 코드/테스트 반영 |
| 2026-03-13 19:25 | DONE | `AuthRateLimitService` 추가, 로그인(IP 15/10분 + 이메일 5/10분) 및 refresh(IP 10/5분) rate limit 반영, `A006/429` 에러 코드 및 4xx warn 로깅 정리 | Auth 통합 테스트 및 문서화 |
| 2026-03-13 19:26 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"` 통과. `README.md`, `docs/phase/phase21_auth_rate_limit.md` 반영 | 사용자 승인 시 add/commit/push |
| 2026-03-13 18:59 | IN_PROGRESS | `ff5b683` push 후 GitHub Actions run `23045682877` 확인. 새 split workflow가 `main` 기준으로 정상 시작됨 | run 완료까지 상태/실패 job 로그 확인 |
| 2026-03-13 19:04 | DONE | GitHub Actions run `23045682877` 통과 확인. `Fast Checks` 1m45s, `Integration Suite` 4m11s, artifact 업로드 정상 확인 | runner annotation 기반 Node24 호환 경고 대응 |
| 2026-03-13 19:06 | DONE | `.github/workflows/ci.yml`에 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true` 추가, `docs/phase/phase20_github_actions_node24_compatibility.md`/`README.md` 반영, `ruby` YAML 파싱 및 `git diff --check` 통과 | 사용자 승인 시 add/commit/push |
| 2026-03-13 18:42 | IN_PROGRESS | 사용자 요청에 따라 남은 과제를 한 배치로 전환, `PLAN.md`를 CI 확인/JWT 세션 설계/대시보드 지표/CI 최적화 기준으로 갱신 | 원격 workflow run 상태 확인 후 Auth/Dashboard 구조 점검 |
| 2026-03-13 18:43 | DONE | `gh run`으로 첫 GitHub Actions 실패 원인 확인. `gradle-wrapper.jar`가 `.gitignore` 후행 `*.jar` 규칙에 막혀 runner에 누락되는 문제로 진단 | wrapper 추적 복구 + CI 분리 반영 |
| 2026-03-13 18:45 | DONE | JWT claim 확장(`memberId/sessionId/tokenType/jti`), Redis 세션 키 구조 도입, refresh rotation, 현재 세션 로그아웃/전체 세션 탈퇴 revoke 반영 | 인증 회귀 테스트 및 문서화 |
| 2026-03-13 18:47 | DONE | `announcement_view` 마이그레이션 추가, 공지 고유 열람률과 주말/입소일 반영 출석률로 대시보드 산식 보정, 집계 projection/쿼리 통합으로 쿼리 수 `13 -> 5` 확인 | Dashboard/Auth 테스트 및 README 반영 |
| 2026-03-13 18:52 | DONE | `fastTest`/`integrationTest` 태스크 추가, GitHub Actions 2-job 분리, README 및 `docs/phase/phase17~19` 문서화 완료. 검증: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.api.DashboardApiIntegrationTest"`, `./gradlew test --tests "com.erp.performance.DashboardPerformanceStoryTest"`, `./gradlew fastTest integrationTest`, `./gradlew test` 모두 통과 | 사용자 승인 시 add/commit/push |
| 2026-03-13 18:29 | DONE | `.github/workflows/ci.yml` 추가. PR/push/manual 트리거, Java 17, Gradle 캐시, `./gradlew test`, test report artifact 업로드 구성 | README/phase 문서와 교차 검토 |
| 2026-03-13 18:28 | DONE | `docs/phase/phase16_github_actions_ci.md`, `README.md`에 CI/Testcontainers 전략과 인터뷰용 설명 포인트 반영 | YAML/저장소 상태 검증 후 로그 마감 |
| 2026-03-13 18:27 | DONE | `ruby`로 `.github/workflows/ci.yml` YAML 파싱 검증, `git diff --check` 통과 | PROGRESS/PLAN 마감 |
| 2026-03-13 18:25 | IN_PROGRESS | 사용자 요청으로 후속 개선을 CI 자동화 작업으로 전환, `PLAN.md`를 GitHub Actions + Testcontainers 기준으로 갱신 | 워크플로우 부재 상태 확인 후 CI 파일/문서 반영 |
| 2026-03-13 18:19 | DONE | `docs/phase/phase15_testcontainers_integration_test_stack.md` 작성 완료. H2/Mock Redis 한계, Testcontainers 전환 이유, 구현 포인트, 면접 답변 포인트를 정리 | 최종 결과 공유 |
| 2026-03-13 18:18 | DONE | 검증 완료: `./gradlew test` 전체 통과. MySQL/Redis Testcontainers 기반 전체 테스트 스위트 정상 동작 확인 | 문서화 및 작업 로그 마무리 |
| 2026-03-13 18:05 | DONE | 검증 완료: `./gradlew test --tests "com.erp.ErpApplicationTests" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.KidApiIntegrationTest" --tests "com.erp.api.AttendanceApiIntegrationTest" --tests "com.erp.api.NotepadApiIntegrationTest" --tests "com.erp.api.AnnouncementApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest"` 통과 | 전체 테스트 스위트 재검증 |
| 2026-03-13 18:03 | DONE | Testcontainers 지원 클래스, `application-test.yml`, Redis 설정, MySQL cleanup/reset 로직, 실제 Redis 기반 인증 테스트 반영 후 `./gradlew compileJava compileTestJava` 성공 | 대표 통합 테스트 실행 |
| 2026-03-13 18:06 | IN_PROGRESS | 사용자 요청에 따라 후속 개선을 Testcontainers 전환 작업으로 전환, `PLAN.md`를 테스트 스택 현실화 기준으로 갱신 | 테스트 설정/의존성/베이스 클래스 점검 후 코드 반영 |
| 2026-03-13 17:58 | DONE | `docs/phase/phase14_multitenant_access_hardening.md` 작성 완료. 문제 재현, 설계 결정, 테스트 증빙, 인터뷰 답변 포인트를 문서화 | 최종 결과 정리 및 사용자 공유 |
| 2026-03-13 17:56 | DONE | 검증 완료: `./gradlew test --tests "com.erp.api.KidApiIntegrationTest" --tests "com.erp.api.AttendanceApiIntegrationTest" --tests "com.erp.api.NotepadApiIntegrationTest" --tests "com.erp.api.AnnouncementApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest"` 통과 | 문서화 및 작업 기록 마무리 |
| 2026-03-13 17:50 | DONE | 권한 하드닝 패치 후 `./gradlew compileJava compileTestJava` 성공. 컨트롤러 시그니처 누락 및 재조회 경로 일관성 정리 | 교차-유치원/soft delete 회귀 테스트 추가 |
| 2026-03-13 17:45 | DONE | `AccessPolicyService` 추가 및 `Kid`, `Classroom`, `Attendance`, `Notepad`, `Announcement`, `Notification` 서비스/컨트롤러에 요청자 기반 권한 검증 반영 | 컴파일 검증 및 테스트 추가 |
| 2026-03-13 17:34 | IN_PROGRESS | 사용자 요청에 따라 리뷰에서 구현 단계로 전환, `PLAN.md`를 권한 경계 하드닝 작업 기준으로 갱신 | 접근 정책 설계 후 코드/테스트/문서 순으로 반영 |
| 2026-03-13 17:30 | IN_PROGRESS | 사용자 요청으로 전면 코드 리뷰 시작, `PLAN.md`를 리뷰 작업 기준으로 갱신 | 설정/보안/도메인/테스트/문서 순으로 근거 수집 |
| 2026-03-13 17:34 | DONE | 보안 경계, 알림장/알림 권한, 테스트 현실성, 대시보드 지표 산식 중심으로 저장소 전면 리뷰 완료 | 사용자에게 severity 기반 리뷰 결과 전달 |
| 2026-02-21 10:05 | IN_PROGRESS | 사용자 요청으로 후속 개선 배치 시작, PLAN.md를 P0/P1/P2 통합 범위로 갱신 | 코드/문서/테스트 변경 순차 적용 |
| 2026-02-21 12:40 | DONE | `SecurityConfig` CORS 허용 메서드에 `PATCH` 추가, 목록 API(`/api/v1/kids`, `/api/v1/classrooms`) 필수 필터 누락 시 400 응답으로 명시화 | 입학 승인 ParentKid 관계값 개선 |
| 2026-02-21 12:50 | DONE | `ApproveKidApplicationRequest`에 `relationship` 확장 및 기본값(FATHER) 처리, 승인 시 요청 관계값으로 ParentKid 생성 반영 | API/백로그 문서 정합화 |
| 2026-02-21 12:58 | DONE | `README.md` API 표를 컨트롤러 매핑 기준으로 확장(알림/캘린더/지원/원생/출석 등), `docs/requirements/dev-todo.md`/`docs/project_plan.md` 최신 상태 반영 | 통합 테스트 보강 및 실행 |
| 2026-02-21 13:04 | DONE | 신규 통합 테스트 추가: `ClassroomApiIntegrationTest`, `KindergartenApiIntegrationTest`, `KidApplicationApiIntegrationTest`, `KindergartenApplicationApiIntegrationTest` + `KidApiIntegrationTest` 필터 누락 케이스 보강 | 최종 검증 및 작업 종료 |
| 2026-02-21 13:05 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.ClassroomApiIntegrationTest" --tests "com.erp.api.KindergartenApiIntegrationTest" --tests "com.erp.api.KidApplicationApiIntegrationTest" --tests "com.erp.api.KindergartenApplicationApiIntegrationTest" --tests "com.erp.api.KidApiIntegrationTest"` 통과 | 세션 종료 |
| 2026-02-20 22:05 | IN_PROGRESS | 사용자 요청에 따라 개선 작업 시작, PLAN.md를 이번 범위(코드 우선/문서 분리)로 갱신 | P0 코드 수정(공지사항/승인 처리) 적용 |
| 2026-02-20 22:10 | DONE | 공지사항 하드코딩 사용자 ID 제거 및 인증 사용자 기반 처리 반영, 서비스 유치원 경계 검증 추가 | 승인 처리 서비스 안정성/로직 보정 |
| 2026-02-20 22:12 | DONE | `KidApplicationService`/`KindergartenApplicationService` null 안전성 보강, 교사 지원 승인 후 후속 거절 대상을 같은 교사의 pending 지원서로 수정 | API 에러 계약 정합화 및 테스트 보강 |
| 2026-02-20 22:14 | DONE | 출석 daily 필수 파라미터 누락 시 400 응답으로 변경, 인증 진입점 응답을 `ApiResponse.error` 포맷으로 통일 | 컴파일/테스트 실행 |
| 2026-02-20 22:16 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AnnouncementApiIntegrationTest"`, `./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest"`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`, `./gradlew test --tests "com.erp.domain.kindergartenapplication.service.KindergartenApplicationServiceTest"` 통과 | 문서 분리 배치는 후속 진행 |
| 2026-02-20 22:22 | IN_PROGRESS | 문서 분리 배치 착수: PLAN 전환(문서 동기화), 컨트롤러/마이그레이션 기준 현행 기능 재확인 | README/CURRENT_FEATURES 업데이트 |
| 2026-02-20 22:28 | DONE | `README.md` 주요 기능/API/구조/실행 명령을 현재 구현 상태로 갱신(OAuth2, 캘린더, 지원/알림/대시보드 반영) | `CURRENT_FEATURES.md` 재작성 |
| 2026-02-20 22:31 | DONE | `CURRENT_FEATURES.md`를 실행/권한/도메인/검증 중심으로 전면 업데이트, 구식 Phase/예정 기능 제거 | 최종 교차 검토 및 작업 종료 |

## 현재 상태 요약
- 현재 단계: `IN_PROGRESS`
- 활성 작업: 인터뷰 패키징 + 운영 관측성 + 감사 로그 조회 API
- 블로커: 없음
