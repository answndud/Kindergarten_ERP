# Phase 13: 인증 보안 하드닝 (CSRF + 쿠키 보안)

## 배경

- 기존 구현은 쿠키 기반 JWT 인증을 사용하면서 CSRF를 비활성화하고, JWT 쿠키 `Secure` 값을 코드에 하드코딩하고 있었다.
- 외부 보안 리뷰에서 쿠키 기반 인증의 CSRF 리스크와 환경별 쿠키 보안 설정 미분리를 지적받았다.

## 결정

1. CSRF를 활성화하고 `CookieCsrfTokenRepository`를 사용한다.
2. 프런트 요청(HTMX/fetch)에서 `X-XSRF-TOKEN` 헤더를 자동 전송한다.
3. JWT 쿠키의 `Secure`, `SameSite` 값을 설정 파일 기반으로 관리한다.

## 구현 요약

- `SecurityConfig`
  - `.csrf(csrf -> csrf.disable())` 제거
  - `CookieCsrfTokenRepository.withHttpOnlyFalse()` 적용
  - 초기 페이지 진입 시 CSRF 쿠키 발급을 위한 `CsrfCookieFilter` 추가
- `app.js`
  - HTMX 요청에 CSRF 헤더 자동 추가
  - 전역 `fetch` 래핑으로 same-origin + unsafe method 요청에 CSRF 헤더 자동 추가
- `AuthService`
  - JWT 쿠키 `secure=false` 하드코딩 제거
  - `jwt.cookie-secure`, `jwt.cookie-same-site` 설정값 사용
- 환경 설정
  - `application.yml`: `jwt.cookie-secure`, `jwt.cookie-same-site` 기본값 정의
  - `application-local.yml`: `jwt.cookie-secure=false`
  - `application-prod.yml`: `jwt.cookie-secure=true`

## 트레이드오프

- 장점: 쿠키 기반 인증의 주요 취약점(CSRF) 대응, 운영/로컬 환경별 보안 정책 분리
- 단점: 모든 상태 변경 요청에 CSRF 토큰이 필요하여 클라이언트 구현 복잡도 증가

## 검증 포인트

1. CSRF 토큰 없이 `POST /api/v1/auth/login` 요청 시 403
2. CSRF 토큰 포함 시 기존 로그인/갱신 API 정상 동작
3. 운영 프로파일에서 JWT 쿠키가 `Secure=true`로 설정되는지 확인
