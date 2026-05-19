# Production-Like Checklist

기준일: 2026-05-19

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
docker compose --env-file docker/.env.example -f docker/docker-compose.yml config >/tmp/docker-compose.base.yml
PROD_ENV_FILE=.env.prod.example docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config >/tmp/docker-compose.prod.yml
git diff --check
```

## 4. 기대 결과

| 항목 | 기대 결과 |
| --- | --- |
| Startup safety | prod에서 legacy JWT fallback, insecure cookie, seed, Swagger/OpenAPI, app-port Prometheus, wildcard/non-HTTPS CORS가 차단된다. |
| Observability default | 기본 app port에서 Swagger/OpenAPI와 Prometheus가 노출되지 않는다. |
| Management opt-in | local/demo처럼 명시적으로 열었을 때만 Swagger/OpenAPI와 app-port Prometheus가 공개된다. |
| Release package | `bootJar`가 성공한다. |
| Local compose | local Docker compose config가 해석된다. |
| Prod compose dry-run | `PROD_ENV_FILE=.env.prod.example` 주입 시 prod compose config가 해석된다. |
| Diff hygiene | `git diff --check`가 통과한다. |

## 5. 2026-05-19 실행 결과

| 명령 | 결과 |
| --- | --- |
| `./gradlew test --tests "com.erp.global.config.StartupSafetyValidatorTest"` | 통과 |
| `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest"` | 통과 |
| `./gradlew test --tests "com.erp.integration.ManagementSurfaceOptInIntegrationTest"` | 통과 |
| `./gradlew bootJar` | 통과 |
| `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config >/tmp/docker-compose.base.yml` | 통과 |
| `PROD_ENV_FILE=.env.prod.example docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config >/tmp/docker-compose.prod.yml` | 통과 |
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
