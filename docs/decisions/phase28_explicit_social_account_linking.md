# Phase 28: 명시적 소셜 계정 연결 플로우 추가

## 배경

이전 단계에서 OAuth2 이메일 충돌 시 자동 연결을 막고 명시적인 실패 안내를 추가했습니다.
하지만 그 상태만으로는 "그럼 기존 로컬 계정에 Google/Kakao를 어떻게 붙이느냐"에 대한 정상 경로가 없었습니다.

즉 정책은 안전해졌지만, 사용자 입장에서는 연결 가능한 기능이 비어 있었고,
설정 화면도 소셜 전용 계정과 로컬 비밀번호 계정을 구분해서 보여주지 못했습니다.

## 핵심 결정

1. **소셜 연결은 설정 화면에서 명시적으로 시작한다.**
   - 기존 계정으로 로그인한 사용자가 `/settings`에서 직접 Google/Kakao 연결 버튼을 눌러야 합니다.
   - 이메일 일치만으로 자동 병합하지 않고, 로컬 인증 + provider 인증 두 단계를 모두 거친 경우에만 연결합니다.

2. **OAuth2 callback은 login/signup과 link를 구분한다.**
   - 세션에 `link intent`를 저장해 두고, callback에서 intent가 있으면 현재 회원의 provider 연결로 처리합니다.
   - intent가 없을 때만 기존 social login/signup 흐름으로 동작합니다.

3. **현재 스키마 제약에 맞춰 provider 1개만 허용한다.**
   - `member.auth_provider + provider_id` 구조는 한 회원당 provider 슬롯이 1개입니다.
   - 이번 배치에서는 교체/다중 provider 연결을 억지로 넣지 않고, settings와 문서에 제약을 명시했습니다.

## 구현 요약

### 1) link intent 저장

- `SocialAccountLinkController`
  - `/auth/social/link/{provider}` 추가
  - 현재 인증 회원을 확인한 뒤 세션에 `memberId`, `provider`를 저장
  - OAuth2 authorization endpoint(`/oauth2/authorization/{provider}`)로 리다이렉트

- `OAuth2LinkSessionService`
  - link intent 저장/조회 전용 컴포넌트
  - OAuth2 handshake 동안만 짧게 사용하는 세션 기반 상태를 한곳에 모음

### 2) callback 분기

- `OAuth2AuthenticationSuccessHandler`
  - link intent가 있으면 `SocialAccountLinkService`로 현재 회원에 provider 연결
  - 연결 성공 시 `/settings?socialLinkStatus=success&provider=...`로 복귀
  - 다른 계정에 이미 연결된 provider거나, 현재 계정에 다른 provider가 이미 연결돼 있으면 settings 오류로 복귀
  - 성공/실패 모두 임시 OAuth 세션 정리

- `SocialAccountLinkService`
  - provider가 이미 다른 회원에 연결돼 있는지 확인
  - 현재 회원이 이미 다른 소셜 provider를 사용 중이면 차단
  - 단일 슬롯 정책을 서비스 계층에서 강제

### 3) settings 화면 정합화

- `AuthViewController`
  - 소셜 연결 성공/실패 메시지 매핑
  - linked provider, 비밀번호 변경 가능 여부, Google/Kakao 연결 상태 모델 전달

- `settings.html`
  - 소셜 연결 카드 추가
  - 현재 연결된 provider 배지 표시
  - 소셜 전용 계정은 비밀번호 변경 폼 대신 안내 문구 표시

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"
git diff --check
```

## 인터뷰 포인트

- "자동 연결을 막았는데 연결 기능은 어떻게 제공했나요?"
  - settings에서 사용자가 먼저 로컬 계정으로 인증된 상태로 link를 시작하게 했고,
    이후 provider 인증까지 완료된 경우에만 명시적으로 연결합니다.

- "왜 세션을 썼나요?"
  - 앱 인증은 JWT cookie 기반이지만, OAuth2 authorization request 자체는 redirect 기반 핸드셰이크입니다.
  - link intent는 handshake 동안만 필요한 짧은 상태라 세션이 가장 단순하고 안전했습니다.

- "왜 다중 provider 연결을 바로 안 넣었나요?"
  - 현재 스키마가 단일 provider 슬롯이어서 억지로 확장하면 엔티티/조회/정책이 모두 흔들립니다.
  - 포트폴리오에서는 제약을 숨기기보다, 현재 구조에서 안전하게 가능한 범위만 구현하고 제약을 명시하는 쪽이 더 낫다고 판단했습니다.
