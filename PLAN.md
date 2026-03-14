# PLAN.md

## 작업명
- 후속 고도화 19차 (감사 로그 export + 인증 이상 징후 알림 + Grafana 대시보드)

## 1) 목표 / 범위
- 원장이 인증 감사 로그를 CSV로 export해 운영/면접 데모에서 바로 사용할 수 있게 만든다.
- 반복 로그인 실패를 이상 징후로 감지하고 원장에게 시스템 알림을 보내 운영 대응 흐름을 닫는다.
- Prometheus 메트릭을 실제 Grafana 대시보드로 소비할 수 있게 monitoring compose와 provisioning 파일을 추가한다.
- README, 결정 로그, 진행 기록을 새 운영 스토리 기준으로 갱신한다.

## 2) 세부 작업 단계
1. 감사 로그 export API
   - 기존 필터와 동일한 조건을 재사용하는 export query/service 추가
   - `/api/v1/auth/audit-logs/export` CSV attachment endpoint 추가
   - 원장 전용 권한과 파일명/헤더 규칙 고정

2. 인증 이상 징후 알림
   - Redis 기반 fixed-window/cooldown 정책 추가
   - 반복 로그인 실패가 특정 유치원 사용자 이메일에 집중될 때 원장에게 `SYSTEM` 알림 발송
   - 스푸핑/중복 발송 방지를 위한 tenant, cooldown, unknown email 처리 명시

3. Grafana/Prometheus 운영 대시보드
   - monitoring compose overlay 추가
   - Prometheus scrape 설정과 Grafana datasource/dashboard provisioning 추가
   - auth event / HTTP 요청 / JVM 상태를 바로 볼 수 있는 기본 dashboard 제공

4. 테스트 / 문서 / 배포
   - export API, alerting 회귀 테스트 보강
   - README, 결정 로그, 진행 기록 갱신
   - add/commit/push 및 GitHub Actions 결과 확인

## 3) 검증 계획
- 컴파일 검증
  - `./gradlew compileJava compileTestJava`
- 핵심 회귀 검증
  - `./gradlew test --tests "com.erp.api.AuthAuditApiIntegrationTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest" --tests "com.erp.integration.ObservabilityIntegrationTest"`
- 전체 테스트
  - `./gradlew test`
- 최종 문서/포맷 검증
  - `git diff --check`

## 4) 리스크 및 대응
- 감사 로그 export가 기존 권한 경계를 우회할 수 있음
  - 대응: 기존 principal 검증과 같은 유치원 member 기반 필터를 그대로 재사용하고, export 전용 테스트로 고정한다
- 로그인 실패 알림이 과도하게 발송될 수 있음
  - 대응: threshold + TTL + cooldown을 Redis로 두고, 알 수 없는 이메일이나 유치원 매핑 불가 케이스는 알림에서 제외한다
- 모니터링 스택 문서가 실제 실행 절차와 어긋날 수 있음
  - 대응: compose overlay, provisioning, README 명령어를 같은 기준으로 맞추고 YAML/JSON 파싱 검증을 수행한다
