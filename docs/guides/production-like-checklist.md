# Production-Like Checklist

기준일: 2026-07-31

이 문서는 실제 클라우드 배포 없이도 운영 전환 전 확인 가능한 항목을 반복 실행하기 위한 checklist입니다.
실제 운영 배포를 대체하지 않으며, cloud 계정, 운영 도메인, OAuth redirect URI, RDS/Redis 접속, backup/rollback은 실제 서버에서 다시 검증해야 합니다.

## 1. 목적

- `prod` profile safety guard가 위험 설정을 막는지 확인한다.
- release jar가 만들어지는지 확인한다.
- local/prod compose config가 해석되는지 확인한다.
- 배포 전 미리 볼 수 있는 실패 신호를 문서와 명령으로 고정한다.

## 2. 사전 조건

- Java 21
- Docker Compose
- 저장소 루트에서 실행
- 실제 secret은 사용하지 않는다.
- `deploy/.env.prod.example`은 dry-run 전용 예시 값이다.

## 3. 명령

```bash
./gradlew test --tests "com.erp.global.config.StartupSafetyValidatorTest"
./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest"
./gradlew test --tests "com.erp.integration.ManagementSurfaceOptInIntegrationTest"
./gradlew bootJar
docker build --tag kindergarten-erp:quality-check .
docker image inspect kindergarten-erp:quality-check --format 'user={{.Config.User}}'
docker compose --env-file docker/.env.example -f docker/docker-compose.yml config >/tmp/docker-compose.base.yml
PROD_ENV_FILE=.env.prod.example docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config >/tmp/docker-compose.prod.yml
ALERTMANAGER_WEBHOOK_URL=https://hooks.example.com/alerts docker compose --profile alerting -f docker/docker-compose.monitoring.yml config >/tmp/docker-compose.monitoring-alerting.yml
docker run --rm -e APP_DOMAIN=erp.example.com -v "$PWD/deploy/Caddyfile:/etc/caddy/Caddyfile:ro" caddy:2 caddy validate --config /etc/caddy/Caddyfile
git diff --check
```

## 4. 기대 결과

| 항목 | 기대 결과 |
| --- | --- |
| Startup safety | prod에서 legacy JWT fallback, insecure cookie, seed, Swagger/OpenAPI, app-port Prometheus, wildcard/non-HTTPS CORS가 차단된다. |
| Observability default | 기본 app port에서 Swagger/OpenAPI와 Prometheus가 노출되지 않는다. |
| Management opt-in | local/demo처럼 명시적으로 열었을 때만 Swagger/OpenAPI와 app-port Prometheus가 공개된다. |
| Release package | `bootJar`가 성공한다. |
| Container build | 애플리케이션 이미지가 빌드되고 `user=10001:10001`로 실행된다. |
| Local compose | local Docker compose config가 해석된다. |
| Prod compose dry-run | `PROD_ENV_FILE=.env.prod.example` 주입 시 prod compose config가 해석된다. |
| Secret scope | Redis에는 Redis password만, Caddy에는 domain만 전달된다. |
| Diff hygiene | `git diff --check`가 통과한다. |

## 5. 2026-07-31 실행 결과

| 명령 | 결과 |
| --- | --- |
| `./gradlew test --tests "com.erp.global.config.StartupSafetyValidatorTest"` | 통과 |
| `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest"` | 통과 |
| `./gradlew test --tests "com.erp.integration.ManagementSurfaceOptInIntegrationTest"` | 통과 |
| `./gradlew bootJar` | 통과 |
| `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config >/tmp/docker-compose.base.yml` | 통과 |
| `PROD_ENV_FILE=.env.prod.example docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config >/tmp/docker-compose.prod.yml` | 통과 |
| `docker build --tag kindergarten-erp:quality-check .` | 통과 |
| `docker image inspect kindergarten-erp:quality-check --format 'user={{.Config.User}}'` | `user=10001:10001` |
| `docker run --rm -e APP_DOMAIN=erp.example.com -v "$PWD/deploy/Caddyfile:/etc/caddy/Caddyfile:ro" caddy:2 caddy validate --config /etc/caddy/Caddyfile` | 통과 |
| `./gradlew --no-daemon integrationTest --tests '*ObservabilityIntegrationTest' --tests '*NotificationOutbox*IntegrationTest'` | 32초, 통과 |
| `./gradlew --no-daemon performanceSmokeTest` | 28초, 통과 |
| `git diff --check` | 통과 |

## 6. 운영 전 남은 외부 의존성

- 실제 HTTPS 도메인
- `CORS_ALLOWED_ORIGINS=https://<real-domain>`
- Google/Kakao OAuth redirect URI 운영 도메인 등록
- RDS MySQL 접속과 backup 정책
- Redis password/volume/backup 정책
- Caddy TLS 발급 확인
- readiness `UP` 확인
- rollback 대상 image tag와 DB forward-fix 전략
- `scripts/backup-production.sh`로 MySQL/Redis backup artifact 생성
- `scripts/verify-production-backup.sh`로 checksum 검증
- 운영 backup은 별도 암호화 object storage로 복제하고 restore drill을 월 1회 수행
- 배포 후 장애는 correlation ID와 `/actuator/health/readiness`를 함께 확인
- DB schema 변경은 rollback 대신 백업 확인 후 forward-fix migration을 우선 검토
- Alertmanager는 `ALERTMANAGER_WEBHOOK_URL`을 주입한 `--profile alerting`으로 기동하고 테스트 alert의 수신을 확인
- HTML 응답은 `Content-Security-Policy` nonce를 포함하고 `Content-Security-Policy-Report-Only`는 사용하지 않음
