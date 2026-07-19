# PLAN.md

## Goal

실제 운영에 투입하기 전에 권한 상승, 로그 유실, 배포 실패, 복구 불가, proxy IP 오인, 공급망 위험을 제거하고 검증 가능한 운영 기준을 만든다.

## Active

### P1 - 보안 경계와 인증 흐름 차단

1. 공개 회원가입에서 `PRINCIPAL` 생성을 차단하고 초대/승인 경계를 명확히 한다.
   - 파일: `AuthApiController`, `AuthService`, `SignUpRequest`, `AuthApiIntegrationTest`
   - 변경: 공개 가입은 허용 역할을 제한하고 관리자 생성 실패 회귀 테스트를 추가한다.
   - 검증: `./gradlew --no-daemon integrationTest --tests '*AuthApiIntegrationTest'`
   - 완료: 비인증 요청으로 원장 계정을 만들 수 없다.

2. reverse proxy 뒤 client IP 전달과 rate limit을 실제 구성에 맞춘다.
   - 파일: `ClientIpResolver`, `ClientIpProperties`, `application-prod.yml`, `Caddyfile`, 관련 테스트
   - 변경: 신뢰 proxy 범위와 forwarded header 검증을 명시한다.
   - 검증: `./gradlew --no-daemon integrationTest --tests '*Auth*IntegrationTest'`
   - 완료: 공격자 IP별 rate limit과 audit IP가 proxy 뒤에서도 분리된다.

### P2 - 운영 로그·배포·복구 안전성

1. prod 로그를 stdout 또는 영속 수집 경로로 전환하고 배포 rollback을 자동화한다.
   - 파일: `logback-spring.xml`, `deploy/docker-compose.prod.yml`, `.github/workflows/cd.yml`, deployment guide
   - 변경: 로그 유실을 막고 readiness retry, 이전 이미지 보존, 실패 rollback을 추가한다.
   - 검증: compose config, Caddy validate, CD shell syntax, targeted Gradle tests
   - 완료: 신규 이미지 실패 시 이전 정상 이미지로 복귀할 수 있다.

2. DB/Redis backup·restore와 대량 조회 제한을 실행 가능한 절차로 고정한다.
   - 파일: `deploy/*`, `scripts/*`, `docs/guides/production-like-checklist.md`, export/query controllers
   - 변경: backup/restore smoke, RPO/RTO 기준, 입력 범위 상한을 추가한다.
   - 검증: `./gradlew --no-daemon performanceSmokeTest`, backup script dry-run, focused tests
   - 완료: 복구 절차를 명령 단위로 재현하고 대량 요청이 제한된다.

### P3 - 공급망·알림·브라우저 운영 검증

1. 이미지/Actions 고정, 취약점·SBOM 검사, CSP와 외부 asset 무결성 정책을 추가한다.
   - 파일: `Dockerfile`, `deploy/docker-compose.prod.yml`, `.github/workflows/*`, templates, `Caddyfile`
   - 검증: YAML/Compose/Caddy 검증, detector, build
   - 완료: mutable dependency와 무제한 script 실행 경로가 통제된다.

2. Prometheus alert rule과 핵심 브라우저 smoke를 추가하고 전체 품질을 재평가한다.
   - 파일: `docker/monitoring/*`, `src/test/*`, `docs/guides/*`
   - 검증: `./gradlew --no-daemon integrationTest performanceSmokeTest`, browser smoke 가능 범위 확인
   - 완료: 장애 알림·핵심 사용자 흐름·최종 review 결과가 문서화된다.

## Backlog

- 실제 provider sandbox, webhook signature 검증
