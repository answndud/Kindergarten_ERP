# PLAN.md

## 작업명
- 후속 고도화 15차 (인터뷰 패키징 + 운영 관측성 + 감사 로그 조회 API)

## 1) 목표 / 범위
- 인터뷰에서 바로 사용할 수 있는 1장 요약 문서와 3분 데모 시나리오를 정리한다.
- Spring Boot Actuator, health/readiness, correlation id, request structured logging으로 운영 관측성 baseline을 추가한다.
- 원장 전용 인증 감사 로그 조회 API를 추가해 “저장만 하는 로그”가 아니라 “운영에서 조회 가능한 로그”로 닫는다.
- README와 phase 문서를 최신 운영/면접 관점 기준으로 다시 연결한다.

## 2) 세부 작업 단계
1. 인터뷰 문서 패키징
   - `docs/interview/`에 1장 요약 문서와 3분 데모 시나리오를 추가한다.
   - `README.md` 문서 섹션에서 인터뷰용 문서를 바로 찾을 수 있게 연결한다.

2. 운영 관측성 baseline 추가
   - `spring-boot-starter-actuator`와 health/info 노출을 추가한다.
   - liveness/readiness probe를 활성화한다.
   - correlation id filter와 request structured logging을 추가한다.
   - 보안 설정에서 actuator 공개 범위를 health/info 수준으로 제한한다.

3. 인증 감사 로그 조회 API 구현
   - 원장 전용 `/api/v1/auth/audit-logs` 조회 API를 추가한다.
   - principal의 유치원 소속 member 기반 로그만 조회하도록 제한한다.
   - eventType/result/provider/email/date 필터를 지원한다.

4. 테스트/문서화 및 검증
   - 운영 관측성/감사 로그 조회 통합 테스트를 추가한다.
   - `README.md`, `docs/phase/phase34~35`에 설계와 인터뷰 포인트를 기록한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.api.AuthAuditApiIntegrationTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.api.AuthAuditApiIntegrationTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- actuator 공개 범위를 넓히면 운영 정보가 과다 노출될 수 있음
  - 대응: `/actuator/health/**`, `/actuator/info`만 공개하고 그 외는 기존 인증 규칙을 유지한다
- 요청 로그가 과도한 개인정보를 남길 수 있음
  - 대응: method/uri/status/duration/clientIp/correlationId 수준으로만 남기고 body/토큰은 기록하지 않는다
- 감사 로그 조회 API가 멀티테넌시 경계를 흐릴 수 있음
  - 대응: 원장만 조회 가능, requester의 kindergarten 소속 `memberId`가 있는 로그만 반환하고 익명 실패 로그는 제외한다
- phase 문서가 많아져 인터뷰 시 오히려 길어질 수 있음
  - 대응: 별도 1장 요약본과 3분 데모 시나리오를 추가해 면접용 entry point를 만든다
