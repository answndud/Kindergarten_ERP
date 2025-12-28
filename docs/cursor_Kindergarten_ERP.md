# 오래된 스프링부트 프로젝트 분석 및 개선
_Exported on 12/28/2025 at 13:13:07 GMT+9 from Cursor (2.2.44)_

---

**User**

@README.md 
지금 이 스프링부트 프로젝트는 스프링부트 기반 백엔드 부트캠프에서 초보자 4명이서 함께 진행한 프로젝트야.
통합 ERP 시스템을 유치원, 원생 등을 관리할 수 있게 적용한 시스템이야.

근데 1년전이라 프로젝트는 죽었어. 내가 백엔드 개발자 취업을 위해 이 프로젝트를 다시 재탄생 시키는 수준으로 리빌딩하고 싶어.
디자인 뿐만 아니라 코딩 철학이나 패턴도 모두 달라서 코드를 읽으며 분석해봐도 남들이 쓴 코드는 어떤 의도인지 전혀 모르겠네.
그리고 aws에 배포하려고 설정까지 되어있어. 

일단 이 프로젝트를 전면 재수정 하기 전에 이 프로젝트에 대해 모든것들을 너가 재조사해서
프로젝트의 의도, 기능, 구조, 사용된 기술(프레임워크, 라이브러리 등등) 등등을 확실하게 한국어로 정리한 project_summary.md 마크다운 파일을 만들어줘.

그 이후에 어떤 것들을 수정해야 하는지. 스프링부트를 사용하는 백엔드 개발자 관점에서 정리한 modify_plan.md 마크다운 파일도 만들어줘. 

일단 프론트엔드는 최대 퓨어한 상태(프레임워크 안쓰고)에서 전문가처럼 보이게 하고 싶고.
- 운영체제: MacOS에서 진행
- DB: MySQL을 기반으로 작성
- 사용언어: Java(Springboot), JPA, JS, QueryDSL,... 이후에 필요하면 추가
- 개발도구: Cursor, IntelliJ
- 기타도구: ERDCloud ...


그리고 최종적으로 새로운 프로젝트를 생성해서 시작할지 아니면 이 프로젝트에서 수정해나갈지도 판단해줘.

답변은 한국어로 부탁해.

---

**Cursor**

프로젝트를 철저히 분석해서 `project_summary.md`와 `modify_plan.md`를 작성해 드리겠습니다. 먼저 프로젝트 구조와 핵심 파일들을 분석하겠습니다.

좋습니다. 이제 주요 엔티티, 서비스, 컨트롤러를 더 분석해 보겠습니다.

주요 엔티티와 컨트롤러를 더 분석해 보겠습니다.

계속해서 QueryDSL 구현, JWT 필터, 그리고 프론트엔드 템플릿을 분석하겠습니다.

더 많은 서비스, 컨트롤러, DTO 구조를 분석하겠습니다.

data.sql 파일이 너무 커서 첫 부분만 읽어보겠습니다. 또한 프론트엔드 템플릿과 추가 엔티티를 분석하겠습니다.

좋습니다. 이제 추가적인 파일들을 분석해서 전체 그림을 완성하겠습니다.

이제 프로젝트를 충분히 분석했습니다. `project_summary.md`와 `modify_plan.md` 파일을 작성하겠습니다.

```markdown
# 아이바요(AiBayo) 프로젝트 분석 보고서

## 📋 목차
1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [도메인 모델](#4-도메인-모델)
5. [주요 기능 분석](#5-주요-기능-분석)
6. [보안 구현](#6-보안-구현)
7. [현재 코드의 문제점](#7-현재-코드의-문제점)

---

## 1. 프로젝트 개요

### 1.1 프로젝트 정의
- **프로젝트명**: 아이바요(AiBayo)
- **팀명**: 아이코(AIco)
- **유형**: 유치원 통합 ERP 시스템
- **목적**: 유치원 운영의 효율성을 높이기 위한 통합 관리 시스템

### 1.2 핵심 비즈니스 도메인
```
유치원(Kindergarten)
├── 원장(Principal)
├── 교사(Teacher)
├── 반(Class)
│   ├── 원생(Kid)
│   └── 학부모(Parent)
└── 운영 기능들
    ├── 공지사항/알림장
    ├── 식단표
    ├── 출석부
    ├── 일정표
    └── 투약의뢰서/귀가동의서
```

### 1.3 사용자 역할 (Role)
| Role Number | Role Name | 설명 |
|-------------|-----------|------|
| 0 | ROLE_ADMIN | 사이트 관리자 |
| 1 | ROLE_PRINCIPAL | 원장 |
| 2 | ROLE_TEACHER | 교사 |
| 3 | ROLE_USER | 학부모 |

---

## 2. 기술 스택

### 2.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 메인 언어 |
| Spring Boot | 3.3.1 | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 보안 |
| Spring OAuth2 Client | - | 소셜 로그인 (Google, Naver) |
| JWT (jjwt) | 0.12.3 | 토큰 인증 |
| Jasypt | 3.0.5 | 설정 암호화 |

### 2.2 Database
| 기술 | 용도 |
|------|------|
| MySQL | 메인 RDB |
| MongoDB | (주석 처리됨 - 미사용) |

### 2.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Thymeleaf Layout Dialect | 레이아웃 관리 |
| Bootstrap 5.0.2 | CSS 프레임워크 |
| jQuery 3.7.1 | JavaScript 라이브러리 |
| FullCalendar 6.1.15 | 캘린더 UI |
| Summernote | WYSIWYG 에디터 |
| SweetAlert2 | 알림창 |

### 2.4 클라우드/배포
| 기술 | 용도 |
|------|------|
| AWS S3 | 파일 업로드 스토리지 |
| Spring Mail (Gmail SMTP) | 이메일 발송 |

### 2.5 의존성 목록 (build.gradle)
```groovy
// 핵심 의존성
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-thymeleaf
- thymeleaf-layout-dialect
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- spring-boot-starter-mail
- spring-boot-starter-aop

// 데이터베이스
- mysql-connector-j
- querydsl-jpa:5.0.0:jakarta

// 인증
- jjwt-api/impl/jackson:0.12.3

// 유틸리티
- lombok
- jasypt-spring-boot-starter:3.0.5
- spring-cloud-starter-aws:2.2.6.RELEASE
```

---

## 3. 프로젝트 구조

### 3.1 디렉토리 구조
```
src/main/java/com/aico/aibayo/
├── AibayoApplication.java          # 메인 애플리케이션
├── aop/
│   └── LoggingAspect.java          # AOP 로깅
├── common/                          # Enum 상수 모음
│   ├── AcceptStatusEnum.java        # 승인 상태 (대기/승인/삭제 등)
│   ├── AcceptTypeEnum.java          # 승인 유형
│   ├── MemberRoleEnum.java          # 회원 역할
│   ├── MemberStatusEnum.java        # 회원 상태
│   ├── BooleanEnum.java             # Y/N 플래그
│   └── ...
├── config/                          # 설정 클래스
│   ├── SecurityConfig.java          # Spring Security 설정
│   ├── QuerydslConfig.java          # QueryDSL 설정
│   ├── JasyptConfig.java            # 암호화 설정
│   ├── S3Config.java                # AWS S3 설정
│   ├── GlobalControllerAdvice.java  # 전역 컨트롤러 어드바이스
│   └── ...
├── control/                         # 컨트롤러 (27개)
│   ├── MainController.java
│   ├── MemberController.java
│   ├── AnnounceController.java
│   ├── AttendanceController.java
│   └── ...
├── dto/                             # DTO (약 30개)
│   ├── member/
│   ├── announce/
│   ├── kid/
│   └── ...
├── entity/                          # JPA 엔티티 (44개)
│   ├── MemberEntity.java
│   ├── KidEntity.java
│   ├── ClassEntity.java
│   └── ...
├── exception/                       # 예외 클래스
│   └── MemberNotFoundException.java
├── jwt/                             # JWT 관련
│   ├── JWTUtil.java
│   ├── JWTFilter.java
│   ├── LoginFilter.java
│   ├── CustomLogoutFilter.java
│   └── CustomMemberStatusFilter.java
├── oauth2/                          # OAuth2
│   └── CustomSuccessHandler.java
├── repository/                      # 레포지토리 (68개)
│   ├── member/
│   │   ├── MemberRepository.java
│   │   ├── MemberRepositoryCustom.java
│   │   └── MemberRepositoryCustomImpl.java
│   └── ...
└── service/                         # 서비스 (35개)
    ├── member/
    │   ├── MemberService.java
    │   └── MemberServiceImpl.java
    └── ...
```

### 3.2 프론트엔드 구조
```
src/main/resources/
├── static/
│   ├── css/                # 스타일시트 (53개)
│   ├── js/                 # JavaScript (52개)
│   ├── images/             # 이미지
│   └── vendor/             # 외부 라이브러리
└── templates/
    ├── layout/
    │   └── layout.html     # 메인 레이아웃
    ├── inc/
    │   ├── admin_header.html
    │   ├── user_header.html
    │   ├── footer.html
    │   └── config.html
    ├── admin/              # 관리자(교사/원장) 페이지 (52개)
    ├── user/               # 학부모 페이지 (27개)
    ├── member/             # 회원 관련 페이지 (11개)
    └── common/             # 공통 컴포넌트 (5개)
```

---

## 4. 도메인 모델

### 4.1 핵심 엔티티
```
┌─────────────────────────────────────────────────────────────────────┐
│                         MEMBER (회원)                               │
├─────────────────────────────────────────────────────────────────────┤
│ id (PK), username(이메일), name, password, phone, role_no, role,   │
│ status, kinder_no, reg_date, modify_date, profile_picture          │
└─────────────────────────────────────────────────────────────────────┘
              │
              │ 1:N
              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    REGISTER_KINDER (유치원)                         │
├─────────────────────────────────────────────────────────────────────┤
│ kinder_no (PK), kinder_name, kinder_addr, kinder_open_time,        │
│ kinder_close_time, sido_list, sgg_list, ...                        │
└─────────────────────────────────────────────────────────────────────┘
              │
              │ 1:N
              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          CLASS (반)                                 │
├─────────────────────────────────────────────────────────────────────┤
│ class_no (PK), kinder_no (FK), class_name, class_age,              │
│ class_delete_flag                                                   │
└─────────────────────────────────────────────────────────────────────┘
              │
     ┌────────┴────────┐
     │                 │
     ▼                 ▼
┌──────────────┐  ┌──────────────┐
│ CLASS_TEACHER│  │  CLASS_KID   │
│ (교사-반)    │  │  (원생-반)   │
└──────────────┘  └──────────────┘
     │                 │
     ▼                 ▼
┌──────────────┐  ┌──────────────┐
│   MEMBER     │  │     KID      │
│  (교사)      │  │   (원생)     │
└──────────────┘  └──────────────┘
                       │
                       │ N:M
                       ▼
               ┌──────────────┐
               │  PARENT_KID  │
               │ (학부모-원생)│
               └──────────────┘
                       │
                       ▼
               ┌──────────────┐
               │   MEMBER     │
               │  (학부모)    │
               └──────────────┘
```

### 4.2 승인 시스템 (Accept Log)
- 모든 관계(교사-유치원, 원생-반, 학부모-원생)는 승인 절차를 거침
- `ACCEPT_LOG` 테이블로 승인 상태 관리
- 상태: WAIT(대기) → ACCEPT(승인) / DELETE(삭제)

### 4.3 게시판 시스템
```
BOARD (게시판 기본)
    ├── ANNOUNCE (공지사항)
    ├── NOTEPAD (알림장)
    └── COMMENT (댓글)
```

---

## 5. 주요 기능 분석

### 5.1 회원 관리
- **일반 회원가입**: 이메일/비밀번호 기반
- **소셜 로그인**: Google, Naver OAuth2
- **JWT 인증**: 쿠키 기반 JWT 토큰 관리
- **회원 유형**: 관리자, 원장, 교사, 학부모
- **비밀번호 재설정**: 이메일 링크 발송

### 5.2 유치원 관리
- 유치원 등록/수정
- 유치원별 설정 메뉴 관리
- 시/도, 시/군/구 정보 관리

### 5.3 반 관리
- 반 생성/수정/삭제 (Soft Delete)
- 교사-반 배정
- 원생-반 배정

### 5.4 원생/학부모 관리
- 원생 등록 (생년월일, 성별 등)
- 학부모-원생 연결
- 초대 코드 기반 가입

### 5.5 공지사항/알림장
- 공지사항: 전체 또는 반별 공개
- 알림장: 원생별 또는 반별 작성
- 중요 공지 설정
- 댓글 기능

### 5.6 출석부
- 일별 출석 체크
- 등/하원 시간 기록
- 결석 사유 관리

### 5.7 식단표
- 일별 식단 등록
- 식사 유형별 관리 (아침/점심/간식 등)

### 5.8 일정표
- FullCalendar 기반 캘린더
- 반별 일정 관리

### 5.9 투약의뢰서/귀가동의서
- 학부모 신청
- 교사 확인/승인

### 5.10 전자결재
- 결재 폼 관리
- 결재 흐름 처리

### 5.11 채팅
- iframe 기반 채팅 UI
- (구현 상세 미확인)

---

## 6. 보안 구현

### 6.1 인증 흐름
```
1. 로그인 요청
   └─> LoginFilter
       └─> AuthenticationManager
           └─> CustomMemberDetailService
               └─> DB 조회 & 비밀번호 검증

2. 로그인 성공
   └─> JWT 토큰 생성
       └─> 쿠키에 저장 (name: "jwt")

3. 인증된 요청
   └─> JWTFilter
       └─> 토큰 검증
           └─> SecurityContext에 인증 정보 설정

4. OAuth2 로그인
   └─> CustomOAuth2MemberService
       └─> CustomSuccessHandler
           └─> 신규 회원이면 원아정보 입력 페이지로 리다이렉트
```

### 6.2 인가 설정
```java
// 공개 경로
permitAll(): /member/**, /, /login, /logout, /css/**, /js/**, /setting/**

// 관리자 전용
hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER"): /main/admin

// 사용자 전용
hasRole("USER"): /main/user
```

### 6.3 세션 정책
- **Stateless**: JWT 기반으로 세션 미사용
- **CSRF**: 비활성화

---

## 7. 현재 코드의 문제점

### 7.1 아키텍처 문제
1. **패키지 네이밍**: `control` 대신 `controller` 사용이 표준
2. **레이어 분리 부족**: Controller에 비즈니스 로직 혼재
3. **DTO 과다 생성자**: MemberDto에 7개 이상의 생성자
4. **API/화면 컨트롤러 혼재**: REST API와 View 반환이 같은 컨트롤러에 존재

### 7.2 보안 문제
1. **CSRF 완전 비활성화**: SPA가 아닌 SSR에서 위험
2. **JWT 쿠키 설정 미흡**: HttpOnly, Secure 설정 불명확
3. **비밀번호 검증 후 null 체크**: orElseThrow 후 null 체크 (불필요)

### 7.3 코드 품질 문제
1. **하드코딩된 값**: 매직 넘버, 매직 스트링 다수
2. **로그에 System.out.println 사용**: 프로덕션 환경에 부적합
3. **예외 처리 미흡**: 커스텀 예외 1개만 존재
4. **테스트 코드 부족**: 테스트 파일 3개만 존재

### 7.4 데이터베이스 문제
1. **엔티티 연관관계 미설정**: @ManyToOne, @OneToMany 대신 직접 FK 관리
2. **N+1 문제 가능성**: QueryDSL로 조인하지만 Lazy Loading 미고려
3. **Soft Delete 일관성 부족**: delete_flag 필드명 불일치

### 7.5 프론트엔드 문제
1. **CSS 중복**: 유사한 스타일이 여러 파일에 분산
2. **JavaScript 모듈화 부족**: 전역 함수 사용
3. **일관성 없는 API 호출**: fetch, jQuery ajax 혼용

### 7.6 설정 문제
1. **환경 분리 미흡**: application-secret.properties 하나에 모든 환경 변수
2. **로깅 설정**: 프로덕션용 설정 없음
3. **AWS SDK 구버전**: spring-cloud-starter-aws:2.2.6.RELEASE (deprecated)

---

## 부록: 주요 파일 위치

| 기능 | 파일 경로 |
|------|-----------|
| 메인 진입점 | `AibayoApplication.java` |
| 보안 설정 | `config/SecurityConfig.java` |
| JWT 유틸 | `jwt/JWTUtil.java` |
| 전역 어드바이스 | `config/GlobalControllerAdvice.java` |
| 메인 페이지 | `control/MainController.java` |
| 레이아웃 | `templates/layout/layout.html` |
| 초기 데이터 | `resources/data.sql` |

```

```markdown
# 아이바요(AiBayo) 리빌딩 계획서

## 📋 목차
1. [리빌딩 전략 결정](#1-리빌딩-전략-결정)
2. [Phase 1: 기반 정비](#2-phase-1-기반-정비)
3. [Phase 2: 백엔드 리팩토링](#3-phase-2-백엔드-리팩토링)
4. [Phase 3: 프론트엔드 개선](#4-phase-3-프론트엔드-개선)
5. [Phase 4: 인프라 및 배포](#5-phase-4-인프라-및-배포)
6. [기술 스택 추천](#6-기술-스택-추천)
7. [작업 우선순위](#7-작업-우선순위)

---

## 1. 리빌딩 전략 결정

### 🎯 결론: 기존 프로젝트 수정 권장

#### 이유:
1. **도메인 로직 보존**: 유치원 ERP의 복잡한 비즈니스 로직(승인 시스템, 역할 기반 접근제어 등)이 이미 구현됨
2. **데이터베이스 스키마**: `data.sql`에 정의된 스키마를 재활용 가능
3. **시간 효율성**: 새로 만드는 것보다 리팩토링이 빠름
4. **학습 효과**: 레거시 코드 개선 경험은 실무에서 더 유용함

#### 전략:
```
점진적 리팩토링 (Strangler Fig Pattern)
├── 기존 코드 분석 및 이해
├── 새로운 구조로 하나씩 이전
├── 테스트 코드 작성하며 검증
└── 구버전 코드 제거
```

---

## 2. Phase 1: 기반 정비 (1-2주)

### 2.1 프로젝트 구조 재정비

#### 패키지 구조 변경
```
기존: com.aico.aibayo
변경: com.aibayo (그룹ID 정리)

com.aibayo/
├── AibayoApplication.java
├── global/                          # 전역 설정
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── QuerydslConfig.java
│   │   ├── WebConfig.java
│   │   └── S3Config.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── security/
│   │   ├── jwt/
│   │   └── oauth2/
│   └── common/
│       ├── BaseEntity.java          # 공통 엔티티 (생성일, 수정일)
│       └── BaseResponse.java        # 공통 응답 포맷
│
├── domain/                          # 도메인별 분리
│   ├── member/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── kindergarten/
│   ├── classroom/
│   ├── kid/
│   ├── attendance/
│   ├── announcement/
│   ├── notepad/
│   ├── meal/
│   ├── schedule/
│   └── medication/
│
└── infra/                           # 외부 연동
    ├── mail/
    └── storage/
```

### 2.2 build.gradle 정리

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.5'
}

group = 'com.aibayo'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Spring Boot Starters ===
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'  // 추가
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    
    // === Database ===
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    
    // === Security ===
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // === AWS (업데이트 권장) ===
    implementation platform('software.amazon.awssdk:bom:2.21.0')
    implementation 'software.amazon.awssdk:s3'
    
    // === Utilities ===
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'  // DTO 변환
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // === Development ===
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // === Test ===
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
    useJUnitPlatform()
}

// QueryDSL 설정
def querydslDir = "$buildDir/generated/querydsl"
sourceSets {
    main.java.srcDirs += [querydslDir]
}
tasks.withType(JavaCompile) {
    options.generatedSourceOutputDirectory = file(querydslDir)
}
clean.doLast {
    file(querydslDir).deleteDir()
}
```

### 2.3 환경 설정 분리

```yaml
# application.yml (공통)
spring:
  profiles:
    active: local
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

---
# application-local.yml (로컬 개발)
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:mysql://localhost:3306/aibayo?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: ${DB_PASSWORD}
  jpa:
    show-sql: true

logging:
  level:
    com.aibayo: DEBUG
    org.hibernate.SQL: DEBUG

---
# application-prod.yml (프로덕션)
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    com.aibayo: INFO
    org.hibernate.SQL: WARN
```

### 2.4 공통 클래스 작성

#### BaseEntity.java
```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

#### ErrorCode.java
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
    RESOURCE_NOT_FOUND(404, "C002", "리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다"),
    
    // Member
    MEMBER_NOT_FOUND(404, "M001", "회원을 찾을 수 없습니다"),
    DUPLICATE_EMAIL(400, "M002", "이미 사용중인 이메일입니다"),
    INVALID_PASSWORD(400, "M003", "비밀번호가 일치하지 않습니다"),
    
    // Authentication
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다"),
    ACCESS_DENIED(403, "A003", "접근 권한이 없습니다");
    
    private final int status;
    private final String code;
    private final String message;
}
```

#### GlobalExceptionHandler.java
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage());
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult()));
    }
}
```

---

## 3. Phase 2: 백엔드 리팩토링 (3-4주)

### 3.1 엔티티 개선

#### 기존 문제
```java
// 기존: 연관관계 없이 FK만 저장
@Column(name = "kinder_no")
private Long kinderNo;
```

#### 개선 방향
```java
// 개선: JPA 연관관계 매핑
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "kinder_no")
private Kindergarten kindergarten;
```

#### MemberEntity 개선 예시
```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String password;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kinder_no")
    private Kindergarten kindergarten;
    
    private String profilePicture;
    
    private LocalDateTime lastLoginAt;
    private LocalDateTime inactivatedAt;
    
    // === 생성 메서드 ===
    public static Member createMember(String email, String name, 
            String encodedPassword, String phone, MemberRole role) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.password = encodedPassword;
        member.phone = phone;
        member.role = role;
        member.status = MemberStatus.ACTIVE;
        return member;
    }
    
    // === 비즈니스 메서드 ===
    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }
    
    public void inactivate() {
        this.status = MemberStatus.INACTIVE;
        this.inactivatedAt = LocalDateTime.now();
    }
    
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
```

### 3.2 Repository 패턴 정리

#### 기존 문제
- JpaRepository + Custom 인터페이스 + CustomImpl 3개 파일
- QueryDSL 쿼리에서 직접 DTO 프로젝션

#### 개선 방향
```java
// MemberRepository.java
public interface MemberRepository extends JpaRepository<Member, Long>, 
        MemberRepositoryCustom {
    
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}

// MemberRepositoryCustom.java
public interface MemberRepositoryCustom {
    List<Member> findAllWithKindergarten(MemberSearchCondition condition);
    Page<Member> findAllWithPage(MemberSearchCondition condition, Pageable pageable);
}

// MemberRepositoryImpl.java (Impl로 통일)
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Member> findAllWithKindergarten(MemberSearchCondition condition) {
        return queryFactory
            .selectFrom(member)
            .leftJoin(member.kindergarten).fetchJoin()
            .where(
                eqStatus(condition.getStatus()),
                eqRole(condition.getRole())
            )
            .fetch();
    }
    
    private BooleanExpression eqStatus(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }
}
```

### 3.3 서비스 레이어 개선

#### 기존 문제
- Interface + Impl 구조 (단일 구현에 불필요)
- 트랜잭션 범위 불명확
- 비즈니스 로직과 인프라 로직 혼재

#### 개선 방향
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Long signUp(MemberSignUpRequest request) {
        validateDuplicateEmail(request.getEmail());
        
        Member member = Member.createMember(
            request.getEmail(),
            request.getName(),
            passwordEncoder.encode(request.getPassword()),
            request.getPhone(),
            request.getRole()
        );
        
        return memberRepository.save(member).getId();
    }
    
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
    
    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest request) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), 
                member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
    
    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
```

### 3.4 컨트롤러 분리

#### 기존 문제
- View 반환과 REST API가 혼재
- `@ResponseBody` 메서드와 일반 메서드 혼용

#### 개선 방향: API와 View 컨트롤러 분리
```java
// REST API 컨트롤러
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {
    
    private final MemberService memberService;
    
    @PostMapping
    public ResponseEntity<Long> signUp(@Valid @RequestBody MemberSignUpRequest request) {
        Long memberId = memberService.signUp(request);
        return ResponseEntity.created(URI.create("/api/v1/members/" + memberId))
            .body(memberId);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }
}

// View 컨트롤러
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberViewController {
    
    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new MemberSignUpForm());
        return "member/signup";
    }
    
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, 
            Model model) {
        model.addAttribute("member", userDetails.getMember());
        return "member/mypage";
    }
}
```

### 3.5 DTO 개선

#### 기존 문제
- 하나의 DTO에 너무 많은 생성자 (7개+)
- Entity와 DTO 변환 로직 분산

#### 개선 방향: Record 또는 MapStruct 사용
```java
// Request DTO (입력용)
public record MemberSignUpRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,
    
    @NotBlank(message = "이름은 필수입니다")
    String name,
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password,
    
    String phone,
    
    @NotNull(message = "역할은 필수입니다")
    MemberRole role
) {}

// Response DTO (출력용)
public record MemberResponse(
    Long id,
    String email,
    String name,
    String phone,
    MemberRole role,
    MemberStatus status,
    LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getEmail(),
            member.getName(),
            member.getPhone(),
            member.getRole(),
            member.getStatus(),
            member.getCreatedAt()
        );
    }
}
```

### 3.6 보안 개선

#### SecurityConfig 개선
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF: API는 비활성화, View는 활성화 고려
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            
            // 세션: Stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인가 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스
                .requestMatchers("/css/**", "/js/**", "/images/**", "/vendor/**").permitAll()
                // 공개 API
                .requestMatchers("/api/v1/auth/**", "/member/**").permitAll()
                // 관리자 API
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "PRINCIPAL")
                // 교사 API
                .requestMatchers("/api/v1/teacher/**").hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER")
                // 그 외 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            
            // OAuth2 로그인
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/member/signIn")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )
            
            // 로그아웃
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("jwt")
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 4. Phase 3: 프론트엔드 개선 (2-3주)

### 4.1 CSS 구조 개선

#### 디렉토리 구조
```
static/css/
├── base/
│   ├── reset.css           # CSS 리셋
│   ├── variables.css       # CSS 변수 (색상, 폰트 등)
│   └── typography.css      # 타이포그래피
├── components/
│   ├── button.css
│   ├── card.css
│   ├── modal.css
│   ├── form.css
│   └── table.css
├── layout/
│   ├── header.css
│   ├── footer.css
│   └── sidebar.css
├── pages/
│   ├── main.css
│   ├── attendance.css
│   └── ...
└── main.css               # 진입점 (모든 CSS import)
```

#### CSS 변수 활용
```css
/* variables.css */
:root {
    /* Colors */
    --color-primary: #FFDB21;
    --color-primary-dark: #E5C400;
    --color-secondary: #4A90A4;
    --color-success: #28A745;
    --color-warning: #FFC107;
    --color-danger: #DC3545;
    
    /* Neutrals */
    --color-white: #FFFFFF;
    --color-gray-100: #F8F9FA;
    --color-gray-200: #E9ECEF;
    --color-gray-500: #6C757D;
    --color-gray-800: #343A40;
    --color-black: #000000;
    
    /* Typography */
    --font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-size-xs: 0.75rem;
    --font-size-sm: 0.875rem;
    --font-size-md: 1rem;
    --font-size-lg: 1.125rem;
    --font-size-xl: 1.25rem;
    
    /* Spacing */
    --spacing-xs: 0.25rem;
    --spacing-sm: 0.5rem;
    --spacing-md: 1rem;
    --spacing-lg: 1.5rem;
    --spacing-xl: 2rem;
    
    /* Border Radius */
    --radius-sm: 4px;
    --radius-md: 8px;
    --radius-lg: 12px;
    --radius-full: 9999px;
    
    /* Shadow */
    --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
    --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
    --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

### 4.2 JavaScript 모듈화

#### ES 모듈 구조
```
static/js/
├── utils/
│   ├── api.js              # fetch 래퍼
│   ├── dom.js              # DOM 유틸리티
│   ├── format.js           # 날짜/숫자 포맷
│   └── validate.js         # 유효성 검사
├── components/
│   ├── Modal.js
│   ├── Toast.js
│   ├── DatePicker.js
│   └── Pagination.js
├── pages/
│   ├── main.js
│   ├── attendance.js
│   └── ...
└── app.js                  # 메인 진입점
```

#### API 래퍼 예시
```javascript
// utils/api.js
class ApiClient {
    constructor(baseUrl = '/api/v1') {
        this.baseUrl = baseUrl;
    }
    
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const error = await response.json();
                throw new ApiError(error.message, response.status, error.code);
            }
            
            return response.json();
        } catch (error) {
            if (error instanceof ApiError) throw error;
            throw new ApiError('네트워크 오류가 발생했습니다', 0, 'NETWORK_ERROR');
        }
    }
    
    get(endpoint, params = {}) {
        const query = new URLSearchParams(params).toString();
        const url = query ? `${endpoint}?${query}` : endpoint;
        return this.request(url, { method: 'GET' });
    }
    
    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

export const api = new ApiClient();
```

### 4.3 Thymeleaf 레이아웃 개선

#### layout.html 개선
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="아이바요 - 유치원 통합 관리 시스템">
    
    <title layout:title-pattern="$CONTENT_TITLE - 아이바요">아이바요</title>
    
    <!-- CSS -->
    <link rel="stylesheet" th:href="@{/css/main.css}">
    <th:block layout:fragment="css"></th:block>
</head>
<body>
    <!-- 헤더 -->
    <th:block th:replace="~{fragments/header :: header}"></th:block>
    
    <!-- 메인 컨텐츠 -->
    <main class="main-content">
        <th:block layout:fragment="content"></th:block>
    </main>
    
    <!-- 푸터 -->
    <th:block th:replace="~{fragments/footer :: footer}"></th:block>
    
    <!-- 공통 모달 -->
    <th:block th:replace="~{fragments/modal :: modal}"></th:block>
    
    <!-- JavaScript -->
    <script th:src="@{/js/app.js}" type="module"></script>
    <th:block layout:fragment="scripts"></th:block>
</body>
</html>
```

---

## 5. Phase 4: 인프라 및 배포 (1-2주)

### 5.1 Docker 설정

#### Dockerfile
```dockerfile
# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://db:3306/aibayo
      - DB_USERNAME=aibayo
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - db
    networks:
      - aibayo-network

  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=aibayo
      - MYSQL_USER=aibayo
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - aibayo-network

volumes:
  mysql-data:

networks:
  aibayo-network:
```

### 5.2 AWS 배포 구성 (업그레이드)

```
현재: spring-cloud-starter-aws (deprecated)
변경: AWS SDK v2 사용

배포 아키텍처:
┌──────────────────────────────────────────────────────┐
│                      AWS Cloud                        │
│  ┌─────────────┐      ┌─────────────┐               │
│  │   Route 53  │─────▶│ CloudFront  │               │
│  └─────────────┘      └──────┬──────┘               │
│                              │                        │
│                     ┌────────▼────────┐              │
│                     │     ALB         │              │
│                     └────────┬────────┘              │
│                              │                        │
│            ┌─────────────────┼─────────────────┐     │
│            │                 │                 │     │
│     ┌──────▼──────┐   ┌──────▼──────┐        │     │
│     │ ECS Fargate │   │ ECS Fargate │  ...   │     │
│     └──────┬──────┘   └──────┬──────┘        │     │
│            │                 │                │     │
│            └────────┬────────┘                │     │
│                     │                         │     │
│              ┌──────▼──────┐   ┌─────────┐   │     │
│              │   RDS MySQL  │   │   S3    │   │     │
│              └─────────────┘   └─────────┘   │     │
└──────────────────────────────────────────────────────┘
```

---

## 6. 기술 스택 추천

### 6.1 최종 기술 스택

| 분류 | 기술 | 버전 | 비고 |
|------|------|------|------|
| **Language** | Java | 17 LTS | 유지 |
| **Framework** | Spring Boot | 3.3.x | 유지 |
| **ORM** | Spring Data JPA + QueryDSL | 5.0.0 | 유지 |
| **Security** | Spring Security + JWT | - | 유지 |
| **Database** | MySQL | 8.0 | 유지 |
| **Template** | Thymeleaf | - | 유지 |
| **Build** | Gradle | 8.x | 업그레이드 |
| **Cloud** | AWS SDK v2 | 2.21.x | 업그레이드 |
| **Container** | Docker + Docker Compose | - | 추가 |
| **Test** | JUnit 5 + Mockito | - | 강화 |

### 6.2 추가 고려 라이브러리

| 라이브러리 | 용도 | 필수 여부 |
|-----------|------|----------|
| MapStruct | DTO 변환 자동화 | 권장 |
| Flyway/Liquibase | DB 마이그레이션 | 권장 |
| Testcontainers | 통합 테스트 | 권장 |
| Spring REST Docs | API 문서화 | 선택 |
| Resilience4j | 서킷브레이커 | 선택 |

---

## 7. 작업 우선순위

### 7.1 필수 작업 (Must Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 1 | 환경 설정 분리 (application.yml) | 2시간 | ⭐⭐⭐⭐⭐ |
| 2 | 전역 예외 처리 구현 | 4시간 | ⭐⭐⭐⭐⭐ |
| 3 | 엔티티 연관관계 매핑 | 1일 | ⭐⭐⭐⭐⭐ |
| 4 | 테스트 코드 작성 | 2일 | ⭐⭐⭐⭐⭐ |
| 5 | API/View 컨트롤러 분리 | 2일 | ⭐⭐⭐⭐ |
| 6 | 서비스 레이어 리팩토링 | 2일 | ⭐⭐⭐⭐ |
| 7 | 보안 설정 개선 | 1일 | ⭐⭐⭐⭐ |

### 7.2 권장 작업 (Should Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 8 | CSS 구조 개선 | 1일 | ⭐⭐⭐ |
| 9 | JavaScript 모듈화 | 1일 | ⭐⭐⭐ |
| 10 | DTO Record 전환 | 1일 | ⭐⭐⭐ |
| 11 | Docker 환경 구성 | 4시간 | ⭐⭐⭐ |
| 12 | AWS SDK 업그레이드 | 4시간 | ⭐⭐⭐ |

### 7.3 선택 작업 (Nice to Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 13 | MapStruct 도입 | 4시간 | ⭐⭐ |
| 14 | API 문서화 (REST Docs) | 1일 | ⭐⭐ |
| 15 | DB 마이그레이션 도구 도입 | 4시간 | ⭐⭐ |
| 16 | 로깅 체계 개선 | 4시간 | ⭐⭐ |

---

## 체크리스트

### Phase 1 완료 조건
- [ ] 패키지 구조 재정비 완료
- [ ] build.gradle 정리 완료
- [ ] 환경 설정 분리 완료 (local/prod)
- [ ] 공통 클래스 작성 완료 (BaseEntity, ErrorCode 등)

### Phase 2 완료 조건
- [ ] 핵심 엔티티 JPA 연관관계 매핑 완료
- [ ] Repository 패턴 통일
- [ ] 서비스 레이어 리팩토링 완료
- [ ] API/View 컨트롤러 분리 완료
- [ ] 테스트 커버리지 50% 이상

### Phase 3 완료 조건
- [ ] CSS 변수 및 구조 개선
- [ ] JavaScript 모듈화
- [ ] Thymeleaf 레이아웃 정리

### Phase 4 완료 조건
- [ ] Docker 환경 구성
- [ ] CI/CD 파이프라인 구축
- [ ] AWS 배포 완료

---

## 마무리

이 계획서는 기존 코드를 점진적으로 개선하는 방식으로 작성되었습니다. 
한 번에 모든 것을 바꾸려 하지 말고, 우선순위가 높은 작업부터 차근차근 진행하세요.

**핵심 원칙:**
1. 테스트 먼저 작성 후 리팩토링
2. 작은 커밋, 자주 커밋
3. 기능 단위로 브랜치 분리
4. 코드 리뷰 습관화 (혼자라도 PR 작성)

화이팅! 🚀

```

두 개의 문서를 작성했습니다. 요약해서 설명드리겠습니다.

---

## 📄 작성된 문서

### 1. `project_summary.md` - 프로젝트 분석 보고서
프로젝트의 모든 것을 정리한 문서입니다:

- **기술 스택**: Spring Boot 3.3.1, Java 17, JPA, QueryDSL, JWT, OAuth2 (Google/Naver)
- **프로젝트 규모**: 
  - 엔티티 44개
  - 컨트롤러 27개
  - 서비스 35개
  - 레포지토리 68개
- **주요 도메인**: 유치원, 반, 원생, 교사, 학부모, 공지사항, 알림장, 출석부, 식단표 등
- **현재 문제점**: 
  - 엔티티 연관관계 미설정 (FK만 직접 관리)
  - DTO에 과다한 생성자 (7개 이상)
  - 테스트 코드 부족 (3개 파일만 존재)
  - API/View 컨트롤러 혼재

### 2. `modify_plan.md` - 리빌딩 계획서
백엔드 개발자 관점에서 수정해야 할 사항들을 정리했습니다:

---

## 🎯 핵심 결론: **기존 프로젝트에서 수정해 나가세요**

### 이유:
1. 유치원 ERP의 복잡한 비즈니스 로직(승인 시스템, 역할 기반 접근제어)이 이미 구현되어 있음
2. `data.sql`에 정의된 DB 스키마를 재활용 가능
3. 레거시 코드 개선 경험은 실무에서 더 유용함

---

## 📅 단계별 계획 (총 7-11주)

| Phase | 내용 | 기간 |
|-------|------|------|
| **Phase 1** | 기반 정비 (패키지 구조, 환경설정, 공통 클래스) | 1-2주 |
| **Phase 2** | 백엔드 리팩토링 (엔티티, 서비스, 컨트롤러) | 3-4주 |
| **Phase 3** | 프론트엔드 개선 (CSS, JS 모듈화) | 2-3주 |
| **Phase 4** | 인프라 및 배포 (Docker, AWS) | 1-2주 |

---

## 🔥 가장 먼저 해야 할 작업 (Top 5)

1. **환경 설정 분리** - `application.yml`로 local/prod 분리
2. **전역 예외 처리** - `ErrorCode`, `GlobalExceptionHandler` 구현
3. **엔티티 연관관계 매핑** - `@ManyToOne`, `@OneToMany` 적용
4. **테스트 코드 작성** - 핵심 서비스에 대한 단위/통합 테스트
5. **API/View 컨트롤러 분리** - REST API는 `/api/v1/**`, View는 별도 분리

---

## 🛠️ 기술 스택 (유지 + 업그레이드)

| 유지 | 업그레이드/추가 |
|------|----------------|
| Java 17, Spring Boot 3.3.x | AWS SDK v2 (기존 deprecated) |
| JPA + QueryDSL | Docker + Docker Compose |
| Spring Security + JWT | MapStruct (DTO 변환) |
| MySQL 8.0 | Flyway (DB 마이그레이션) |
| Thymeleaf | JUnit 5 테스트 강화 |

---

궁금한 점이나 특정 부분부터 시작하고 싶으시면 말씀해 주세요! 함께 하나씩 진행해 나가면 됩니다. 🚀

---

**User**

솔직히 페이지 디자인도 마음에 안들고 필요없는 기능들도 꽤 있는것 같아. 기술 스택 (유지 + 업그레이드)는 마음에 들어. 여기에 Redis도 나중에 추가하고 싶어. 그냥 처음 프로젝트 설정부터 다시 만들고 싶은데 프로젝트 설정, 파일 생성부터 모두 설계해줄래? 그리고 cursor를 이용하지만 내가 직접 타이핑하면서 프로젝트를 끝까지 만들고 싶어. 그점 고려해서 project_idea.md 파일 만들어줘. 

---

**Cursor**

알겠습니다! 처음부터 새로 만드는 것으로 방향을 바꿔서, 직접 타이핑하며 프로젝트를 완성할 수 있도록 상세한 가이드 문서를 만들어 드리겠습니다.

```markdown
# 🎯 아이바요(AiBayo) v2 - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: AiBayo (아이바요)
- **버전**: v2.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
aibayo-v2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aibayo/
│   │   │           ├── AibayoApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aibayo/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.aibayo
    - Artifact: aibayo
    - Package name: com.aibayo
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**

```

`project_idea.md` 파일을 작성했습니다! 🎉

---

## 📄 작성된 문서 요약

### 핵심 내용

#### 1. 기능 정리 (MVP 기준)
기존 프로젝트에서 **핵심만 추려서** 단순화했습니다:

| 포함 ✅ | 제외 ❌ |
|---------|---------|
| 회원가입/로그인 (JWT) | 전자결재 |
| 유치원 & 반 관리 | 청구서/결제 |
| 원생 관리 | OAuth 소셜 로그인 (추후) |
| 출석부 | 채팅 (추후) |
| 알림장 | 귀가동의서 (추후) |
| 공지사항 | |

#### 2. 기술 스택
```
Backend:  Java 17 + Spring Boot 3.3.x + JPA + QueryDSL + Spring Security + JWT
Database: MySQL 8.0 + Redis 7.x + Flyway
Frontend: Thymeleaf + Vanilla JS (ES6+) + CSS (프레임워크 없이!)
Infra:    Docker + Docker Compose + AWS S3
```

#### 3. 단계별 가이드 (16일 계획)
```
Day 1     : 프로젝트 초기 설정
Day 2     : 전역 설정 (예외처리, Config 등)
Day 3-4   : 인증 시스템 (JWT, Security)
Day 5-6   : 유치원 & 반 관리
Day 7-8   : 원생 관리
Day 9-10  : 출석 관리
Day 11-12 : 알림장
Day 13-14 : 공지사항
Day 15-16 : 마무리 (대시보드, 테스트, Docker)
```

#### 4. 직접 타이핑을 위한 가이드
- **파일 생성 순서** 명시
- **코드 스니펫** 포함 (엔티티, 서비스, 컨트롤러 기본 구조)
- **체크리스트** 형태로 진행 상황 추적 가능
- **와이어프레임** 포함 (로그인, 대시보드, 출석부)

#### 5. UI/UX 컨셉
- **미니멀 & 클린** 디자인
- **노란색 계열** (아이들 친화적)
- **사이드바 기반** 레이아웃
- CSS Variables 활용

---

## 🚀 다음 단계

이제 선택하세요:

1. **바로 시작하기**: 새 폴더에 Spring Initializr로 프로젝트 생성
2. **추가 논의**: 기능 범위, 디자인 컨셉 등 더 조율하기
3. **코드 예시 더 보기**: 특정 파일의 전체 코드 예시 요청

어떻게 진행할까요? 😊

```plaintext
Backend:  Java 17 + Spring Boot 3.3.x + JPA + QueryDSL + Spring Security + JWT
Database: MySQL 8.0 + Redis 7.x + Flyway
Frontend: Thymeleaf + Vanilla JS (ES6+) + CSS (프레임워크 없이!)
Infra:    Docker + Docker Compose + AWS S3
```

```plaintext
Day 1     : 프로젝트 초기 설정
Day 2     : 전역 설정 (예외처리, Config 등)
Day 3-4   : 인증 시스템 (JWT, Security)
Day 5-6   : 유치원 & 반 관리
Day 7-8   : 원생 관리
Day 9-10  : 출석 관리
Day 11-12 : 알림장
Day 13-14 : 공지사항
Day 15-16 : 마무리 (대시보드, 테스트, Docker)
```

---

**User**

프로젝트 이름을 그냥 AiBayo에서 erp로 바꿀게 demo도 erp로 바꾸고 이거 수정해줘

---

**Cursor**

`project_idea.md` 파일에서 프로젝트 이름을 AiBayo에서 erp로 변경하겠습니다.

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: AiBayo (아이바요)
- **버전**: v2.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
aibayo-v2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aibayo/
│   │   │           ├── AibayoApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aibayo/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.aibayo
    - Artifact: aibayo
    - Package name: com.aibayo
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
aibayo-v2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aibayo/
│   │   │           ├── AibayoApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aibayo/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.aibayo
    - Artifact: aibayo
    - Package name: com.aibayo
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
aibayo-v2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aibayo/
│   │   │           ├── AibayoApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aibayo/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── erp/
│   │   │           ├── ErpApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── aibayo/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── erp/
│   │   │           ├── ErpApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── erp/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/aibayo/AibayoApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── erp/
│   │   │           ├── ErpApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── erp/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 아이바요                   │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/erp/ErpApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── erp/
│   │   │           ├── ErpApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── erp/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 유치원 ERP                 │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/erp/ErpApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**


```

```markdown
# 🎯 유치원 ERP - 새 프로젝트 설계서

> 유치원 통합 관리 시스템을 처음부터 새롭게 만드는 프로젝트입니다.
> Cursor에서 직접 타이핑하며 완성하는 것을 목표로 합니다.

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [핵심 기능 정의](#2-핵심-기능-정의)
3. [기술 스택](#3-기술-스택)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [API 설계](#6-api-설계)
7. [단계별 구현 가이드](#7-단계별-구현-가이드)
8. [UI/UX 컨셉](#8-uiux-컨셉)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
- **이름**: ERP (유치원 ERP)
- **버전**: v1.0
- **목적**: 유치원 운영을 위한 미니멀하고 효율적인 관리 시스템

### 1.2 프로젝트 철학
```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
- 테스트 주도 개발
```

### 1.3 타겟 사용자
| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 원장 | 유치원 총괄 관리자 | 전체 관리, 승인 |
| 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## 2. 핵심 기능 정의

### 2.1 필수 기능 (MVP)

```
Phase 1: 인증 시스템
├── 회원가입 (이메일/비밀번호)
├── 로그인 (JWT)
├── 비밀번호 재설정
└── 역할 기반 접근 제어

Phase 2: 유치원 & 반 관리
├── 유치원 등록/수정
├── 반 생성/수정/삭제
├── 교사 배정
└── 원생 등록/관리

Phase 3: 출석 관리
├── 일별 출석 체크
├── 등/하원 시간 기록
├── 결석 사유 입력
└── 월별 출석 통계

Phase 4: 알림장
├── 알림장 작성 (교사)
├── 알림장 확인 (학부모)
├── 사진 첨부
└── 읽음 확인

Phase 5: 공지사항
├── 공지 작성/수정/삭제
├── 중요 공지 설정
└── 공지 목록 조회
```

### 2.2 선택 기능 (확장)
```
추후 확장 가능:
├── 식단표 관리
├── 일정 캘린더
├── 투약의뢰서
├── 채팅 (WebSocket)
├── 푸시 알림
└── 통계 대시보드
```

### 2.3 제외 기능 (기존 대비)
```
❌ 전자결재 - 복잡도 대비 효용 낮음
❌ 청구서/결제 - 외부 서비스 연동 권장
❌ OAuth 소셜 로그인 - MVP 이후 추가
❌ 채팅 - MVP 이후 추가
❌ 귀가동의서 - MVP 이후 추가
```

---

## 3. 기술 스택

### 3.1 Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.3.x | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.x | 토큰 인증 |
| Spring Validation | - | 입력 검증 |

### 3.2 Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 세션, 토큰 관리 |
| Flyway | - | DB 마이그레이션 |

### 3.3 Frontend (Server-Side Rendering)
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript (ES6+) | 인터랙션 |
| CSS (Custom Properties) | 스타일링 |
| Fetch API | AJAX 요청 |

> **Note**: 프레임워크 없이 순수 HTML/CSS/JS로 구현

### 3.4 Infrastructure
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| AWS S3 | 파일 업로드 |

### 3.5 개발 도구
| 도구 | 용도 |
|------|------|
| Gradle | 빌드 도구 |
| Cursor | IDE |
| DBeaver | DB 클라이언트 |
| Postman | API 테스트 |

---

## 4. 프로젝트 구조

### 4.1 디렉토리 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── erp/
│   │   │           ├── ErpApplication.java
│   │   │           │
│   │   │           ├── global/                    # 전역 설정
│   │   │           │   ├── config/
│   │   │           │   │   ├── SecurityConfig.java
│   │   │           │   │   ├── JpaConfig.java
│   │   │           │   │   ├── RedisConfig.java
│   │   │           │   │   ├── QuerydslConfig.java
│   │   │           │   │   └── WebConfig.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ErrorCode.java
│   │   │           │   ├── security/
│   │   │           │   │   ├── jwt/
│   │   │           │   │   │   ├── JwtTokenProvider.java
│   │   │           │   │   │   ├── JwtAuthenticationFilter.java
│   │   │           │   │   │   └── JwtProperties.java
│   │   │           │   │   ├── CustomUserDetails.java
│   │   │           │   │   └── CustomUserDetailsService.java
│   │   │           │   ├── common/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── ApiResponse.java
│   │   │           │   └── util/
│   │   │           │       └── DateUtils.java
│   │   │           │
│   │   │           └── domain/                    # 도메인별 패키지
│   │   │               ├── member/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── MemberApiController.java
│   │   │               │   │   └── MemberViewController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── MemberService.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── MemberRepository.java
│   │   │               │   │   └── MemberRepositoryImpl.java
│   │   │               │   ├── entity/
│   │   │               │   │   └── Member.java
│   │   │               │   └── dto/
│   │   │               │       ├── MemberRequest.java
│   │   │               │       └── MemberResponse.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   ├── service/
│   │   │               │   │   └── AuthService.java
│   │   │               │   └── dto/
│   │   │               │       ├── LoginRequest.java
│   │   │               │       ├── SignUpRequest.java
│   │   │               │       └── TokenResponse.java
│   │   │               │
│   │   │               ├── kindergarten/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kindergarten.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── classroom/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Classroom.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── kid/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Kid.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── attendance/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Attendance.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               ├── notepad/
│   │   │               │   ├── controller/
│   │   │               │   ├── service/
│   │   │               │   ├── repository/
│   │   │               │   ├── entity/
│   │   │               │   │   └── Notepad.java
│   │   │               │   └── dto/
│   │   │               │
│   │   │               └── announcement/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── entity/
│   │   │                   │   └── Announcement.java
│   │   │                   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_schema.sql
│   │       │       ├── V2__add_classroom.sql
│   │       │       └── ...
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── base/
│   │       │   │   │   ├── reset.css
│   │       │   │   │   └── variables.css
│   │       │   │   ├── components/
│   │       │   │   │   ├── button.css
│   │       │   │   │   ├── card.css
│   │       │   │   │   ├── form.css
│   │       │   │   │   ├── modal.css
│   │       │   │   │   └── table.css
│   │       │   │   ├── layout/
│   │       │   │   │   ├── header.css
│   │       │   │   │   ├── sidebar.css
│   │       │   │   │   └── footer.css
│   │       │   │   └── main.css
│   │       │   ├── js/
│   │       │   │   ├── utils/
│   │       │   │   │   ├── api.js
│   │       │   │   │   ├── dom.js
│   │       │   │   │   └── validate.js
│   │       │   │   ├── components/
│   │       │   │   │   ├── modal.js
│   │       │   │   │   └── toast.js
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       │       └── logo.svg
│   │       └── templates/
│   │           ├── layout/
│   │           │   └── default.html
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── sidebar.html
│   │           │   └── footer.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── signup.html
│   │           ├── dashboard/
│   │           │   └── index.html
│   │           ├── classroom/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── kid/
│   │           │   ├── list.html
│   │           │   └── detail.html
│   │           ├── attendance/
│   │           │   ├── daily.html
│   │           │   └── monthly.html
│   │           ├── notepad/
│   │           │   ├── list.html
│   │           │   ├── write.html
│   │           │   └── detail.html
│   │           └── announcement/
│   │               ├── list.html
│   │               ├── write.html
│   │               └── detail.html
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── erp/
│                   ├── domain/
│                   │   ├── member/
│                   │   │   ├── service/
│                   │   │   │   └── MemberServiceTest.java
│                   │   │   └── repository/
│                   │   │       └── MemberRepositoryTest.java
│                   │   └── ...
│                   └── integration/
│                       └── AuthIntegrationTest.java
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore
├── build.gradle
├── settings.gradle
└── README.md
```

### 4.2 패키지 명명 규칙

| 패키지 | 역할 | 클래스 네이밍 |
|--------|------|--------------|
| `controller` | HTTP 요청 처리 | `*ApiController`, `*ViewController` |
| `service` | 비즈니스 로직 | `*Service` |
| `repository` | 데이터 접근 | `*Repository`, `*RepositoryImpl` |
| `entity` | JPA 엔티티 | 단수형 (Member, Kid) |
| `dto` | 데이터 전송 객체 | `*Request`, `*Response` |

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                           MEMBER                                 │
│  id, email, password, name, phone, role, status,                │
│  kindergarten_id, created_at, updated_at                        │
└─────────────────────────────────────────────────────────────────┘
                              │ N:1
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        KINDERGARTEN                              │
│  id, name, address, phone, open_time, close_time,               │
│  created_at, updated_at                                          │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CLASSROOM                                │
│  id, kindergarten_id, name, age_group, teacher_id,              │
│  created_at, updated_at, deleted_at                              │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            KID                                   │
│  id, classroom_id, name, birth_date, gender,                    │
│  parent_id, admission_date, created_at, updated_at               │
└─────────────────────────────────────────────────────────────────┘
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ATTENDANCE                                │
│  id, kid_id, date, status, drop_off_time, pick_up_time,        │
│  note, created_at, updated_at                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          NOTEPAD                                 │
│  id, classroom_id, kid_id, writer_id, title, content,           │
│  is_read, created_at, updated_at                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       ANNOUNCEMENT                               │
│  id, kindergarten_id, writer_id, title, content,                │
│  is_important, created_at, updated_at, deleted_at                │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 테이블 상세

#### MEMBER (회원)
```sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,           -- PRINCIPAL, TEACHER, PARENT
    status VARCHAR(20) NOT NULL,          -- ACTIVE, INACTIVE
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id)
);
```

#### KINDERGARTEN (유치원)
```sql
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### CLASSROOM (반)
```sql
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(20),                -- 5세반, 6세반, 7세반
    teacher_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (teacher_id) REFERENCES member(id)
);
```

#### KID (원생)
```sql
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,          -- MALE, FEMALE
    parent_id BIGINT,
    admission_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (parent_id) REFERENCES member(id)
);
```

#### ATTENDANCE (출석)
```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,          -- PRESENT, ABSENT, LATE
    drop_off_time TIME,
    pick_up_time TIME,
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    UNIQUE KEY uk_kid_date (kid_id, date)
);
```

#### NOTEPAD (알림장)
```sql
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    kid_id BIGINT,                        -- NULL이면 반 전체
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (kid_id) REFERENCES kid(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

#### ANNOUNCEMENT (공지사항)
```sql
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    
    FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id),
    FOREIGN KEY (writer_id) REFERENCES member(id)
);
```

### 5.3 Redis 활용 계획

```
Redis 사용 용도:

1. JWT Refresh Token 저장
   Key: "refresh:{userId}"
   Value: refreshToken
   TTL: 7 days

2. 이메일 인증 코드
   Key: "email:verify:{email}"
   Value: verificationCode
   TTL: 10 minutes

3. 비밀번호 재설정 토큰
   Key: "password:reset:{token}"
   Value: userId
   TTL: 1 hour

4. API Rate Limiting (추후)
   Key: "rate:{userId}:{endpoint}"
   Value: count
   TTL: 1 minute

5. 캐시 (추후)
   Key: "cache:kindergarten:{id}"
   Value: KindergartenDTO (JSON)
   TTL: 5 minutes
```

---

## 6. API 설계

### 6.1 API 규칙

```
Base URL: /api/v1

인증:
- Authorization: Bearer {accessToken}
- 토큰 만료 시 401 응답

응답 형식:
{
    "success": true,
    "data": { ... },
    "message": null
}

에러 응답:
{
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "code": "ERROR_CODE"
}
```

### 6.2 API 목록

#### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/password/reset-request` | 비밀번호 재설정 요청 |
| POST | `/api/v1/auth/password/reset` | 비밀번호 재설정 |

#### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PUT | `/api/v1/members/me` | 내 정보 수정 |
| PUT | `/api/v1/members/me/password` | 비밀번호 변경 |

#### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

#### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| GET | `/api/v1/classrooms/{id}` | 반 상세 조회 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 (Soft) |

#### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 조회 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

#### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily?date={date}` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly?year={year}&month={month}` | 월별 통계 |

#### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

#### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| GET | `/api/v1/announcements/{id}` | 공지 상세 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 (Soft) |

---

## 7. 단계별 구현 가이드

> 🎯 직접 타이핑하며 구현하기 위한 순서입니다.

### Step 1: 프로젝트 초기 설정 (Day 1)

```
[ ] 1-1. Spring Initializr로 프로젝트 생성
    - https://start.spring.io/
    - Project: Gradle - Groovy
    - Language: Java
    - Spring Boot: 3.3.x
    - Group: com.erp
    - Artifact: erp
    - Package name: com.erp
    - Packaging: Jar
    - Java: 17
    - Dependencies:
      • Spring Web
      • Spring Data JPA
      • Spring Security
      • Spring Validation
      • Thymeleaf
      • Lombok
      • MySQL Driver
      • Spring Data Redis

[ ] 1-2. build.gradle 수정
    - QueryDSL 추가
    - JWT 라이브러리 추가
    - Flyway 추가

[ ] 1-3. 디렉토리 구조 생성
    - global 패키지 생성
    - domain 패키지 생성

[ ] 1-4. application.yml 설정
    - 환경별 분리 (local, prod)
    - 데이터베이스 연결 설정
    - Redis 연결 설정
```

### Step 2: 전역 설정 구현 (Day 2)

```
[ ] 2-1. BaseEntity 작성
    - createdAt, updatedAt 필드
    - JPA Auditing 설정

[ ] 2-2. 예외 처리 구현
    - ErrorCode enum 작성
    - BusinessException 클래스 작성
    - GlobalExceptionHandler 작성

[ ] 2-3. 공통 응답 클래스
    - ApiResponse 작성

[ ] 2-4. Config 클래스 작성
    - JpaConfig (Auditing 활성화)
    - QuerydslConfig
    - RedisConfig
```

### Step 3: 인증 시스템 구현 (Day 3-4)

```
[ ] 3-1. Member 엔티티 작성
    - Member.java
    - MemberRole enum
    - MemberStatus enum

[ ] 3-2. Member Repository 작성
    - MemberRepository.java (JpaRepository)

[ ] 3-3. JWT 구현
    - JwtProperties.java
    - JwtTokenProvider.java
    - JwtAuthenticationFilter.java

[ ] 3-4. Security 설정
    - CustomUserDetails.java
    - CustomUserDetailsService.java
    - SecurityConfig.java

[ ] 3-5. Auth 기능 구현
    - SignUpRequest.java
    - LoginRequest.java
    - TokenResponse.java
    - AuthService.java
    - AuthController.java

[ ] 3-6. 로그인/회원가입 화면
    - login.html
    - signup.html
    - 관련 CSS/JS
```

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

```
[ ] 4-1. Kindergarten 도메인
    - Kindergarten 엔티티
    - KindergartenRepository
    - KindergartenService
    - KindergartenController
    - Request/Response DTO

[ ] 4-2. Classroom 도메인
    - Classroom 엔티티
    - ClassroomRepository
    - ClassroomService
    - ClassroomController
    - Request/Response DTO

[ ] 4-3. 반 관리 화면
    - classroom/list.html
    - classroom/detail.html
    - 관련 CSS/JS
```

### Step 5: 원생 관리 구현 (Day 7-8)

```
[ ] 5-1. Kid 도메인
    - Kid 엔티티
    - KidRepository
    - KidService
    - KidController
    - Request/Response DTO

[ ] 5-2. 원생 관리 화면
    - kid/list.html
    - kid/detail.html
    - 관련 CSS/JS
```

### Step 6: 출석 관리 구현 (Day 9-10)

```
[ ] 6-1. Attendance 도메인
    - Attendance 엔티티
    - AttendanceStatus enum
    - AttendanceRepository
    - AttendanceService
    - AttendanceController

[ ] 6-2. 출석 관리 화면
    - attendance/daily.html
    - attendance/monthly.html
    - 관련 CSS/JS
```

### Step 7: 알림장 구현 (Day 11-12)

```
[ ] 7-1. Notepad 도메인
    - Notepad 엔티티
    - NotepadRepository
    - NotepadService
    - NotepadController

[ ] 7-2. 알림장 화면
    - notepad/list.html
    - notepad/write.html
    - notepad/detail.html
    - 관련 CSS/JS
```

### Step 8: 공지사항 구현 (Day 13-14)

```
[ ] 8-1. Announcement 도메인
    - Announcement 엔티티
    - AnnouncementRepository
    - AnnouncementService
    - AnnouncementController

[ ] 8-2. 공지사항 화면
    - announcement/list.html
    - announcement/write.html
    - announcement/detail.html
    - 관련 CSS/JS
```

### Step 9: 마무리 (Day 15-16)

```
[ ] 9-1. 대시보드 구현
    - 메인 대시보드 화면
    - 역할별 메뉴 분기

[ ] 9-2. 테스트 코드 작성
    - 서비스 단위 테스트
    - 통합 테스트

[ ] 9-3. Docker 환경 구성
    - Dockerfile
    - docker-compose.yml

[ ] 9-4. README.md 작성
    - 프로젝트 설명
    - 실행 방법
    - API 문서 링크
```

---

## 8. UI/UX 컨셉

### 8.1 디자인 원칙

```
1. 미니멀 & 클린
   - 불필요한 장식 제거
   - 여백을 충분히 활용
   - 콘텐츠 중심 레이아웃

2. 일관된 색상 체계
   - Primary: 노란색 계열 (아이들 친화적)
   - Neutral: 그레이 계열
   - Semantic: 성공(초록), 경고(주황), 에러(빨강)

3. 직관적인 네비게이션
   - 사이드바 기반 메뉴
   - Breadcrumb 활용
   - 명확한 액션 버튼

4. 반응형 디자인
   - Mobile-first 접근
   - 태블릿/데스크톱 대응
```

### 8.2 색상 팔레트

```css
:root {
    /* Primary - 따뜻한 노란색 */
    --color-primary-50: #FFFDE7;
    --color-primary-100: #FFF9C4;
    --color-primary-200: #FFF59D;
    --color-primary-300: #FFF176;
    --color-primary-400: #FFEE58;
    --color-primary-500: #FFEB3B;    /* Main */
    --color-primary-600: #FDD835;
    --color-primary-700: #FBC02D;

    /* Neutral - 그레이 */
    --color-gray-50: #FAFAFA;
    --color-gray-100: #F5F5F5;
    --color-gray-200: #EEEEEE;
    --color-gray-300: #E0E0E0;
    --color-gray-400: #BDBDBD;
    --color-gray-500: #9E9E9E;
    --color-gray-600: #757575;
    --color-gray-700: #616161;
    --color-gray-800: #424242;
    --color-gray-900: #212121;

    /* Semantic */
    --color-success: #4CAF50;
    --color-warning: #FF9800;
    --color-error: #F44336;
    --color-info: #2196F3;
}
```

### 8.3 레이아웃 구조

```
┌─────────────────────────────────────────────────────────────┐
│                        Header (60px)                        │
│  ┌──────┐                                    ┌───────────┐  │
│  │ Logo │        (page title)                │  Profile  │  │
│  └──────┘                                    └───────────┘  │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ Sidebar │              Main Content                        │
│ (240px) │                                                   │
│         │  ┌─────────────────────────────────────────────┐ │
│ • 대시보드  │                                             │ │
│ • 반 관리   │              Page Content                   │ │
│ • 원생 관리 │                                             │ │
│ • 출석부    │                                             │ │
│ • 알림장    │                                             │ │
│ • 공지사항  │                                             │ │
│         │  └─────────────────────────────────────────────┘ │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

### 8.4 핵심 화면 와이어프레임

#### 로그인 화면
```
┌─────────────────────────────────────────┐
│                                         │
│            🏫 유치원 ERP                 │
│                                         │
│     ┌─────────────────────────────┐     │
│     │ 📧 이메일                    │     │
│     └─────────────────────────────┘     │
│     ┌─────────────────────────────┐     │
│     │ 🔒 비밀번호                  │     │
│     └─────────────────────────────┘     │
│                                         │
│     ┌─────────────────────────────┐     │
│     │         로그인               │     │
│     └─────────────────────────────┘     │
│                                         │
│        비밀번호 찾기 | 회원가입         │
│                                         │
└─────────────────────────────────────────┘
```

#### 대시보드 (교사)
```
┌─────────────────────────────────────────────────────────────┐
│ 🏠 대시보드                                   👤 김선생님   │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│ 📊 대시보드│  안녕하세요, 김선생님! 🌞                       │
│ 🏫 반 관리 │                                                │
│ 👶 원생   │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│ ✅ 출석부 │  │ 🌻 해님반 │ │ ✅ 출석  │ │ 📝 알림장 │       │
│ 📝 알림장 │  │  15명     │ │  12/15  │ │   3건    │       │
│ 📢 공지   │  └──────────┘ └──────────┘ └──────────┘       │
│         │                                                   │
│         │  📅 오늘의 일정                                  │
│         │  ┌───────────────────────────────────────────┐   │
│         │  │ 10:00 - 체육활동                          │   │
│         │  │ 14:00 - 학부모 상담                       │   │
│         │  └───────────────────────────────────────────┘   │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

#### 출석부 (일별)
```
┌─────────────────────────────────────────────────────────────┐
│ ✅ 출석부 > 일별 출석                                       │
├─────────┬───────────────────────────────────────────────────┤
│         │                                                   │
│         │  📅 2024년 12월 28일 (토)    ◀  ▶               │
│         │                                                   │
│         │  ┌────────────────────────────────────────────┐  │
│         │  │ 이름    │ 상태  │ 등원   │ 하원   │ 비고  │  │
│         │  ├─────────┼───────┼────────┼────────┼───────┤  │
│         │  │ 김민준  │ ✅ 출석│ 09:05 │ -     │       │  │
│         │  │ 이서윤  │ ✅ 출석│ 08:55 │ -     │       │  │
│         │  │ 박지우  │ ❌ 결석│ -     │ -     │ 감기  │  │
│         │  │ 최하윤  │ ✅ 출석│ 09:10 │ -     │       │  │
│         │  └────────────────────────────────────────────┘  │
│         │                                                   │
│         │  출석: 14명 | 결석: 1명 | 전체: 15명             │
│         │                                                   │
└─────────┴───────────────────────────────────────────────────┘
```

---

## 📝 파일별 구현 순서 (세부)

### 1️⃣ 프로젝트 초기화

```
📁 생성할 파일 순서:

1. build.gradle
2. settings.gradle
3. src/main/resources/application.yml
4. src/main/resources/application-local.yml
5. src/main/java/com/erp/ErpApplication.java
```

### 2️⃣ 전역 설정

```
📁 생성할 파일 순서:

6. global/common/BaseEntity.java
7. global/exception/ErrorCode.java
8. global/exception/BusinessException.java
9. global/exception/GlobalExceptionHandler.java
10. global/common/ApiResponse.java
11. global/config/JpaConfig.java
12. global/config/QuerydslConfig.java
13. global/config/RedisConfig.java
```

### 3️⃣ 인증

```
📁 생성할 파일 순서:

14. domain/member/entity/Member.java
15. domain/member/entity/MemberRole.java
16. domain/member/entity/MemberStatus.java
17. domain/member/repository/MemberRepository.java
18. global/security/jwt/JwtProperties.java
19. global/security/jwt/JwtTokenProvider.java
20. global/security/jwt/JwtAuthenticationFilter.java
21. global/security/CustomUserDetails.java
22. global/security/CustomUserDetailsService.java
23. global/config/SecurityConfig.java
24. domain/auth/dto/SignUpRequest.java
25. domain/auth/dto/LoginRequest.java
26. domain/auth/dto/TokenResponse.java
27. domain/auth/service/AuthService.java
28. domain/auth/controller/AuthController.java
```

---

## ⚡ Quick Reference

### Gradle 명령어
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# clean
./gradlew clean
```

### Docker 명령어
```bash
# 개발 환경 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

### 자주 쓰는 코드 스니펫

#### 엔티티 기본 구조
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntityName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 필드들...
    
    // 정적 팩토리 메서드
    public static EntityName create(...) {
        EntityName entity = new EntityName();
        // 필드 설정...
        return entity;
    }
    
    // 비즈니스 메서드
    public void update(...) {
        // 업데이트 로직...
    }
}
```

#### 서비스 기본 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SomeService {
    
    private final SomeRepository someRepository;
    
    @Transactional
    public Long create(CreateRequest request) {
        // 생성 로직
    }
    
    public SomeResponse findById(Long id) {
        // 조회 로직
    }
}
```

#### 컨트롤러 기본 구조
```java
@RestController
@RequestMapping("/api/v1/some")
@RequiredArgsConstructor
public class SomeApiController {
    
    private final SomeService someService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody CreateRequest request) {
        Long id = someService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/some/" + id))
            .body(ApiResponse.success(id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SomeResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(someService.findById(id))
        );
    }
}
```

---

## 🚀 시작하기

1. 이 문서를 참고하여 Spring Initializr에서 프로젝트 생성
2. Step 1부터 차근차근 진행
3. 각 Step 완료 후 커밋
4. 막히는 부분은 Cursor에게 질문

**화이팅! 🌟**

---

## 📌 프로젝트 정보 요약

| 항목 | 값 |
|------|-----|
| 프로젝트명 | erp |
| 그룹 ID | com.erp |
| 패키지명 | com.erp |
| 메인 클래스 | ErpApplication.java |


```

완료했습니다! ✅

변경된 내용:

| 변경 전 | 변경 후 |
|---------|---------|
| 아이바요(AiBayo) v2 | 유치원 ERP |
| `com.aibayo` | `com.erp` |
| `aibayo` (artifact) | `erp` |
| `AibayoApplication.java` | `ErpApplication.java` |
| `aibayo-v2/` (폴더) | `erp/` |

마지막에 프로젝트 정보 요약 테이블도 추가했습니다:

```
📌 프로젝트 정보 요약
| 항목 | 값 |
|------|-----|
| 프로젝트명 | erp |
| 그룹 ID | com.erp |
| 패키지명 | com.erp |
| 메인 클래스 | ErpApplication.java |
```

이제 Spring Initializr에서 프로젝트를 생성할 때 위 정보를 그대로 사용하시면 됩니다! 🚀

```plaintext
📌 프로젝트 정보 요약
| 항목 | 값 |
|------|-----|
| 프로젝트명 | erp |
| 그룹 ID | com.erp |
| 패키지명 | com.erp |
| 메인 클래스 | ErpApplication.java |
```

---

**User**

Group에서 com.erp로 앞에 com을 붙이는 이유가 궁금해

---

**Cursor**

좋은 질문입니다! 🎓

## Java 패키지 명명 규칙: `com.xxx`

### 이유: **역방향 도메인 네임(Reverse Domain Name) 규칙**

Java에서는 패키지 이름이 **전 세계적으로 고유**해야 충돌을 방지할 수 있습니다. 
그래서 **도메인 이름을 거꾸로** 사용하는 것이 표준 관례입니다.

```
도메인: google.com     →  패키지: com.google.xxx
도메인: naver.com      →  패키지: com.naver.xxx
도메인: kakao.com      →  패키지: com.kakao.xxx
도메인: example.co.kr  →  패키지: kr.co.example.xxx
```

### 예시

| 회사/조직 | 도메인 | 패키지 |
|-----------|--------|--------|
| Google | google.com | `com.google.guava` |
| Apache | apache.org | `org.apache.commons` |
| Spring | spring.io | `io.spring.boot` |
| 카카오 | kakao.com | `com.kakao.sdk` |

### 왜 거꾸로?

파일 시스템에서 **계층 구조**를 만들기 위해서입니다:

```
src/main/java/
└── com/
    └── erp/
        ├── domain/
        │   ├── member/
        │   └── kid/
        └── global/
            └── config/
```

**큰 범위(com) → 작은 범위(erp) → 더 작은 범위(domain)**로 좁혀가는 구조!

---

## 실무에서는?

### 회사가 있는 경우
```
회사 도메인: mycompany.com
패키지: com.mycompany.projectname
```

### 개인 프로젝트 / 학습용
```
# 방법 1: 본인 도메인이 있다면
도메인: alex.dev → 패키지: dev.alex.erp

# 방법 2: GitHub 기반
GitHub: github.com/alex → 패키지: com.github.alex.erp

# 방법 3: 그냥 관례적으로
패키지: com.erp (간단하게)
패키지: io.erp (요즘 많이 씀)
```

---

## 우리 프로젝트에서

`com.erp`를 사용하는 이유:
1. **관례적으로 `com.`으로 시작** (가장 일반적)
2. **짧고 간결** (학습/포트폴리오 목적)
3. **실제 도메인이 없어도 OK** (개인 프로젝트)

만약 본인 도메인이나 GitHub 아이디로 바꾸고 싶다면:
```
com.github.alexmoon.erp
dev.alexmoon.erp
```

이런 식으로 변경해도 됩니다! 원하시면 수정해드릴까요? 😊

```plaintext
도메인: google.com     →  패키지: com.google.xxx
도메인: naver.com      →  패키지: com.naver.xxx
도메인: kakao.com      →  패키지: com.kakao.xxx
도메인: example.co.kr  →  패키지: kr.co.example.xxx
```

```plaintext
src/main/java/
└── com/
    └── erp/
        ├── domain/
        │   ├── member/
        │   └── kid/
        └── global/
            └── config/
```

```plaintext
회사 도메인: mycompany.com
패키지: com.mycompany.projectname
```

```plaintext
# 방법 1: 본인 도메인이 있다면
도메인: alex.dev → 패키지: dev.alex.erp

# 방법 2: GitHub 기반
GitHub: github.com/alex → 패키지: com.github.alex.erp

# 방법 3: 그냥 관례적으로
패키지: com.erp (간단하게)
패키지: io.erp (요즘 많이 씀)
```

```plaintext
com.github.alexmoon.erp
dev.alexmoon.erp
```

---

**User**

oauth2 기능 구현할건데 Dependencies에서 OAuth2 Client, OAuth2 Resource Server 설치 안해도 괜찮아? 컴팩트하게 대답해 

---

**Cursor**

## 결론: **OAuth2 Client만 필요**

| 의존성 | 필요 여부 | 용도 |
|--------|----------|------|
| **OAuth2 Client** | ✅ 필요 | 구글/네이버 **소셜 로그인** |
| **OAuth2 Resource Server** | ❌ 불필요 | 외부에서 우리 API를 OAuth로 호출할 때 사용 (우리는 JWT 사용) |

### 정리
- **우리가 구글/네이버에 로그인 요청** → `OAuth2 Client` ✅
- **외부가 우리에게 OAuth 토큰으로 API 호출** → `Resource Server` (안 씀)

`project_idea.md`에서 MVP 이후 OAuth 추가 예정이라 Dependencies에 안 넣어뒀는데, 나중에 추가할 때 **OAuth2 Client만** 추가하면 됩니다! 👍

---

**User**

처음 프로젝트 만드는 설정할 때 Package name이 com.erp.erp 이렇게 설정되던데 com.erp로 바꿔? 예 아니요로 답변해

---

**Cursor**

**예** ✅

`com.erp.erp`는 중복이라 지저분합니다. `com.erp`로 바꾸세요!

---

**User**

springboot version이 4.0.2 (SNAPSHOT), 4.0.1, 3.5.10 (SNAPSHOT), 3.5.9 이렇게 나와있어 어떤거 설정하는지 답만 답변

---

**Cursor**

**3.5.9**

---

**User**

자 이제 프로젝트 이동할거야. 현재 transcript도 export해서 가져갈거야. 이 프로젝트에서 어떤 파일들 가져갈까? 그리고 프로젝트의 어떤 디렉토리에 넣어놀까?

---

**Cursor**

## 가져갈 파일

| 파일 | 필수 | 이유 |
|------|------|------|
| `project_idea.md` | ✅ 필수 | 새 프로젝트 설계서 (핵심!) |
| `project_summary.md` | 📌 권장 | 기존 프로젝트 분석 (도메인 참고용) |
| `modify_plan.md` | ❌ 선택 | 기존 프로젝트 수정 계획 (새로 만들거라 불필요) |

---

## 새 프로젝트 디렉토리 구조

```
erp/                          ← 새 프로젝트 루트
├── docs/                     ← 여기에 넣기!
│   ├── project_idea.md       ← 설계서
│   └── project_summary.md    ← 참고용
├── src/
├── build.gradle
└── ...
```

---

## 요약

```bash
# 새 프로젝트에서
mkdir docs
# project_idea.md → docs/project_idea.md
# project_summary.md → docs/project_summary.md (선택)
```

`docs/` 폴더에 넣어두면 프로젝트 문서로 깔끔하게 관리됩니다! 📁

```plaintext
erp/                          ← 새 프로젝트 루트
├── docs/                     ← 여기에 넣기!
│   ├── project_idea.md       ← 설계서
│   └── project_summary.md    ← 참고용
├── src/
├── build.gradle
└── ...
```

```shellscript
# 새 프로젝트에서
mkdir docs
# project_idea.md → docs/project_idea.md
# project_summary.md → docs/project_summary.md (선택)
```

