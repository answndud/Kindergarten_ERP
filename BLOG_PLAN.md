# BLOG_PLAN.md

## 작업명
- Kindergarten ERP 취업용 개발 블로그 시리즈 설계 2차

## 문서 역할
- 이 문서는 `blog/` 아래 실제 글을 쓰기 전에 기준이 되는 **블로그 집필 SSOT**다.
- 기존 [PLAN.md](/Users/alex/project/kindergarten_ERP/erp/PLAN.md), [PROGRESS.md](/Users/alex/project/kindergarten_ERP/erp/PROGRESS.md)는 애플리케이션 개발용 SSOT이고, 이 문서는 오직 **블로그 시리즈 설계와 집필 운영**만 다룬다.
- 목표는 단순 회고가 아니라, **Java / Spring Boot 입문자도 이 저장소를 따라가며 “이런 순서로 설계하고 구현하면 되는구나”를 이해할 수 있는 수준의 시리즈**를 만드는 것이다.

## 0) 코드베이스 스냅샷
- 도메인 패키지 수: `17`
- Flyway 마이그레이션 수: `13`
- 테스트 클래스 수: `33`
- HTML 템플릿 수: `39`
- 핵심 축
  - Bootstrapping: `settings.gradle`, `build.gradle`, `docker/*`, `application*.yml`
  - Shared foundation: `global/*`, `BaseEntity`, `ApiResponse`, `ErrorCode`
  - Core domain: `member`, `kindergarten`, `classroom`, `kid`, `attendance`, `notepad`, `announcement`, `kidapplication`, `kindergartenapplication`, `calendar`, `dashboard`
  - Security/Auth: `SecurityConfig`, `JwtTokenProvider`, `JwtFilter`, `AccessPolicyService`, `AuthService`, `OAuth2*`
  - Operational backend: `authaudit`, `domainaudit`, `notification`, `global/monitoring`, `global/logging`
  - Verification: `TestcontainersSupport`, `BaseIntegrationTest`, `AuthApiIntegrationTest`, `NotificationOutboxIntegrationTest`, `ObservabilityIntegrationTest`, `AuditConsolePerformanceSmokeTest`

## 1) 최종 목표

### 1-1. 블로그가 달성해야 하는 것
- 입문자가 본문만 읽어도 아래 흐름을 따라갈 수 있어야 한다.
  - 프로젝트 주제 선정
  - Gradle / Spring Boot 시작
  - Docker / MySQL / Redis / profile 구성
  - JPA / Flyway / Security / JWT 적용
  - 도메인 모델링
  - 기능 구현
  - 보안 하드닝
  - 테스트 현실화
  - 운영 관측성 / 감사 로그 / incident 대응
  - 포트폴리오 문서화
- 취업 준비생이 본문만 읽어도 아래 질문에 답할 수 있어야 한다.
  - “이 프로젝트는 왜 이 도메인을 골랐는가?”
  - “왜 이 구조로 패키지를 나눴는가?”
  - “왜 JWT를 세션 레지스트리까지 끌고 갔는가?”
  - “왜 멀티테넌시 권한 검증을 서비스 계층으로 올렸는가?”
  - “왜 H2가 아니라 Testcontainers를 선택했는가?”
  - “운영 가능한 백엔드라고 말할 근거가 무엇인가?”

### 1-2. Definition of Done
- `blog/` 아래에 실제 집필 가능한 시리즈 설계가 있어야 한다.
- 각 글은 반드시 아래를 포함해야 한다.
  - 해결하려는 문제
  - 실제 파일 경로
  - 실제 클래스 / 메서드
  - 테스트 / 문서 근거
  - 입문자 설명
  - 취업 포인트
- 시리즈 전체를 읽으면 “처음엔 CRUD 중심 프로젝트였지만, 점점 운영형 백엔드로 고도화했다”는 서사가 명확해야 한다.

### 1-3. 재현성 강화 목표
- 현재 블로그는 **설계 이해 / 포트폴리오 설명**에는 충분하지만, `blog/`만 보고 저장소와 거의 같은 프로젝트를 다시 만드는 수준까지는 아니다.
- 다음 단계 목표는 각 글을 “설명형 글”에서 “중간 체크포인트까지 재현 가능한 구현 가이드”로 끌어올리는 것이다.

#### 재현성 강화 기준
- 각 글에는 아래 항목이 추가되어야 한다.
  1. 시작 상태
  2. 이번 글에서 바뀌는 파일
  3. 구현 체크리스트
  4. 실행 / 검증 명령
  5. 글 종료 체크포인트
  6. 자주 막히는 지점

#### 재현성 강화 DoD
- `blog/00_rebuild_guide.md` 같은 공통 규칙 문서가 있어야 한다.
- 재현형 보강용 템플릿이 있어야 한다.
- 최소 `02`~`05`는 실제로 재현형 섹션이 들어가 있어야 한다.
- 이후 글도 같은 패턴으로 확장 가능한 운영 규칙이 정리돼 있어야 한다.

## 2) 독자 정의

### 1차 독자
- Java 문법은 알지만 Spring Boot 프로젝트를 처음 구성하는 사람
- JPA, Security, Testcontainers, CI가 아직 익숙하지 않은 사람
- “무엇부터 만들어야 하는지” 감이 없는 취업 준비생

### 2차 독자
- CRUD 프로젝트는 해봤지만 권한, 세션, 테스트, 운영성까지 올리는 법이 궁금한 사람
- 단순 기능 구현보다 “포트폴리오 설명력”을 높이고 싶은 사람

### 독자가 이 시리즈를 끝까지 읽고 얻어야 하는 것
- 기능 개발 순서가 아니라 **설계 순서**를 이해한다.
- 클래스와 메서드를 “이름만 만드는” 수준이 아니라 **책임 단위로 분리하는 기준**을 배운다.
- 테스트 / 보안 / 운영을 “마지막에 붙이는 것”이 아니라 **기능과 같이 성장시키는 축**으로 이해한다.
- 블로그 자체도 포트폴리오라는 감각을 얻는다.

## 3) 블로그가 사용할 소스 맵

### 3-1. 부트스트랩 / 실행 환경
- `settings.gradle`
- `build.gradle`
- `docker/docker-compose.yml`
- `docker/docker-compose.monitoring.yml`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-demo.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/logback-spring.xml`
- `src/main/java/com/erp/ErpApplication.java`
- `src/main/java/com/erp/global/config/DataLoader.java`

### 3-2. DB / 스키마 진화
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__add_application_workflow.sql`
- `src/main/resources/db/migration/V3__kid_application_unique_parent_kindergarten.sql`
- `src/main/resources/db/migration/V4__create_calendar_events.sql`
- `src/main/resources/db/migration/V5__add_performance_indexes_for_dashboard_and_notepad.sql`
- `src/main/resources/db/migration/V6__add_oauth_columns_to_member.sql`
- `src/main/resources/db/migration/V7__add_announcement_unique_views.sql`
- `src/main/resources/db/migration/V8__normalize_member_social_accounts.sql`
- `src/main/resources/db/migration/V9__preserve_social_account_history.sql`
- `src/main/resources/db/migration/V10__create_auth_audit_log.sql`
- `src/main/resources/db/migration/V11__denormalize_auth_audit_log_and_add_retention_archive.sql`
- `src/main/resources/db/migration/V12__add_notification_outbox.sql`
- `src/main/resources/db/migration/V13__add_admission_workflow_attendance_requests_and_domain_audit.sql`

### 3-3. 공통 백엔드 토대
- `src/main/java/com/erp/global/common/*`
- `src/main/java/com/erp/global/exception/*`
- `src/main/java/com/erp/global/config/*`
- `src/main/java/com/erp/global/logging/*`
- `src/main/java/com/erp/global/monitoring/*`

### 3-4. 보안 / 인증 / 권한
- `src/main/java/com/erp/global/config/SecurityConfig.java`
- `src/main/java/com/erp/global/security/jwt/*`
- `src/main/java/com/erp/global/security/oauth2/*`
- `src/main/java/com/erp/global/security/access/AccessPolicyService.java`
- `src/main/java/com/erp/global/security/ClientIpResolver.java`
- `src/main/java/com/erp/global/security/AuthenticatedMemberResolver.java`
- `src/main/java/com/erp/domain/auth/*`
- `src/main/java/com/erp/domain/member/entity/MemberSocialAccount.java`

### 3-5. 핵심 도메인
- `src/main/java/com/erp/domain/member/*`
- `src/main/java/com/erp/domain/kindergarten/*`
- `src/main/java/com/erp/domain/classroom/*`
- `src/main/java/com/erp/domain/kid/*`
- `src/main/java/com/erp/domain/attendance/*`
- `src/main/java/com/erp/domain/notepad/*`
- `src/main/java/com/erp/domain/announcement/*`
- `src/main/java/com/erp/domain/kidapplication/*`
- `src/main/java/com/erp/domain/kindergartenapplication/*`
- `src/main/java/com/erp/domain/calendar/*`
- `src/main/java/com/erp/domain/dashboard/*`
- `src/main/java/com/erp/domain/notification/*`
- `src/main/java/com/erp/domain/authaudit/*`
- `src/main/java/com/erp/domain/domainaudit/*`

### 3-6. 테스트 / CI / 운영 문서
- `src/test/java/com/erp/common/TestcontainersSupport.java`
- `src/test/java/com/erp/common/BaseIntegrationTest.java`
- `src/test/java/com/erp/api/*`
- `src/test/java/com/erp/integration/*`
- `src/test/java/com/erp/performance/*`
- `.github/workflows/ci.yml`
- `docs/decisions/phase00_setup.md` ~ `docs/decisions/phase44_tagged_ci_readiness_and_hiring_pack.md`
- `docs/portfolio/architecture/system-architecture.md`
- `docs/portfolio/demo/demo-preflight.md`
- `docs/portfolio/demo/demo-runbook.md`
- `docs/portfolio/case-studies/auth-incident-response.md`
- `docs/portfolio/hiring-pack/backend-hiring-pack.md`

## 4) 서사 전략

### 4-1. 실제 개발 순서와 게시 순서를 분리한다
- 이 프로젝트는 실제로 `기능 구현 -> 보안 문제 발견 -> 테스트 현실화 -> 운영형 기능 확장 -> 문서 패키징` 순서로 자랐다.
- 하지만 입문자에게 그대로 보여주면 맥락 전환이 너무 잦다.
- 그래서 시리즈는 아래 두 축을 동시에 만족하도록 재구성한다.
  - **학습 순서**: 입문자가 따라가기 쉬운 순서
  - **실전 서사**: 나중에 왜 리팩터링 / 하드닝이 필요했는지 설명 가능한 순서

### 4-2. 기본 원칙
- “처음 버전”을 먼저 설명하고, “왜 문제가 생겼는지”를 보여준 뒤, “개선 버전”으로 넘어간다.
- 코드만 설명하지 않고 반드시 아래 세 층을 함께 설명한다.
  - 도메인 규칙
  - 보안 / 운영 규칙
  - 테스트 / 문서 근거
- 글의 중심은 항상 한 문장으로 요약 가능해야 한다.
  - 예: `JWT를 붙였다`가 아니라 `JWT를 세션 운영 가능한 구조로 키웠다`

### 4-3. 글 하나당 품질 기준
- 하나의 핵심 질문만 다룬다.
- 실제 클래스 / 메서드 3~8개를 중심으로 설명한다.
- SQL migration, 테스트, 문서를 최소 하나 이상 근거로 붙인다.
- “입문자 설명”과 “취업 포인트”를 분리한다.
- 긴 코드 전체를 붙이지 않고, 흐름과 책임을 설명한다.

## 5) 실제 코드 기준 개발 연대기

### Stage A. 기초 구축
- `settings.gradle`, `build.gradle`, `ErpApplication`
- `application.yml`, `application-local.yml`
- `V1__init_schema.sql`
- 초기 도메인: `Member`, `Kindergarten`, `Classroom`, `Kid`

### Stage B. 기능 확장
- `Attendance`, `Notepad`, `Announcement`
- `KidApplication`, `KindergartenApplication`
- `Calendar`, `Dashboard`
- `V2`, `V3`, `V4`, `V5`

### Stage C. 보안 / 인증 고도화
- `AccessPolicyService`
- `AuthService`, `JwtTokenProvider`, `AuthSessionRegistryService`
- `AuthRateLimitService`, `ClientIpResolver`
- `OAuth2AuthenticationSuccessHandler`, `SocialAccountLinkService`
- `V6`, `V7`, `V8`, `V9`

### Stage D. 운영형 백엔드 고도화
- `AuthAuditLogService`, `AuthAuditRetentionService`
- `NotificationDispatchService`
- `DomainAuditLogService`
- `CriticalDependenciesHealthIndicator`
- `PrometheusScrapeController`, `RequestLoggingFilter`, `CorrelationIdFilter`
- `V10`, `V11`, `V12`, `V13`

### Stage E. 포트폴리오 패키징
- OpenAPI / Swagger
- Demo seed / demo profile
- Grafana / Prometheus
- Hiring pack / architecture / demo runbook / interview docs

## 6) 게시 순서

### Part 1. 처음 시작하는 사람도 따라갈 수 있는 기초 구축

#### 01. 왜 유치원 ERP를 주제로 잡았는가
- 핵심 질문
  - 왜 이 도메인이 첫 포트폴리오로 좋은가?
- 주요 파일 / 문서
  - `README.md`
  - `docs/archive/legacy/project-idea.md`
  - `docs/archive/legacy/project-plan.md`
  - `docs/guides/user-guide.md`
- 핵심 포인트
  - 역할 모델: `PRINCIPAL`, `TEACHER`, `PARENT`
  - 이 도메인은 CRUD보다 권한과 상태 전이를 자연스럽게 만들 수 있다.
- 입문자 설명
  - 주제를 기능 목록이 아니라 “관계와 문제 공간”으로 정하는 법
- 취업 포인트
  - “왜 이 도메인을 골랐는가?”에 구조적인 답을 할 수 있어야 한다.

#### 02. `settings.gradle`과 `build.gradle`로 프로젝트 뼈대 세우기
- 핵심 질문
  - Spring Boot 프로젝트는 최소 무엇으로 시작하는가?
- 주요 파일
  - `settings.gradle`
  - `build.gradle`
  - `src/main/java/com/erp/ErpApplication.java`
- 반드시 설명할 메서드 / 설정
  - Gradle plugin 블록
  - dependency 블록
  - `tasks.register('fastTest' / 'integrationTest' / 'performanceSmokeTest')`
- 입문자 설명
  - starter 의존성을 무엇 기준으로 넣는지
  - 나중에 test task 분리까지 어떻게 확장됐는지
- 취업 포인트
  - 처음부터 운영형 확장을 고려한 빌드 구조라는 점

#### 03. Docker로 MySQL / Redis 개발 환경 만들기
- 핵심 질문
  - 왜 로컬 설치 대신 Docker Compose로 시작해야 하는가?
- 주요 파일
  - `docker/docker-compose.yml`
  - `docker/docker-compose.monitoring.yml`
- 반드시 설명할 포인트
  - MySQL 문자셋 / timezone
  - Redis appendonly
  - monitoring overlay 분리
- 입문자 설명
  - 애플리케이션보다 먼저 “실행 환경 재현성”을 챙기는 이유
- 취업 포인트
  - 로컬 실행 절차가 문서가 아니라 코드로 남아 있다는 점

#### 04. `application.yml`과 profile 전략 설계하기
- 핵심 질문
  - local / demo / prod 설정은 왜 처음부터 나눠야 하는가?
- 주요 파일
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-demo.yml`
  - `src/main/resources/application-prod.yml`
  - `src/main/resources/logback-spring.xml`
  - `src/main/java/com/erp/global/config/DataLoader.java`
- 반드시 설명할 포인트
  - `spring.profiles.group.demo`
  - `management`와 `springdoc` prod 차단
  - local seed data
- 입문자 설명
  - 공통 설정과 환경별 설정을 분리하는 기준
- 취업 포인트
  - 시연 편의와 운영 보안을 한 코드베이스에서 동시에 다룬 경험

#### 05. 공통 백엔드 토대 만들기: 패키지 구조, 공통 응답, 예외 처리
- 핵심 질문
  - 코드가 커지기 전에 무엇을 공통 토대로 고정해야 하는가?
- 주요 파일
  - `src/main/java/com/erp/global/common/*`
  - `src/main/java/com/erp/global/exception/*`
  - `src/main/java/com/erp/domain/dto/*`
- 반드시 설명할 클래스 / 메서드
  - `BaseEntity`
  - `ApiResponse`
  - `ErrorCode`
  - `GlobalExceptionHandler`
- 입문자 설명
  - 패키지 구조를 왜 `domain/*` + `global/*`로 나눴는지
- 취업 포인트
  - 초반에 공통 규약을 세운 덕분에 이후 기능이 커져도 일관성을 유지한 점

#### 06. Flyway와 JPA를 어떻게 같이 가져갈 것인가
- 핵심 질문
  - 왜 `ddl-auto=create`가 아니라 Flyway + validate 전략을 택했는가?
- 주요 파일
  - `src/main/resources/db/migration/V1__init_schema.sql`
  - `src/main/resources/application.yml`
  - `build.gradle`
- 반드시 설명할 포인트
  - `open-in-view: false`
  - `default_batch_fetch_size`
  - Flyway baseline / validate
- 입문자 설명
  - JPA가 엔티티를 다루고, Flyway가 스키마 변화를 관리한다는 역할 분리
- 취업 포인트
  - 운영형 마이그레이션 감각을 초반부터 넣었다는 점

### Part 2. 도메인 모델과 기능을 실제로 만드는 단계

#### 07. 첫 도메인 설계: `Member`, `Kindergarten`, `Classroom`, `Kid`
- 핵심 질문
  - CRUD 전에 어떤 관계를 먼저 모델링해야 하는가?
- 주요 파일
  - `domain/member/entity/Member.java`
  - `domain/kindergarten/entity/Kindergarten.java`
  - `domain/classroom/entity/Classroom.java`
  - `domain/kid/entity/Kid.java`
  - `domain/kid/entity/ParentKid.java`
- 반드시 설명할 메서드
  - `Member.create(...)`
  - `Member.createSocial(...)`
  - `Member.assignKindergarten(...)`
  - `Classroom.create(...)`
  - `Kid.create(...)`
  - `Kid.addParent(...)`
- 입문자 설명
  - 엔티티를 테이블이 아니라 “상태를 가진 객체”로 보는 관점
- 취업 포인트
  - 테넌트 경계의 기초를 도메인 모델 단계에서 먼저 만든 점

#### 08. 첫 인증 흐름 만들기: 회원가입, 로그인, `SecurityConfig`
- 핵심 질문
  - Spring Security와 JWT를 처음 프로젝트에 어떻게 붙이는가?
- 주요 파일
  - `domain/auth/service/AuthService.java`
  - `domain/auth/controller/AuthApiController.java`
  - `global/config/SecurityConfig.java`
  - `global/security/jwt/JwtTokenProvider.java`
  - `domain/member/service/MemberService.java`
- 반드시 설명할 메서드
  - `AuthService.signUp(...)`
  - `AuthService.login(...)`
  - `JwtTokenProvider.createAccessToken(...)`
  - `SecurityConfig.securityFilterChain(...)`
- 입문자 설명
  - 인증은 필터 체인, 서비스, 토큰 생성이 어떻게 연결되는가
- 취업 포인트
  - 단순 로그인 구현이 아니라 구조적으로 Security를 붙였다는 점

#### 09. 뷰 레이어를 붙여 전체 흐름을 보이게 만들기
- 핵심 질문
  - API만 있는 프로젝트보다 SSR 화면이 있으면 무엇이 좋아지는가?
- 주요 파일
  - `domain/auth/controller/AuthViewController.java`
  - `src/main/resources/templates/auth/*`
  - `src/main/resources/templates/fragments/header.html`
  - `src/main/resources/templates/layout/*`
- 반드시 설명할 메서드
  - `AuthViewController.loginPage(...)`
  - `AuthViewController.profilePage(...)`
  - `AuthViewController.settingsPage(...)`
- 입문자 설명
  - Thymeleaf, HTMX, Alpine.js가 어떤 역할로 섞였는가
- 취업 포인트
  - 시연 가능한 백엔드는 설명력과 검증력이 높아진다는 점

#### 10. 출석 도메인 만들기: 단순 저장이 아니라 상태 관리로 보기
- 핵심 질문
  - 출석은 왜 단순 CRUD보다 상태 변경 메서드가 중요한가?
- 주요 파일
  - `domain/attendance/entity/Attendance.java`
  - `domain/attendance/service/AttendanceService.java`
  - `domain/attendance/controller/AttendanceController.java`
  - `domain/attendance/dto/*`
- 반드시 설명할 메서드
  - `AttendanceService.upsertAttendance(...)`
  - `AttendanceService.bulkUpdateAttendance(...)`
  - `AttendanceService.recordDropOff(...)`
  - `AttendanceService.recordPickUp(...)`
  - `AttendanceService.getMonthlyReportByClassroom(...)`
- 입문자 설명
  - 서비스는 저장 담당이 아니라 유스케이스 조정자라는 점
- 취업 포인트
  - 출석 집계와 리포트로 읽기 모델까지 확장한 점

#### 11. 알림장과 공지사항: 비슷해 보이지만 다른 두 도메인
- 핵심 질문
  - 둘 다 글쓰기 기능인데 왜 서비스가 분리되어야 하는가?
- 주요 파일
  - `domain/notepad/service/NotepadService.java`
  - `domain/announcement/service/AnnouncementService.java`
  - `domain/notepad/controller/NotepadController.java`
  - `domain/announcement/controller/AnnouncementController.java`
  - `V7__add_announcement_unique_views.sql`
- 반드시 설명할 메서드
  - `NotepadService.createNotepad(...)`
  - `NotepadService.getNotepadDetail(...)`
  - `NotepadService.markAsRead(...)`
  - `AnnouncementService.createAnnouncement(...)`
  - `AnnouncementService.toggleImportant(...)`
  - `AnnouncementService.recordView(...)`
- 입문자 설명
  - 읽음 처리와 조회수는 왜 별도 모델 / 서비스 관심사인가
- 취업 포인트
  - “비슷한 CRUD”도 역할과 정책에 따라 분리해야 한다는 점

#### 12. 신청과 승인: CRUD에서 워크플로우로 넘어가기
- 핵심 질문
  - 입력 저장 이후의 상태 전이는 어떻게 모델링하는가?
- 주요 파일
  - `domain/kidapplication/*`
  - `domain/kindergartenapplication/*`
  - `V2__add_application_workflow.sql`
  - `V3__kid_application_unique_parent_kindergarten.sql`
- 반드시 설명할 메서드
  - `KidApplicationService.apply(...)`
  - `KidApplicationService.approve(...)`
  - `KidApplicationService.reject(...)`
- 입문자 설명
  - 신청과 승인 도메인은 일반 CRUD와 어떻게 다른가
- 취업 포인트
  - 상태 전이가 필요한 비즈니스 도메인을 직접 설계한 경험

#### 13. 캘린더와 대시보드: 조회 모델과 집계 모델을 설계하는 법
- 핵심 질문
  - 쓰기 모델과 읽기 모델은 왜 같은 방식으로 설계하면 안 되는가?
- 주요 파일
  - `domain/calendar/*`
  - `domain/dashboard/service/DashboardService.java`
  - `V4__create_calendar_events.sql`
  - `V5__add_performance_indexes_for_dashboard_and_notepad.sql`
- 반드시 설명할 메서드
  - `DashboardService.getDashboardStatistics(...)`
  - `DashboardService.calculateAttendanceRate(...)`
  - `DashboardService.calculateAnnouncementReadRate(...)`
- 입문자 설명
  - 반복 일정 occurrence와 대시보드 집계는 왜 별도 계산 로직이 필요한가
- 취업 포인트
  - 정확도와 성능을 동시에 고민한 읽기 모델 설계

### Part 3. 기능을 안전하게 만들기 위한 보안 / 인증 고도화

#### 14. 멀티테넌시 권한 경계 하드닝
- 핵심 질문
  - 역할 체크만으로 왜 충분하지 않은가?
- 주요 파일
  - `global/security/access/AccessPolicyService.java`
  - `global/config/SecurityConfig.java`
  - `KidService`, `ClassroomService`, `AttendanceService`, `NotepadService`, `AnnouncementService`
  - `docs/decisions/phase14_multitenant_access_hardening.md`
- 반드시 설명할 메서드
  - `AccessPolicyService.getRequester(...)`
  - `validateSameKindergarten(...)`
  - `validateKidReadAccess(...)`
  - `validateNotepadReadAccess(...)`
  - `validateNotificationReceiverAccess(...)`
- 입문자 설명
  - IDOR와 데이터 경계 검증이 무엇인지
- 취업 포인트
  - 컨트롤러가 아니라 서비스 계층에 보안 정책을 올린 선택

#### 15. 테스트를 현실화하기: MySQL / Redis Testcontainers
- 핵심 질문
  - 왜 H2 / mock Redis에서 멈추면 안 되는가?
- 주요 파일
  - `src/test/java/com/erp/common/TestcontainersSupport.java`
  - `src/test/java/com/erp/common/BaseIntegrationTest.java`
  - `src/test/resources/application-test.yml`
  - `docs/decisions/phase15_testcontainers_integration_test_stack.md`
- 반드시 설명할 메서드
  - `BaseIntegrationTest.readCommitted(...)`
  - `BaseIntegrationTest.writeCommitted(...)`
  - identity reset / cleanup 전략
- 입문자 설명
  - “테스트가 통과한다”와 “운영과 비슷하게 검증했다”의 차이
- 취업 포인트
  - 실환경형 테스트 스택을 직접 만든 점

#### 16. CI를 의미 단위로 나누기: `fast`, `integration`, `performance`
- 핵심 질문
  - 테스트가 많아지면 CI를 어떻게 설계해야 하는가?
- 주요 파일
  - `build.gradle`
  - `.github/workflows/ci.yml`
  - `docs/decisions/phase16_github_actions_ci.md`
  - `docs/decisions/phase44_tagged_ci_readiness_and_hiring_pack.md`
- 반드시 설명할 포인트
  - `@Tag("fast")`, `@Tag("integration")`, `@Tag("performance")`
  - GitHub Actions job 분리
- 입문자 설명
  - 모든 테스트를 한 job에서 돌리는 것의 한계
- 취업 포인트
  - 테스트의 의미를 CI 구조에 반영한 경험

#### 17. JWT를 진짜 세션 관리로 확장하기
- 핵심 질문
  - JWT를 써도 왜 세션 레지스트리가 필요한가?
- 주요 파일
  - `domain/auth/service/AuthService.java`
  - `domain/auth/service/AuthSessionRegistryService.java`
  - `global/security/jwt/JwtTokenProvider.java`
  - `global/security/jwt/JwtFilter.java`
  - `docs/decisions/phase17_jwt_refresh_session_rotation.md`
  - `docs/decisions/phase39_management_plane_and_active_session_control.md`
- 반드시 설명할 메서드
  - `AuthService.refreshAccessToken(...)`
  - `AuthService.getActiveSessions(...)`
  - `AuthService.revokeSession(...)`
  - `AuthSessionRegistryService.registerSession(...)`
  - `AuthSessionRegistryService.revokeOtherSessions(...)`
- 입문자 설명
  - stateless access token과 stateful session control이 함께 가는 이유
- 취업 포인트
  - refresh rotation과 세션 강제 종료를 실제로 설명할 수 있는 구조

#### 18. 인증 남용 방어: Rate Limit과 trusted proxy 기반 Client IP
- 핵심 질문
  - 로그인 API는 왜 일반 API보다 더 조심해야 하는가?
- 주요 파일
  - `domain/auth/service/AuthRateLimitService.java`
  - `global/security/ClientIpResolver.java`
  - `global/security/ClientIpProperties.java`
  - `docs/decisions/phase21_auth_rate_limit.md`
  - `docs/decisions/phase24_auth_client_ip_trust_model.md`
  - `docs/decisions/phase25_login_rate_limit_policy_refinement.md`
- 반드시 설명할 메서드
  - `AuthRateLimitService`의 사전 확인 / 실패 기록 / 성공 초기화
  - `ClientIpResolver.resolve(...)`
- 입문자 설명
  - 헤더 스푸핑, trusted proxy, 실패 전용 rate limit
- 취업 포인트
  - 보안 정책을 단순 라이브러리 사용이 아니라 직접 설계한 점

#### 19. OAuth2를 붙이는 것이 아니라 lifecycle을 설계하기
- 핵심 질문
  - 소셜 로그인은 “로그인 성공” 이후가 더 중요한 이유가 무엇인가?
- 주요 파일
  - `global/security/oauth2/OAuth2AuthenticationSuccessHandler.java`
  - `global/security/AuthenticatedMemberResolver.java`
  - `domain/auth/service/SocialAccountLinkService.java`
  - `domain/member/entity/Member.java`
  - `domain/member/entity/MemberSocialAccount.java`
  - `V6__add_oauth_columns_to_member.sql`
  - `V8__normalize_member_social_accounts.sql`
  - `V9__preserve_social_account_history.sql`
- 반드시 설명할 메서드
  - `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(...)`
  - `SocialAccountLinkService.linkSocialAccount(...)`
  - `Member.linkSocialAccount(...)`
  - `Member.unlinkSocialAccount(...)`
  - `Member.hasProviderBindingWithDifferentIdentity(...)`
- 입문자 설명
  - 이메일 충돌, explicit linking, password bootstrap, unlink safeguard, provider identity immutability
- 취업 포인트
  - 흔한 “소셜 로그인 붙였다”가 아니라 계정 lifecycle까지 설계한 점

### Part 4. 운영 가능한 백엔드로 고도화하고 포트폴리오로 패키징하기

#### 20. 인증 감사 로그를 운영 도구로 키우기
- 핵심 질문
  - 로그인 로그를 저장만 하면 왜 부족한가?
- 주요 파일
  - `domain/authaudit/service/AuthAuditLogService.java`
  - `domain/authaudit/service/AuthAuditRetentionService.java`
  - `domain/authaudit/controller/AuthAuditLogController.java`
  - `domain/authaudit/service/AuthAuditLogQueryService.java`
  - `V10__create_auth_audit_log.sql`
  - `V11__denormalize_auth_audit_log_and_add_retention_archive.sql`
- 반드시 설명할 메서드
  - `recordLoginSuccess(...)`
  - `recordLoginFailure(...)`
  - `resolveKindergartenId(...)`
  - retention / archive scheduling
- 입문자 설명
  - auth audit와 business audit를 왜 나누는가
- 취업 포인트
  - 조회, export, retention까지 갖춘 운영 감사 체계

#### 21. OpenAPI, Actuator, Prometheus, correlation id로 운영 관측성 만들기
- 핵심 질문
  - “기능이 된다”에서 “운영할 수 있다”로 가려면 무엇이 필요한가?
- 주요 파일
  - `global/monitoring/*`
  - `global/logging/*`
  - `global/config/OpenApiConfig.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-prod.yml`
  - `src/main/resources/logback-spring.xml`
  - `docs/decisions/phase34_operability_observability_baseline.md`
  - `docs/decisions/phase36_api_contract_observability_demo.md`
- 반드시 설명할 클래스
  - `CriticalDependenciesHealthIndicator`
  - `PrometheusScrapeController`
  - `RequestLoggingFilter`
  - `CorrelationIdFilter`
- 입문자 설명
  - liveness와 readiness의 차이
  - Prometheus와 Swagger는 각각 무엇을 위한 도구인가
- 취업 포인트
  - 운영 관측성과 계약 문서를 실제 코드로 제공한 점

#### 22. 알림을 신뢰성 있는 전달 구조로 바꾸기: Outbox, retry, dead-letter
- 핵심 질문
  - 외부 채널 호출은 왜 동기 처리하면 안 되는가?
- 주요 파일
  - `domain/notification/service/NotificationDispatchService.java`
  - `domain/notification/entity/*`
  - `V12__add_notification_outbox.sql`
  - `docs/decisions/phase40_notification_outbox_and_incident_channel.md`
  - `docs/portfolio/case-studies/auth-incident-response.md`
- 반드시 설명할 메서드
  - `dispatch(...)`
  - `processReadyDeliveriesOnSchedule()`
  - `processReadyDeliveriesBatch()`
  - `claimReadyDeliveries(...)`
  - `resolveRetryDelay(...)`
- 입문자 설명
  - Outbox 패턴과 dead-letter queue의 역할
- 취업 포인트
  - 알림을 기능이 아니라 운영 파이프라인으로 다룬 경험

#### 23. 운영형 워크플로우: 반 정원, waitlist, offer, 출결 변경 요청
- 핵심 질문
  - CRUD를 상태 전이형 워크플로우로 바꾸려면 무엇이 달라져야 하는가?
- 주요 파일
  - `domain/classroom/service/ClassroomCapacityService.java`
  - `domain/kidapplication/entity/KidApplication.java`
  - `domain/kidapplication/service/KidApplicationService.java`
  - `domain/attendance/service/AttendanceChangeRequestService.java`
  - `V13__add_admission_workflow_attendance_requests_and_domain_audit.sql`
  - `docs/decisions/phase41_admission_capacity_waitlist_workflow.md`
  - `docs/decisions/phase42_attendance_change_request_workflow.md`
- 반드시 설명할 메서드
  - `ClassroomCapacityService.validateSeatAvailable(...)`
  - `KidApplication.placeOnWaitlist(...)`
  - `KidApplication.offerSeat(...)`
  - `KidApplication.acceptOffer(...)`
  - `KidApplicationService.expireOffers()`
  - `AttendanceChangeRequestService.create(...)`
  - `AttendanceChangeRequestService.approve(...)`
- 입문자 설명
  - 상태 전이, 좌석 예약, 요청자와 승인자 분리
- 취업 포인트
  - 운영 현실을 코드 모델로 끌어올린 설계 경험

#### 24. 업무 감사 로그와 운영자 책임 추적
- 핵심 질문
  - 비즈니스 상태 변화는 왜 별도 감사 로그가 필요한가?
- 주요 파일
  - `domain/domainaudit/entity/DomainAuditLog.java`
  - `domain/domainaudit/service/DomainAuditLogService.java`
  - `domain/domainaudit/service/DomainAuditLogQueryService.java`
  - `domain/domainaudit/controller/DomainAuditLogController.java`
  - `docs/decisions/phase43_domain_audit_log.md`
- 반드시 설명할 메서드
  - `DomainAuditLogService.record(...)`
  - `DomainAuditLogService.recordSystem(...)`
- 입문자 설명
  - 인증 로그와 업무 로그의 목적 차이
- 취업 포인트
  - “누가 무엇을 바꿨는가”를 남기는 운영 감각

#### 25. 성능 스토리를 만드는 법: 측정 -> 개선 -> 재측정
- 핵심 질문
  - 성능 개선은 어떻게 포트폴리오 스토리로 바꿀 수 있는가?
- 주요 파일 / 문서
  - `docs/portfolio/performance/*`
  - `domain/dashboard/service/DashboardService.java`
  - `domain/notepad/service/NotepadService.java`
  - `src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java`
- 반드시 설명할 포인트
  - 쿼리 수 / 응답 시간 / p95, p99
  - 인덱스, 캐시, 조회 구조 개선
- 입문자 설명
  - “빠르다”가 아니라 “어떻게 측정했고 무엇을 바꿨는가”로 말해야 한다는 점
- 취업 포인트
  - 정량적 성능 개선을 문서와 테스트로 남긴 경험

#### 26. 이 프로젝트를 취업용 포트폴리오로 완성하는 문서 패키지
- 핵심 질문
  - 코드가 좋아도 왜 문서 구조가 중요한가?
- 주요 파일 / 문서
  - `docs/portfolio/architecture/system-architecture.md`
  - `docs/portfolio/demo/demo-preflight.md`
  - `docs/portfolio/demo/demo-runbook.md`
  - `docs/portfolio/hiring-pack/backend-hiring-pack.md`
  - `docs/portfolio/interview/*`
  - `docs/README.md`
- 반드시 설명할 포인트
  - README는 입구
  - decisions는 근거
  - architecture는 구조 설명
  - demo는 재현 절차
  - hiring pack은 채용 담당자용 압축본
- 입문자 설명
  - 문서도 설계의 일부라는 점
- 취업 포인트
  - 코드를 “읽히게 만들기 위한 문서 구조”까지 설계한 경험

## 7) 글쓰기 작업 규칙

### 7-1. 각 글의 기본 구조
- 문제
- 선행 개념
- 다룰 파일
- 설계 선택
- 클래스 / 메서드 설명
- 요청 흐름 / 데이터 흐름
- 테스트 근거
- 회고
- 취업 포인트

### 7-2. 코드 설명 방식
- 소스 전체를 붙이지 않는다.
- 긴 메서드는 아래 방식으로 설명한다.
  - 입력
  - 출력
  - 핵심 분기
  - 책임
  - 왜 이 메서드가 필요한지
- 반드시 “메서드가 하나의 책임을 갖는 이유”를 함께 설명한다.

### 7-3. 근거 문서 사용 방식
- `docs/decisions/*`는 “왜 이 변경이 필요했는지” 설명할 때 사용한다.
- `docs/portfolio/*`는 “현재 기준에서 어떻게 보여줄 것인지” 설명할 때 사용한다.
- 블로그는 기존 문서를 복붙하지 않고, 학습 흐름에 맞게 재구성한다.

### 7-4. 테스트 설명 방식
- 성공 케이스만 적지 않는다.
- 반드시 실패 / 차단 / 경계 조건을 함께 설명한다.
- 예시
  - 다른 유치원 접근 차단
  - 만료된 offer 수락 차단
  - 소셜 provider 교체 금지
  - rate limit 초과
  - readiness DOWN / liveness UP

## 8) 실제 집필 우선순위

### 1차 우선 집필
- 02. `build.gradle`과 Spring Boot 프로젝트 뼈대
- 03. Docker + MySQL + Redis + local 실행 환경
- 08. 회원가입 / 로그인 / `SecurityConfig`
- 14. 멀티테넌시 권한 경계 하드닝

이유:
- 입문자 유입이 가장 많다.
- 프로젝트 차별점이 빨리 드러난다.
- 이후 글의 맥락을 설명하기 쉽다.

### 2차 우선 집필
- 15. Testcontainers
- 17. JWT 세션 레지스트리
- 19. OAuth2 lifecycle
- 23. waitlist / offer / attendance request workflow

이유:
- “이 프로젝트가 왜 흔한 CRUD 포트폴리오와 다른가”를 가장 강하게 보여준다.

### 3차 우선 집필
- 21. Observability
- 22. Notification outbox
- 24. Domain audit
- 26. Hiring pack / demo / interview docs

이유:
- 후반부지만 취업용 설명력은 매우 높다.

## 9) 집필 운영 방식

### 9-1. 글 하나를 쓸 때 먼저 확인할 것
- 관련 코드 파일
- 관련 SQL migration
- 관련 테스트
- 관련 결정 로그
- 관련 시연 화면 / 템플릿

### 9-2. 글 하나를 쓸 때 남길 산출물
- 본문 초안
- 사용할 다이어그램 목록
- 설명할 메서드 목록
- 테스트 근거 목록
- 면접 질문 예상 2~3개

### 9-3. 글을 쓴 뒤 체크할 것
- 입문자도 따라올 수 있는가
- 실제 파일과 메서드가 들어갔는가
- “왜 이렇게 설계했는가”가 드러나는가
- 취업 포인트가 억지스럽지 않고 코드와 연결되는가

## 10) 리스크와 대응
- 리스크: 글이 너무 방대해져서 한 편이 책처럼 길어질 수 있다
  - 대응: 글마다 하나의 질문만 다룬다
- 리스크: 기존 결정 로그와 중복될 수 있다
  - 대응: 결정 로그는 사실 근거, 블로그는 학습형 해설로 역할을 나눈다
- 리스크: 초심자 설명과 취업 포인트가 섞여 산만해질 수 있다
  - 대응: 본문 안에 `입문자 설명`, `취업 포인트`를 명시적으로 분리한다
- 리스크: 후반부 운영 문서가 너무 고급처럼 보일 수 있다
  - 대응: 먼저 문제를 쉬운 말로 설명하고, 그 다음 용어를 붙인다

## 11) 다음 액션
- `blog/00_series_plan.md`를 이 문서 기준의 간략판으로 유지
- 실제 본문은 `blog/02-gradle-spring-boot-bootstrap.md`부터 작성 시작
- 각 글을 작성할 때 `BLOG_PROGRESS.md`에 진행 로그와 근거 파일을 남긴다

## 12) 재현성 강화 실행 배치

### Batch R1. 공통 재현형 규칙 문서
- `blog/00_rebuild_guide.md`
- `blog/_rebuild_template.md`
- `blog/README.md`에 재현형 읽기 순서 연결

### Batch R2. 부트스트랩 구간 우선 보강
- 대상: `02`, `03`, `04`, `05`
- 이유
  - 환경 / 설정 / 실행이 안 잡히면 뒤 글은 재현이 불가능하다
  - 입문자가 가장 빨리 막히는 구간이다

### Batch R3. 인증 / 테스트 구간 보강
- 대상: `11`~`15`
- 이유
  - Security, JWT, Testcontainers, CI는 설명형 글만으로 따라가기가 가장 어렵다

### Batch R4. 운영형 기능 / 포트폴리오 구간 보강
- 대상: `16`~`26`
- 이유
  - 상태 전이, 감사 로그, 관측성은 체크포인트와 실패 대응까지 같이 있어야 재현성이 생긴다
