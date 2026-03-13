# Phase 27: OAuth2 계정 충돌 정책/UX 정합화

## 배경

기존 OAuth2 로그인 흐름은 `auth_provider + provider_id`로 회원을 찾고, 없으면 신규 소셜 회원을 생성했습니다.
하지만 소셜 제공자가 내려준 이메일이 이미 기존 계정에 사용 중인 경우에는 `IllegalStateException`을 던진 뒤
일반 `social_login_failed`로만 리다이렉트했습니다.

이 상태의 문제는 두 가지였습니다.

1. 사용자 경험이 모호했습니다.
   - 실제 문제는 "기존 계정과 이메일이 충돌함"인데, 화면에는 "소셜 로그인 실패"만 보였습니다.
2. 보안 정책이 코드에 드러나지 않았습니다.
   - 왜 자동 연결을 금지하는지, 충돌 시 어떻게 처리하는지가 명시적이지 않아 면접에서 설명력이 약했습니다.

## 핵심 결정

1. **같은 이메일이라고 자동 연결하지 않는다.**
   - 이메일 일치만으로 기존 계정과 소셜 계정을 자동 병합하면 계정 탈취/오연결 리스크를 설명하기 어렵습니다.
   - 기존 계정은 기존 로그인 방식으로 직접 인증한 뒤 별도 linking 절차를 밟는 정책이 더 안전합니다.

2. **OAuth2 충돌은 일반 실패와 구분한다.**
   - `social_account_conflict`를 별도 실패 사유로 분리해 로그인 화면에서 안내합니다.

3. **실패 시에도 임시 OAuth 세션을 정리한다.**
   - OAuth2 인증은 이미 성공했지만 로컬 계정 매핑이 실패한 경우, 임시 세션/`SecurityContext`가 남으면 흐름이 불안정해집니다.
   - 성공뿐 아니라 충돌/예외 시에도 세션을 정리해 JWT cookie 기반 앱 인증 상태와 분리했습니다.

## 구현 요약

### 1) 성공 핸들러 정책 분리

- `OAuth2AuthenticationSuccessHandler`
  - `SocialAccountConflictException` 추가
  - 이메일 충돌 시 일반 예외가 아니라 명시적 충돌 예외를 발생
  - 충돌 시 `/login?error=social_account_conflict`로 리다이렉트
  - 충돌/일반 실패 모두 `SecurityContext`와 임시 세션을 정리

### 2) 로그인 화면 메시지 정리

- `AuthViewController`
  - `error` 쿼리 파라미터를 읽어 로그인 오류 메시지를 모델로 매핑
- `login.html`
  - 일반 소셜 실패와 계정 충돌을 구분해 제목/메시지/다음 행동을 안내

### 3) 테스트 추가

- `OAuth2AuthenticationSuccessHandlerTest`
  - 기존 이메일 충돌 시 `social_account_conflict`로 리다이렉트되는지 검증
  - JWT 발급 경로(`authService.loginBySocial`)가 호출되지 않는지 검증
  - 실패 후 세션과 `SecurityContext`가 정리되는지 검증
- `ViewEndpointTest`
  - `/login?error=social_account_conflict` 요청 시 충돌 안내 문구가 렌더링되는지 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"
git diff --check
```

## 인터뷰 포인트

- "OAuth2 이메일이 같으면 왜 자동 연결하지 않았나요?"
  - 이메일 일치만으로는 기존 계정 소유권을 완전히 증명했다고 보기 어려워서 자동 linking을 막았습니다.
  - 대신 기존 로그인 방식으로 먼저 인증하게 하고, 연결 기능이 필요하면 별도 보안 절차로 분리하는 게 더 안전합니다.

- "왜 실패 시 세션까지 정리했나요?"
  - OAuth2 핸드셰이크는 세션 기반이지만 애플리케이션 인증은 JWT cookie 기반입니다.
  - 로컬 회원 매핑이 실패한 상태에서 OAuth 세션이 남으면 인증 상태가 어정쩡해지므로, 실패 시에도 정리해 상태를 단순화했습니다.

- "이번 변경의 포트폴리오 의미는 무엇인가요?"
  - 단순히 소셜 로그인을 붙인 수준이 아니라, 계정 충돌 정책과 실패 상태 정리를 명시적으로 설계했다는 점을 보여줍니다.
  - 인증 기능을 기능 구현이 아니라 보안 정책/상태 전이 관점에서 다뤘다는 신호가 됩니다.
