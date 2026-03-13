# PLAN.md

## 작업명
- 후속 고도화 10차 (소셜 전용 계정의 로컬 비밀번호 설정 추가)

## 1) 목표 / 범위
- 소셜 전용 계정이 settings 화면에서 초기 로컬 비밀번호를 설정할 수 있게 한다.
- 기존 비밀번호 변경 API와 신규 비밀번호 bootstrap API를 분리해 정책을 명확히 한다.
- settings 화면에서 로컬 비밀번호 보유 여부에 따라 "변경"과 "설정"을 구분해 노출한다.

## 2) 세부 작업 단계
1. 비밀번호 bootstrap 정책 구현
   - 소셜 전용 계정만 사용할 수 있는 초기 비밀번호 설정 메서드를 서비스 계층에 추가한다.
   - 이미 로컬 비밀번호가 있는 계정은 bootstrap API를 호출하지 못하게 막는다.

2. 회원 API 확장
   - `/api/v1/members/password/bootstrap` endpoint를 추가한다.
   - 기존 `/api/v1/members/password`와 역할을 분리해 면접에서 정책 설명이 가능하도록 정리한다.

3. settings 화면 개선
   - 로컬 비밀번호가 없는 계정에는 "로컬 비밀번호 설정" 폼을 노출한다.
   - 기존 로컬 계정에는 현재 비밀번호 기반 변경 폼만 노출한다.

4. 테스트/문서화 및 검증
   - 회원 API 통합 테스트로 소셜 전용 계정의 비밀번호 bootstrap 성공/실패를 검증한다.
   - 뷰 통합 테스트로 settings 화면이 bootstrap 상태를 제대로 렌더링하는지 검증한다.
   - `README.md`, `docs/phase/`에 설계 이유와 사용자 흐름을 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 소셜 전용 계정에 비밀번호를 추가하면 `auth_provider` 의미가 모호해질 수 있음
  - 대응: `auth_provider/provider_id`는 연결된 소셜 로그인 정보로 유지하고, 로컬 로그인 가능 여부는 `password` 존재 여부로 분리해 문서화한다
- bootstrap API가 기존 비밀번호 변경 API와 혼동될 수 있음
  - 대응: endpoint, 요청 DTO, settings UI 문구를 분리해 "설정"과 "변경"을 명시적으로 구분한다
- 초기 비밀번호 설정 후 로그인 경험을 검증하지 않으면 반쪽 구현이 될 수 있음
  - 대응: API 성공 후 `passwordEncoder.matches`와 화면 분기 테스트로 로컬 로그인 가능 상태 전환을 확인한다
