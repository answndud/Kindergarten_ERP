# PLAN.md

## 작업명
- 후속 고도화 22차 (보안 기본값 하드닝 + 도메인 신뢰성 보강 + 배포/문서/블로그 싱크)

## 현재 배치 상태
- 완료: 이전 Batch A~D (`management plane`, `활성 세션`, `outbox`, `waitlist/domain audit`, `tagged CI`, `hiring-pack`) 구현 및 문서화
- 완료: 블로그 시리즈 초보자 친화성/면접 대응력 리라이트
- 신규 착수: 서브에이전트 병렬 리뷰를 바탕으로 `보안/운영 기본값`, `서비스/API 권한/동시성`, `배포/CI/운영 문서`, `블로그 싱크`를 한 배치 계획으로 재정리

## 1) 목표 / 범위
- 프로젝트를 “기능 많은 포트폴리오”에서 “기본값까지 안전한 운영형 포트폴리오”로 끌어올린다.
- 코드 수정은 반드시 `테스트 + 결정 로그 + README/가이드 + 블로그 본문`까지 한 세트로 닫는다.
- 이번 배치의 핵심 범위는 아래 4가지다.
  1. `safe-by-default` 설정과 관리면(Management Surface) 하드닝
  2. 신청서 상세 권한, 출결 요청, outbox 등 서비스/API 신뢰성 보강
  3. CI/배포/운영 계약 정리
  4. 위 변경 사항을 초보자용 블로그와 면접/운영 문서에 정확히 동기화

## 2) 세부 작업 단계
1. Batch A: 안전한 기본값으로 뒤집기
   - `application.yml`, `application-local.yml`, `application-prod.yml`, `ManagementSurfaceProperties`, `SecurityConfig`, `PrometheusScrapeController`, `DataLoader` 점검
   - 기본 활성 프로파일 제거
   - JWT/OAuth fallback secret 제거 또는 prod fail-fast 검증 추가
   - Swagger/OpenAPI, Prometheus app-port 노출 기본값을 fail-closed로 전환
   - local/demo 전용 seed와 자격증명은 이중 게이트(`profile + enabled flag`)로 제한
   - 운영 필수 env matrix와 `.env.example` 또는 동등 문서 추가
   - 동기화 문서/블로그
     - `README.md`
     - `docs/guides/developer-guide.md`
     - `docs/portfolio/demo/demo-preflight.md`
     - `blog/03-docker-mysql-redis-dev-environment.md`
     - `blog/04-application-yml-and-profile-strategy.md`
     - `blog/11-securityconfig-signup-login-basics.md`
     - `blog/12-jwt-cookie-auth-flow.md`
     - `blog/17-rate-limit-and-client-ip-trust-model.md`
     - `blog/20-openapi-management-plane-and-observability.md`

2. Batch B: 서비스/API 권한 경계와 상태 전이 보강
   - `KidApplication`, `KindergartenApplication` 상세 조회 권한을 본인/승인권자/원장 중심으로 재정의
   - `AttendanceChangeRequest` 생성 race 방지용 유니크 제약 또는 원자적 생성 전략 추가
   - `KindergartenApplication` 상태 전이/예외를 `BusinessException` 중심으로 정리하고 필요시 lock 전략 보강
   - `DashboardService` 캐시 eviction을 입학 승인/offer 수락/교사 승인 경로까지 확장
   - `Notepad`의 감사/삭제 모델을 현재 운영 기준과 얼마나 맞출지 결정하고, 최소한 문서에서 한계를 명시
   - 동기화 문서/블로그
     - `README.md`
     - `docs/decisions/*` 신규 결정 로그
     - `blog/10-calendar-dashboard-and-application-workflows.md`
     - `blog/22-classroom-capacity-and-waitlist-workflow.md`
     - `blog/23-attendance-change-request-and-domain-audit.md`
     - `blog/24-audit-logs-as-operations-tools.md`

3. Batch C: 비동기 전달/배포/CI 신뢰성 보강
   - `NotificationDispatchService`/`NotificationOutboxRepository`의 claim을 원자적으로 변경
   - outbox 동시 claim 회귀 테스트 추가
   - `.github/workflows/ci.yml`의 duplicate job key 제거
   - CI에 packaging artifact 검증 추가
   - `docker-compose.yml`, `docker-compose.monitoring.yml` 포트 바인딩/기본 자격증명/로컬 전용 경계 정리
   - monitoring/demo runbook을 실제 compose 경로와 일치시키기
   - Redis를 auth critical dependency로 문서/알림/runbook에 명시
   - 동기화 문서/블로그
     - `README.md`
     - `docs/guides/developer-guide.md`
     - `docs/portfolio/demo/demo-runbook.md`
     - `docs/portfolio/case-studies/auth-incident-response.md`
     - `blog/14-why-testcontainers-over-h2.md`
     - `blog/15-github-actions-and-tagged-test-suites.md`
     - `blog/19-auth-audit-log-and-operations-console.md`
     - `blog/20-openapi-management-plane-and-observability.md`
     - `blog/21-notification-outbox-and-incident-channel.md`
     - `blog/26-demo-architecture-and-interview-pack.md`

4. Batch D: 블로그/문서 최종 싱크 마감
   - 위 3개 배치의 코드 변경을 기준으로 블로그 글의 상황표, 구현 한계, 검증 명령, 산출물 체크리스트 재동기화
   - “현재 구현의 한계” 박스를 실제 코드 기준으로 업데이트
   - 면접 답변 문서와 블로그 메시지가 충돌하지 않게 정리
   - 후보 문서
     - `BLOG_PLAN.md`, `BLOG_PROGRESS.md`
     - `blog/README.md`
     - `blog/00_rebuild_guide.md`
     - `blog/00_quality_checklist.md`
     - `blog/01-why-kindergarten-erp-domain.md`
     - `blog/11`, `12`, `17`, `20`, `21`, `22`, `23`, `24`, `26`
     - `docs/portfolio/interview/*`

## 3) 검증 계획
- Batch A
  - `./gradlew compileJava compileTestJava`
  - security/config 관련 테스트 또는 신규 통합 테스트
  - 공개 경로/관리 경로 권한 수동 검증
  - `git diff --check`
- Batch B
  - 권한 성공/실패 통합 테스트
  - race/concurrency 재현 테스트 또는 최소 회귀 테스트
  - 대시보드 캐시 invalidation 검증
  - `./gradlew --no-daemon integrationTest`
- Batch C
  - outbox 동시 claim 테스트
  - `./gradlew --no-daemon fastTest`
  - `./gradlew --no-daemon integrationTest`
  - CI YAML 파싱
  - 필요시 GitHub Actions run 확인
- Batch D
  - 링크/경로 검토
  - `git diff --check`
  - README, demo runbook, blog 검증 명령의 상호 정합성 수동 점검

## 4) 리스크 및 대응
- `safe-by-default`로 바꾸면 local/demo 편의가 떨어질 수 있음
  - 대응: 편의 설정은 유지하되 profile/flag를 이중 게이트로 묶고, 문서에 명시적으로 분리
- outbox/출결 요청 동시성은 설계보다 테스트가 더 어려울 수 있음
  - 대응: 원자적 DB 제약/claim 전략을 먼저 고정하고, 테스트는 최소 재현 경로부터 추가
- 권한 경계 수정은 기존 화면/API 기대와 충돌할 수 있음
  - 대응: 컨트롤러 계약을 깨지 않는 선에서 서비스 접근 정책을 우선 수정하고 실패 응답 테스트를 추가
- 블로그가 코드보다 더 낙관적으로 설명돼 있는 부분이 있음
  - 대응: 각 배치마다 “무엇이 위험했고 어떻게 바꿨는지”를 블로그 본문과 결정 로그에 함께 반영

## 5) 서브에이전트 기반 작업 원칙
- `Faraday`: 보안/시크릿/management surface 검토 및 검증 포인트
- `Banach`: 서비스/API 권한 경계, 상태 전이, 동시성 검토 및 회귀 범위
- `Turing`: 구성/배포/CI/monitoring 문서와 실행 경로 정합성 검토
- `Lagrange`: 블로그/포트폴리오 문서의 메시지, 초보자 설명, 면접 답변 싱크 검토
- 각 배치 시작 전 관련 서브에이전트에게 다시 범위를 쪼개서 확인시키고, 코드/문서 변경 후에는 동일한 축으로 재검토한다.
