# Kindergarten ERP 개발 블로그 시리즈 요약 인덱스

이 문서는 공개용 목차다.

상세 집필 전략, 실제 코드 근거, 메서드 단위 설명 계획, 테스트/결정 로그 매핑은 [BLOG_PLAN.md](/Users/alex/project/kindergarten_ERP/erp/BLOG_PLAN.md)를 SSOT로 사용한다.

## 공개 순서

### Part A. 문제 정의

1. 왜 유치원 ERP를 주제로 잡았는가

### Part B. 부트스트랩과 공통 설정

2. `settings.gradle`, `build.gradle`, `ErpApplication`으로 시작하기
3. Docker로 MySQL / Redis / monitoring overlay 만들기
4. `application.yml`과 profile 전략 설계하기
5. JPA / Flyway / QueryDSL / Redis / Cache 공통 설정 잡기
6. `global` / `domain` 패키지 구조를 어떻게 잡았는가

### Part C. 핵심 도메인 만들기

7. `Member`, `Kindergarten`, `Classroom`으로 첫 관계 만들기
8. `Kid`, `ParentKid`, `Attendance`로 첫 업무 Aggregate 만들기
9. 알림장, 공지, 알림으로 기능을 확장하는 법
10. 일정, 대시보드, 신청 도메인으로 조회와 상태 전이를 넓히기

### Part D. 인증과 보안

11. `SecurityConfig`와 회원가입/로그인 기본 흐름 만들기
12. `JwtTokenProvider`, `JwtFilter`, `AuthService`로 쿠키 JWT 연결하기
13. 멀티테넌시 보안 문제를 어떻게 발견하고 고쳤는가

### Part E. 테스트와 CI

14. H2가 아니라 Testcontainers를 붙인 이유
15. GitHub Actions와 태그 기반 테스트 스위트 분리

### Part F. 인증을 운영형으로 발전시키기

16. Refresh rotation과 활성 세션 제어로 인증을 한 단계 올리기
17. 로그인 남용 방어: Rate Limit과 Client IP 신뢰 모델
18. OAuth2와 소셜 계정 lifecycle을 안전하게 설계하기

### Part G. 운영성, 감사 추적, 장애 대응

19. 인증 감사 로그를 어떻게 설계하고 운영 콘솔까지 연결했는가
20. OpenAPI, management plane, Actuator, Prometheus, structured logging
21. 알림 전달을 `notification_outbox`로 비동기화한 이유

### Part H. 운영형 워크플로우

22. 반 정원, waitlist, offer expiry로 입학 워크플로우를 모델링하기
23. 학부모 출결 변경 요청과 업무 감사 로그를 왜 같이 설계했는가

### Part I. 시연과 취업 패키징

24. Demo seed, 아키텍처 문서, 성능 스토리, 면접 자료로 프로젝트를 마감하기

## 실제 집필 우선순위

공개는 1번부터 하지만, 실제 집필은 아래 순서로 먼저 진행한다.

1. 02. Gradle / Spring Boot bootstrap
2. 03. Docker / MySQL / Redis
3. 04. application.yml / profile
4. 11. SecurityConfig / 로그인
5. 13. 멀티테넌시 권한 하드닝

이유는 코드 근거가 명확하고, 입문자 유입과 취업 포인트가 가장 빠르게 드러나기 때문이다.
