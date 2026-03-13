# PLAN.md

## 작업명
- 후속 고도화 14차 (인증/소셜 감사 로그 도입)

## 1) 목표 / 범위
- 로그인, refresh, 소셜 link/unlink 같은 보안 이벤트를 DB에 남기는 감사 로그를 추가한다.
- 실패 이벤트도 rollback과 분리해 보존되도록 별도 트랜잭션으로 저장한다.
- 포트폴리오/면접 설명용으로 "어떤 인증 이벤트를 왜 저장했는가"를 문서화한다.

## 2) 세부 작업 단계
1. 감사 로그 모델 추가
   - `auth_audit_log` 마이그레이션, 엔티티, 리포지토리를 추가한다.
   - event type/result/provider/memberId/email/clientIp/reason을 저장한다.

2. 인증/소셜 이벤트 저장 경로 구현
   - `AuthService`에서 login/refresh 성공·실패를 기록한다.
   - `OAuth2AuthenticationSuccessHandler`에서 social login/link 성공·실패를 기록한다.
   - `MemberApiController`에서 social unlink 성공·실패를 기록한다.

3. 테스트/문서 보강
   - auth/member/OAuth2 테스트에 감사 로그 생성 검증을 추가한다.
   - `README.md`, `docs/phase/`에 감사 로그 목적, 저장 필드, 트랜잭션 설계를 기록한다.

4. 테스트/문서화 및 검증
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 감사 로그 저장 실패가 본래 인증 플로우를 깨뜨릴 수 있음
  - 대응: 감사 로그 저장은 `REQUIRES_NEW` + 내부 예외 swallow/warn 처리로 분리한다
- 실패 이벤트가 메인 트랜잭션 rollback에 함께 지워질 수 있음
  - 대응: 별도 서비스/별도 트랜잭션에서 저장해 실패 로그도 남긴다
- 로그가 너무 많은 개인정보를 담을 수 있음
  - 대응: 비밀번호/토큰 원문은 저장하지 않고 memberId/email/provider/clientIp/reason code만 최소한으로 남긴다
