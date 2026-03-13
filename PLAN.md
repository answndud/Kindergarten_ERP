# PLAN.md

## 작업명
- 후속 고도화 12차 (소셜 계정 다중 연결 구조 정규화)

## 1) 목표 / 범위
- 소셜 로그인 정보를 `member.auth_provider/provider_id` 단일 슬롯에서 `member_social_account` 정규화 테이블로 분리한다.
- Google과 Kakao를 동시에 연결할 수 있도록 로그인, 연결, 해제, settings 화면을 새 구조 기준으로 정렬한다.
- 기존 `member.auth_provider/provider_id`는 호환성 필드로 남기되, 새 테이블의 primary provider를 동기화하는 방식으로 운영한다.

## 2) 세부 작업 단계
1. DB/도메인 정규화
   - `member_social_account` 마이그레이션을 추가하고 기존 소셜 회원 데이터를 backfill한다.
   - `Member`에 social account 컬렉션을 추가하고, link/unlink/summary/legacy sync 로직을 정리한다.

2. 인증/연결 플로우 전환
   - OAuth2 login, explicit link, authenticated member resolve를 새 social account 조회 기준으로 바꾼다.
   - provider별 독립 연결/해제 정책을 적용하고, 마지막 로그인 수단을 잃지 않도록 unlink guard를 유지한다.

3. settings 화면/UX 재정렬
   - 단일 linked provider 표시를 제거하고 Google/Kakao 카드별 연결 상태/해제 버튼/차단 사유를 노출한다.
   - 연결 요약은 복수 provider를 읽기 좋게 보여주고, 성공/실패 피드백 문구도 새 정책에 맞춘다.

4. 테스트/문서화 및 검증
   - 회원 API/뷰/OAuth2 handler 테스트에 다중 provider 연결 및 unlink 회귀 케이스를 추가한다.
   - `README.md`, `docs/phase/`에 정규화 배경, backfill 전략, 호환성 필드 유지 이유를 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 기존 `auth_provider/provider_id`를 참조하는 코드가 남아 있을 수 있음
  - 대응: 새 테이블을 SSOT로 만들고, 인증/연결 핵심 경로를 우선 전환한 뒤 legacy 필드는 동기화 전용으로만 유지한다
- 다중 provider unlink 시 마지막 로그인 수단이 사라질 수 있음
  - 대응: 로컬 비밀번호가 있거나 다른 social provider가 남는 경우에만 unlink를 허용한다
- settings UI가 provider별 상태 분기로 복잡해질 수 있음
  - 대응: 컨트롤러에서 provider별 boolean/message를 계산해 템플릿은 카드 단위 분기만 남긴다
