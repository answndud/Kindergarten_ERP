# PLAN.md

## Goal

실제 운영과 배포 환경에서 안전하게 부팅·관측·복구할 수 있는 상태를 만든다. 이미지, 환경변수, DB migration, health/readiness, 로그·메트릭, 장애 대응을 production-like 검증으로 닫고 정량 결과를 문서화한다.

## Active

### 1. 배포 산출물과 환경 안전성 검증

1. `Dockerfile`, prod compose, Caddy 설정, GitHub Actions를 함께 점검하고 재현 가능한 `bootJar`/compose config 검증을 만든다.
2. prod 필수 환경변수·시크릿·CORS·management port·seed·Swagger 설정을 startup safety 테스트와 deployment checklist에 연결한다.
3. 이미지 실행 사용자의 권한, 파일시스템 read-only 가능성, JVM/healthcheck, graceful shutdown을 확인한다.
4. Flyway migration을 빈 DB와 기존 데이터 기준으로 검증하고 파괴적/장시간 migration 위험을 문서화한다.

- 파일: `Dockerfile`, `deploy/*`, `docker/*`, `.github/workflows/*`, `src/main/resources/application*.yml`, `StartupSafetyValidator`, `docs/guides/deployment-guide.md`, `docs/guides/production-like-checklist.md`
- 검증: `./gradlew --no-daemon bootJar`, `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config`, `docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config`, 관련 targeted test
- 완료: prod-like compose가 설정 오류 없이 렌더링되고, 필수 환경변수 누락·위험한 공개 설정이 부팅 전에 차단된다.

### 2. 운영 관측성과 장애 대응 강화

1. health/readiness/liveness 응답과 DB·Redis 장애 상태를 운영 노출면별로 검증한다.
2. correlation ID, request log, error log, 개인정보 마스킹, 로그 보존/rotation을 점검한다.
3. Prometheus 핵심 지표와 Grafana dashboard가 인증, 요청 오류, DB/Redis, outbox retry/dead-letter를 설명하도록 보강한다.
4. 알림 outbox incident webhook, retry, dead-letter 복구 runbook을 실제 실패 시나리오로 검증한다.

- 파일: `src/main/java/com/erp/global/monitoring/*`, `src/main/java/com/erp/global/logging/*`, `src/main/resources/logback-spring.xml`, `docker/monitoring/*`, `docs/guides/risk-response.md`, `docs/guides/deployment-guide.md`
- 검증: `./gradlew --no-daemon integrationTest --tests '*ObservabilityIntegrationTest' --tests '*NotificationOutbox*IntegrationTest'`, `./gradlew --no-daemon bootJar`
- 완료: 장애 유형별로 health 상태·correlation ID·metric·runbook의 연결을 재현할 수 있다.

### 3. 운영 부하·복구·릴리스 검증

1. Notepad, Dashboard, 로그인/refresh, Outbox worker의 baseline을 측정하고 개선 전후 query count/응답 시간/오류율을 기록한다.
2. 동시성, Redis 재시작, DB 연결 실패, stale processing, 재시도·dead-letter 복구 시나리오를 targeted smoke로 검증한다.
3. 배포 전 backup/rollback/forward-fix 절차와 migration 승인 체크리스트를 정리한다.
4. 브라우저 핵심 흐름은 최소 smoke 자동화 범위를 정하고, 전체 suite는 CI 비용 기준을 명시한다.

- 파일: `src/test/java/com/erp/performance/*`, `src/test/java/com/erp/integration/*`, `scripts/performance/*`, `docs/guides/production-like-checklist.md`, `docs/guides/risk-response.md`
- 검증: `./gradlew --no-daemon integrationTest`, `./gradlew --no-daemon performanceSmokeTest`, 필요 시 k6 시나리오 실행
- 완료: 핵심 운영 경로의 수치 baseline, 장애 복구 결과, rollback 판단 기준이 문서와 테스트에 남는다.

## Backlog

- Tailwind CDN을 빌드 산출물/CSP/asset fingerprinting 기반으로 전환
- 실제 provider sandbox, webhook signature, rate limit 검증
- 브라우저 기반 핵심 사용자 흐름 자동화
