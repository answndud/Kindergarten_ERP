# Phase 37. 감사 로그 export + 인증 이상 징후 알림 + Grafana 대시보드

## 배경

Phase 36까지 오면서

1. Swagger/OpenAPI로 API 계약은 바로 볼 수 있게 됐고
2. 감사 로그는 DB 저장 + 원장 조회 화면/API까지 붙었고
3. Prometheus scrape와 auth event counter도 노출됐다.

하지만 운영 흐름은 아직 두 군데가 비어 있었다.

- 감사 로그를 화면에서만 보고 끝나서, 면접/운영 시나리오에서 "내보내기"가 안 됐다.
- 반복 로그인 실패 같은 이상 징후를 운영자가 능동적으로 찾아야 했다.
- Prometheus metric은 있었지만 Grafana 대시보드가 없어 "실제로 본다"까지 닫히지 않았다.

즉, 저장과 조회는 했지만
운영 대응과 시계열 시각화는 아직 미완성이었다.

## 목표

- 원장이 같은 필터 조건으로 인증 감사 로그를 CSV로 export할 수 있게 만든다.
- 반복 로그인 실패를 Redis 기반으로 감지하고 원장에게 시스템 알림을 보낸다.
- Prometheus metric을 바로 소비하는 Grafana dashboard/provisioning을 제공한다.

## 1) 감사 로그 export API 추가

추가 endpoint:

- `GET /api/v1/auth/audit-logs/export`

정책:

- principal만 호출 가능
- 기존 조회 API와 같은 필터(`eventType`, `result`, `provider`, `email`, `from`, `to`)를 그대로 사용
- 같은 유치원에 안전하게 귀속된 감사 로그만 export
- 응답은 `text/csv` attachment

구현:

- `AuthAuditLogRepository.searchAllByKindergartenId(...)`
  - 기존 페이지 조회와 동일한 tenant/filter 조건을 재사용
- `AuthAuditLogQueryService.exportAuditLogsCsvForPrincipal(...)`
  - CSV 생성 로직 추가
- `AuthAuditLogController`
  - attachment filename과 content type 지정
- `authaudit/audit-logs.html`
  - 현재 필터 조건 그대로 `CSV Export` 버튼에서 다운로드 가능

왜 CSV인가?

- 운영/면접 데모에서는 가장 단순하고 범용적이다.
- DB dump나 admin 전용 포맷보다 의존성이 적고 바로 공유 가능하다.

## 2) 반복 로그인 실패 이상 징후 알림

문제:

- rate limit만 있으면 공격을 막는 쪽은 일부 되지만,
  운영자는 "누가 집중적으로 맞고 있는지"를 직접 찾아야 한다.

이번 단계 정책:

- 대상: 기존 member 이메일에 대한 로그인 실패
- threshold: 기본 3회
- window: 10분
- cooldown: 30분
- 수신자: 해당 유치원 principal
- 알림 타입: `NotificationType.SYSTEM`

구현:

- `AuthAlertProperties`
  - `app.security.auth-alert.*` 설정으로 threshold/window/cooldown을 외부화
- `AuthAnomalyAlertService`
  - Redis counter + cooldown key로 fixed-window/cooldown 정책 구현
  - `memberRepository.findByEmail(...)`로 tenant를 식별할 수 있을 때만 알림 발송
  - 링크는 `/audit-logs?eventType=LOGIN&result=FAILURE&email=...`
- `AuthService.login(...)`
  - 로그인 실패 catch 경로에서 감사 로그 저장 후 이상 징후 알림 평가
  - 성공 로그인 시 alert counter 정리

중요한 제한:

- 익명 이메일 실패는 tenant 귀속이 안전하지 않아 알림에서 제외했다.
- 같은 provider의 로그인 실패를 따로 쪼개지 않고, 현 단계는 local login 중심으로 본다.

## 3) Grafana/Prometheus monitoring overlay

추가 파일:

- `docker/docker-compose.monitoring.yml`
- `docker/monitoring/prometheus/prometheus.yml`
- `docker/monitoring/grafana/provisioning/...`
- `docker/monitoring/grafana/dashboards/kindergarten-erp-observability.json`

포함 내용:

- Prometheus가 host app의 `/actuator/prometheus` scrape
- Grafana datasource/dashboard 자동 provisioning
- 기본 dashboard:
  - auth event rate
  - 5xx RPS
  - heap used
  - endpoint별 HTTP request rate

왜 compose overlay로 분리했나?

- 기본 개발 스택(MySQL/Redis)과 운영 관측성 스택을 느슨하게 분리하기 위해서다.
- 항상 Grafana/Prometheus를 띄울 필요는 없고, 데모/분석할 때만 켤 수 있게 했다.

## 테스트 / 검증

로컬 검증:

- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.api.AuthAuditApiIntegrationTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest" --tests "com.erp.integration.ObservabilityIntegrationTest"`
- `./gradlew test`
- `ruby -e "require 'yaml'; YAML.load_file('docker/docker-compose.monitoring.yml'); YAML.load_file('docker/monitoring/prometheus/prometheus.yml'); YAML.load_file('docker/monitoring/grafana/provisioning/datasources/datasource.yml'); YAML.load_file('docker/monitoring/grafana/provisioning/dashboards/dashboard.yml')"`
- `ruby -rjson -e "JSON.parse(File.read('docker/monitoring/grafana/dashboards/kindergarten-erp-observability.json'))"`
- `git diff --check`

핵심 회귀 테스트:

- `AuthAuditApiIntegrationTest`
  - principal CSV export 성공
  - teacher export 차단
- `AuthApiIntegrationTest`
  - 반복 로그인 실패 시 principal 시스템 알림 1회 생성
  - 존재하지 않는 이메일 반복 실패는 알림 미생성

## 면접에서 강조할 포인트

1. "감사 로그를 조회 API로만 끝내지 않고 CSV export까지 붙여 운영 대응과 공유 흐름을 만들었습니다."
2. "보안 이벤트는 저장만으로 충분하지 않아서, 반복 로그인 실패를 principal 시스템 알림으로 연결했습니다."
3. "Prometheus endpoint를 열어 두는 것에서 멈추지 않고, Grafana provisioning으로 실제 대시보드까지 바로 볼 수 있게 했습니다."
4. "tenant에 안전하게 귀속할 수 있는 감사 로그만 조회/export/alert에 포함시키고, 완전히 익명인 실패 로그는 운영 범위에서 제외했습니다."
