# COMPLETED.md

기준일: 2026-04-24

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

<a id="archive-009"></a>
## `009` Impeccable Phase 1 출결/출결 변경 요청 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 1 목표는 출결 등록, 월간 리포트, 출결 변경 요청 검토 흐름을 한 화면군으로 묶어 모바일 조작성과 정보 위계를 먼저 개선하는 것이었다.
  - 기존 화면은 날짜/반/빠른 등록이 별도 surface로 나뉘고, 모바일 카드 내부 입력과 버튼이 작으며, 변경 요청 화면에는 운영자가 바로 쓰지 않는 설명 surface가 검토 액션보다 먼저 보이는 문제가 있었다.
- 사용 skill:
  - `$impeccable`: repo-local 디자인 컨텍스트 확인
  - `$critique`: 출결 화면군의 hierarchy, nested surface, 모바일 리스크 재평가
  - `$adapt`: 390px 모바일 터치 타깃과 카드/테이블 전환 보강
  - `$layout`: 필터, 빠른 등록, 목록, 검토 큐의 배치 정리
  - `$clarify`: empty/error/loading 문구와 상태 문구 개선
  - `$polish`: rounding/shadow/focus/detail 정리
- 변경 내용:
  - `/attendance`에서 날짜, 반, 빠른 등록을 하나의 업무 필터 영역으로 합치고, 모바일 액션 버튼과 입력 높이를 44px 기준에 맞췄다.
  - `/attendance` 모바일 저장 버튼이 기존에는 `tr`만 찾도록 되어 카드 레이아웃에서 동작하지 않던 문제를 `data-attendance-row` 기반 탐색으로 보정했다.
  - `/attendance/monthly`는 desktop table 밀도를 유지하되 mobile에서는 카드/요약 구조와 unframed empty state를 제공하도록 정리했다.
  - `/attendance-requests`는 과한 3xl/shadow surface, KPI 카드 반복, 내부 구현 설명 카드를 줄이고 운영자 검토 큐와 처리 기준 중심으로 재구성했다.
  - 동적 렌더링 텍스트는 `escapeHtml`을 적용해 출결/요청 화면의 HTML 주입 리스크를 줄였다.
- 코드/문서:
  - `src/main/resources/templates/attendance/attendance.html`
  - `src/main/resources/templates/attendance/monthly.html`
  - `src/main/resources/templates/attendance/requests.html`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/attendance`
    - 결과: exit 2, Phase 1 범위 잔여 3건
    - 잔여: `single-font` 3건, 기존 Pretendard 단일 폰트 정책 경고
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 35건
    - 잔여: Phase 1 밖의 `single-font`, `flat-type-hierarchy`, `gray-on-color` 이슈 포함
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*Attendance*"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
  - Chrome headless screenshot
    - 결과: `/attendance`, `/attendance/monthly`, `/attendance-requests` desktop/mobile에서 page overflow 0건 확인
    - screenshot: `/tmp/erp-phase1-screens/attendance-desktop.png`, `/tmp/erp-phase1-screens/attendance-mobile.png`, `/tmp/erp-phase1-screens/monthly-desktop.png`, `/tmp/erp-phase1-screens/monthly-mobile.png`, `/tmp/erp-phase1-screens/requests-desktop.png`, `/tmp/erp-phase1-screens/requests-mobile.png`
- 남은 리스크:
  - 공통 header/nav의 40px 터치 타깃과 Pretendard 단일 폰트 경고는 Phase 1 화면군 밖 공통 디자인 시스템 범위로 남겼다.
  - desktop 출결 table의 40px row control은 운영 밀도와 기존 패턴을 우선해 유지했다.
- 결과:
  - 출결 화면군은 API/도메인 계약 변경 없이 모바일 조작성, empty/error 안내, surface 밀도가 개선됐다.
  - 완료된 Phase 1은 active plan에서 제거했고 다음 active 작업은 Phase 2 신청 처리 흐름으로 넘겼다.

<a id="archive-010"></a>
## `010` Impeccable Phase 2 입학/소속 신청 처리 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 2 목표는 `/applications/pending`, `/kindergarten/create`, `/kindergarten/select`를 한 흐름으로 보고 신청 상태, empty state, 승인/반려/취소 action을 더 명확하게 만드는 것이었다.
  - 기존 신청 화면은 status/action hierarchy가 약하고, form control 높이와 모바일 touch target이 화면마다 달랐다. 또한 신청 처리 버튼이 호출하던 일부 frontend handler가 실제로 정의되어 있지 않아 approval/cancel/reject interaction이 끊길 수 있었다.
- 사용 skill:
  - `$impeccable`: repo-local 디자인 컨텍스트 확인
  - `$critique`: 신청 상태와 사용자별 판단 지점 재평가
  - `$layout`: 신청/유치원 선택 카드와 form surface 정리
  - `$clarify`: 승인, 반려, 취소, empty state 문구 개선
  - `$adapt`: 모바일 form/list touch target 보강
  - `$polish`: rounding, focus, detector 잔여 이슈 정리
- 변경 내용:
  - `/applications/pending`의 loading/status/empty state를 운영 큐 중심으로 재구성하고, 내부 surface를 줄여 현재 상태와 pending count가 먼저 읽히도록 정리했다.
  - 교사/학부모 신청 form과 원장 승인 목록의 input/button 높이를 44px 이상으로 맞추고, status badge와 action button hierarchy를 통일했다.
  - `/kindergarten/create`는 원장 초기 설정 form으로 보이도록 header copy, label, focus ring, submit button, mobile spacing을 정리했다.
  - `/kindergarten/select`는 큰 wrapper card를 제거하고 각 유치원을 독립적인 선택 항목으로 만들었으며, loading/empty/error copy와 HTML escaping을 보강했다.
  - `Applications.loadKindergartens`, `Applications.loadClassroomsByKindergarten`, 승인/반려/취소 UI handler를 추가해 template button과 실제 API 호출을 다시 연결했다.
  - `UI.confirm`이 SweetAlert 취소를 true로 처리할 수 있던 문제를 `result.isConfirmed === true` 기준으로 수정했다.
- 기능/계약 판단:
  - 도메인 상태, API URL, payload 계약은 변경하지 않았다.
  - frontend handler 복구와 `UI.confirm` 수정은 Phase 2 화면의 기존 버튼을 정상 동작시키기 위한 최소 기능 수정으로 제한했다.
- 코드/문서:
  - `src/main/resources/templates/applications/pending.html`
  - `src/main/resources/templates/applications/fragments/pending-content.html`
  - `src/main/resources/templates/kindergarten/create.html`
  - `src/main/resources/templates/kindergarten/select.html`
  - `src/main/resources/static/js/app.js`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/applications src/main/resources/templates/kindergarten`
    - 결과: exit 2, Phase 2 범위 잔여 3건
    - 잔여: `single-font` 3건, 기존 Pretendard 단일 폰트 정책 경고
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 34건
    - 참고: 전체 detector findings가 35건에서 34건으로 감소
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*Application*"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
  - Chrome headless screenshot
    - 결과: `/applications/pending`, `/kindergarten/create`, `/kindergarten/select` desktop/mobile에서 page overflow 0건, JS exception 0건 확인
    - screenshot: `/tmp/erp-phase2-screens/applications-pending-desktop.png`, `/tmp/erp-phase2-screens/applications-pending-mobile.png`, `/tmp/erp-phase2-screens/kindergarten-create-desktop.png`, `/tmp/erp-phase2-screens/kindergarten-create-mobile.png`, `/tmp/erp-phase2-screens/kindergarten-select-desktop.png`, `/tmp/erp-phase2-screens/kindergarten-select-mobile.png`
- 남은 리스크:
  - Phase 2 범위의 `single-font` detector 경고는 공통 typography/system phase로 남겼다.
  - Tailwind CDN production warning과 공통 header/logo touch target은 이번 화면군 밖 공통 layout/system 범위다.
  - 실제 승인/반려 API의 권한/상태 전이는 기존 통합 테스트와 API 계약에 의존했고, 브라우저에서 destructive action은 실행하지 않았다.
- 결과:
  - 신청 처리 화면군은 API/도메인 계약 변경 없이 status hierarchy, mobile touch target, empty/error 안내, 주요 action 연결성이 개선됐다.
  - 완료된 Phase 2는 active plan에서 제거했고 다음 active 작업은 Phase 3 원아/반 관리 흐름으로 넘겼다.

<a id="archive-011"></a>
## `011` Impeccable Phase 3 원아/반 관리 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 3 목표는 원아 목록, 원아 상세/등록/수정, 반 관리 화면을 한 화면군으로 보고 원장/교사가 이름, 반, 보호자 연결, 위험 액션을 빠르게 스캔하게 만드는 것이었다.
  - 기존 `/kids` 목록은 큰 section 안에 다시 row card가 반복되고, 모바일에서 action button이 가로로 밀집했다. `/classrooms`는 detector가 flat hierarchy를 잡고 있었고, form/button touch target도 화면마다 달랐다.
- 사용 skill:
  - `$impeccable`: repo-local 디자인 컨텍스트 확인
  - `$critique`: 원아/반 목록, 상세, 편집 흐름의 사용성 평가
  - `$layout`: 목록 row, filter, form, 상세 정보 hierarchy 재배치
  - `$adapt`: desktop-first 확인 후 모바일 action grouping과 44px touch target 보강
  - `$clarify`: empty state, 위험 액션, 보호자 연결 copy 개선
  - `$polish`: rounding, focus, dynamic HTML escaping, detector 잔여 이슈 정리
- 변경 내용:
  - `/kids`의 검색/목록 section을 `rounded-xl` 기반의 더 낮은 surface로 정리하고, 목록 내부 row card 반복을 divider 기반 행 구조로 바꿨다.
  - `/kids` 목록 action을 desktop에서는 오른쪽 스캔형 그룹, mobile에서는 2열 action grid로 정리해 touch target을 44px 이상으로 맞췄다.
  - `/kids/{id}`는 원생 이름을 1차 제목으로 올리고, 기본 정보/학부모 정보를 더 명확한 label-value 구조로 정리했다.
  - `/kids/new`, `/kids/{id}/edit`는 Tailwind primary config를 명시하고, input/select/button 높이와 focus ring, 제출/취소 순서를 정리했다.
  - `/classrooms`는 heading scale과 section copy를 조정해 detector `flat-type-hierarchy`를 해소하고, 반 목록을 nested card 대신 divider row로 표시했다.
  - kid/classroom 동적 렌더링에 `escapeHtml` 계열 helper를 적용해 원아/반/보호자 이름의 HTML 주입 리스크를 줄였다.
  - SweetAlert 기반 원아/보호자 modal의 input/select/button touch target과 confirm/cancel button class를 정리했다.
  - 사용자 피드백에 따라 공통 page background의 green-tinted radial wash를 제거하고 white-first 배경 기준으로 맞췄다.
- 기능/계약 판단:
  - 원아/반 API URL, request/response payload, 권한 정책, 삭제/보호자 연결 도메인 로직은 변경하지 않았다.
  - 동적 HTML escaping과 modal button class 조정은 기존 UI 동작을 유지하면서 표시 안정성과 조작성만 높이는 frontend 한정 변경이다.
- 코드/문서:
  - `src/main/resources/templates/kid/kids.html`
  - `src/main/resources/templates/kid/kid-detail.html`
  - `src/main/resources/templates/kid/kid-form.html`
  - `src/main/resources/templates/classroom/classrooms.html`
  - `src/main/resources/static/css/custom.css`
  - `.impeccable.md`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/kid src/main/resources/templates/classroom`
    - 결과: exit 2, Phase 3 범위 잔여 4건
    - 잔여: `single-font` 4건, 기존 Pretendard 단일 폰트 정책 경고
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 33건
    - 참고: 전체 detector findings가 34건에서 33건으로 감소
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*Kid*" --tests "*Classroom*"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
  - Chrome headless screenshot
    - 결과: `/kids`, `/kids/{id}`, `/kids/{id}/edit`, `/kids/new`, `/classrooms` desktop/mobile에서 page overflow 0건, JS exception 0건 확인
    - desktop-first screenshot: `/tmp/erp-phase3-screens/kids-desktop.png`, `/tmp/erp-phase3-screens/kid-detail-desktop.png`, `/tmp/erp-phase3-screens/kid-edit-desktop.png`, `/tmp/erp-phase3-screens/kids-new-desktop.png`, `/tmp/erp-phase3-screens/classrooms-desktop.png`
    - mobile 보조 screenshot: `/tmp/erp-phase3-screens/kids-mobile.png`, `/tmp/erp-phase3-screens/kid-detail-mobile.png`, `/tmp/erp-phase3-screens/kid-edit-mobile.png`, `/tmp/erp-phase3-screens/kids-new-mobile.png`, `/tmp/erp-phase3-screens/classrooms-mobile.png`
  - White background 재확인
    - 결과: `/kids`, `/classrooms` desktop에서 body background `rgb(255, 255, 255)`, background image `none`, overflow 0건, JS exception 0건
    - screenshot: `/tmp/erp-phase3-screens/kids-white-desktop.png`, `/tmp/erp-phase3-screens/classrooms-white-desktop.png`
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
    - 참고: white background 조정 후 재실행
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
    - 참고: white background 조정 후 재실행
- 남은 리스크:
  - Phase 3 범위의 `single-font` detector 경고는 공통 typography/system phase로 남겼다.
  - 공통 header/logo touch target과 Tailwind CDN production warning은 이번 화면군 밖 공통 layout/system 범위다.
  - 보호자 연결/해제 같은 destructive action은 브라우저에서 실제 실행하지 않고 기존 API 테스트와 UI 렌더링 검증에 의존했다.
- 결과:
  - 원아/반 화면군은 API/도메인 계약 변경 없이 desktop scanability, 모바일 action grouping, form touch target, nested card 밀도, white-first page background가 개선됐다.
  - 완료된 Phase 3은 active plan에서 제거했고 다음 active 작업은 Phase 4 커뮤니케이션 흐름으로 넘겼다.

<a id="archive-012"></a>
## `012` Impeccable Phase 4 알림장/공지/캘린더 커뮤니케이션 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 4 목표는 교사/원장이 작성하고 학부모가 읽는 알림장, 공지, 캘린더 화면군의 읽기/작성 hierarchy와 모바일 조작성을 정리하는 것이었다.
  - 기존 화면은 녹색 그라디언트, 큰 `rounded-2xl` surface, shadow, weak heading scale이 반복됐고, 캘린더는 초기 HTMX 목록 호출이 날짜 파라미터 없이 발생해 `/calendar/list` 500이 날 수 있었다.
- 사용 skill:
  - `$impeccable`: repo-local white-first 디자인 컨텍스트 확인
  - `$critique`: 커뮤니케이션 읽기/작성 흐름과 hierarchy 평가
  - `$typeset`: 제목, 메타 정보, 본문 preview의 scale 정리
  - `$layout`: 목록, 상세, 작성 form surface와 spacing 정리
  - `$clarify`: empty state, 작성/수정/삭제 copy와 필터 label 정리
  - `$adapt`: desktop-first 확인 후 모바일 form/select/button touch target 보강
  - `$polish`: detector `gray-on-color`, `flat-type-hierarchy`, rounding, modal control 정리
- 변경 내용:
  - `/notepad`, `/announcements`, `/calendar`의 page header를 3xl heading과 짧은 scope label로 통일하고, green gradient/shadow 기반 CTA를 `bg-primary-600` 단일 액션으로 정리했다.
  - 알림장/공지 목록 fragment의 empty state와 list item heading scale을 키워 detector `flat-type-hierarchy`를 해소했다.
  - 알림장/공지 작성/수정/상세 화면의 card radius를 낮추고 form input, textarea, submit/cancel button을 44px touch target 기준에 맞췄다.
  - SweetAlert 기반 알림장/캘린더 작성 modal의 input/select/button 크기와 `rounded-xl` 기준을 맞췄다.
  - 공지 중요 필터/중요 badge의 `gray-on-color` 조합을 `text-yellow-800` 기준으로 정리했다.
  - 캘린더의 decorative blurred background/orb를 제거하고, white-first page background를 유지했다.
  - 캘린더 HTMX list filter input/select에 `name` 속성을 추가하고 초기 `load` trigger를 제거해, JS가 기본 기간을 설정한 뒤 `calendar-filters-changed` 이벤트로 `/calendar/list`를 호출하게 했다.
- 기능/계약 판단:
  - 알림장, 공지, 캘린더 API URL, request/response payload, 권한 정책, 반복 일정 도메인 로직은 변경하지 않았다.
  - 캘린더 `name` 속성 추가와 초기 HTMX trigger 조정은 기존 controller가 요구하는 `startDate/endDate` request parameter를 정상 전송하기 위한 템플릿 계약 보정이다.
- 코드/문서:
  - `src/main/resources/templates/notepad/notepad.html`
  - `src/main/resources/templates/notepad/fragments/list.html`
  - `src/main/resources/templates/notepad/write.html`
  - `src/main/resources/templates/notepad/edit.html`
  - `src/main/resources/templates/notepad/detail.html`
  - `src/main/resources/templates/announcement/announcements.html`
  - `src/main/resources/templates/announcement/fragments/list.html`
  - `src/main/resources/templates/announcement/write.html`
  - `src/main/resources/templates/announcement/edit.html`
  - `src/main/resources/templates/announcement/detail.html`
  - `src/main/resources/templates/calendar/calendar.html`
  - `src/main/resources/templates/calendar/fragments/list.html`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/notepad src/main/resources/templates/announcement src/main/resources/templates/calendar`
    - 결과: exit 2, Phase 4 범위 잔여 9건
    - 잔여: `single-font` 9건, 기존 Pretendard 단일 폰트 정책 경고
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 29건
    - 참고: 전체 detector findings가 Phase 3 완료 후 33건에서 29건으로 감소
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*Notepad*" --tests "*Announcement*" --tests "*Calendar*"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
  - Chrome headless screenshot
    - 결과: `/notepad`, `/notepad/write`, `/notepad/{id}`, `/notepad/{id}/edit`, `/announcements`, `/announcement/write`, `/announcement/{id}`, `/announcement/{id}/edit`, `/calendar` desktop-first 및 mobile 보조 캡처에서 page overflow 0건 확인
    - desktop-first screenshot: `/tmp/erp-phase4-screens/notepad-desktop.png`, `/tmp/erp-phase4-screens/notepad-write-desktop.png`, `/tmp/erp-phase4-screens/notepad-detail-desktop.png`, `/tmp/erp-phase4-screens/notepad-edit-desktop.png`, `/tmp/erp-phase4-screens/announcements-desktop.png`, `/tmp/erp-phase4-screens/announcement-write-desktop.png`, `/tmp/erp-phase4-screens/announcement-detail-desktop.png`, `/tmp/erp-phase4-screens/announcement-edit-desktop.png`, `/tmp/erp-phase4-screens/calendar-desktop.png`
    - mobile 보조 screenshot: `/tmp/erp-phase4-screens/notepad-mobile.png`, `/tmp/erp-phase4-screens/announcements-mobile.png`, `/tmp/erp-phase4-screens/calendar-mobile.png`
    - 확인: body background `rgb(255, 255, 255)`, background image `none`, overflow 0건
    - 캘린더 재확인: 서버 로그에서 `/calendar/list?startDate=...&endDate=...`가 200으로 응답
- 남은 리스크:
  - Phase 4 범위의 `single-font` detector 경고는 기존 Pretendard 단일 폰트 정책을 유지해 공통 typography/system phase로 남겼다.
  - Chrome headless console에는 기존 `/favicon.ico` 404가 남아 있다. Phase 4 화면 기능/UI와 직접 관련 없는 공통 static asset 이슈로 별도 처리한다.
  - 실제 삭제/중요 토글 같은 destructive action은 브라우저에서 실행하지 않고 기존 API 테스트와 UI 렌더링 검증에 의존했다.
- 결과:
  - 커뮤니케이션 화면군은 API/도메인 계약 변경 없이 desktop scanability, 작성 form 조작성, modal touch target, detector color/hierarchy 이슈, white-first page background가 개선됐다.
  - 완료된 Phase 4는 active plan에서 제거했고 다음 active 작업은 Phase 5 원장 대시보드/감사 로그 흐름으로 넘겼다.

<a id="archive-013"></a>
## `013` Impeccable Phase 5 원장 대시보드/감사 로그 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 5 목표는 `/dashboard`, `/audit-logs`, `/domain-audit-logs`, `/notifications`를 원장 운영 판단 흐름으로 보고 KPI, 감사 로그, 알림 상태를 더 빠르게 스캔하게 만드는 것이었다.
  - 기존 화면은 큰 카드, shadow, 넓은 표면, 약한 heading scale이 반복됐고, audit table 일부 badge/header가 좁은 폭에서 줄바꿈되어 운영 로그의 밀도가 흔들렸다.
  - 사용자 피드백에 따라 데스크톱 웹 캡처를 1차 기준으로 삼고, white-first 배경 원칙을 다시 확인했다.
- 사용 skill:
  - `$impeccable`: repo-local white-first 디자인 컨텍스트 확인
  - `$critique`: dashboard/audit/notification의 원장 판단 흐름과 density 평가
  - `$distill`: KPI card, filter card, notification surface의 과한 shadow/rounding 제거
  - `$layout`: dashboard KPI, audit filter/table, notification filter chip 배치 정리
  - `$adapt`: desktop-first 확인 후 mobile touch target과 table/filter 조작성 확인
  - `$clarify`: audit/log/filter/empty state copy 정리
  - `$polish`: footer target, audit nowrap, screenshot metric 잔여 이슈 정리
- 변경 내용:
  - `/dashboard`의 header를 운영 콘솔 맥락으로 정리하고 KPI card의 장식 아이콘 box를 제거해 label, value, scope badge 중심으로 단순화했다.
  - dashboard/chart card의 shadow와 과한 surface감을 줄이고 white-first page background를 유지했다.
  - `/audit-logs`, `/domain-audit-logs`의 filter panel, summary card, table container를 `rounded-xl`/border 중심으로 낮추고, input/select/button 높이를 44px 기준에 맞췄다.
  - audit table header와 주요 badge/client fields에 `whitespace-nowrap`를 적용해 desktop에서 header가 세로로 쪼개지는 문제를 줄였다.
  - `/notifications`의 page header, filter chip, empty state, action button touch target을 정리해 알림 유형과 미읽음 상태가 먼저 보이게 했다.
  - 공통 footer 링크를 `min-h-8` click target으로 보정하고, footer logo holder의 green gradient surface를 white border surface로 바꿨다.
  - Playwright 브라우저 런타임은 전역 설치 없이 repo-local `.cache/playwright-browsers`에 설치했고 `.gitignore`의 `.cache/` 규칙으로 커밋 대상에서 제외됨을 확인했다.
- 기능/계약 판단:
  - dashboard API 통계 계약, audit retention/security policy, notification API, logging schema는 변경하지 않았다.
  - audit table nowrap, footer link target, notification filter layout은 frontend 표시/조작성 보정으로 제한했다.
- 코드/문서:
  - `src/main/resources/templates/dashboard/dashboard.html`
  - `src/main/resources/templates/authaudit/audit-logs.html`
  - `src/main/resources/templates/domainaudit/audit-logs.html`
  - `src/main/resources/templates/notifications/index.html`
  - `src/main/resources/templates/notifications/fragments/list.html`
  - `src/main/resources/templates/fragments/footer.html`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/dashboard src/main/resources/templates/authaudit src/main/resources/templates/domainaudit src/main/resources/templates/notifications`
    - 결과: exit 2, Phase 5 범위 잔여 3건
    - 잔여: `single-font` 3건, 기존 Pretendard 단일 폰트 정책 경고
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 29건
    - 참고: 전체 detector findings는 Phase 4 완료 후와 동일하게 29건 유지
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*DashboardApiIntegrationTest" --tests "*AuthAuditApiIntegrationTest" --tests "*DomainAuditApiIntegrationTest" --tests "*NotificationApiIntegrationTest" --tests "*AuditConsolePerformanceSmokeTest"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
  - Chrome headless screenshot
    - 결과: `/dashboard`, `/audit-logs`, `/domain-audit-logs`, `/notifications` desktop-first 및 mobile 보조 캡처에서 page overflow 0건, JS error 0건, 32px 미만 클릭 타깃 0건 확인
    - desktop-first screenshot: `/tmp/erp-phase5-screens/dashboard-desktop.png`, `/tmp/erp-phase5-screens/audit-desktop.png`, `/tmp/erp-phase5-screens/domain-audit-desktop.png`, `/tmp/erp-phase5-screens/notifications-desktop.png`
    - mobile 보조 screenshot: `/tmp/erp-phase5-screens/dashboard-mobile.png`, `/tmp/erp-phase5-screens/audit-mobile.png`, `/tmp/erp-phase5-screens/domain-audit-mobile.png`, `/tmp/erp-phase5-screens/notifications-mobile.png`
    - 확인: body background `rgb(255, 255, 255)`, background image `none`, overflow 0건
- 남은 리스크:
  - Phase 5 범위의 `single-font` detector 경고는 기존 Pretendard 단일 폰트 정책을 유지해 공통 typography/system phase로 남겼다.
  - audit table은 desktop density를 우선해 내부 horizontal scroll 가능 구조를 유지했다. 컬럼 축소나 server-side column preset은 별도 기능 범위다.
  - 실제 notification read/delete 같은 상태 변경 action은 브라우저에서 실행하지 않고 기존 API 테스트와 렌더링 검증에 의존했다.
- 결과:
  - 원장 대시보드/감사 로그 화면군은 API/도메인 계약 변경 없이 desktop scanability, table readability, filter 조작성, notification empty/filter state, white-first page background가 개선됐다.
  - 완료된 Phase 5는 active plan에서 제거했고 다음 active 작업은 Phase 6 인증/계정 설정 흐름으로 넘겼다.

<a id="archive-014"></a>
## `014` Impeccable Phase 6 인증/계정 설정 흐름 개선

- 완료일: `2026-04-23`
- 배경:
  - Phase 6 목표는 `/login`, `/signup`, `/profile`, `/settings`를 인증/계정 trust surface로 보고, generic auth card와 과한 surface를 줄이며 비밀번호, 소셜 연결, 세션 관리의 위험도를 명확히 정리하는 것이었다.
  - baseline detector는 auth 범위에서 `single-font` 4건을 보고했고, 로그인/회원가입은 큰 카드와 중복 font override, 설정 화면은 긴 세션 목록과 active처럼 보이는 미연결 알림 checkbox가 문제였다.
  - 사용자 피드백에 따라 desktop-first screenshot을 1차 기준으로 삼고, 흰색 배경을 유지하며 green은 action/status accent로만 사용했다.
- 사용 skill:
  - `$impeccable`: repo-local 디자인 컨텍스트와 white-first 원칙 확인
  - `$critique`: auth/account trust flow, 과한 surface, fake affordance 평가
  - `$clarify`: password hint, social/session/security copy 정리
  - `$adapt`: signup role selector, social buttons, checkbox/touch target 보강
  - `$distill`: login/signup gradient, settings nested card/shadow 축소
  - `$layout`: profile/settings 계정 정보, 보안, 소셜, 세션 영역 재배치
  - `$polish`: detector 잔여 font override, focus/touch target, screenshot metric 정리
- 변경 내용:
  - `/login`과 `/signup`의 logo/auth card에서 gradient, shadow, `rounded-2xl` 중심 스타일을 제거하고 white border surface와 명확한 h1/copy로 정리했다.
  - 로그인 화면의 미구현 `비밀번호 찾기` 링크와 서버 계약에 연결되지 않은 `로그인 상태 유지` checkbox를 제거했다.
  - 회원가입 role selector를 mobile-first 한 열 배치와 80px 이상 선택 영역으로 바꾸고, 약관 영역의 `href="#"` 링크를 제거해 실제 이동 없는 affordance를 없앴다.
  - auth 템플릿의 Pretendard-only inline font override와 개별 Google font link를 제거하고 `custom.css`의 repo-local font pair/design system을 상속하게 했다.
  - `/profile`의 green gradient hero를 white-first profile header로 바꾸고, avatar, role badge, action button을 낮은 surface로 정리했다.
  - `/settings`를 `max-w-6xl` grid로 넓히고 account, operation, social, password, session, notification, app info 영역을 shadow 없는 border surface로 정리했다.
  - 활성 세션 목록은 많은 세션이 있어도 하단 설정이 밀리지 않도록 `max-h-[34rem]` 내부 스크롤 영역으로 제한했다.
  - 알림 설정 checkbox는 저장 API가 없는 기존 표시용 컨트롤이므로 disabled 상태와 기본 정책 copy로 바꿔 사용자가 즉시 변경 가능한 설정처럼 보이지 않게 했다.
  - 공통 `custom.css`의 body/page background를 흰색으로 정리해 green radial/gradient 배경을 제거했다.
- 기능/계약 판단:
  - JWT, OAuth2, 비밀번호 변경, 세션 revoke API 계약은 변경하지 않았다.
  - 로그인 remember-me와 forgot-password는 실제 동작 경로가 없던 view affordance만 제거했다.
  - 알림 preference 저장 기능은 새로 만들지 않고, 기존 미연결 checkbox를 disabled 표시로 제한했다.
- 코드/문서:
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/auth/profile.html`
  - `src/main/resources/templates/auth/settings.html`
  - `src/main/resources/static/css/custom.css`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `npm run impeccable:detect -- --fast src/main/resources/templates/auth`
    - baseline 결과: exit 2, `single-font` 4건
    - final 결과: exit 0, auth 범위 detector findings 0건
  - `npm run impeccable:detect -- --fast`
    - 결과: exit 2, 전체 잔여 25건
    - 잔여: auth 범위는 0건, 나머지는 다른 화면/공통 layout의 `single-font` 및 `flat-type-hierarchy`
  - Chrome headless screenshot
    - 결과: `/login`, `/signup`, `/profile`, `/settings` desktop-first, mobile, narrow 캡처에서 body background `rgb(255, 255, 255)`, background image `none`, page overflow 0건, 32px 미만 액션 타깃 0건, JS console/page error 0건 확인
    - desktop-first screenshot: `/tmp/erp-phase6-screens/login-desktop.png`, `/tmp/erp-phase6-screens/signup-desktop.png`, `/tmp/erp-phase6-screens/profile-desktop.png`, `/tmp/erp-phase6-screens/settings-desktop.png`
    - mobile 보조 screenshot: `/tmp/erp-phase6-screens/login-mobile.png`, `/tmp/erp-phase6-screens/signup-mobile.png`, `/tmp/erp-phase6-screens/profile-mobile.png`, `/tmp/erp-phase6-screens/settings-mobile.png`
    - narrow 보조 screenshot: `/tmp/erp-phase6-screens/login-narrow.png`, `/tmp/erp-phase6-screens/signup-narrow.png`, `/tmp/erp-phase6-screens/profile-narrow.png`, `/tmp/erp-phase6-screens/settings-narrow.png`
    - settings 재확인: disabled notification checkbox 3개, page overflow 0건, 32px 미만 액션 타깃 0건
  - `./gradlew compileJava compileTestJava`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew test --tests "*Auth*" --tests "*Member*" --tests "*ViewEndpointTest*"`
    - 결과: BUILD SUCCESSFUL
  - `./gradlew build -x test`
    - 결과: BUILD SUCCESSFUL
- 남은 리스크:
  - 전체 detector는 25건이 남아 있다. Phase 6 범위 밖의 다른 화면 또는 공통 layout typography 항목이므로 이번 phase에서는 변경하지 않았다.
  - 소셜 로그인 연결/해제와 세션 종료 버튼의 실제 destructive action은 브라우저에서 클릭하지 않고 기존 API 테스트와 렌더링 검증에 의존했다.
  - 로컬 screenshot 과정에서 demo principal 계정의 active session row가 많이 늘어났다. UI는 내부 스크롤로 처리하지만 세션 정리 정책/API는 이번 phase 범위에서 바꾸지 않았다.
  - 알림 preference 저장 기능은 아직 구현하지 않았다. 이 phase에서는 미연결 checkbox를 disabled 상태로 표시해 오해만 줄였다.
- 결과:
  - 인증/계정 설정 흐름은 API/보안 계약 변경 없이 white-first 배경, 명확한 trust hierarchy, 작은 액션 타깃 제거, detector 0건, settings density 개선을 완료했다.
  - Phase 1부터 Phase 6까지 계획된 Impeccable 디자인 개선 workflow가 archive됐고 active plan/progress는 `현재 active 작업 없음`으로 비웠다.

<a id="archive-015"></a>
## `015` README 포트폴리오 피치 강화

- 완료일: `2026-04-24`
- 배경:
  - ERP README는 백엔드 깊이와 기능 범위는 충분히 보였지만, Altteulmap README와 비교하면 첫인상 기준의 `왜 이 저장소를 봐야 하는가`, `서비스가 실제로 어떻게 닫히는가`, `현재 상태` 전달력이 상대적으로 약했다.
  - 상단에 빠른 증거 링크와 서비스 흐름 요약이 부족해 면접관이 README만 보고 30초 안에 판단하기에는 설명 순서가 다소 기술 요약서에 가까웠다.
- 변경 내용:
  - README 상단에 `바로 확인할 것` 섹션을 추가해 데모 실행, 성능 수치, 화면, archive 문서로 빠르게 이동할 수 있게 했다.
  - 기존 소개 문단을 `왜 이 저장소를 열어볼 만한가` 관점으로 다듬어 서비스/엔지니어링/운영 관점의 읽을 이유가 더 빨리 드러나도록 바꿨다.
  - `서비스가 실제로 어떻게 닫히는가` 4단계 흐름을 추가해 학부모 요청, 교사/원장 운영, 감사 로그, 운영 대시보드까지의 서비스 loop를 한 번에 설명하도록 정리했다.
  - `현재 상태` 표를 추가해 MVP 완성 범위, demo 가능 여부, 검증 체계, 운영 기능, 배포 자산, active 작업 부재 상태를 README 안에서 바로 확인할 수 있게 했다.
- 코드/문서:
  - `README.md`
  - `docs/COMPLETED.md`
- 검증:
  - Altteulmap README와 비교 기준 점검
  - `git diff --check`
  - README line review로 현재 저장소 구조/문서 경로/실행 경로 정합성 확인
- 결과:
  - ERP README는 기능 나열형 요약에서 한 단계 나아가, 면접관이 상단에서 `읽을 이유`, `서비스 loop`, `현재 상태`, `검증 근거`를 더 빠르게 파악할 수 있는 포트폴리오 피치 문서에 가까워졌다.

<a id="archive-016"></a>
## `016` CD workflow 수동 실행 전환

- 완료일: `2026-04-24`
- 배경:
  - 클라우드 배포를 아직 하지 않는 상태인데도 `main` push마다 `Backend CD`가 실행되어 GitHub Actions에 실패 상태가 표시됐다.
  - 확인한 실패 run은 `Backend CD / Build And Deploy`의 `Validate deployment secrets` 단계에서 발생했으며, 원인은 `DEPLOY_SSH_KEY`, `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_PATH`, `GHCR_USERNAME`, `GHCR_READ_TOKEN` 같은 배포 secret이 설정되지 않은 상태였다.
- 변경 내용:
  - `.github/workflows/cd.yml`에서 `push` trigger를 제거하고 `workflow_dispatch` 수동 실행만 남겼다.
  - 배포 가이드의 `main push 자동 배포` 설명을 현재 저장소 상태에 맞게 `수동 CD` 기준으로 정리했다.
  - 클라우드 서버와 repository secret을 준비하기 전에는 `main` push 때 CD가 실패로 표시되지 않는 것이 정상이라고 문서화했다.
- 코드/문서:
  - `.github/workflows/cd.yml`
  - `docs/guides/deployment-guide.md`
  - `docs/PLAN.md`
  - `docs/PROGRESS.md`
  - `docs/COMPLETED.md`
- 검증:
  - `gh run view 24868095291 --repo answndud/Kindergarten_ERP`
  - `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/cd.yml'); puts 'ok'"`
  - `git diff --check`
- 결과:
  - 이후 `main` push는 `Backend CI`만 자동 실행하고, `Backend CD`는 배포 준비가 끝난 뒤 GitHub Actions에서 수동으로 실행하는 구조가 됐다.
