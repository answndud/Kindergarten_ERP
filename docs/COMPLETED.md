# COMPLETED.md

기준일: 2026-04-18

## 목적

- 이 문서는 완료된 작업의 archive입니다.
- raw snapshot을 그대로 붙여넣지 않고, 사람이 다시 읽기 쉬운 형식으로 정리합니다.
- 완료 archive는 작업 종료 시점의 `PROGRESS.md` 내용을 정리해 옮기는 것을 기본값으로 합니다.
- 문서는 시간 오름차순으로 유지합니다. 가장 최근에 끝난 작업이 가장 아래에 오도록 append합니다.

## archive 번호 규칙

- archive 번호는 이 문서에만 존재합니다.
- 새 항목을 추가할 때는 마지막 번호 다음 순번을 사용합니다.
- 번호는 append 순서 기준의 연속 번호로만 증가시킵니다.
- active 문서인 `docs/PLAN.md`, `docs/PROGRESS.md`는 archive 번호를 공유하지 않습니다.

<a id="archive-001"></a>
## `001` 초기 안정화와 API 계약 정리

- 완료일: `2026-02-21`
- 배경:
  - 초기 구현 단계에서 공지/승인/출석 API의 동작이 일부 하드코딩과 null-safe 부족 상태로 남아 있었고, README/CURRENT_FEATURES도 실제 코드와 드리프트가 있었다.
- 변경 내용:
  - 공지사항 작성 경로를 인증 사용자 기준으로 정리하고 서비스 유치원 경계를 보강했다.
  - `KidApplication`, `KindergartenApplication` 승인 처리의 null-safe와 후속 거절 대상을 정리했다.
  - 출석 필수 파라미터 누락 시 400 응답과 `ApiResponse.error` 계약을 맞췄다.
  - `PATCH` CORS 허용, 목록 API 필수 필터 검증, 입학 승인 관계값(`relationship`) 처리, README/CURRENT_FEATURES/API 표 정리를 함께 마감했다.
- 코드/문서:
  - 공지/출석/입학 신청 서비스와 API
  - `SecurityConfig`
  - `README.md`
  - `CURRENT_FEATURES.md`
- 검증:
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.AnnouncementApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.domain.kindergartenapplication.service.KindergartenApplicationServiceTest"`
  - 후속으로 classroom/kindergarten/kid application 계열 통합 테스트를 추가 실행했다.
- 결과:
  - 초기 API 계약과 권한 경계, 문서가 실제 구현 기준으로 다시 맞춰졌고 이후 고도화 배치를 올릴 기반이 정리됐다.

<a id="archive-002"></a>
## `002` 권한 경계, Testcontainers, CI, 인증/캘린더 고도화

- 완료일: `2026-03-13`
- 배경:
  - 핵심 기능은 갖춰졌지만 포트폴리오 관점에서 멀티테넌시 보안, 실환경 테스트, CI, 인증 lifecycle, 대시보드 지표, 캘린더 정합성이 모두 약한 상태였다.
- 변경 내용:
  - requester 기반 `AccessPolicyService`로 권한 경계를 하드닝하고 교차 유치원 접근 문제를 막았다.
  - H2/Mock Redis 대신 MySQL/Redis Testcontainers 기반 통합 테스트로 전환했다.
  - GitHub Actions CI를 도입하고 `fastTest`/`integrationTest` 분리, Node24 네이티브 action 전환까지 마감했다.
  - JWT refresh rotation, 세션 레지스트리, 활성 세션 제어, auth rate limit, trusted proxy 기준 client IP 해석, 로그인 정책 정교화를 반영했다.
  - 대시보드 지표 산식과 쿼리 수를 정리하고, 캘린더 반복 일정/학부모 접근 정합성을 보강했다.
- 코드/문서:
  - 권한 검증 계층과 auth/session 구성요소
  - Testcontainers 베이스 테스트와 API 통합 테스트
  - `build.gradle`
  - `.github/workflows/ci.yml`
  - dashboard/calendar 관련 서비스와 README
- 검증:
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test`
  - `./gradlew fastTest integrationTest`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.CalendarApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.performance.DashboardPerformanceStoryTest"`
  - GitHub Actions 원격 run 성공을 반복 확인했다.
- 결과:
  - 보안 경계, 테스트 현실성, CI 자동화, 인증 lifecycle, 캘린더 정합성이 한 단계 올라가며 백엔드 포트폴리오의 핵심 축이 만들어졌다.

<a id="archive-003"></a>
## `003` 감사 로그, 관측성, management plane, outbox, 운영형 워크플로우 패키지

- 완료일: `2026-03-20`
- 배경:
  - 인증 이후 단계에서는 운영 관점의 audit/export/alerting, management surface, 비동기 알림 전달, 승인 워크플로우, 업무 감사 추적이 필요했다.
- 변경 내용:
  - 인증 감사 로그 조회/export, anomaly alerting, monitoring overlay(Prometheus/Grafana), retention/archive를 반영했다.
  - prod management plane 하드닝과 활성 세션 조회/종료를 정리했다.
  - `notification_outbox`, retry/backoff/dead-letter, incident webhook fan-out을 도입했다.
  - 반 정원(capacity), waitlist/offer/accept/expire, 학부모 출결 변경 요청/승인, `domain_audit_log`를 묶어 운영형 워크플로우를 완성했다.
  - JUnit tag 기반 `fastTest`/`integrationTest`/`performanceSmokeTest`, readiness failure mode 검증, hiring-pack/demo/runbook 문서 패키징도 함께 마감했다.
- 코드/문서:
  - `authaudit`, `notification`, `attendance`, `kidapplication`, `domainaudit` 관련 도메인
  - `docker/docker-compose.monitoring.yml`
  - `.github/workflows/ci.yml`
  - 운영 콘솔/시연/채용 패키지 문서
- 검증:
  - `./gradlew compileJava compileTestJava`
  - `./gradlew fastTest`
  - `./gradlew integrationTest`
  - `./gradlew performanceSmokeTest`
  - `./gradlew test`
  - YAML/JSON parse, monitoring compose config, `git diff --check`
- 결과:
  - 인증 이후 운영성, 승인 워크플로우, 업무 감사 추적, readiness/performance smoke까지 이어지는 운영형 백엔드 패키지가 완성됐다.

<a id="archive-004"></a>
## `004` 블로그 품질 리라이트와 블로그 작업 공간 정리

- 완료일: `2026-03-25`
- 배경:
  - 블로그 시리즈가 길어지면서 초보자 재현성, 면접 대응력, 현재 코드 기준 정합성이 문서마다 들쭉날쭉해졌다.
- 변경 내용:
  - `blog/00_quality_checklist.md`, `blog/00_rebuild_guide.md`를 기준으로 블로그 작성 규칙을 정리했다.
  - `blog/07`~`26` 주요 글에 상황표, 현재 구현의 한계, 꼬리 질문, 산출물 체크리스트를 보강했다.
  - `BLOG_PLAN.md`, `BLOG_PROGRESS.md`, `blog/README.md`를 현재 작업 트리 기준으로 맞췄다.
  - 사용자가 삭제한 템플릿과 체크포인트 스크립트 흔적도 함께 정리했다.
- 코드/문서:
  - `blog/README.md`
  - `blog/00_rebuild_guide.md`
  - `blog/00_quality_checklist.md`
  - `blog/07`~`26`
  - `BLOG_PLAN.md`
  - `BLOG_PROGRESS.md`
- 검증:
  - 블로그/문서 드리프트 검색
  - `git diff --check`
  - 체크포인트 스크립트, fail-open 설명, 멀티 인스턴스 lock 전략 등 stale 표현 제거 확인
- 결과:
  - 블로그 작업 공간이 현재 코드 기준으로 정리됐고, 입문자 재현성과 취업용 설명력 모두 보강됐다.

<a id="archive-005"></a>
## `005` fail-closed defaults, Java 21 기준선, outbox atomic claim, active 문서 싱크

- 완료일: `2026-03-28`
- 배경:
  - 실행 기본값이 fail-open으로 읽힐 여지가 있었고, Java 기준선/CI/package-smoke/outbox 동시성/doc drift까지 한 번에 정리할 필요가 있었다.
- 변경 내용:
  - `application*.yml`, `SecurityConfig`, management surface 기본값을 fail-closed로 정리하고 seed를 explicit opt-in으로 바꿨다.
  - Gradle/CI 기준선을 Java 21로 올리고 workflow를 정리했다.
  - `AttendanceChangeRequest` DB 가드, 신청서 상세 권한, 대시보드 캐시 무효화 등 서비스/API 신뢰성을 보강했다.
  - `NotificationOutboxRepository`에 `FOR UPDATE SKIP LOCKED` 기반 claim query를 넣어 outbox atomic claim을 도입했다.
  - localhost bind 기본값, package-smoke, active 운영 문서와 블로그 드리프트를 현재 코드 기준으로 맞췄다.
- 코드/문서:
  - `application*.yml`
  - `SecurityConfig`
  - `build.gradle`
  - `.github/workflows/ci.yml`
  - `NotificationOutboxRepository`
  - `NotificationDispatchService`
  - `docker/docker-compose*.yml`
  - `README.md`, `AGENTS.md`, active 문서, 블로그 문서
- 검증:
  - `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew compileJava compileTestJava`
  - `./gradlew --no-daemon fastTest integrationTest`
  - `./gradlew --no-daemon bootJar`
  - compose config 검증
  - workflow YAML parse
  - `git diff --check`
- 결과:
  - Java 21 기준선, 안전한 기본 설정, packaging/CI 검증, outbox 동시성, 문서 정합성이 한 배치로 정리됐다.

<a id="archive-006"></a>
## `006` 배포 가이드/배포 자산, CI smoke fix, docs 기반 작업 운영 체계 재편

- 완료일: `2026-04-18`
- 배경:
  - 초보자용 배포 가이드와 실제 운영 자산이 필요했고, `docker/.env.example` 추적 문제로 package smoke가 깨졌다. 동시에 작업 추적 문서가 루트에 흩어져 있어 `docs/` 기반 active/archive 체계로 정리할 필요가 생겼다.
- 변경 내용:
  - `docs/guides/deployment-guide.md`, `Dockerfile`, `deploy/*`, `.github/workflows/cd.yml`을 추가해 배포 가이드와 배포 자산을 맞췄다.
  - `.gitignore`와 `docker/.env.example` 추적 상태를 바로잡아 CI smoke 실패 경로를 해결했다.
  - git 상태 기준으로 문서를 최신화한 뒤, 루트 `PLAN.md`, `PROGRESS.md`의 긴 이력을 이 문서로 요약 archive했다.
  - `docs/README.md`, `docs/PLAN.md`, `docs/PROGRESS.md`, `docs/COMPLETED.md`를 새 SSOT로 만들고 `AGENTS.md`, `README.md`, `CURRENT_FEATURES.md`, `blog/README.md`, `docs/guides/developer-guide.md` 참조를 모두 `docs/` 기준으로 재정리했다.
- 코드/문서:
  - `docs/guides/deployment-guide.md`
  - `Dockerfile`
  - `deploy/docker-compose.prod.yml`
  - `deploy/Caddyfile`
  - `deploy/.env.prod.example`
  - `.github/workflows/cd.yml`
  - `.gitignore`
  - `docker/.env.example`
  - `docs/README.md`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
  - `AGENTS.md`
- 검증:
  - `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --no-daemon bootJar`
  - `docker compose --env-file deploy/.env.prod -f deploy/docker-compose.prod.yml config`
  - `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config`
  - `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/cd.yml')"`
  - `git diff --check`
  - git 상태와 원격 동기화 재확인
- 결과:
  - 배포 패키지와 CI smoke fix를 마감했고, 이후 세션은 `docs/` 기반 active/archive 문서 체계만 보면 바로 이어서 작업할 수 있는 상태가 됐다.

<a id="archive-007"></a>
## `007` 블로그 역사 링크 정리와 로컬 docs 정리 마감

- 완료일: `2026-04-18`
- 배경:
  - 로컬 기준으로 `docs/archive`, `docs/decisions`, `docs/portfolio` 트리가 제거된 뒤에도 블로그 본문에는 그 경로를 직접 가리키는 역사 링크가 남아 있었다.
  - 이 상태로는 저장소 로컬 구조와 블로그 문서가 어긋나고, 링크가 깨진 채로 남았다.
- 변경 내용:
  - `blog/01`~`blog/26` 본문에 남아 있던 삭제된 docs 경로를 `docs/COMPLETED.md` archive anchor 기준으로 모두 치환했다.
  - 연속 중복으로 생긴 archive bullet을 정리하고, 깨진 Markdown 링크와 어색한 몇몇 문장을 수동 보정했다.
  - 로컬 기준에서 이미 제거된 `docs/archive`, `docs/decisions`, `docs/portfolio` 트리 삭제를 이번 배치 범위에 포함해 문서 구조와 git 상태를 일치시켰다.
  - 작업 완료 후 `docs/PLAN.md`, `docs/PROGRESS.md`를 다시 `현재 active 작업 없음` 상태로 비웠다.
- 코드/문서:
  - `blog/01-why-kindergarten-erp-domain.md`
  - `blog/02-gradle-spring-boot-bootstrap.md`
  - `blog/03-docker-mysql-redis-dev-environment.md`
  - `blog/04-application-yml-and-profile-strategy.md`
  - `blog/05-jpa-flyway-querydsl-redis-cache-foundation.md`
  - `blog/06-global-and-domain-package-structure.md`
  - `blog/07`~`blog/26`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
- 검증:
  - `rg -n "docs/(portfolio|decisions|archive)/|\\.\\./docs/(portfolio|decisions|archive)/|\\[docs/COMPLETED\\.md\\)|\\[docs/COMPLETED\\.md#archive-[0-9]+\\)" blog AGENTS.md README.md CURRENT_FEATURES.md docs`
  - `git diff --check`
  - `git status --short`
- 결과:
  - 로컬 기준 삭제된 docs 트리를 참조하는 블로그 역사 링크가 모두 정리됐고, 문서 구조와 저장소 상태를 한 번에 clean하게 마감할 수 있는 상태가 됐다.

<a id="archive-008"></a>
## `008` Impeccable repo-local 디자인 개선 준비

- 완료일: `2026-04-23`
- 배경:
  - 향후 디자인/프론트엔드 개선을 Impeccable 기준으로 체계화하되, 전역 Codex/Claude 설정, global npm install, 사용자 홈 설정은 건드리지 않는 repo-local 적용이 필요했다.
  - 기존 프론트엔드는 Thymeleaf + HTMX + Alpine.js + Tailwind CDN 구조이고, 화면별 Tailwind config와 card-heavy 운영 화면이 섞여 있어 먼저 컨텍스트와 detector 실행 경로를 잡는 것이 적절했다.
- 공식 문서 확인:
  - Impeccable README: Codex/skills/commands 및 `impeccable detect` CLI 사용 방식 확인
  - Impeccable getting started: `.impeccable.md` 기반 프로젝트 디자인 컨텍스트 흐름 확인
  - Impeccable HARNESSES: Codex의 repo-local `.agents/skills` 경로 확인
- 변경 내용:
  - 공식 Impeccable repo-local skill bundle을 `.agents/skills/*`에 추가했다.
  - 루트 `.impeccable.md`에 Kindergarten ERP의 사용자, 제품 톤, 화면 원칙, 피해야 할 패턴, 현재 design debt를 정리했다.
  - 전역 npm 설치 없이 detector를 실행하도록 `package.json`, `.npmrc`, `scripts/impeccable-detect.mjs`를 추가했다.
  - `.npmrc`와 스크립트 환경변수로 npm cache/log를 repo 내부 `.cache/npm`에 고정했다.
  - `AGENTS.md`, `docs/guides/developer-guide.md`에 repo-local Impeccable 사용 규칙과 금지 사항을 기록했다.
- 코드/문서:
  - `.agents/skills/*`
  - `.impeccable.md`
  - `.npmrc`
  - `package.json`
  - `scripts/impeccable-detect.mjs`
  - `AGENTS.md`
  - `docs/guides/developer-guide.md`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `node .agents/skills/impeccable/scripts/cleanup-deprecated.mjs`
    - 결과: deprecated Impeccable skill 없음
  - `npm run impeccable:detect -- --fast`
    - 결과: detector 실행 성공, 기존 화면에서 35개 anti-pattern 발견으로 exit 2
    - 주요 발견: Pretendard 단일 폰트 사용, 일부 flat type hierarchy, `text-gray-700` on `bg-yellow-100`
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
    - 참고: 기존 테스트 코드의 `@MockBean` removal warning 5건 발생
- 결과:
  - 전역 설정을 바꾸지 않고 Impeccable 기반 디자인 컨텍스트, skill bundle, detector 실행 경로가 repo 안에 준비됐다.
  - detector가 포착한 기존 UI 이슈는 다음 디자인 개선 작업의 출발점으로 사용할 수 있다.
