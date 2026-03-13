# PLAN.md

## 작업명
- 후속 고도화 11차 (소셜 계정 연결 해제 정책 추가)

## 1) 목표 / 범위
- 연결된 소셜 계정을 settings 화면에서 해제할 수 있는 경로를 추가한다.
- 계정 잠금 방지를 위해 로컬 비밀번호가 없는 상태에서는 소셜 연결 해제를 막는다.
- settings 화면에서 연결 해제 가능 여부와 사유를 실제 계정 상태에 맞게 노출한다.

## 2) 세부 작업 단계
1. 연결 해제 정책 구현
   - 현재 회원이 연결한 provider만 해제할 수 있게 한다.
   - 로컬 비밀번호가 없는 계정은 소셜 연결 해제를 금지한다.

2. 회원 API 확장
   - `/api/v1/members/social-link/{provider}` 삭제 endpoint를 추가한다.
   - 성공 시 `auth_provider/provider_id`를 비우고, 이후 로컬 로그인만 남도록 정리한다.

3. settings 화면 개선
   - 연결된 provider가 있으면 "연결 해제" 버튼을 노출한다.
   - 로컬 비밀번호가 없는 경우에는 버튼 대신 차단 사유를 노출한다.

4. 테스트/문서화 및 검증
   - 회원 API 통합 테스트로 연결 해제 성공/차단 케이스를 검증한다.
   - 뷰 통합 테스트로 settings 화면이 연결 해제 가능 상태를 제대로 렌더링하는지 검증한다.
   - `README.md`, `docs/phase/`에 설계 이유와 잠금 방지 정책을 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 소셜 연결 해제 후 로컬 비밀번호가 없으면 계정 접근이 막힐 수 있음
  - 대응: 로컬 비밀번호가 없는 상태에서는 unlink 자체를 차단하고, settings에 사유를 명시한다
- provider path와 실제 연결된 provider가 다를 수 있음
  - 대응: 서비스 계층에서 현재 회원의 linked provider를 다시 검증한다
- settings UI에 상태가 많아지면 메시지가 난잡해질 수 있음
  - 대응: 연결 성공/실패/차단 이유를 컨트롤러에서 정규화해 템플릿에는 단순한 분기만 남긴다
