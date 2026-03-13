# PLAN.md

## 작업명
- 후속 고도화 7차 (OAuth2 Principal 런타임 안전성 보강)

## 1) 목표 / 범위
- OAuth2/social 로그인 이후 view 계층에서 principal 타입 가정으로 인한 런타임 예외 가능성을 제거한다.
- `CustomUserDetails` 고정 캐스팅을 줄이고, 인증 principal에서 `Member`를 공통 해석하는 경로를 만든다.
- 소셜 로그인 성공 후에는 임시 세션 인증을 정리해 JWT cookie 기반 흐름으로 수렴시킨다.

## 2) 세부 작업 단계
1. principal 타입 의존 지점 점검
   - `RoleRedirectInterceptor`, `GlobalControllerAdvice`, OAuth2 success handler를 확인한다.
   - 어떤 요청에서 `CustomUserDetails`가 아닐 수 있는지 정리한다.

2. 공통 member resolver 구현
   - `Authentication`에서 `Member`를 찾아내는 resolver를 추가한다.
   - `CustomUserDetails`, `UserDetails`, `OAuth2AuthenticationToken`을 모두 다룬다.

3. OAuth2 후속 흐름 정리
   - social login 성공 후 임시 세션 인증을 정리한다.
   - 인터셉터와 공통 뷰 모델 주입이 resolver를 쓰도록 변경한다.

4. 테스트/문서화 및 검증
   - OAuth2 principal 상태의 뷰 요청이 500 없이 처리되는 통합 테스트를 추가한다.
   - `README.md`, `docs/phase/`에 결정 배경과 면접 포인트를 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.integration.ViewEndpointTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.integration.ViewEndpointTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- OAuth2 principal 해석을 잘못하면 다른 사용자를 잘못 매핑할 수 있음
  - 대응: provider/providerId를 우선 사용하고, email fallback은 최소 범위에서만 사용한다
- 세션 정리 시 social login 직후 redirect 흐름이 깨질 수 있음
  - 대응: JWT cookie 발급 후에만 세션을 정리하고, 뷰 통합 테스트로 확인한다
- resolver가 예외를 던지면 뷰 전역 렌더링이 불안정해질 수 있음
  - 대응: resolver는 `Optional<Member>` 반환으로 만들고, 공통 계층은 실패 시 로그인 또는 null fallback으로 처리한다
