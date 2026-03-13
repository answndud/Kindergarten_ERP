# Phase 32: 소셜 provider 식별자 불변 정책 도입

## 배경

Phase 31에서 `member_social_account`로 정규화하면서
Google과 Kakao를 동시에 연결할 수 있게 만들었지만,
여전히 하나의 빈틈이 남아 있었습니다.

- 사용자가 Google을 unlink
- 이후 다른 Google 계정으로 다시 link

이 흐름이 가능하면, 같은 provider의 로그인 식별자가 사실상 교체 가능합니다.

이 프로젝트에서는 이메일을 사용자가 임의로 바꾸지 못하게 두고 있습니다.
소셜 로그인도 같은 축으로 봐야 합니다.

- 비밀번호는 바꿀 수 있음
- 로그인 식별자는 임의로 교체할 수 없음

## 핵심 결정

1. **같은 provider의 `providerId`는 계정에 귀속된 불변 식별자다.**
   - Google은 Google끼리, Kakao는 Kakao끼리 다른 계정으로 교체할 수 없습니다.

2. **unlink는 삭제가 아니라 비활성화다.**
   - row를 삭제하면 과거에 어떤 provider가 어떤 식별자로 연결됐는지 잃어버립니다.
   - 따라서 `member_social_account.unlinked_at`으로 이력을 남깁니다.

3. **같은 provider는 같은 `providerId`로만 재연결할 수 있다.**
   - 예전과 같은 Google 계정이면 relink 허용
   - 다른 Google 계정이면 차단

4. **인증 조회는 active social account만 사용한다.**
   - unlink된 소셜 계정으로는 더 이상 로그인할 수 없습니다.

## 구현 요약

### 1) DB / Entity

- `V9__preserve_social_account_history.sql`
  - `member_social_account.unlinked_at` 추가

- `MemberSocialAccount`
  - `isActive`, `unlink`, `relink`
  - historical link와 active link를 구분

### 2) 도메인 / 서비스

- `Member`
  - `hasSocialAccountHistory`
  - `hasProviderBindingWithDifferentIdentity`
  - `linkSocialAccount`는 기존 row가 있으면 재활성화
  - `unlinkSocialAccount`는 삭제 대신 `unlinked_at` 설정
  - summary / linked 상태 / legacy sync는 active link만 기준으로 계산

- `SocialAccountLinkService`
  - 같은 provider에 과거 이력이 있는데 `providerId`가 다르면
    `SOCIAL_PROVIDER_REPLACEMENT_NOT_ALLOWED(A011)`로 차단

- `MemberRepository`
  - active social lookup과 any-history lookup을 분리

### 3) UX / OAuth2 callback

- `settings.html`
  - 과거 연결 이력이 있지만 현재는 해제된 provider를 "재연결" 상태로 표시
  - "처음 연결했던 동일한 계정만 다시 연결 가능" 안내 노출

- `OAuth2AuthenticationSuccessHandler`
  - callback에서 다른 같은 provider 계정으로 바꾸려 하면
    `/settings?socialLinkStatus=error&reason=provider-replacement-not-allowed`
    로 복귀

## 테스트

- `SocialAccountLinkServiceTest`
  - 같은 provider + 같은 `providerId` 재연결 허용
  - 같은 provider + 다른 `providerId` 교체 차단

- `MemberApiIntegrationTest`
  - unlink 후 active link는 없어지지만 historical binding은 남는지 검증

- `ViewEndpointTest`
  - settings 화면의 재연결 문구/교체 금지 안내 검증

- `OAuth2AuthenticationSuccessHandlerTest`
  - 교체 금지 에러가 settings reason으로 매핑되는지 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest" --tests "com.erp.domain.auth.service.SocialAccountLinkServiceTest"
git diff --check
```

## 인터뷰 포인트

- "왜 교체를 허용하지 않았나요?"
  - 소셜 `providerId`를 비밀번호가 아니라 이메일과 같은 로그인 식별자로 봤기 때문입니다.

- "왜 unlink 때 row를 삭제하지 않았나요?"
  - 삭제하면 과거 바인딩을 잃어버려 같은 provider 교체 금지 정책을 강제할 수 없습니다.

- "왜 active query와 history query를 나눴나요?"
  - 로그인은 현재 활성 연결만 봐야 하지만,
  - 보안 정책은 과거에 어떤 계정이 연결됐었는지도 알아야 하기 때문입니다.
