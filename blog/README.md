# Blog Workspace

이 폴더는 Kindergarten ERP를 기반으로 작성할 **취업용 개발 블로그 시리즈**의 작업 공간입니다.

목표는 두 가지입니다.

1. Java / Spring Boot 입문자가 “이런 순서로 프로젝트를 설계하고 개발하면 되는구나”를 이해하게 만들기
2. 취업 준비생이 “기능 구현 -> 보안 -> 테스트 -> 운영 -> 포트폴리오 패키징”으로 성장하는 흐름을 따라가게 만들기

## 블로그 작업 SSOT

- [BLOG_PLAN.md](/Users/alex/project/kindergarten_ERP/erp/BLOG_PLAN.md)
  - 블로그 시리즈 전체 계획
  - 집필 순서와 범위
- [BLOG_PROGRESS.md](/Users/alex/project/kindergarten_ERP/erp/BLOG_PROGRESS.md)
  - 블로그 작업 진행 로그

기존 [PLAN.md](/Users/alex/project/kindergarten_ERP/erp/PLAN.md), [PROGRESS.md](/Users/alex/project/kindergarten_ERP/erp/PROGRESS.md)는 **애플리케이션 개발 작업용 SSOT**로 유지합니다.

## 이 폴더의 역할

- `00_series_plan.md`
  - 연재 전체 구조
  - 글 순서
  - 글마다 다룰 파일/클래스/설정 범위
  - 독자 학습 목표
- `_post_template.md`
  - 실제 글을 쓸 때 사용할 공통 템플릿

## 이 시리즈가 다루는 범위

- 프로젝트 주제 선정과 요구사항 정리
- Gradle / Spring Boot 프로젝트 초기 구성
- Docker, MySQL, Redis, profile 설계
- JPA / Flyway / Redis / Security 기초 설정
- 도메인 모델링
- 인증/JWT/OAuth2
- 출석/알림장/공지/신청/승인
- 멀티테넌시 권한 하드닝
- Testcontainers / GitHub Actions / 테스트 전략
- 감사 로그 / Outbox / Observability / Incident 대응
- 운영형 워크플로우
- 면접/데모/문서 패키징

## 집필 원칙

- “무엇을 만들었는가”보다 “왜 이렇게 설계했는가”를 먼저 설명한다.
- 매 글마다 실제 코드 파일과 클래스 이름을 명시한다.
- 가능하면 메서드 단위까지 내려간다.
- 글 구조는 항상 `문제 -> 설계 -> 코드 -> 테스트 -> 회고` 순서를 유지한다.
- 초심자를 위해 용어를 바로 쓰지 않고 한 번은 풀어서 설명한다.
- 각 글 끝에는 반드시 “취업 포인트”를 넣는다.

## 시작 순서

1. `00_series_plan.md`로 연재 전체를 본다.
2. `_post_template.md`로 글 형식을 맞춘다.
3. 실제 작성은 `01`번 글부터 순서대로 진행한다.
