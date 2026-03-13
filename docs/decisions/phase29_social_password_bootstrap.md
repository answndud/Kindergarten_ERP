# Phase 29: 소셜 전용 계정의 로컬 비밀번호 설정 추가

## 배경

이전 단계에서 settings 기반 명시적 소셜 계정 연결은 추가했지만,
소셜 로그인으로만 가입한 사용자는 여전히 로컬 이메일/비밀번호 로그인을 사용할 수 없었습니다.

settings 화면도 "소셜 전용 계정이라 비밀번호 변경 불가"까지만 안내하고 끝났기 때문에,
정상적인 다음 행동이 없는 상태였습니다.

## 핵심 결정

1. **비밀번호 변경과 초기 설정을 분리한다.**
   - 기존 `changePassword`는 현재 비밀번호를 알고 있는 로컬 계정용입니다.
   - 소셜 전용 계정은 현재 비밀번호가 없으므로, 별도의 password bootstrap endpoint를 둡니다.

2. **소셜 연결 정보와 로컬 로그인 가능 여부를 분리한다.**
   - `auth_provider/provider_id`는 연결된 소셜 로그인 정보로 유지합니다.
   - 로컬 로그인 가능 여부는 `password` 존재 여부로 판단합니다.

3. **settings 화면에서 상태에 맞는 폼만 보여준다.**
   - 로컬 비밀번호가 있으면 "비밀번호 변경"
   - 없으면 "로컬 비밀번호 설정"

## 구현 요약

### 1) 서비스/API

- `MemberService`
  - `setInitialPassword(memberId, newPassword)` 추가
  - 이미 로컬 비밀번호가 있으면 `PASSWORD_ALREADY_SET(M005)` 반환

- `MemberApiController`
  - `POST /api/v1/members/password/bootstrap` 추가
  - 현재 인증된 회원의 초기 로컬 비밀번호 설정 처리

### 2) settings 화면

- `settings.html`
  - 소셜 전용 계정에는 bootstrap form 노출
  - 기존 로컬 계정에는 현재 비밀번호 기반 변경 form 유지
  - bootstrap 성공 후 새로고침 시 즉시 "비밀번호 변경" 상태로 전환

### 3) 테스트

- `MemberApiIntegrationTest`
  - 소셜 전용 계정 bootstrap 성공 검증
  - bootstrap 후 `passwordEncoder.matches`와 실제 로그인 성공 검증
  - 이미 비밀번호가 있는 계정의 bootstrap 차단 검증

- `ViewEndpointTest`
  - 소셜 전용 계정 settings 진입 시 "로컬 비밀번호 설정" 폼이 렌더링되는지 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"
git diff --check
```

## 인터뷰 포인트

- "왜 기존 비밀번호 변경 API를 재사용하지 않았나요?"
  - 소셜 전용 계정에는 현재 비밀번호가 없기 때문에, 정책이 다른 두 흐름을 분리하는 편이 명확했습니다.

- "소셜 계정이 로컬 로그인도 가능해지면 auth_provider 의미가 깨지지 않나요?"
  - `auth_provider/provider_id`는 연결된 소셜 로그인 정보이고,
    로컬 로그인 가능 여부는 `password` 보유 여부로 별도 판단하도록 의미를 분리했습니다.

- "이번 변경의 포트폴리오 포인트는 무엇인가요?"
  - 단순 UI 추가가 아니라, 인증 수단이 늘어날 때 상태 모델을 어떻게 나누는지 보여주는 사례입니다.
  - 사용자 흐름 관점에서 막힌 상태를 끝까지 연결했다는 점도 설명 포인트가 됩니다.
