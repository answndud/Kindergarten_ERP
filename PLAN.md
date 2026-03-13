# PLAN.md

## 작업명
- 후속 고도화 9차 (명시적 소셜 계정 연결 플로우 추가)

## 1) 목표 / 범위
- 기존 계정으로 로그인한 사용자가 설정 화면에서 Google/Kakao를 명시적으로 연결할 수 있는 경로를 추가한다.
- OAuth2 callback이 일반 로그인/신규 가입과 계정 연결을 구분해 처리하도록 정리한다.
- 설정 화면에서 소셜 연결 상태와 비밀번호 변경 가능 여부를 실제 계정 상태에 맞게 노출한다.

## 2) 세부 작업 단계
1. 링크 의도 저장/시작점 구현
   - 인증 사용자가 `/auth/social/link/{provider}`로 진입하면 세션에 link intent를 저장한 뒤 OAuth2 authorization endpoint로 리다이렉트한다.
   - 단일 `auth_provider/provider_id` 구조에 맞춰 한 계정당 소셜 로그인 1개만 허용한다.

2. callback 분기 구현
   - `OAuth2AuthenticationSuccessHandler`가 link intent를 감지하면 현재 회원에 provider를 연결하고 `/settings`로 복귀시킨다.
   - 일반 OAuth2 로그인/가입 흐름과 명시적 연결 흐름을 분리한다.

3. settings 화면 정합화
   - 연결된 provider, 연결 가능 여부, 소셜 전용 계정의 비밀번호 변경 불가 상태를 설정 화면에 반영한다.
   - 링크 성공/실패 사유를 사람이 이해 가능한 문구로 노출한다.

4. 테스트/문서화 및 검증
   - success handler 단위 테스트로 link intent 성공 경로를 검증한다.
   - 뷰 통합 테스트로 link 시작 리다이렉트와 settings 상태/메시지를 검증한다.
   - `README.md`, `docs/phase/`에 설계 이유와 제약을 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 현재 스키마는 소셜 provider 슬롯이 1개뿐이므로 다중 provider 연결을 지원하지 못함
  - 대응: 이번 배치에서는 1개 provider만 명시적으로 허용하고, settings와 문서에 제약을 명확히 드러낸다
- link intent를 세션에 저장하는 흐름이 OAuth2 handshake와 충돌할 수 있음
  - 대응: intent 저장/소비를 별도 컴포넌트로 분리하고, 성공/실패 모두 세션 정리 테스트로 고정한다
- 소셜 전용 계정에 비밀번호 변경 폼이 그대로 노출되면 UX가 어색함
  - 대응: settings 화면에서 비밀번호 변경 가능 여부를 모델로 전달해 UI를 분기한다
