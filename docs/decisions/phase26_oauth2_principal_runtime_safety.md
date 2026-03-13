# Phase 26: OAuth2 Principal 런타임 안전성 보강

## 배경

- 저장소 전반은 `@AuthenticationPrincipal CustomUserDetails`와 `CustomUserDetails` 직접 캐스팅을 전제로 작성되어 있다.
- 그런데 OAuth2/social 로그인 직후에는 Spring Security 세션에 `OAuth2AuthenticationToken`과 `OAuth2User` principal이 남을 수 있다.
- 이 상태에서 view 요청이 들어오면 특히 `RoleRedirectInterceptor`의 고정 캐스팅 지점에서 `ClassCastException`이 날 가능성이 있었다.

## 목표

1. 인증 principal 타입이 `CustomUserDetails`가 아니어도 view 공통 경로가 500 없이 동작하게 만든다.
2. social login 성공 후에는 임시 세션 인증을 정리해 JWT cookie 기반 흐름으로 수렴시킨다.
3. 면접에서 "OAuth2 핸드셰이크는 세션, 이후 앱 인증은 JWT"라는 설계를 설명 가능하게 만든다.

## 결정

1. `Authentication -> Member` 해석을 공통 `AuthenticatedMemberResolver`로 모은다.
2. resolver는 다음 순서로 `Member`를 찾는다.
   - `CustomUserDetails`
   - `OAuth2AuthenticationToken` + provider/providerId
   - `UserDetails`/email fallback
3. social login 성공 후에는 JWT cookie를 발급한 다음 세션과 SecurityContext를 정리한다.

## 구현 요약

### 1) 공통 resolver 추가

- `AuthenticatedMemberResolver`
  - `CustomUserDetails`, `OAuth2AuthenticationToken`, `UserDetails`, 문자열 username을 처리
  - 가능한 경우 `findByIdWithKindergarten`로 다시 로드해 view 계층 lazy 문제를 줄임

### 2) 공통 계층 반영

- `RoleRedirectInterceptor`
  - `CustomUserDetails` 고정 캐스팅 제거
  - resolver로 `Member`를 찾고, 해석 실패 시 로그인 페이지로 fallback
- `GlobalControllerAdvice`
  - `currentMember` 모델 주입도 resolver 기준으로 통일
- `AuthViewController`
  - `/profile`, `/settings` 접근 체크를 resolver 기반으로 변경

### 3) OAuth2 후속 흐름 정리

- `OAuth2AuthenticationSuccessHandler`
  - JWT cookie 발급 후 `SecurityContext` clear
  - 임시 세션 invalidate

이렇게 해서 OAuth2는 인증 핸드셰이크에만 세션을 쓰고, 실제 앱 탐색은 JWT cookie 인증으로 이어지게 정리했다.

## 테스트

- `ViewEndpointTest`
  - Google OAuth2 principal을 직접 넣은 상태로 `/profile` 접근 시 200 응답 검증

## 검증 결과

- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.integration.ViewEndpointTest"`

둘 다 통과했다.

## 인터뷰 답변 포인트

### 왜 이 문제가 중요했는가

- 인증 기능은 "로그인 된다"에서 끝나지 않고, 로그인 직후와 subsequent request의 principal 타입까지 일관돼야 한다.
- 특히 소셜 로그인은 핸드셰이크용 세션 principal과 앱 내부 principal이 다를 수 있어서, 이 경계를 정리하지 않으면 런타임 예외가 숨는다.

### 왜 resolver를 따로 뒀는가

- principal 타입 분기 로직을 인터셉터나 컨트롤러마다 흩뿌리면 유지보수가 나빠진다.
- 공통 resolver로 모아 두면 이후 인증 수단이 늘어도 한 곳에서 정책을 확장할 수 있다.

### 왜 세션을 바로 정리했는가

- 이 프로젝트의 실제 앱 인증 모델은 JWT cookie 기반이다.
- OAuth2 로그인 성공 후에도 세션 principal을 계속 들고 가면 인증 경로가 두 갈래가 되고, 그만큼 예외 케이스가 늘어난다.
