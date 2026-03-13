# PLAN.md

## 작업명
- 후속 고도화 18차 (API 계약 가시화 + 감사 로그 화면 + 메트릭 + 데모 진입점)

## 1) 목표 / 범위
- Swagger UI / OpenAPI를 추가해 `/api/v1/**` 계약을 바로 확인할 수 있게 만든다.
- 원장 전용 인증 감사 로그 조회 화면을 추가해 API-only 상태를 운영 UI까지 닫는다.
- Prometheus endpoint와 인증/소셜 이벤트 메트릭을 추가해 운영 관측성을 숫자로 확장한다.
- 시연용 `demo` 프로파일과 문서를 정리해 면접/데모 진입을 고정한다.
- README, 결정 로그, 진행 기록을 새 기능 기준으로 갱신한다.

## 2) 세부 작업 단계
1. API 계약 가시화
   - `springdoc-openapi` 의존성 및 OpenAPI 메타데이터/보안 스키마 설정
   - Swagger UI / API docs 공개 경로와 보안 규칙 정리
   - API 계약 접근 회귀 테스트 추가
   - 완료

2. 감사 로그 운영 UI
   - 원장 전용 뷰 컨트롤러/템플릿 추가
   - 필터/페이지네이션/상태 배지/원인 표시 반영
   - 설정 화면 또는 네비게이션에서 진입 링크 제공
   - 완료

3. 운영 메트릭 확장
   - Prometheus exposure 활성화
   - 인증 감사 이벤트 기반 custom counter 추가
   - observability 회귀 테스트 보강
   - 완료

4. 데모 진입점 정리
   - `demo` 프로파일 추가
   - seed data / Swagger / 감사 로그 화면 진입 문서 정리
   - 완료

5. 검증 및 배포
   - `./gradlew compileJava compileTestJava`
   - 대상 통합 테스트 및 observability/view 테스트
   - `git diff --check`
   - add/commit/push
   - GitHub Actions run 결과 확인
   - 로컬 검증 완료, 원격 배포 대기

## 3) 검증 계획
- 컴파일 검증
  - `./gradlew compileJava compileTestJava`
- API/뷰/운영 관측성 회귀 검증
  - `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.api.AuthAuditApiIntegrationTest"`
- 최종 문서/포맷 검증
  - `git diff --check`

## 4) 리스크 및 대응
- Swagger UI와 Security 설정이 충돌할 수 있음
  - 대응: 공개 경로를 최소 범위(`/swagger-ui/**`, `/v3/api-docs/**`)로 열고 통합 테스트로 고정한다
- 감사 로그 UI가 principal 전용 정책을 깨뜨릴 수 있음
  - 대응: 뷰 접근은 컨트롤러/보안 설정/테스트에서 모두 원장 기준으로 검증한다
- Prometheus 공개 범위가 과도할 수 있음
  - 대응: health/info/prometheus만 노출하고 README/결정 로그에 의도를 명시한다
- demo 프로파일이 local seed와 충돌할 수 있음
  - 대응: DataLoader를 `local`, `demo` 공용으로 쓰고 계정 존재 시 skip 정책을 유지한다
