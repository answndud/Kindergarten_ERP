# PLAN.md

## 작업명
- 후속 고도화 13차 (소셜 provider 식별자 불변 정책 도입)

## 1) 목표 / 범위
- 소셜 provider를 "교체 가능한 로그인 수단"이 아니라 계정에 귀속된 불변 식별자로 정의한다.
- `unlink -> 다른 같은 provider 재연결`이 가능하던 빈틈을 막기 위해 social account를 삭제가 아닌 비활성화로 전환한다.
- 동일 provider는 처음 연결했던 동일한 `providerId`로만 재연결할 수 있게 하고, 다른 계정으로의 교체는 금지한다.

## 2) 세부 작업 단계
1. social account lifecycle 모델 보강
   - `member_social_account`에 unlink 이력을 보존할 수 있는 상태 필드를 추가한다.
   - `MemberSocialAccount`와 `Member`에 active/historical link를 구분하는 메서드를 추가한다.

2. 같은 provider 교체 금지 정책 구현
   - 동일 provider에 과거 연결 이력이 있으면, 다른 `providerId`로의 link를 서비스 계층에서 차단한다.
   - 동일한 `providerId`에 대한 재연결만 허용하고, OAuth2 login/resolver는 active link만 인증에 사용한다.

3. settings 화면/UX 정리
   - 과거 연결 이력이 있지만 현재는 해제된 provider에 대해 "재연결" 상태와 교체 금지 메시지를 노출한다.
   - OAuth callback 실패 reason도 "이미 같은 provider는 다른 계정으로 교체할 수 없음"으로 구체화한다.

4. 테스트/문서화 및 검증
   - OAuth2 handler/view/API 테스트에 same-provider replacement 차단과 same-provider relink 허용 케이스를 추가한다.
   - `README.md`, `docs/phase/`에 이메일 불변 정책과 같은 축의 소셜 식별자 불변 정책을 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest" --tests "com.erp.domain.auth.service.SocialAccountLinkServiceTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest" --tests "com.erp.domain.auth.service.SocialAccountLinkServiceTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- unlink 후 row를 보존하면 active link와 historical link를 혼동할 수 있음
  - 대응: `isActive`, `unlink`, `relink` 메서드를 엔티티에 두고, 인증 조회는 active link만 보도록 쿼리를 분리한다
- 같은 provider 교체 금지 정책이 UI에 명확히 보이지 않으면 사용자 입장에서 모호할 수 있음
  - 대응: settings 화면과 OAuth callback error message에 "처음 연결한 동일한 계정만 재연결 가능" 문구를 노출한다
- provider별 불변 정책이 DB unique 제약과 충돌할 수 있음
  - 대응: insert 대신 historical row 재활성화 방식으로 relink를 처리하고, 기존 unique 제약은 유지한다
