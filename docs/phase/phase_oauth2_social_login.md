# Phase: OAuth2 소셜 로그인/가입 도입

## 개요
- 목표: 로그인/회원가입 페이지에서 Google, Kakao OAuth2 인증을 실제로 동작시킨다.
- 범위: Security 설정, OAuth2 사용자 매핑, 회원 생성 정책, UI 버튼 연결, DB 스키마 확장.

## 핵심 결정
1. 신규 소셜 가입 기본 역할은 `PARENT`로 고정한다.
2. 신규 소셜 가입 기본 상태는 기존 정책을 따라 `PENDING`으로 둔다.
3. OAuth2 핸드셰이크를 위해 Security 세션 정책은 `IF_REQUIRED`를 사용하고,
   앱 인증 상태는 기존 JWT(HTTP-only cookie) 기반 흐름을 유지한다.
4. 소셜 회원 식별은 `auth_provider + provider_id`를 기준으로 처리한다.

## 구현 요약
- DB: `member`에 `auth_provider`, `provider_id` 컬럼 추가, `password` nullable 전환.
- Backend:
  - `oauth2Login()` 활성화
  - `CustomOAuth2UserService`로 provider 응답 정규화/검증
  - `OAuth2AuthenticationSuccessHandler`에서 회원 조회/신규 생성/JWT 발급/리다이렉트 처리
- Frontend:
  - `/login`, `/signup`에서 Google/Kakao 버튼을 `/oauth2/authorization/{provider}`로 연결

## 운영 체크리스트
- Google Console Redirect URI 등록
  - `https://<domain>/login/oauth2/code/google`
- Kakao Developers Redirect URI 등록
  - `https://<domain>/login/oauth2/code/kakao`
- 환경변수 설정
  - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
  - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
