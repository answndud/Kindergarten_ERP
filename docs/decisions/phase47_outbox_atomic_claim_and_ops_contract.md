# Phase 47. Outbox Atomic Claim + Ops Contract Hardening

## 배경

- `notification_outbox`는 retry/dead-letter까지는 갖췄지만, claim 단계가 `조회 -> 메모리에서 markProcessing()` 방식이라 멀티 인스턴스에서 같은 row를 동시에 집을 수 있었다.
- 로컬 Docker compose도 MySQL/Redis/Prometheus/Grafana를 모든 인터페이스에 바로 바인딩하고 있어 “로컬 전용”이라는 의도가 설정 파일에서 충분히 드러나지 않았다.
- GitHub Actions는 테스트 스위트는 분리돼 있었지만, 실제 배포 단위인 bootJar 생성과 compose config 해석을 검증하지 않았다.

## 목표

1. outbox worker가 같은 row를 중복 claim하지 않게 만든다.
2. 로컬/데모 운영 도구는 기본적으로 localhost에만 바인딩되게 한다.
3. CI에서 테스트뿐 아니라 bootJar와 compose config까지 검증한다.
4. 위 변경을 README, runbook, 블로그와 같이 맞춘다.

## 결정

### 1. Outbox claim은 MySQL 8 `FOR UPDATE SKIP LOCKED`로 원자화한다

- `NotificationOutboxRepository`에 claim 전용 native query를 추가했다.
  - `claimPendingIds(...)`
  - `claimStaleProcessingIds(...)`
- `NotificationDispatchService.claimReadyDeliveries(...)`는
  1. `PENDING` ready row를 claim
  2. 없으면 stale `PROCESSING` row를 reclaim
  3. 같은 트랜잭션 안에서 `PROCESSING`으로 전이
  4. claim된 id만 다음 처리 단계로 넘긴다
- 이로써 같은 outbox row를 두 worker가 동시에 집는 문제를 줄였다.

### 2. outbox 정확도는 “DB claim once”까지 보장하고, downstream exactly-once는 별도 문제로 둔다

- 이번 배치는 DB row claim의 중복 방지까지를 범위로 잡았다.
- webhook/email downstream 시스템의 exactly-once 보장은 아직 별도 과제다.
- 문서와 블로그에는 이 한계를 명시한다.

### 3. 로컬 compose는 기본 localhost bind를 사용한다

- `docker/docker-compose.yml`
  - MySQL/Redis 포트를 `${DOCKER_BIND_HOST:-127.0.0.1}` 기준으로 바인딩
- `docker/docker-compose.monitoring.yml`
  - Prometheus/Grafana도 같은 기준으로 바인딩
- `docker/.env.example`에 bind host와 published port 값을 명시했다.
- Grafana/MySQL 필수 credential은 env가 비어 있으면 compose가 바로 실패하게 했다.

### 4. Compose 실행 계약은 `--env-file docker/.env`를 SSOT로 고정한다

- README, developer guide, demo preflight/runbook, 블로그의 compose 명령을 모두 `--env-file docker/.env` 기준으로 맞췄다.
- “로컬 전용 인프라 값”과 “앱 프로세스 환경 변수”를 문서상으로도 더 선명하게 분리했다.

### 5. CI는 bootJar와 compose config도 검증한다

- `.github/workflows/ci.yml`에 `package-smoke` job을 추가했다.
- 이 job은 아래를 수행한다.
  - `./gradlew --no-daemon bootJar`
  - 생성된 JAR 구조 확인
  - `docker compose ... config`로 base/monitoring compose 해석 검증
  - bootJar artifact 업로드

## 구현 파일

- outbox
  - `src/main/java/com/erp/domain/notification/service/NotificationDispatchService.java`
  - `src/main/java/com/erp/domain/notification/repository/NotificationOutboxRepository.java`
  - `src/test/java/com/erp/integration/NotificationOutboxClaimConcurrencyIntegrationTest.java`
- CI / compose
  - `.github/workflows/ci.yml`
  - `docker/docker-compose.yml`
  - `docker/docker-compose.monitoring.yml`
  - `docker/.env.example`
- 문서 / 블로그
  - `README.md`
  - `docs/guides/env-contract.md`
  - `docs/guides/developer-guide.md`
  - `docs/portfolio/demo/demo-preflight.md`
  - `docs/portfolio/demo/demo-runbook.md`
  - `docs/portfolio/case-studies/auth-incident-response.md`
  - `docs/portfolio/interview/interview_one_pager.md`
  - `docs/portfolio/interview/interview_qa_script.md`
  - `blog/03-docker-mysql-redis-dev-environment.md`
  - `blog/14-why-testcontainers-over-h2.md`
  - `blog/15-github-actions-and-tagged-test-suites.md`
  - `blog/19-auth-audit-log-and-operations-console.md`
  - `blog/20-openapi-management-plane-and-observability.md`
  - `blog/21-notification-outbox-and-incident-channel.md`
  - `blog/26-demo-architecture-and-interview-pack.md`

## 검증

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew compileJava compileTestJava`
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --no-daemon test --tests "com.erp.integration.NotificationOutboxIntegrationTest" --tests "com.erp.integration.NotificationOutboxRetryIntegrationTest" --tests "com.erp.integration.NotificationOutboxClaimConcurrencyIntegrationTest"`
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --no-daemon fastTest integrationTest`
- `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml')"`
- `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config`
- `docker compose --env-file docker/.env.example -f docker/docker-compose.yml -f docker/docker-compose.monitoring.yml config`
- `git diff --check`

## 면접 포인트

- `outbox는 retry/dead-letter만으로 끝나지 않고, 멀티 인스턴스에서 같은 row를 중복 집지 않게 atomic claim까지 보강했습니다.`
- `로컬 운영 도구도 기본 공개가 아니라 localhost bind를 기본값으로 둬서 dev convenience와 안전한 기본값을 같이 챙겼습니다.`
- `CI는 테스트만 통과하는지 보지 않고, 실제 배포 단위인 bootJar와 compose config까지 같이 검증합니다.`
