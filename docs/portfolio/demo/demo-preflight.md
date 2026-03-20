# Demo Preflight

이 문서는 시연 직전 체크리스트 SSOT입니다.

## 1. 실행 상태

- Docker: `docker compose -f docker/docker-compose.yml up -d`
- 앱: `./gradlew bootRun --args='--spring.profiles.active=demo'`
- monitoring overlay가 필요하면:
  - `docker compose -f docker/docker-compose.monitoring.yml up -d`

## 2. 접속 주소

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Prometheus: `http://localhost:8080/actuator/prometheus`
- Grafana: `http://localhost:3000`

## 3. 데모 계정

- principal: `principal@test.com / test1234!`
- teacher: `teacher@test.com / test1234!`
- parent: `parent1@test.com / test1234!`

## 4. 반드시 확인할 데이터

- 정원이 찬 반과 waitlist/offer 전환용 입학 신청 데이터
- 출결 변경 요청을 만들 자녀 데이터
- auth/domain audit console에서 조회 가능한 로그 데이터
- Grafana의 `Kindergarten ERP Observability` 대시보드

## 5. 데모 전에 빠르게 확인할 화면

1. `/swagger-ui.html`
2. `/attendance-requests`
3. `/domain-audit-logs`
4. `/audit-logs`
5. `/actuator/health/readiness`

## 6. 실패 시 백업 플랜

- Grafana가 늦게 뜨면 `/actuator/prometheus`와 audit console로 운영 관측성 스토리를 대체합니다.
- UI 동작이 불안정하면 Swagger + audit CSV export 중심으로 API 시연으로 전환합니다.
- 소셜 로그인은 외부 provider 상태 영향을 받으므로, 기본 데모는 local 계정 기준으로 진행합니다.
