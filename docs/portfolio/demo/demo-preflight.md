# Demo Preflight

이 문서는 시연 직전 체크리스트 SSOT입니다.

## 1. 실행 상태

- Docker:
  - `cp docker/.env.example docker/.env`
  - `docker compose --env-file docker/.env -f docker/docker-compose.yml up -d`
- 앱: `./gradlew bootRun --args='--spring.profiles.active=demo'`
- monitoring overlay가 필요하면:
  - `docker compose --env-file docker/.env -f docker/docker-compose.yml -f docker/docker-compose.monitoring.yml up -d`

## 2. 접속 주소

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (`demo`에서만 의도적으로 공개)
- Health: `http://localhost:8080/actuator/health`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Prometheus: `http://localhost:8080/actuator/prometheus` (`demo`에서만 app port 공개)
- Grafana: `http://localhost:3000`
- 로컬 monitoring 포트는 `docker/.env` 기본값 기준 `127.0.0.1`에만 바인딩됩니다.

## 3. 데모 계정

- principal: `principal@test.com / test1234!`
- teacher: `teacher1@test.com / test1234!`
- parent: `parent1@test.com / test1234!`

## 4. 반드시 확인할 데이터와 시연 중 생성할 대상

- DataLoader가 넣어 주는 기본 데이터
  - `demo` 프로파일이 `app.seed.enabled=true`를 켠 상태에서만 생성됨
  - principal / teacher / parent 계정
  - 출석, 알림장, 공지, auth audit 로그
- 시연 중 직접 만들거나 확인할 대상
  - `/applications/pending`에서 보여 줄 입학 신청/승인 흐름
  - `/attendance-requests`에서 보여 줄 출결 변경 요청
  - auth/domain audit console의 조회 및 CSV export
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

## 7. 종료 / 정리

- 앱은 실행 중인 터미널에서 `Ctrl+C`
- Docker 정리:
  - `docker compose --env-file docker/.env -f docker/docker-compose.yml down`
  - `docker compose --env-file docker/.env -f docker/docker-compose.yml -f docker/docker-compose.monitoring.yml down`
