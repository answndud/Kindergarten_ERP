# Phase 30: 소셜 계정 연결 해제 정책 추가

## 배경

소셜 계정 명시적 연결과 로컬 비밀번호 설정까지 구현한 뒤에도,
settings 화면에서는 연결 해제가 불가능했습니다.

이 상태는 계정 lifecycle 관점에서 반쪽 구현입니다.
연결은 가능한데 해제가 안 되면 사용자 제어권이 부족하고,
반대로 해제를 무조건 열어 두면 로컬 비밀번호가 없는 계정은 바로 잠길 수 있습니다.

## 핵심 결정

1. **연결 해제는 로컬 비밀번호가 있을 때만 허용한다.**
   - 로컬 로그인 경로 없이 소셜 provider만 제거하면 계정 접근이 막힙니다.
   - 따라서 unlink는 `password`가 있는 계정에서만 허용합니다.

2. **현재 계정이 실제로 연결한 provider만 해제한다.**
   - URL path만 믿지 않고 서비스 계층에서 다시 검증합니다.

3. **UI는 차단 사유를 먼저 보여주고, 보안 정책은 API에서 강제한다.**
   - settings 화면은 이유를 설명하지만,
   - 실제 차단은 서비스 계층의 `SOCIAL_UNLINK_REQUIRES_LOCAL_PASSWORD(A010)`로 보장합니다.

## 구현 요약

### 1) 서비스/API

- `SocialAccountLinkService`
  - `unlinkSocialAccount(memberId, provider)` 추가
  - 현재 회원의 linked provider 검증
  - 로컬 비밀번호 존재 여부 검증
  - 성공 시 `auth_provider=LOCAL`, `provider_id=null`로 전환

- `MemberApiController`
  - `DELETE /api/v1/members/social-link/{provider}` 추가

### 2) settings 화면

- `AuthViewController`
  - linked provider 값, unlink 가능 여부, 차단 사유를 모델로 전달
  - unlink 성공/실패 사유를 alert 메시지로 매핑

- `settings.html`
  - linked provider가 있으면 "연결 해제" 버튼 또는 차단 사유를 노출
  - unlink 성공 후 settings success message 표시

### 3) 테스트

- `MemberApiIntegrationTest`
  - 로컬 비밀번호가 있는 계정의 unlink 성공 검증
  - 로컬 비밀번호가 없는 계정의 unlink 차단 검증

- `ViewEndpointTest`
  - 소셜 전용 계정은 unlink 차단 사유가 보이는지 검증
  - 로컬 비밀번호 + linked social 상태에서는 unlink 버튼이 보이는지 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"
git diff --check
```

## 인터뷰 포인트

- "왜 unlink를 바로 허용하지 않았나요?"
  - provider만 지우고 로컬 비밀번호가 없으면 계정 접근이 끊기기 때문에, 계정 잠금 방지가 더 중요한 정책이었습니다.

- "unlink 후 auth_provider를 왜 LOCAL로 바꿨나요?"
  - 현재 스키마에서는 소셜 연결이 없는 상태를 `LOCAL + provider_id null`로 표현하는 것이 가장 단순하고 일관적입니다.

- "이번 변경이 왜 포트폴리오적으로 의미 있나요?"
  - 연결 기능 자체보다, 인증 수단을 제거할 때 어떤 안전장치를 두는지 보여주는 사례입니다.
  - 기능 추가가 아니라 계정 lifecycle 완성도와 보안 정책을 같이 다뤘다는 점이 포인트입니다.
