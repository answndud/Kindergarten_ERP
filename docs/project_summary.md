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

