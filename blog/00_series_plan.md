# Kindergarten ERP 개발 블로그 시리즈 계획

## 1. 시리즈 목적

이 시리즈는 단순히 “프로젝트를 만들었다”를 기록하는 글이 아닙니다.

다음 질문에 답하는 형태로 씁니다.

- 프로젝트를 처음 시작할 때 무엇부터 설계해야 하나?
- Spring Boot 프로젝트는 어떤 파일부터 만들어야 하나?
- 기능이 커질수록 인증, 권한, 테스트, 운영은 어떻게 같이 발전시켜야 하나?
- 취업용 포트폴리오는 어떤 흐름으로 보여줘야 하나?

즉, **초심자용 개발 학습 자료**이자 **취업용 백엔드 성장 서사**가 되도록 설계합니다.

## 2. 독자 정의

### 1차 독자

- Java를 막 공부하기 시작한 입문자
- Spring Boot로 첫 CRUD 프로젝트를 만들고 싶은 사람
- 백엔드 취업 준비를 시작한 사람

### 2차 독자

- 이미 CRUD는 해봤지만, 프로젝트를 더 운영형으로 고도화하고 싶은 사람
- 테스트, 보안, 관측성, 포트폴리오 문서화까지 같이 배우고 싶은 사람

## 3. 시리즈 전체 구조

전체 시리즈는 4개의 큰 구간으로 나눕니다.

1. 기초 구축
   - 프로젝트 주제 선정
   - Gradle/Spring Boot 초기 구성
   - Docker/MySQL/Redis/local profile 구성
   - JPA/Flyway/Redis/Security 기초 설정

2. 핵심 도메인 구현
   - 회원/유치원/반/원생
   - 인증/JWT
   - 출석/알림장/공지/일정/신청

3. 운영형 백엔드로 고도화
   - 권한 경계
   - 세션/Rate Limit/OAuth2 lifecycle
   - 감사 로그/outbox/incident 대응
   - observability/readiness/CI/Testcontainers

4. 포트폴리오 패키징
   - 운영형 워크플로우
   - 성능 스토리
   - 인터뷰 문서
   - 데모 runbook

## 4. 글 작성 공통 규칙

각 글은 아래 순서를 고정합니다.

1. 이번 글에서 해결할 문제
2. 선행 개념 설명
3. 어떤 파일을 만들거나 수정하는지
4. 클래스와 메서드를 왜 그렇게 나눴는지
5. 실제 요청/응답 또는 화면 흐름
6. 테스트로 어떻게 검증했는지
7. 다음 글과 어떻게 이어지는지
8. 취업 포인트

## 5. 연재 목록

아래 순서대로 작성합니다.

### 01. 왜 유치원 ERP를 주제로 잡았는가

- 목적
  - 프로젝트 주제 선정과 범위 설정 과정을 설명
- 다룰 자료
  - `README.md`
  - `docs/archive/legacy/project-idea.md`
  - `docs/archive/legacy/project-plan.md`
  - `docs/guides/user-guide.md`
- 설명 포인트
  - 원장/교사/학부모 역할 모델
  - 왜 이 도메인이 권한/상태 전이 설계에 좋은지
  - 처음에 기능을 얼마나 작게 잡았는지
- 독자가 얻는 것
  - 프로젝트 주제를 “기능 목록”이 아니라 “문제 공간”으로 잡는 법

### 02. Gradle로 Spring Boot 프로젝트 뼈대 만들기

- 목적
  - 가장 처음 어떤 설정 파일이 필요한지 설명
- 다룰 파일
  - `settings.gradle`
  - `build.gradle`
  - `src/main/java/com/erp/ErpApplication.java`
- 다룰 클래스/메서드
  - `ErpApplication`
  - Gradle plugin/dependency/task 설정
- 설명 포인트
  - 왜 Java 17을 선택했는지
  - Spring Boot starter를 어떤 기준으로 넣는지
  - 나중에 `fastTest`, `integrationTest`, `performanceSmokeTest`까지 어떻게 확장됐는지
- 독자가 얻는 것
  - 첫 Spring Boot 프로젝트의 최소 파일 구성

### 03. Docker로 MySQL/Redis 개발 환경 만들기

- 목적
  - 로컬 개발 환경을 코드 밖에서 재현 가능하게 만드는 방법 설명
- 다룰 파일
  - `docker/docker-compose.yml`
  - `docker/docker-compose.monitoring.yml`
- 설명 포인트
  - 왜 DB/Redis를 로컬 설치가 아니라 Docker로 띄우는지
  - 초반 인프라와 후반 monitoring overlay가 어떻게 분리되는지
- 독자가 얻는 것
  - Spring Boot 프로젝트의 로컬 인프라 준비 순서

### 04. application.yml과 profile을 어떻게 설계했는가

- 목적
  - 환경별 설정 분리를 설명
- 다룰 파일
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-demo.yml`
  - `src/main/resources/application-prod.yml`
- 설명 포인트
  - 공통 설정과 환경별 설정을 나누는 기준
  - local/demo/prod를 왜 다르게 두는지
  - 보안 노출면과 시연 편의성 사이의 균형
- 독자가 얻는 것
  - 설정 파일이 커져도 유지 가능한 profile 전략

### 05. JPA, Flyway, Redis 기초 설정부터 잡기

- 목적
  - 데이터 계층과 캐시 계층의 첫 세팅 설명
- 다룰 파일/클래스
  - `src/main/java/com/erp/global/config/RedisConfig.java`
  - `src/main/resources/db/migration/*`
  - JPA 관련 공통 설정 (`application.yml`)
- 설명 포인트
  - 왜 Flyway를 초반부터 넣는지
  - 왜 OSIV를 끄는지
  - RedisTemplate은 왜 따로 설정하는지
- 독자가 얻는 것
  - “일단 H2로 시작”이 아닌 운영형 초기 세팅 감각

### 06. 패키지 구조는 왜 domain/global로 나눴는가

- 목적
  - 프로젝트 구조 설계 원칙 설명
- 다룰 파일
  - `src/main/java/com/erp/domain/*`
  - `src/main/java/com/erp/global/*`
- 설명 포인트
  - `controller/service/repository/entity/dto`를 왜 도메인 아래 묶는지
  - 공통 기능은 왜 `global`로 올리는지
- 독자가 얻는 것
  - 첫 프로젝트의 패키지 구조를 어떻게 잡을지 기준

### 07. 첫 도메인 모델: Member, Kindergarten, Classroom, Kid

- 목적
  - 도메인 중심 설계의 시작 설명
- 다룰 파일/클래스
  - `domain/member/entity/Member.java`
  - `domain/kindergarten/entity/Kindergarten.java`
  - `domain/classroom/entity/Classroom.java`
  - `domain/kid/entity/Kid.java`
  - 관련 repository/service/controller
- 설명 포인트
  - Entity를 어떤 책임 단위로 쪼갰는지
  - 정적 팩토리/상태 변경 메서드를 왜 엔티티에 넣는지
  - 회원-유치원-반-원생 관계를 어떻게 모델링했는지
- 독자가 얻는 것
  - CRUD 이전에 도메인 관계를 먼저 생각하는 법

### 08. 회원가입과 로그인: 첫 SecurityConfig 설계

- 목적
  - 인증 시스템의 첫 버전 설명
- 다룰 파일/클래스
  - `domain/auth/controller/*`
  - `domain/auth/service/AuthService.java`
  - `global/config/SecurityConfig.java`
  - `global/security/jwt/*`
  - `domain/member/service/MemberService.java`
- 설명 포인트
  - 왜 JWT cookie 방식을 선택했는지
  - `SecurityConfig.securityFilterChain()`을 어떻게 읽어야 하는지
  - 회원가입/로그인 흐름에서 service가 맡는 책임
- 독자가 얻는 것
  - Spring Security를 처음 프로젝트에 붙이는 순서

### 09. 첫 업무 기능: 출석 관리 만들기

- 목적
  - 하나의 도메인 기능을 API/서비스/엔티티/테스트로 닫는 방법 설명
- 다룰 파일/클래스
  - `domain/attendance/entity/Attendance.java`
  - `domain/attendance/service/AttendanceService.java`
  - `domain/attendance/controller/AttendanceController.java`
  - `domain/attendance/dto/*`
- 설명 포인트
  - 왜 엔티티에 상태 변경 메서드를 두는지
  - 출석 수정/삭제/특수 처리 메서드를 어떻게 나눴는지
- 독자가 얻는 것
  - 첫 비즈니스 도메인을 구현하는 방법

### 10. 알림장과 공지로 CRUD를 확장하기

- 목적
  - 비슷하지만 다른 도메인 두 개를 어떻게 분리하는지 설명
- 다룰 파일/클래스
  - `domain/notepad/*`
  - `domain/announcement/*`
- 설명 포인트
  - 읽음 처리, 조회수, 중요 공지 같은 부가 상태를 어떻게 다루는지
  - 교사/원장/학부모 권한 차이를 어디서 처리하는지
- 독자가 얻는 것
  - 기능이 많아질수록 서비스 경계를 어떻게 유지하는지

### 11. 신청/승인 도메인 만들기

- 목적
  - 단순 CRUD에서 승인 워크플로우로 넘어가는 첫 단계 설명
- 다룰 파일/클래스
  - `domain/kidapplication/*`
  - `domain/kindergartenapplication/*`
- 설명 포인트
  - 신청 엔티티와 승인 로직을 어떻게 나눴는지
  - 승인 후 어떤 후속 작업이 생기는지
- 독자가 얻는 것
  - “입력 저장” 이후의 상태 전이를 보는 시각

### 12. 캘린더와 대시보드로 조회 모델 넓히기

- 목적
  - CRUD 외에 집계/반복 조회 성격의 기능을 설명
- 다룰 파일/클래스
  - `domain/calendar/*`
  - `domain/dashboard/*`
- 설명 포인트
  - 반복 일정 occurrence를 왜 별도 계산으로 푸는지
  - 대시보드 숫자는 왜 정확도와 성능을 같이 봐야 하는지
- 독자가 얻는 것
  - 조회용 기능과 쓰기용 기능의 설계 차이

### 13. 멀티테넌시 보안 문제를 어떻게 발견하고 고쳤는가

- 목적
  - 기능 구현 이후 반드시 마주치는 권한 경계 문제 설명
- 다룰 파일/클래스
  - `global/security/access/AccessPolicyService.java`
  - `global/config/SecurityConfig.java`
  - 관련 domain service/controller 수정 지점
- 설명 포인트
  - IDOR 성격 문제가 왜 생겼는지
  - 컨트롤러가 아니라 서비스에서 requester 검증을 강제한 이유
- 독자가 얻는 것
  - “기능 완성”과 “안전한 기능”이 다르다는 감각

### 14. Testcontainers로 테스트를 현실화하기

- 목적
  - 왜 H2/mock Redis가 아니라 MySQL/Redis Testcontainers로 갔는지 설명
- 다룰 파일/클래스
  - `src/test/java/com/erp/common/TestcontainersSupport.java`
  - `src/test/java/com/erp/common/BaseIntegrationTest.java`
  - `src/test/resources/application-test.yml`
- 설명 포인트
  - 통합 테스트 베이스 클래스를 어떻게 만들었는지
  - fixture/reset/transaction helper를 왜 넣는지
- 독자가 얻는 것
  - 실환경형 테스트를 프로젝트에 도입하는 순서

### 15. GitHub Actions와 테스트 스위트 분리

- 목적
  - CI를 단순 실행에서 의미 기반 검증 체계로 발전시키는 과정 설명
- 다룰 파일
  - `.github/workflows/ci.yml`
  - `build.gradle`
  - `src/test/java/com/erp/**/*Test.java`
- 설명 포인트
  - `fastTest`, `integrationTest`, `performanceSmokeTest`를 왜 나눴는지
  - JUnit Tag를 왜 도입했는지
- 독자가 얻는 것
  - 테스트가 많아졌을 때 CI를 설계하는 법

### 16. JWT 세션을 “로그인 한 번”에서 “세션 관리”로 키우기

- 목적
  - 인증을 운영형으로 고도화한 과정을 설명
- 다룰 파일/클래스
  - `domain/auth/service/AuthService.java`
  - `domain/auth/service/AuthSessionRegistryService.java`
  - `global/security/jwt/*`
  - `domain/auth/controller/AuthApiController.java`
- 설명 포인트
  - refresh rotation
  - 활성 세션 목록
  - 개별 세션 종료
  - 세션 레지스트리와 access token revoke
- 독자가 얻는 것
  - JWT를 “세션 없는 인증”으로만 오해하지 않는 법

### 17. 로그인 남용 방어와 Client IP 신뢰 모델

- 목적
  - 인증 남용 방어를 어디까지 해야 하는지 설명
- 다룰 파일/클래스
  - `domain/auth/service/AuthRateLimitService.java`
  - `global/security/ClientIpResolver.java`
  - `global/security/ClientIpProperties.java`
- 설명 포인트
  - 왜 로그인 성공은 rate limit 예산을 먹지 않게 했는지
  - 왜 trusted proxy일 때만 forwarded header를 믿는지
- 독자가 얻는 것
  - 인증 보안은 토큰만이 아니라 네트워크 신뢰 모델도 포함한다는 점

### 18. OAuth2 소셜 로그인 lifecycle 설계

- 목적
  - 소셜 로그인은 “붙였다”로 끝나지 않는다는 점 설명
- 다룰 파일/클래스
  - `global/security/oauth2/*`
  - `domain/auth/service/SocialAccountLinkService.java`
  - `domain/member/service/MemberService.java`
  - `domain/member/entity/MemberSocialAccount.java`
- 설명 포인트
  - 자동 연결 금지
  - 명시적 link
  - password bootstrap
  - unlink safeguards
  - provider identity immutability
- 독자가 얻는 것
  - OAuth2의 실제 계정 lifecycle 설계 감각

### 19. 감사 로그와 운영 관측성은 왜 따로 설계했는가

- 목적
  - auth/domain audit, readiness, Prometheus, Grafana를 연결해서 설명
- 다룰 파일/클래스
  - `domain/authaudit/*`
  - `domain/domainaudit/*`
  - `global/monitoring/*`
  - `domain/authaudit/service/AuthAuditMetricsService.java`
- 설명 포인트
  - auth audit와 domain audit를 왜 분리했는지
  - readiness와 liveness의 차이
  - 운영자가 실제로 보는 화면과 export가 왜 필요한지
- 독자가 얻는 것
  - “로그 남기기”와 “운영 가능성”의 차이

### 20. notification_outbox와 incident 대응 흐름 만들기

- 목적
  - 동기 알림 호출을 왜 outbox로 바꾸는지 설명
- 다룰 파일/클래스
  - `domain/notification/*`
  - `notification_outbox` 관련 엔티티/서비스/워커
  - auth anomaly alert 관련 서비스
- 설명 포인트
  - retry/backoff/dead-letter
  - incident webhook
  - 앱 내 알림과 외부 전달의 분리
- 독자가 얻는 것
  - 전달 신뢰성을 설계하는 기본기

### 21. 운영형 워크플로우: 반 정원, waitlist, offer

- 목적
  - 프로젝트 후반부 도메인 고도화의 핵심 설명
- 다룰 파일/클래스
  - `domain/classroom/service/ClassroomCapacityService.java`
  - `domain/kidapplication/entity/KidApplication.java`
  - `domain/kidapplication/service/KidApplicationService.java`
  - 관련 DTO/controller
- 설명 포인트
  - 왜 승인을 단순 boolean으로 두지 않았는지
  - 왜 active offer도 좌석 점유로 보는지
- 독자가 얻는 것
  - CRUD를 상태 머신으로 바꾸는 방법

### 22. 운영형 워크플로우: AttendanceChangeRequest와 Domain Audit

- 목적
  - 요청 aggregate와 최종 snapshot을 분리한 이유 설명
- 다룰 파일/클래스
  - `domain/attendance/entity/AttendanceChangeRequest.java`
  - `domain/attendance/service/AttendanceChangeRequestService.java`
  - `domain/domainaudit/service/DomainAuditLogService.java`
  - `domain/domainaudit/service/DomainAuditLogQueryService.java`
- 설명 포인트
  - 학부모 요청과 최종 출결을 왜 분리했는지
  - 원장이 업무 감사 로그를 왜 직접 볼 수 있어야 하는지
- 독자가 얻는 것
  - 승인 흐름과 감사 추적을 함께 설계하는 방법

### 23. 성능 스토리는 어떻게 문서화해야 하는가

- 목적
  - 취업용 성능 서사를 어떻게 만드는지 설명
- 다룰 자료
  - `docs/portfolio/performance/*`
  - `src/test/java/com/erp/performance/*`
- 설명 포인트
  - before/after 측정
  - query count와 응답 시간
  - 왜 단순 “빠르다”가 아니라 수치가 필요한지
- 독자가 얻는 것
  - 성능 최적화를 블로그와 면접 스토리로 바꾸는 방법

### 24. README, 데모, 아키텍처 문서까지 패키징하기

- 목적
  - 마지막에 프로젝트를 어떻게 보여줘야 하는지 설명
- 다룰 파일
  - `docs/portfolio/hiring-pack/backend-hiring-pack.md`
  - `docs/portfolio/architecture/system-architecture.md`
  - `docs/portfolio/demo/demo-preflight.md`
  - `docs/portfolio/demo/demo-runbook.md`
  - `docs/portfolio/interview/*`
- 설명 포인트
  - 왜 기능 개발 이후 문서 패키징이 중요한지
  - 채용 담당자/면접관이 읽는 순서를 어떻게 설계했는지
- 독자가 얻는 것
  - 프로젝트를 “잘 만든 코드”에서 “잘 설명되는 포트폴리오”로 바꾸는 방법

## 6. 실제 집필 우선순위

바로 쓰기 시작할 때는 아래 순서가 가장 좋습니다.

1. 02 Gradle / Spring Boot 프로젝트 뼈대
2. 03 Docker / MySQL / Redis 개발 환경
3. 08 인증 시스템 첫 설계
4. 13 멀티테넌시 권한 경계 하드닝
5. 14 Testcontainers 테스트 현실화
6. 21~22 운영형 워크플로우
7. 24 포트폴리오 패키징

이 순서가 좋은 이유:

- 초심자에게는 시작 지점이 필요합니다.
- 취업용 독자에게는 “이 프로젝트의 강한 포인트”가 빨리 보여야 합니다.

## 7. 글마다 꼭 넣을 코드 증거

각 글마다 아래 중 최소 3개는 반드시 포함합니다.

- 실제 Java 파일 경로
- 실제 클래스 이름
- 실제 공개 메서드 이름
- 실제 테스트 클래스
- 실제 설정 파일(`build.gradle`, `application*.yml`, `docker-compose.yml`)
- 실제 화면/엔드포인트 경로

## 8. 글쓰기 시 주의점

- 설명을 너무 추상적으로 쓰지 않는다.
- “Spring Boot가 알아서 해준다”로 넘기지 않는다.
- 코드 한 줄 한 줄을 전부 설명하려고 하지 않는다.
- 대신 클래스 책임과 메서드 분리 이유를 설명한다.
- 입문자 글이어도, 잘못된 단순화는 피한다.

## 9. 다음 액션

이 계획 다음 단계는 아래 둘 중 하나입니다.

1. `01`번 글부터 실제 본문 쓰기 시작
2. 먼저 `02`, `03`, `08`, `13`처럼 핵심 글 몇 편부터 우선 집필

추천은 `02 -> 03 -> 08 -> 13` 순서입니다.
