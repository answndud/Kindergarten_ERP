# BLOG_PLAN.md

## 문서 역할

이 문서는 Kindergarten ERP 기반 **취업용 개발 블로그 시리즈의 마스터 플랜**이다.

목표는 단순히 “무슨 기능을 만들었다”를 나열하는 것이 아니다.

1. Java / Spring Boot 입문자가 프로젝트를 처음 어떻게 설계하고 키워가야 하는지 이해하게 만든다.
2. 취업 준비생이 CRUD 프로젝트를 운영형 백엔드 포트폴리오로 발전시키는 흐름을 따라오게 만든다.
3. 각 글이 반드시 실제 코드, 테스트, 결정 로그, 운영 문서에 근거하도록 한다.

이 문서는 기존 개발용 [PLAN.md](/Users/alex/project/kindergarten_ERP/erp/PLAN.md), [PROGRESS.md](/Users/alex/project/kindergarten_ERP/erp/PROGRESS.md)와 분리된 **블로그 집필용 SSOT**다.

## 1) 독자와 최종 도달점

### 1차 독자

- Java 문법은 알지만 Spring Boot 프로젝트를 처음 제대로 만들어보는 입문자
- 포트폴리오 프로젝트를 시작하려는데 “무엇부터 만들어야 하는지” 감이 없는 취업 준비생

### 2차 독자

- CRUD는 만들어봤지만 보안, 테스트, 운영성, 문서화까지 연결한 프로젝트를 만들어본 적이 없는 사람

### 이 시리즈를 다 읽고 나면 독자가 이해해야 하는 것

- `settings.gradle`, `build.gradle`, `ErpApplication.java`만으로 프로젝트를 시작하는 최소 단위
- Docker, MySQL, Redis, profile, Flyway를 초반부터 어떻게 잡아야 하는지
- Entity, Service, Controller, DTO를 어떤 책임 단위로 나누는지
- Spring Security, JWT, OAuth2를 실전 프로젝트에서 어떻게 발전시키는지
- CRUD를 넘어 승인, 대기열, 감사 로그, outbox, observability로 어떻게 확장하는지
- 취업용 포트폴리오는 코드만이 아니라 테스트, 문서, 데모, 인터뷰 스토리까지 묶어서 보여줘야 한다는 점

## 2) 집필 근거 우선순위

블로그는 아래 순서로 근거를 확인하면서 작성한다.

1. **현재 최종 코드**
   - `src/main/java`
   - `src/main/resources`
   - `build.gradle`
   - `docker/*`
2. **현재 테스트**
   - `src/test/java`
   - `.github/workflows/ci.yml`
3. **결정 로그**
   - `docs/decisions/phase00_*` ~ `phase44_*`
4. **포트폴리오 문서**
   - `docs/portfolio/*`
5. **초기 아이디어/기획 기록**
   - `docs/archive/legacy/*`

블로그는 항상 **최종 코드 기준 설명 + 당시 설계 배경 + 나중에 바뀐 이유**의 3단 구조를 유지한다.

즉, final state를 설명하되 “처음부터 이렇게 잘 만든 것처럼” 쓰지 않는다.

## 3) 실제 코드베이스 지도

### 빌드 / 런타임 진입점

- [settings.gradle](/Users/alex/project/kindergarten_ERP/erp/settings.gradle)
- [build.gradle](/Users/alex/project/kindergarten_ERP/erp/build.gradle)
- [ErpApplication.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/ErpApplication.java)

### 인프라 / 설정

- [docker/docker-compose.yml](/Users/alex/project/kindergarten_ERP/erp/docker/docker-compose.yml)
- [docker/docker-compose.monitoring.yml](/Users/alex/project/kindergarten_ERP/erp/docker/docker-compose.monitoring.yml)
- [application.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application.yml)
- [application-local.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-local.yml)
- [application-demo.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-demo.yml)
- [application-prod.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-prod.yml)
- [logback-spring.xml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/logback-spring.xml)
- [db/migration](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration)

### 공통 기반 / Config

- [BaseEntity.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/common/BaseEntity.java)
- [JpaConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/JpaConfig.java)
- [QuerydslConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/QuerydslConfig.java)
- [RedisConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/RedisConfig.java)
- [CacheConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/CacheConfig.java)
- [WebMvcConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/WebMvcConfig.java)
- [SecurityConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/SecurityConfig.java)
- [OpenApiConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/OpenApiConfig.java)
- [GlobalControllerAdvice.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/GlobalControllerAdvice.java)
- [CsrfCookieFilter.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/CsrfCookieFilter.java)
- [DataLoader.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/DataLoader.java)

### 보안 / 인증

- [JwtTokenProvider.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtTokenProvider.java)
- [JwtFilter.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtFilter.java)
- [CustomUserDetailsService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/user/CustomUserDetailsService.java)
- [AccessPolicyService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/access/AccessPolicyService.java)
- [ClientIpResolver.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/ClientIpResolver.java)
- [AuthenticatedMemberResolver.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/AuthenticatedMemberResolver.java)
- [OAuth2AuthenticationSuccessHandler.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/oauth2/OAuth2AuthenticationSuccessHandler.java)
- [OAuth2LinkSessionService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/oauth2/OAuth2LinkSessionService.java)

### 핵심 도메인

- `member`, `kindergarten`, `classroom`, `kid`
- `attendance`, `notepad`, `announcement`, `calendar`, `dashboard`
- `kidapplication`, `kindergartenapplication`
- `notification`
- `authaudit`, `domainaudit`

### 테스트 / 운영 검증

- [BaseIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/common/BaseIntegrationTest.java)
- [TestcontainersSupport.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/common/TestcontainersSupport.java)
- [AuthApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AuthApiIntegrationTest.java)
- [KidApplicationApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/KidApplicationApiIntegrationTest.java)
- [AttendanceChangeRequestApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AttendanceChangeRequestApiIntegrationTest.java)
- [DomainAuditApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/DomainAuditApiIntegrationTest.java)
- [ObservabilityIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/ObservabilityIntegrationTest.java)
- [NotificationOutboxIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/NotificationOutboxIntegrationTest.java)
- [AuditConsolePerformanceSmokeTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java)
- [ci.yml](/Users/alex/project/kindergarten_ERP/erp/.github/workflows/ci.yml)

### 포트폴리오 / 면접 자료

- [docs/portfolio/architecture/system-architecture.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/architecture/system-architecture.md)
- [docs/portfolio/demo/demo-preflight.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-preflight.md)
- [docs/portfolio/demo/demo-runbook.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-runbook.md)
- [docs/portfolio/hiring-pack/backend-hiring-pack.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/hiring-pack/backend-hiring-pack.md)
- [docs/portfolio/interview/interview_one_pager.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/interview/interview_one_pager.md)
- [docs/portfolio/interview/interview_qa_script.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/interview/interview_qa_script.md)
- [docs/portfolio/performance/README.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/performance/README.md)

## 4) 블로그 설계 원칙

### 원칙 1. 연대기와 개념을 같이 잡는다

시간순으로만 쓰면 입문자가 길을 잃고, 개념순으로만 쓰면 실제 프로젝트 감각이 사라진다.

그래서 각 글은 다음 둘을 같이 잡는다.

- 이 시점에 왜 이 기능이 필요했는가
- 그 기능이 어떤 Spring 개념을 보여주는가

### 원칙 2. 클래스 이름만 말하지 말고 메서드까지 내려간다

예시:

- `SecurityConfig.securityFilterChain()`
- `SecurityConfig.buildPublicEndpoints()`
- `RedisConfig.redisConnectionFactory()`
- `QuerydslConfig.jpaQueryFactory()`
- `AuthService.login()`
- `AuthService.refreshAccessToken()`
- `AuthSessionRegistryService.registerSession()`
- `KidApplicationService.offer()`
- `KidApplicationService.acceptOffer()`
- `AttendanceChangeRequestService.approve()`
- `NotificationOutbox.create()`

### 원칙 3. 테스트를 글의 마지막 장식이 아니라 설계 검증으로 사용한다

각 글은 최소 1개 이상의 테스트 클래스를 함께 설명한다.

### 원칙 4. “최종 구조”와 “진화 과정”을 분리해서 설명한다

예시:

- 인증은 처음부터 세션 레지스트리와 refresh rotation이 있던 것이 아니라, later hardening으로 발전했다.
- 입학 신청도 처음에는 단순 승인/거절이었고, later batch에서 waitlist/offer/expiry로 확장되었다.

### 원칙 5. 글 끝에는 항상 취업 포인트를 넣는다

각 글은 반드시 아래 질문 중 하나와 연결한다.

- 왜 이 기술을 선택했는가
- 어떤 장애/보안 문제를 예상했는가
- 테스트는 왜 이렇게 짰는가
- 운영 환경에서 무엇이 달라지는가

## 5) 글 하나의 Definition of Done

각 글은 아래 조건을 만족해야 완료로 본다.

- 실제 파일 경로가 3개 이상 들어간다.
- 실제 클래스 이름이 2개 이상 들어간다.
- 실제 메서드가 2개 이상 들어간다.
- 테스트 또는 CI 근거가 1개 이상 들어간다.
- 입문자용 개념 설명 섹션이 있다.
- “처음 설계”와 “나중에 바뀐 점”이 분리되어 있다.
- 글 끝에 `취업 포인트` 섹션이 있다.

## 6) 집필 순서와 공개 순서

### 공개 순서

독자가 처음부터 따라올 수 있게 `01 -> 24` 순으로 공개한다.

### 실제 집필 순서

효율을 위해 아래 순서로 먼저 집필한다.

1. `02` Gradle / Spring Boot bootstrap
2. `03` Docker / MySQL / Redis
3. `04` profile / application.yml 전략
4. `11` Spring Security + 회원가입 / 로그인
5. `13` 멀티테넌시 권한 하드닝

이유는 세 가지다.

- 독자 유입이 많은 주제다.
- 실제 코드 근거가 명확하다.
- 취업 포인트가 빨리 드러난다.

## 7) 연재 전체 구조

### Part A. 왜 이 프로젝트를 시작했는가

#### 01. 왜 유치원 ERP를 주제로 잡았는가

- 글의 질문:
  - 왜 이 도메인이 첫 백엔드 포트폴리오로 적절한가
- 핵심 파일:
  - [README.md](/Users/alex/project/kindergarten_ERP/erp/README.md)
  - [project-idea.md](/Users/alex/project/kindergarten_ERP/erp/docs/archive/legacy/project-idea.md)
  - [project-plan.md](/Users/alex/project/kindergarten_ERP/erp/docs/archive/legacy/project-plan.md)
  - [user-guide.md](/Users/alex/project/kindergarten_ERP/erp/docs/guides/user-guide.md)
- 반드시 설명할 코드:
  - [MemberRole.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/entity/MemberRole.java)
  - [ApplicationStatus.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kidapplication/entity/ApplicationStatus.java)
- 입문자 포인트:
  - 도메인 선정은 기능 나열이 아니라 사용자/권한/상태 전이 관점으로 해야 한다.
- 취업 포인트:
  - “왜 이 주제를 골랐나” 질문에 대한 답변의 기반 글

### Part B. 프로젝트 부트스트랩과 공통 설정

#### 02. `settings.gradle`, `build.gradle`, `ErpApplication`으로 시작하기

- 글의 질문:
  - Spring Boot 프로젝트는 어떤 최소 파일에서 시작되는가
- 핵심 파일:
  - [settings.gradle](/Users/alex/project/kindergarten_ERP/erp/settings.gradle)
  - [build.gradle](/Users/alex/project/kindergarten_ERP/erp/build.gradle)
  - [ErpApplication.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/ErpApplication.java)
- 반드시 설명할 메서드 / 설정:
  - `ErpApplication.main()`
  - `tasks.named("test")`
  - `tasks.register("fastTest")`
  - `tasks.register("integrationTest")`
  - `tasks.register("performanceSmokeTest")`
- 함께 인용할 문서:
  - [phase00_setup.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase00_setup.md)
- 입문자 포인트:
  - plugin, dependency, task가 각각 무슨 역할인지
- 취업 포인트:
  - “왜 Java 17 / Spring Boot 3.5 / Gradle을 선택했나”

#### 03. Docker로 MySQL / Redis / monitoring overlay 만들기

- 글의 질문:
  - 왜 로컬 개발 환경을 설치형이 아니라 compose로 고정했는가
- 핵심 파일:
  - [docker-compose.yml](/Users/alex/project/kindergarten_ERP/erp/docker/docker-compose.yml)
  - [docker-compose.monitoring.yml](/Users/alex/project/kindergarten_ERP/erp/docker/docker-compose.monitoring.yml)
  - [prometheus.yml](/Users/alex/project/kindergarten_ERP/erp/docker/monitoring/prometheus/prometheus.yml)
- 반드시 설명할 포인트:
  - MySQL volume / init mount / charset 설정
  - Redis appendonly
  - monitoring overlay 분리 이유
- 입문자 포인트:
  - container, volume, port binding, bridge network
- 취업 포인트:
  - “로컬 환경 재현성을 어떻게 확보했나”

#### 04. `application.yml`과 profile을 왜 이렇게 나눴는가

- 글의 질문:
  - local / demo / prod를 어떻게 분리해야 나중에 운영성이 무너지지 않는가
- 핵심 파일:
  - [application.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application.yml)
  - [application-local.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-local.yml)
  - [application-demo.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-demo.yml)
  - [application-prod.yml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/application-prod.yml)
- 반드시 설명할 설정:
  - `spring.profiles.group.demo`
  - `management.*`
  - `springdoc.*`
  - `app.security.management-surface.*`
  - `notification.delivery.*`
- 입문자 포인트:
  - 공통 설정과 환경별 override의 차이
- 취업 포인트:
  - “prod에서 Swagger를 왜 닫았나”, “demo를 왜 local group으로 묶었나”

#### 05. JPA / Flyway / QueryDSL / Redis / Cache 공통 설정 잡기

- 글의 질문:
  - 첫 프로젝트에서 persistence 공통 설정을 어디까지 미리 깔아야 하는가
- 핵심 파일:
  - [BaseEntity.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/common/BaseEntity.java)
  - [JpaConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/JpaConfig.java)
  - [QuerydslConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/QuerydslConfig.java)
  - [RedisConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/RedisConfig.java)
  - [CacheConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/CacheConfig.java)
  - [V1__init_schema.sql](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration/V1__init_schema.sql)
- 반드시 설명할 메서드:
  - `QuerydslConfig.jpaQueryFactory()`
  - `RedisConfig.redisConnectionFactory()`
  - `RedisConfig.redisTemplate()`
  - `CacheConfig.cacheManager()`
- 입문자 포인트:
  - OSIV OFF
  - `default_batch_fetch_size`
  - RedisTemplate 직렬화
  - Flyway와 ddl-auto의 역할 차이
- 취업 포인트:
  - “처음부터 운영형 설정을 왜 깔았나”

#### 06. `global` / `domain` 패키지 구조를 어떻게 잡았는가

- 글의 질문:
  - 작은 프로젝트도 왜 패키지 구조를 먼저 정리해야 하는가
- 핵심 파일:
  - [system-architecture.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/architecture/system-architecture.md)
  - `src/main/java/com/erp/domain/*`
  - `src/main/java/com/erp/global/*`
- 반드시 설명할 포인트:
  - `domain/{controller,service,repository,entity,dto}`
  - `global/config`, `global/security`, `global/monitoring`
  - `BaseEntity` 위치 선정 이유
- 입문자 포인트:
  - 레이어드 아키텍처와 도메인 패키징의 차이
- 취업 포인트:
  - “폴더 구조를 왜 이렇게 나눴나”

### Part C. 핵심 도메인 모델 만들기

#### 07. `Member`, `Kindergarten`, `Classroom`으로 첫 관계 만들기

- 글의 질문:
  - 사용자와 소속 조직, 반을 어떻게 모델링하는가
- 핵심 파일:
  - [Member.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/entity/Member.java)
  - [Kindergarten.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kindergarten/entity/Kindergarten.java)
  - [Classroom.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/classroom/entity/Classroom.java)
  - [MemberResponse.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/dto/response/MemberResponse.java)
  - [ClassroomRequest.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/classroom/dto/request/ClassroomRequest.java)
- 반드시 설명할 메서드:
  - `Member.create()`
  - `Member.updateProfile()`
  - `Member.assignKindergarten()`
  - `Classroom.create()`
  - `Classroom.assignTeacher()`
  - `Classroom.canResizeTo()`
- 함께 인용할 테스트 / 문서:
  - [ClassroomApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/ClassroomApiIntegrationTest.java)
  - [KindergartenApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/KindergartenApiIntegrationTest.java)
- 입문자 포인트:
  - 정적 팩토리 메서드를 왜 쓰는가
  - 엔티티에 어디까지 비즈니스 메서드를 둘 것인가
- 취업 포인트:
  - “도메인 모델링을 어떻게 했나”

#### 08. `Kid`, `ParentKid`, `Attendance`로 첫 업무 Aggregate 만들기

- 글의 질문:
  - 원생과 보호자 관계, 출석 상태를 어떻게 모델링하는가
- 핵심 파일:
  - [Kid.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/entity/Kid.java)
  - [ParentKid.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/entity/ParentKid.java)
  - [Attendance.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/attendance/entity/Attendance.java)
  - [KidRequest.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/dto/request/KidRequest.java)
  - [KidDetailResponse.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/dto/response/KidDetailResponse.java)
- 반드시 설명할 메서드:
  - `Kid.create()`
  - `Kid.assignClassroom()`
  - `Kid.addParent()`
  - `Attendance.create()`
  - `Attendance.recordDropOff()`
  - `Attendance.markAbsent()`
- 함께 인용할 테스트:
  - [KidApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/KidApiIntegrationTest.java)
  - [AttendanceApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AttendanceApiIntegrationTest.java)
- 입문자 포인트:
  - ManyToOne / OneToMany 관계
  - 상태 enum을 엔티티 메서드와 같이 쓰는 법
- 취업 포인트:
  - 첫 CRUD를 넘어 비즈니스 상태를 엔티티로 옮긴 이유

#### 09. 알림장, 공지, 알림으로 기능을 확장하는 법

- 글의 질문:
  - 서로 비슷해 보이지만 다른 도메인을 어떻게 분리하는가
- 핵심 파일:
  - `src/main/java/com/erp/domain/notepad/*`
  - `src/main/java/com/erp/domain/announcement/*`
  - `src/main/java/com/erp/domain/notification/*`
- 반드시 설명할 메서드:
  - `NotificationService.create()`
  - `NotificationService.notifyWithLink()`
  - `NotificationService.markAsRead()`
  - `NotificationOutbox.create()`는 이 글의 마지막에서 “다음 버전” 예고로 연결
- 함께 인용할 테스트:
  - [NotepadApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/NotepadApiIntegrationTest.java)
  - [AnnouncementApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AnnouncementApiIntegrationTest.java)
  - [NotificationApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/NotificationApiIntegrationTest.java)
- 입문자 포인트:
  - “기능 비슷하다고 서비스 하나로 뭉치지 않는” 기준
- 취업 포인트:
  - 서비스 경계와 알림 시스템 초기 설계 설명

#### 10. 일정, 대시보드, 신청 도메인으로 조회와 상태 전이를 넓히기

- 글의 질문:
  - CRUD를 넘어 반복 조회와 승인 흐름은 어떻게 다르게 설계하는가
- 핵심 파일:
  - `src/main/java/com/erp/domain/calendar/*`
  - `src/main/java/com/erp/domain/dashboard/*`
  - `src/main/java/com/erp/domain/kidapplication/*`
  - `src/main/java/com/erp/domain/kindergartenapplication/*`
- 반드시 설명할 메서드:
  - `KidApplicationService.apply()`
  - `DashboardService`의 집계 메서드
  - 캘린더 occurrence 계산 메서드
- 함께 인용할 테스트:
  - [CalendarApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/CalendarApiIntegrationTest.java)
  - [DashboardApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/DashboardApiIntegrationTest.java)
  - [KidApplicationApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/KidApplicationApiIntegrationTest.java)
- 입문자 포인트:
  - 조회 모델과 쓰기 모델의 차이
- 취업 포인트:
  - “숫자를 만든 것”과 “믿을 수 있는 숫자”의 차이

### Part D. 인증과 보안을 제대로 붙이기

#### 11. `SecurityConfig`와 회원가입/로그인 기본 흐름 만들기

- 글의 질문:
  - 처음 Security를 붙일 때 무엇부터 구현해야 하는가
- 핵심 파일:
  - [SecurityConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/SecurityConfig.java)
  - [AuthApiController.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/controller/AuthApiController.java)
  - [AuthService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthService.java)
  - [CustomUserDetailsService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/user/CustomUserDetailsService.java)
- 반드시 설명할 메서드:
  - `SecurityConfig.securityFilterChain()`
  - `SecurityConfig.buildPublicEndpoints()`
  - `AuthService.signUp()`
  - `AuthService.login()`
- 함께 인용할 테스트:
  - [AuthApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AuthApiIntegrationTest.java)
- 입문자 포인트:
  - AuthenticationManager, PasswordEncoder, FilterChain
- 취업 포인트:
  - “세션 로그인 대신 JWT cookie를 선택한 이유”

#### 12. `JwtTokenProvider`, `JwtFilter`, `AuthService`로 쿠키 JWT 연결하기

- 글의 질문:
  - JWT를 실제 요청 흐름에 어떻게 넣는가
- 핵심 파일:
  - [JwtTokenProvider.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtTokenProvider.java)
  - [JwtFilter.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtFilter.java)
  - [JwtProperties.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtProperties.java)
- 반드시 설명할 메서드:
  - `JwtTokenProvider.createAccessToken()`
  - `JwtTokenProvider.createRefreshToken()`
  - `JwtTokenProvider.getSessionId()`
  - `JwtTokenProvider.validateToken()`
- 함께 인용할 문서:
  - [phase01_auth.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase01_auth.md)
  - [phase17_jwt_refresh_session_rotation.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase17_jwt_refresh_session_rotation.md)
- 입문자 포인트:
  - claim, signature, TTL, cookie 옵션
- 취업 포인트:
  - “JWT를 stateless로 쓰되 왜 Redis와 같이 묶었나”

#### 13. 멀티테넌시 보안 문제를 어떻게 발견하고 고쳤는가

- 글의 질문:
  - 기능이 되더라도 왜 보안상 위험할 수 있는가
- 핵심 파일:
  - [AccessPolicyService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/access/AccessPolicyService.java)
  - [SecurityConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/SecurityConfig.java)
  - 관련 domain service 전반
- 반드시 설명할 메서드:
  - `AccessPolicyService.getRequester()`
  - `validateSameKindergarten()`
  - `validateKidReadAccess()`
  - `validateAttendanceChangeRequestReviewAccess()`
  - `validateNotificationReceiverAccess()`
- 함께 인용할 테스트 / 문서:
  - [phase13_security_hardening.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase13_security_hardening.md)
  - [phase14_multitenant_access_hardening.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase14_multitenant_access_hardening.md)
- 입문자 포인트:
  - Controller에서 막는 것과 Service에서 막는 것의 차이
- 취업 포인트:
  - 보안 리뷰와 하드닝 경험을 보여주는 대표 글

### Part E. 테스트와 CI를 실전형으로 끌어올리기

#### 14. H2가 아니라 Testcontainers를 붙인 이유

- 글의 질문:
  - 왜 통합 테스트를 실제 MySQL / Redis에 가깝게 바꿨는가
- 핵심 파일:
  - [TestcontainersSupport.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/common/TestcontainersSupport.java)
  - [BaseIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/common/BaseIntegrationTest.java)
  - [application-test.yml](/Users/alex/project/kindergarten_ERP/erp/src/test/resources/application-test.yml)
- 반드시 설명할 메서드:
  - container bootstrap
  - test data reset helper
  - committed transaction helper
- 함께 인용할 문서:
  - [phase15_testcontainers_integration_test_stack.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase15_testcontainers_integration_test_stack.md)
- 입문자 포인트:
  - 왜 H2와 MySQL은 같지 않은가
- 취업 포인트:
  - “실환경형 통합 테스트를 어떻게 구성했나”

#### 15. GitHub Actions와 태그 기반 테스트 스위트 분리

- 글의 질문:
  - 테스트가 많아졌을 때 CI를 어떻게 분리해야 하는가
- 핵심 파일:
  - [build.gradle](/Users/alex/project/kindergarten_ERP/erp/build.gradle)
  - [ci.yml](/Users/alex/project/kindergarten_ERP/erp/.github/workflows/ci.yml)
- 반드시 설명할 설정:
  - `fastTest`
  - `integrationTest`
  - `performanceSmokeTest`
  - workflow job 분리
- 함께 인용할 테스트:
  - [AuditConsolePerformanceSmokeTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java)
- 함께 인용할 문서:
  - [phase16_github_actions_ci.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase16_github_actions_ci.md)
  - [phase19_ci_fast_integration_split.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase19_ci_fast_integration_split.md)
  - [phase44_tagged_ci_readiness_and_hiring_pack.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase44_tagged_ci_readiness_and_hiring_pack.md)
- 입문자 포인트:
  - 빠른 테스트와 무거운 테스트를 섞으면 왜 안 되는가
- 취업 포인트:
  - “CI를 어떻게 설계했나”

### Part F. 인증을 운영형으로 발전시키기

#### 16. Refresh rotation과 활성 세션 제어로 인증을 한 단계 올리기

- 글의 질문:
  - 로그인 기능을 세션 관리 기능으로 어떻게 키우는가
- 핵심 파일:
  - [AuthService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthService.java)
  - [AuthSessionRegistryService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthSessionRegistryService.java)
  - [JwtTokenProvider.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/jwt/JwtTokenProvider.java)
- 반드시 설명할 메서드:
  - `AuthService.refreshAccessToken()`
  - `AuthService.getActiveSessions()`
  - `AuthService.revokeSession()`
  - `AuthSessionRegistryService.registerSession()`
  - `AuthSessionRegistryService.rotateSession()`
  - `AuthSessionRegistryService.revokeOtherSessions()`
- 함께 인용할 문서:
  - [phase17_jwt_refresh_session_rotation.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase17_jwt_refresh_session_rotation.md)
  - [phase39_management_plane_and_active_session_control.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase39_management_plane_and_active_session_control.md)
- 입문자 포인트:
  - sessionId, refresh token, revoke, Redis TTL
- 취업 포인트:
  - “JWT인데도 왜 세션 제어가 가능한가”

#### 17. 로그인 남용 방어: Rate Limit과 Client IP 신뢰 모델

- 글의 질문:
  - 로그인 API는 왜 평범한 CRUD API처럼 다루면 안 되는가
- 핵심 파일:
  - [AuthRateLimitService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthRateLimitService.java)
  - [ClientIpResolver.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/ClientIpResolver.java)
  - [ClientIpProperties.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/ClientIpProperties.java)
- 반드시 설명할 메서드:
  - 로그인 사전 검사 / 실패 기록 / 성공 초기화 흐름
  - `ClientIpResolver`의 trusted proxy 해석
- 함께 인용할 문서:
  - [phase21_auth_rate_limit.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase21_auth_rate_limit.md)
  - [phase24_auth_client_ip_trust_model.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase24_auth_client_ip_trust_model.md)
  - [phase25_login_rate_limit_policy_refinement.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase25_login_rate_limit_policy_refinement.md)
- 입문자 포인트:
  - X-Forwarded-For를 무조건 믿으면 안 되는 이유
- 취업 포인트:
  - “보안 정책을 어떤 기준으로 세웠나”

#### 18. OAuth2와 소셜 계정 lifecycle을 안전하게 설계하기

- 글의 질문:
  - 소셜 로그인은 “붙였다”가 아니라 계정 정책까지 설계해야 한다는 뜻이 무엇인가
- 핵심 파일:
  - [CustomOAuth2UserService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/oauth2/CustomOAuth2UserService.java)
  - [OAuth2AuthenticationSuccessHandler.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/oauth2/OAuth2AuthenticationSuccessHandler.java)
  - [SocialAccountLinkService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/SocialAccountLinkService.java)
  - [Member.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/entity/Member.java)
  - [MemberSocialAccount.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/entity/MemberSocialAccount.java)
- 반드시 설명할 메서드:
  - `Member.createSocial()`
  - `Member.linkSocialAccount()`
  - `Member.unlinkSocialAccount()`
  - `OAuth2AuthenticationSuccessHandler`의 충돌 분기
- 함께 인용할 문서:
  - `phase26` ~ `phase32`
- 입문자 포인트:
  - provider / providerId / account linking / immutable binding
- 취업 포인트:
  - “소셜 계정 교체를 왜 금지했나”

### Part G. 운영성, 감사 추적, 장애 대응

#### 19. 인증 감사 로그를 어떻게 설계하고 운영 콘솔까지 연결했는가

- 글의 질문:
  - 로그인/refresh/소셜 연결 같은 보안 사건을 어떻게 남기고 조회하는가
- 핵심 파일:
  - `src/main/java/com/erp/domain/authaudit/*`
  - [V10__create_auth_audit_log.sql](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration/V10__create_auth_audit_log.sql)
  - [V11__denormalize_auth_audit_log_and_add_retention_archive.sql](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration/V11__denormalize_auth_audit_log_and_add_retention_archive.sql)
- 반드시 설명할 메서드:
  - `AuthAuditLogService`의 기록 메서드
  - `AuthAuditLogQueryService`의 필터 조회
  - retention/archive 스케줄링
- 함께 인용할 테스트 / 문서:
  - [AuthAuditApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AuthAuditApiIntegrationTest.java)
  - [AuthAuditRetentionIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/AuthAuditRetentionIntegrationTest.java)
  - `phase33`, `phase35`, `phase37`, `phase38`
- 입문자 포인트:
  - audit log와 일반 application log의 차이
- 취업 포인트:
  - “보안 사건을 어떻게 남기고 보존하나”

#### 20. OpenAPI, management plane, Actuator, Prometheus, structured logging

- 글의 질문:
  - API 문서와 운영 노출면을 어떻게 같이 설계하는가
- 핵심 파일:
  - [OpenApiConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/OpenApiConfig.java)
  - [ManagementSurfaceProperties.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/ManagementSurfaceProperties.java)
  - [CriticalDependenciesHealthIndicator.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/monitoring/CriticalDependenciesHealthIndicator.java)
  - [PrometheusRegistryConfig.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/monitoring/PrometheusRegistryConfig.java)
  - [PrometheusScrapeController.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/monitoring/PrometheusScrapeController.java)
  - [logback-spring.xml](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/logback-spring.xml)
- 반드시 설명할 메서드:
  - `OpenApiConfig.apiV1GroupedOpenApi()`
  - `OpenApiConfig.kindergartenErpOpenApi()`
  - `CriticalDependenciesHealthIndicator.health()`
  - `PrometheusScrapeController.scrape()`
- 함께 인용할 테스트 / 문서:
  - [ObservabilityIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/ObservabilityIntegrationTest.java)
  - `phase34`, `phase36`, `phase39`, `phase44`
- 입문자 포인트:
  - liveness vs readiness
  - app port vs management port
- 취업 포인트:
  - “운영 환경에서 어떤 노출면을 닫고 어떤 지표를 보는가”

#### 21. 알림 전달을 `notification_outbox`로 비동기화한 이유

- 글의 질문:
  - 왜 알림은 DB 저장과 외부 채널 전달을 분리해야 하는가
- 핵심 파일:
  - [NotificationOutbox.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/notification/entity/NotificationOutbox.java)
  - [NotificationDispatchService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/notification/service/NotificationDispatchService.java)
  - [NotificationDeliveryPolicyService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/notification/service/NotificationDeliveryPolicyService.java)
  - [NotificationDeliveryProperties.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/notification/config/NotificationDeliveryProperties.java)
  - [V12__add_notification_outbox.sql](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration/V12__add_notification_outbox.sql)
- 반드시 설명할 메서드:
  - `NotificationOutbox.create()`
  - `markProcessing()`
  - `scheduleRetry()`
  - `markDeadLetter()`
- 함께 인용할 테스트 / 문서:
  - [NotificationOutboxIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/NotificationOutboxIntegrationTest.java)
  - [NotificationOutboxRetryIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/integration/NotificationOutboxRetryIntegrationTest.java)
  - `phase40_notification_outbox_and_incident_channel.md`
- 입문자 포인트:
  - retry / dead-letter / backoff
- 취업 포인트:
  - “동기 처리의 한계를 어떻게 해결했나”

### Part H. 운영형 워크플로우로 확장하기

#### 22. 반 정원, waitlist, offer expiry로 입학 워크플로우를 모델링하기

- 글의 질문:
  - 단순 승인/거절을 왜 상태 머신으로 바꿔야 하는가
- 핵심 파일:
  - [Classroom.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/classroom/entity/Classroom.java)
  - [KidApplication.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kidapplication/entity/KidApplication.java)
  - [KidApplicationService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java)
  - [KidApplicationWorkflowProperties.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kidapplication/config/KidApplicationWorkflowProperties.java)
  - [V13__add_admission_workflow_attendance_requests_and_domain_audit.sql](/Users/alex/project/kindergarten_ERP/erp/src/main/resources/db/migration/V13__add_admission_workflow_attendance_requests_and_domain_audit.sql)
- 반드시 설명할 메서드:
  - `Classroom.remainingSeats()`
  - `Classroom.canResizeTo()`
  - `KidApplication.placeOnWaitlist()`
  - `KidApplication.offerSeat()`
  - `KidApplication.acceptOffer()`
  - `KidApplicationService.expireOffers()`
- 함께 인용할 테스트 / 문서:
  - [KidApplicationApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/KidApplicationApiIntegrationTest.java)
  - `phase41_admission_capacity_waitlist_workflow.md`
- 입문자 포인트:
  - 상태 enum + 엔티티 메서드 + 배치 처리의 조합
- 취업 포인트:
  - “운영형 워크플로우를 어떻게 모델링했나”

#### 23. 학부모 출결 변경 요청과 업무 감사 로그를 왜 같이 설계했는가

- 글의 질문:
  - 요청/승인 흐름은 왜 최종 데이터와 별도 aggregate로 가져가야 하는가
- 핵심 파일:
  - [AttendanceChangeRequest.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/attendance/entity/AttendanceChangeRequest.java)
  - [AttendanceChangeRequestService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java)
  - [DomainAuditLog.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/domainaudit/entity/DomainAuditLog.java)
  - [DomainAuditLogService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/domainaudit/service/DomainAuditLogService.java)
- 반드시 설명할 메서드:
  - `AttendanceChangeRequest.create()`
  - `AttendanceChangeRequest.approve()`
  - `AttendanceChangeRequest.reject()`
  - `AttendanceChangeRequest.cancel()`
  - `AttendanceChangeRequestService.create()`
  - `AttendanceChangeRequestService.approve()`
  - `DomainAuditLog.create()`
  - `DomainAuditLogService.record()`
- 함께 인용할 테스트 / 문서:
  - [AttendanceChangeRequestApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/AttendanceChangeRequestApiIntegrationTest.java)
  - [DomainAuditApiIntegrationTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/api/DomainAuditApiIntegrationTest.java)
  - `phase42_attendance_change_request_workflow.md`
  - `phase43_domain_audit_log.md`
- 입문자 포인트:
  - 요청 데이터와 최종 데이터 분리
- 취업 포인트:
  - “감사 추적을 왜 업무 로그로 분리했나”

### Part I. 시연과 취업 패키징으로 마무리하기

#### 24. Demo seed, 아키텍처 문서, 성능 스토리, 면접 자료로 프로젝트를 마감하기

- 글의 질문:
  - 코드가 좋아도 왜 문서와 데모가 없으면 취업 포트폴리오가 약해지는가
- 핵심 파일:
  - [DataLoader.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/config/DataLoader.java)
  - [demo-preflight.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-preflight.md)
  - [demo-runbook.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-runbook.md)
  - [backend-hiring-pack.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/hiring-pack/backend-hiring-pack.md)
  - [interview_one_pager.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/interview/interview_one_pager.md)
  - [interview_qa_script.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/interview/interview_qa_script.md)
  - [docs/portfolio/performance/README.md](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/performance/README.md)
- 반드시 설명할 메서드:
  - `DataLoader.run()`
  - `createKindergarten()`
  - `createMember()`
  - `createAttendance()`
  - `createAuthAuditLog()`
- 함께 인용할 테스트 / 문서:
  - [DashboardPerformanceStoryTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/performance/DashboardPerformanceStoryTest.java)
  - [NotepadPerformanceStoryTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/performance/NotepadPerformanceStoryTest.java)
  - [AuditConsolePerformanceSmokeTest.java](/Users/alex/project/kindergarten_ERP/erp/src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java)
  - `phase44_tagged_ci_readiness_and_hiring_pack.md`
- 입문자 포인트:
  - 왜 demo 계정과 실행 순서를 고정해야 하는가
- 취업 포인트:
  - “이 프로젝트를 면접에서 어떻게 보여줄 것인가”

## 8) 글 작성 워크플로우

각 글은 아래 순서로 쓴다.

1. 관련 결정 로그를 먼저 읽는다.
2. 현재 최종 코드 파일을 연다.
3. 관련 테스트 클래스를 연다.
4. 문서 / README / 포트폴리오 자료를 읽어 설명에 필요한 운영 포인트를 뽑는다.
5. `blog/_post_template.md`를 기반으로 초안을 작성한다.
6. 코드 인용이 실제 메서드명과 맞는지 마지막에 다시 검수한다.

## 9) 글에서 처음 등장시키는 용어 순서

입문자가 한꺼번에 무너지지 않도록 용어를 아래 순서로 도입한다.

1. Gradle, dependency, plugin, task
2. container, volume, port, network
3. profile, configuration property, bean
4. entity, repository, service, controller, dto
5. transaction, lazy loading, OSIV, migration
6. authentication, authorization, filter chain, password encoder
7. JWT, claim, cookie, refresh token, rotation
8. Testcontainers, integration test, CI suite
9. audit log, outbox, retry, dead-letter
10. Actuator, readiness, Prometheus, Grafana

## 10) 결정 로그 매핑

블로그는 아래 decision 문서 흐름을 적극 활용한다.

- 초기 기능: `phase00` ~ `phase10`
- 보안 하드닝: `phase13` ~ `phase18`
- CI / 인증 고도화: `phase19` ~ `phase25`
- OAuth2 / 소셜 정책: `phase26` ~ `phase32`
- 감사 로그 / 운영성: `phase33` ~ `phase40`
- 운영형 워크플로우 / 채용 패키지: `phase41` ~ `phase44`

## 11) 리스크와 대응

- 리스크:
  - 최종 구조를 마치 처음부터 있던 것처럼 써서 성장 서사가 사라질 수 있다.
  - 글이 코드 설명만 하다 끝나 입문자에게 어려워질 수 있다.
  - 반대로 개념 설명만 하다 실제 프로젝트 감각이 약해질 수 있다.
  - Phase 문서를 그대로 옮겨 적으면 블로그의 가치가 떨어진다.
- 대응:
  - 각 글에 `처음 버전 / 문제 / 개선 버전`을 넣는다.
  - 메서드 단위 설명과 개념 설명을 같이 쓴다.
  - 테스트와 운영 문서까지 반드시 연결한다.
  - decision 문서는 근거로만 쓰고, 블로그는 재구성된 학습 서사로 쓴다.
