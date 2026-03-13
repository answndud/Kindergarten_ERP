# PLAN.md

## 작업명
- 후속 고도화 8차 (OAuth2 계정 충돌 정책/UX 정합화)

## 1) 목표 / 범위
- OAuth2/social 로그인 시 동일 이메일의 기존 계정을 자동 연결하지 않는 보안 정책을 코드와 UX에 명시한다.
- 소셜 가입 충돌을 일반 실패와 구분해 로그인 화면에 사람이 이해 가능한 안내를 노출한다.
- 충돌 리다이렉트와 로그인 페이지 렌더링을 테스트로 고정하고, 인터뷰용 문서에 계정 탈취 방어 논리를 정리한다.

## 2) 세부 작업 단계
1. OAuth2 충돌 정책 정리
   - `OAuth2AuthenticationSuccessHandler`에서 기존 이메일 충돌을 별도 사유로 분기한다.
   - 동일 이메일 자동 연결을 금지하는 이유를 코드 흐름에 드러낸다.

2. 로그인 화면 UX 정합화
   - `/login` 뷰가 OAuth2 실패 사유별 안내 문구를 렌더링하도록 정리한다.
   - 일반 실패와 계정 충돌을 구분하고, 사용자가 다음 행동을 이해할 수 있게 만든다.

3. 테스트 추가
   - OAuth2 success handler 단위 테스트로 충돌 시 리다이렉트 쿼리와 JWT 미발급을 검증한다.
   - 뷰 통합 테스트로 `/login?error=social_account_conflict` 렌더링을 검증한다.

4. 문서화 및 검증
   - `README.md`, `docs/phase/`에 정책 이유와 면접 답변 포인트를 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 충돌 안내 문구가 너무 구체적이면 인증 정책이 과도하게 노출될 수 있음
  - 대응: 자동 연결 금지와 기존 로그인 사용만 안내하고, 내부 계정 상태는 직접 노출하지 않는다
- success handler 분기 추가 시 기존 social 신규 가입/재로그인 흐름이 깨질 수 있음
  - 대응: 기존 provider/providerId 매칭 경로는 유지하고, 충돌 케이스만 별도 테스트로 고정한다
- 로그인 뷰 오류 처리 로직이 과해지면 템플릿 유지보수가 어려워질 수 있음
  - 대응: 에러 코드별 문구 매핑을 컨트롤러에 모아 단순 문자열 모델만 템플릿에 전달한다
