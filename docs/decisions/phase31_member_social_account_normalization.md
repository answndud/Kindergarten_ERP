# Phase 31: 소셜 계정 다중 연결 구조 정규화

## 배경

Phase 28~30까지의 소셜 로그인 개선으로
- 명시적 연결
- 로컬 비밀번호 bootstrap
- 안전한 unlink

까지는 구현됐지만, 저장 구조는 여전히 `member.auth_provider/provider_id` 단일 슬롯에 묶여 있었습니다.

이 구조는 다음 한계를 갖습니다.

1. Google과 Kakao를 동시에 연결할 수 없습니다.
2. settings 화면도 "연결된 provider 1개" 전제를 강하게 가집니다.
3. OAuth2 로그인과 계정 연결 로직이 회원 테이블 한 칼럼 쌍에 과도하게 결합됩니다.

포트폴리오 관점에서도 "소셜 로그인 붙였다"보다
"왜 별도 테이블로 정규화했는지"를 설명할 수 있어야 설계 깊이가 생깁니다.

## 핵심 결정

1. **`member_social_account`를 소셜 계정 정보의 SSOT로 만든다.**
   - provider, providerId는 이제 별도 테이블에서 관리합니다.
   - member 1명에 여러 social account를 연결할 수 있습니다.

2. **`member.auth_provider/provider_id`는 즉시 제거하지 않고 호환성 필드로 유지한다.**
   - 기존 스키마와 테스트, 일부 조회/표현 로직이 여전히 이 필드를 기대합니다.
   - 따라서 새 테이블을 기준으로 first-linked provider를 legacy 필드에 동기화합니다.

3. **provider별 연결은 1개만 허용하고, provider 종류는 여러 개 허용한다.**
   - 한 계정에 Google 2개를 연결하는 것은 금지합니다.
   - 대신 Google 1개 + Kakao 1개는 허용합니다.

4. **unlink 정책은 "마지막 로그인 수단 보호"를 유지한다.**
   - 로컬 비밀번호가 있으면 unlink 가능
   - 또는 다른 social provider가 남아 있어도 unlink 가능
   - 둘 다 없으면 unlink 차단

## 구현 요약

### 1) DB / Entity

- `V8__normalize_member_social_accounts.sql`
  - `member_social_account` 테이블 추가
  - `(provider, provider_id)` unique
  - `(member_id, provider)` unique
  - 기존 `member.auth_provider/provider_id` 데이터를 backfill

- `MemberSocialAccount`
  - provider / providerId / member 연관관계 엔티티 추가

- `Member`
  - `socialAccounts` 컬렉션 추가
  - `linkSocialAccount`, `unlinkSocialAccount`, `canUnlinkSocialAccount`
  - `getLinkedSocialProviderSummary`
  - legacy 필드 sync 메서드 추가

### 2) 인증 / 연결 플로우

- `MemberRepository`
  - `findBySocialProviderAndProviderId`
  - `findByIdWithSocialAccounts`
  - `findByIdWithKindergartenAndSocialAccounts`

- `AuthenticatedMemberResolver`
  - OAuth2 principal 해석 시 새 social account 조회 기준 사용

- `OAuth2AuthenticationSuccessHandler`
  - social login 회원 매칭을 `member_social_account` 기준으로 전환
  - explicit link 성공/실패 reason도 새 정책에 맞춰 정리

- `SocialAccountLinkService`
  - provider별 uniqueness 검증
  - 다중 provider 연결 허용
  - 마지막 로그인 수단 보호 unlink 정책 유지

### 3) settings 화면

- 단일 linked provider 뱃지를 복수 provider summary로 변경
- Google / Kakao 카드별로
  - 연결 상태
  - 연결 버튼
  - unlink 가능 여부
  - 차단 사유

를 독립적으로 표시하도록 재구성

## 테스트

- `MemberApiIntegrationTest`
  - 단일 provider unlink 성공
  - 마지막 로그인 수단인 social account unlink 차단
  - 다른 social provider가 남아 있는 경우 unlink 허용

- `ViewEndpointTest`
  - 소셜 전용 단일 provider 계정의 차단 메시지
  - 로컬 비밀번호가 있는 계정의 unlink 버튼 노출
  - Google + Kakao 동시 연결 시 settings 화면 렌더링

- `OAuth2AuthenticationSuccessHandlerTest`
  - OAuth2 로그인 회원 조회를 새 social account 기준으로 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"
git diff --check
```

## 인터뷰 포인트

- "왜 `member` 테이블에 provider 칼럼만 두지 않았나요?"
  - 한 회원이 여러 소셜 제공자를 연결하는 순간 1:N 구조가 되므로, 단일 칼럼 쌍은 모델링 한계가 분명합니다.

- "왜 legacy 필드를 바로 지우지 않았나요?"
  - 운영/리팩터링에서는 한 번에 다 뒤집기보다, 새 구조를 SSOT로 세우고 호환 계층을 두는 편이 안전합니다.

- "unlink 정책을 왜 바꾸지 않고 유지했나요?"
  - 구조가 복수 provider로 확장돼도, 마지막 로그인 수단을 제거하지 않게 막는 보안 정책은 그대로 중요합니다.

- "이 작업의 포트폴리오적 의미는 무엇인가요?"
  - 단순 기능 추가가 아니라, 데이터 모델 정규화, 마이그레이션/backfill, 하위 호환, 인증 정책 재정렬까지 한 번에 설명할 수 있는 설계 사례입니다.
