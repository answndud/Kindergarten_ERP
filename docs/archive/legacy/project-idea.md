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
| 기술 | 버전 | 용도 |
|------|------|------|
| Thymeleaf | - | 템플릿 엔진 |
| HTMX | 1.9.x | 동적 HTML 교체 (AJAX 없이) |
| Alpine.js | 3.x | 가벼운 클라이언트 상태 관리 |
| Tailwind CSS | 3.4 | 유틸리티 퍼스트 스타일링 |

> **Note**: Thymeleaf + HTMX 조합으로 서버 사이드 렌더링하면서 동적 상호작용 가능
> - HTMX: 서버에서 HTML 조각만 받아와서 DOM 업데이트
> - Alpine.js: 간단한 클라이언트 사이드 상태 (모달, 토글 등)
> - Tailwind CSS: 클래스 기반 스타일링으로 빠른 UI 개발

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
│   │       │   │   └── custom.css           # Tailwind로 커버 안되는 커스텀 스타일
│   │       │   ├── js/
│   │       │   │   └── app.js               # HTMX/Alpine 설정 및 전역 함수
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

#### 2-1. BaseEntity 작성
**파일**: `global/common/BaseEntity.java`

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**체크리스트**:
- [ ] `@MappedSuperclass` 어노테이션 적용
- [ ] `@EntityListeners(AuditingEntityListener.class)` 적용
- [ ] `@CreatedDate`, `@LastModifiedDate` 필드 추가
- [ ] `createdAt`은 `updatable = false` 설정

---

#### 2-2. 예외 처리 구현

**파일 1**: `global/exception/ErrorCode.java`
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
    ENTITY_NOT_FOUND(404, "C002", "엔티티를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다"),
    
    // Auth
    INVALID_CREDENTIALS(401, "A001", "이메일 또는 비밀번호가 잘못되었습니다"),
    TOKEN_EXPIRED(401, "A002", "토큰이 만료되었습니다"),
    TOKEN_INVALID(401, "A003", "유효하지 않은 토큰입니다"),
    ACCESS_DENIED(403, "A004", "접근 권한이 없습니다"),
    
    // Member
    EMAIL_ALREADY_EXISTS(409, "M001", "이미 사용 중인 이메일입니다"),
    MEMBER_NOT_FOUND(404, "M002", "회원을 찾을 수 없습니다"),
    
    // Kindergarten
    KINDERGARTEN_NOT_FOUND(404, "K001", "유치원을 찾을 수 없습니다"),
    
    // Classroom
    CLASSROOM_NOT_FOUND(404, "CL001", "반을 찾을 수 없습니다"),
    
    // Kid
    KID_NOT_FOUND(404, "KD001", "원생을 찾을 수 없습니다");
    
    private final int status;
    private final String code;
    private final String message;
}
```

**파일 2**: `global/exception/BusinessException.java`
```java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

**파일 3**: `global/exception/GlobalExceptionHandler.java`
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // BusinessException 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e);
    
    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e);
    
    // 그 외 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e);
}
```

**체크리스트**:
- [ ] ErrorCode enum에 status, code, message 필드
- [ ] BusinessException이 ErrorCode를 포함
- [ ] GlobalExceptionHandler에서 3가지 예외 처리

---

#### 2-3. 공통 응답 클래스
**파일**: `global/common/ApiResponse.java`

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String code;
    
    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data);
    
    // 성공 응답 (데이터 없음)
    public static ApiResponse<Void> success();
    
    // 성공 응답 (메시지 포함)
    public static <T> ApiResponse<T> success(T data, String message);
    
    // 실패 응답
    public static ApiResponse<?> error(ErrorCode errorCode);
    
    // 실패 응답 (커스텀 메시지)
    public static ApiResponse<?> error(ErrorCode errorCode, String message);
}
```

**체크리스트**:
- [ ] 제네릭 타입 `<T>` 사용
- [ ] 정적 팩토리 메서드 패턴 적용
- [ ] success, error 메서드 구현

---

#### 2-4. Config 클래스 작성

**파일 1**: `global/config/JpaConfig.java`
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing 활성화
}
```

**파일 2**: `global/config/QuerydslConfig.java`
```java
@Configuration
public class QuerydslConfig {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

**파일 3**: `global/config/RedisConfig.java`
```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String host;
    
    @Value("${spring.data.redis.port}")
    private int port;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory();
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate();
}
```

**체크리스트**:
- [ ] JpaConfig: `@EnableJpaAuditing` 적용
- [ ] QuerydslConfig: `JPAQueryFactory` 빈 등록
- [ ] RedisConfig: `RedisTemplate` 빈 등록

### Step 3: 인증 시스템 구현 (Day 3-4)

#### 3-1. Member 엔티티 작성

**파일 1**: `domain/member/entity/MemberRole.java`
```java
@Getter
@RequiredArgsConstructor
public enum MemberRole {
    PRINCIPAL("ROLE_PRINCIPAL", "원장"),
    TEACHER("ROLE_TEACHER", "교사"),
    PARENT("ROLE_PARENT", "학부모");
    
    private final String key;
    private final String title;
}
```

**파일 2**: `domain/member/entity/MemberStatus.java`
```java
public enum MemberStatus {
    ACTIVE,      // 활성
    INACTIVE,    // 비활성
    PENDING      // 승인 대기
}
```

**파일 3**: `domain/member/entity/Member.java`
```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kindergarten_id")
    private Kindergarten kindergarten;
    
    // === 정적 팩토리 메서드 ===
    public static Member create(String email, String encodedPassword, 
                                String name, String phone, MemberRole role);
    
    // === 비즈니스 메서드 ===
    public void updateProfile(String name, String phone);
    public void changePassword(String encodedPassword);
    public void assignKindergarten(Kindergarten kindergarten);
    public void activate();
    public void deactivate();
}
```

**체크리스트**:
- [ ] MemberRole: PRINCIPAL, TEACHER, PARENT 정의
- [ ] MemberStatus: ACTIVE, INACTIVE, PENDING 정의
- [ ] Member: BaseEntity 상속
- [ ] Member: 연관관계 매핑 (Kindergarten)
- [ ] Member: 정적 팩토리 메서드 `create()`
- [ ] Member: 비즈니스 메서드들

---

#### 3-2. Member Repository 작성

**파일 1**: `domain/member/repository/MemberRepository.java`
```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    
    Optional<Member> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
```

**파일 2**: `domain/member/repository/MemberRepositoryCustom.java`
```java
public interface MemberRepositoryCustom {
    // QueryDSL용 커스텀 메서드 (필요시 추가)
}
```

**파일 3**: `domain/member/repository/MemberRepositoryImpl.java`
```java
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    // QueryDSL 구현체 (필요시 추가)
}
```

**체크리스트**:
- [ ] `findByEmail()` 메서드 추가
- [ ] `existsByEmail()` 메서드 추가
- [ ] QueryDSL용 인터페이스/구현체 준비

---

#### 3-3. JWT 구현

**파일 1**: `global/security/jwt/JwtProperties.java`
```java
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessTokenValidity;   // 15분
    private long refreshTokenValidity;  // 7일
}
```

**파일 2**: `global/security/jwt/JwtTokenProvider.java`
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    
    private final JwtProperties jwtProperties;
    private Key key;
    
    @PostConstruct
    public void init();  // 키 초기화
    
    // Access Token 생성
    public String createAccessToken(Long memberId, String email, MemberRole role);
    
    // Refresh Token 생성
    public String createRefreshToken(Long memberId);
    
    // 토큰에서 Claims 추출
    public Claims getClaims(String token);
    
    // 토큰에서 memberId 추출
    public Long getMemberId(String token);
    
    // 토큰 유효성 검증
    public boolean validateToken(String token);
    
    // 토큰 만료 여부 확인
    public boolean isExpired(String token);
}
```

**파일 3**: `global/security/jwt/JwtAuthenticationFilter.java`
```java
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain);
    
    // 헤더에서 토큰 추출 (Bearer 제거)
    private String resolveToken(HttpServletRequest request);
}
```

**체크리스트**:
- [ ] JwtProperties: `@ConfigurationProperties` 사용
- [ ] JwtTokenProvider: 토큰 생성/검증 메서드
- [ ] JwtAuthenticationFilter: `OncePerRequestFilter` 상속
- [ ] 토큰은 `Authorization: Bearer {token}` 헤더에서 추출

---

#### 3-4. Security 설정

**파일 1**: `global/security/CustomUserDetails.java`
```java
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private final Member member;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities();
    
    @Override
    public String getPassword();
    
    @Override
    public String getUsername();  // email 반환
    
    @Override
    public boolean isAccountNonExpired();
    
    @Override
    public boolean isAccountNonLocked();
    
    @Override
    public boolean isCredentialsNonExpired();
    
    @Override
    public boolean isEnabled();  // status == ACTIVE 확인
}
```

**파일 2**: `global/security/CustomUserDetailsService.java`
```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
    
    // ID로 조회 (JWT 필터에서 사용)
    public UserDetails loadUserById(Long id);
}
```

**파일 3**: `global/config/SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception;
    
    @Bean
    public PasswordEncoder passwordEncoder();  // BCryptPasswordEncoder
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config);
}
```

**SecurityFilterChain 설정 내용**:
```java
http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()
        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
        .requestMatchers("/", "/login", "/signup").permitAll()
        .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
```

**체크리스트**:
- [ ] CustomUserDetails: `UserDetails` 구현
- [ ] CustomUserDetailsService: `UserDetailsService` 구현
- [ ] SecurityConfig: CSRF 비활성화, Stateless 세션
- [ ] SecurityConfig: JWT 필터 등록
- [ ] PasswordEncoder: BCrypt 사용

---

#### 3-5. Auth 기능 구현

**파일 1**: `domain/auth/dto/SignUpRequest.java`
```java
@Getter
@NoArgsConstructor
public class SignUpRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    
    private String phone;
    
    @NotNull(message = "역할은 필수입니다")
    private MemberRole role;
}
```

**파일 2**: `domain/auth/dto/LoginRequest.java`
```java
@Getter
@NoArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
```

**파일 3**: `domain/auth/dto/TokenResponse.java`
```java
@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
```

**파일 4**: `domain/auth/service/AuthService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    
    // 회원가입
    @Transactional
    public Long signUp(SignUpRequest request);
    
    // 로그인
    public TokenResponse login(LoginRequest request);
    
    // 토큰 갱신
    public TokenResponse refresh(String refreshToken);
    
    // 로그아웃 (Redis에서 Refresh Token 삭제)
    @Transactional
    public void logout(Long memberId);
    
    // Refresh Token을 Redis에 저장
    private void saveRefreshToken(Long memberId, String refreshToken);
    
    // Redis에서 Refresh Token 조회
    private String getRefreshToken(Long memberId);
}
```

**파일 5**: `domain/auth/controller/AuthController.java`
```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    // POST /api/v1/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody SignUpRequest request);
    
    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request);
    
    // POST /api/v1/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestHeader("X-Refresh-Token") String refreshToken);
    
    // POST /api/v1/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails);
}
```

**체크리스트**:
- [ ] SignUpRequest: Validation 어노테이션 적용
- [ ] LoginRequest: Validation 어노테이션 적용
- [ ] TokenResponse: accessToken, refreshToken 포함
- [ ] AuthService: signUp, login, refresh, logout 구현
- [ ] AuthService: Redis에 Refresh Token 저장
- [ ] AuthController: 4개 엔드포인트 구현

---

#### 3-6. 로그인/회원가입 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] login.html, signup.html
- [ ] 관련 CSS/JS

### Step 4: 유치원 & 반 관리 구현 (Day 5-6)

#### 4-1. Kindergarten 도메인

**파일 1**: `domain/kindergarten/entity/Kindergarten.java`
```java
@Entity
@Table(name = "kindergarten")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Kindergarten extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 255)
    private String address;
    
    @Column(length = 20)
    private String phone;
    
    private LocalTime openTime;
    
    private LocalTime closeTime;
    
    @OneToMany(mappedBy = "kindergarten")
    private List<Classroom> classrooms = new ArrayList<>();
    
    // 정적 팩토리 메서드
    public static Kindergarten create(String name, String address, String phone,
                                      LocalTime openTime, LocalTime closeTime);
    
    // 비즈니스 메서드
    public void update(String name, String address, String phone,
                       LocalTime openTime, LocalTime closeTime);
}
```

**파일 2**: `domain/kindergarten/repository/KindergartenRepository.java`
```java
public interface KindergartenRepository extends JpaRepository<Kindergarten, Long> {
    
    boolean existsByName(String name);
}
```

**파일 3**: `domain/kindergarten/dto/KindergartenRequest.java`
```java
@Getter
@NoArgsConstructor
public class KindergartenRequest {
    
    @NotBlank(message = "유치원명은 필수입니다")
    private String name;
    
    private String address;
    private String phone;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
}
```

**파일 4**: `domain/kindergarten/dto/KindergartenResponse.java`
```java
@Getter
@Builder
public class KindergartenResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private int classroomCount;
    
    public static KindergartenResponse from(Kindergarten kindergarten);
}
```

**파일 5**: `domain/kindergarten/service/KindergartenService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KindergartenService {
    
    private final KindergartenRepository kindergartenRepository;
    
    // 유치원 등록
    @Transactional
    public Long create(KindergartenRequest request);
    
    // 유치원 조회
    public KindergartenResponse findById(Long id);
    
    // 유치원 수정
    @Transactional
    public void update(Long id, KindergartenRequest request);
}
```

**파일 6**: `domain/kindergarten/controller/KindergartenApiController.java`
```java
@RestController
@RequestMapping("/api/v1/kindergartens")
@RequiredArgsConstructor
public class KindergartenApiController {
    
    private final KindergartenService kindergartenService;
    
    // POST /api/v1/kindergartens
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@Valid @RequestBody KindergartenRequest request);
    
    // GET /api/v1/kindergartens/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KindergartenResponse>> findById(@PathVariable Long id);
    
    // PUT /api/v1/kindergartens/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                     @Valid @RequestBody KindergartenRequest request);
}
```

**체크리스트**:
- [ ] Kindergarten 엔티티: BaseEntity 상속, 연관관계 매핑
- [ ] Repository: 기본 CRUD + existsByName
- [ ] Request DTO: Validation 적용
- [ ] Response DTO: 정적 팩토리 메서드 `from()`
- [ ] Service: create, findById, update 메서드
- [ ] Controller: 3개 엔드포인트

---

#### 4-2. Classroom 도메인

**파일 1**: `domain/classroom/entity/Classroom.java`
```java
@Entity
@Table(name = "classroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE classroom SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Classroom extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kindergarten_id", nullable = false)
    private Kindergarten kindergarten;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 20)
    private String ageGroup;  // 5세반, 6세반, 7세반
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Member teacher;
    
    private LocalDateTime deletedAt;
    
    @OneToMany(mappedBy = "classroom")
    private List<Kid> kids = new ArrayList<>();
    
    // 정적 팩토리 메서드
    public static Classroom create(Kindergarten kindergarten, String name, String ageGroup);
    
    // 비즈니스 메서드
    public void update(String name, String ageGroup);
    public void assignTeacher(Member teacher);
    public void removeTeacher();
}
```

**파일 2**: `domain/classroom/repository/ClassroomRepository.java`
```java
public interface ClassroomRepository extends JpaRepository<Classroom, Long>, ClassroomRepositoryCustom {
    
    List<Classroom> findByKindergartenId(Long kindergartenId);
    
    Optional<Classroom> findByIdAndKindergartenId(Long id, Long kindergartenId);
}
```

**파일 3**: `domain/classroom/dto/ClassroomRequest.java`
```java
@Getter
@NoArgsConstructor
public class ClassroomRequest {
    
    @NotBlank(message = "반 이름은 필수입니다")
    private String name;
    
    private String ageGroup;
    
    private Long teacherId;
}
```

**파일 4**: `domain/classroom/dto/ClassroomResponse.java`
```java
@Getter
@Builder
public class ClassroomResponse {
    private Long id;
    private String name;
    private String ageGroup;
    private String teacherName;
    private Long teacherId;
    private int kidCount;
    
    public static ClassroomResponse from(Classroom classroom);
}
```

**파일 5**: `domain/classroom/service/ClassroomService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    private final KindergartenRepository kindergartenRepository;
    private final MemberRepository memberRepository;
    
    // 반 목록 조회
    public List<ClassroomResponse> findAll(Long kindergartenId);
    
    // 반 상세 조회
    public ClassroomResponse findById(Long id);
    
    // 반 생성
    @Transactional
    public Long create(Long kindergartenId, ClassroomRequest request);
    
    // 반 수정
    @Transactional
    public void update(Long id, ClassroomRequest request);
    
    // 반 삭제 (Soft Delete)
    @Transactional
    public void delete(Long id);
    
    // 교사 배정
    @Transactional
    public void assignTeacher(Long classroomId, Long teacherId);
}
```

**파일 6**: `domain/classroom/controller/ClassroomApiController.java`
```java
@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassroomApiController {
    
    private final ClassroomService classroomService;
    
    // GET /api/v1/classrooms?kindergartenId={id}
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> findAll(
            @RequestParam Long kindergartenId);
    
    // GET /api/v1/classrooms/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> findById(@PathVariable Long id);
    
    // POST /api/v1/classrooms?kindergartenId={id}
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestParam Long kindergartenId,
            @Valid @RequestBody ClassroomRequest request);
    
    // PUT /api/v1/classrooms/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClassroomRequest request);
    
    // DELETE /api/v1/classrooms/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id);
}
```

**체크리스트**:
- [ ] Classroom 엔티티: Soft Delete 적용 (`@SQLDelete`, `@Where`)
- [ ] Classroom 엔티티: Kindergarten, Teacher 연관관계
- [ ] Repository: kindergartenId로 조회 메서드
- [ ] Service: CRUD + assignTeacher 메서드
- [ ] Controller: 5개 엔드포인트

---

#### 4-3. 반 관리 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] classroom/list.html, classroom/detail.html
- [ ] 관련 CSS/JS

### Step 5: 원생 관리 구현 (Day 7-8)

#### 5-1. Kid 도메인

**파일 1**: `domain/kid/entity/Gender.java`
```java
public enum Gender {
    MALE,
    FEMALE
}
```

**파일 2**: `domain/kid/entity/Kid.java`
```java
@Entity
@Table(name = "kid")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Kid extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false)
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Member parent;
    
    private LocalDate admissionDate;
    
    // 정적 팩토리 메서드
    public static Kid create(String name, LocalDate birthDate, Gender gender, LocalDate admissionDate);
    
    // 비즈니스 메서드
    public void update(String name, LocalDate birthDate, Gender gender, LocalDate admissionDate);
    public void assignClassroom(Classroom classroom);
    public void removeClassroom();
    public void assignParent(Member parent);
    
    // 나이 계산 (만 나이)
    public int getAge();
}
```

**파일 3**: `domain/kid/repository/KidRepository.java`
```java
public interface KidRepository extends JpaRepository<Kid, Long>, KidRepositoryCustom {
    
    List<Kid> findByClassroomId(Long classroomId);
    
    List<Kid> findByParentId(Long parentId);
    
    Optional<Kid> findByIdAndClassroomId(Long id, Long classroomId);
}
```

**파일 4**: `domain/kid/dto/KidRequest.java`
```java
@Getter
@NoArgsConstructor
public class KidRequest {
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;
    
    @NotNull(message = "성별은 필수입니다")
    private Gender gender;
    
    private LocalDate admissionDate;
    
    private Long classroomId;
    
    private Long parentId;
}
```

**파일 5**: `domain/kid/dto/KidResponse.java`
```java
@Getter
@Builder
public class KidResponse {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private int age;
    private Gender gender;
    private LocalDate admissionDate;
    private String classroomName;
    private Long classroomId;
    private String parentName;
    private Long parentId;
    
    public static KidResponse from(Kid kid);
}
```

**파일 6**: `domain/kid/service/KidService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KidService {
    
    private final KidRepository kidRepository;
    private final ClassroomRepository classroomRepository;
    private final MemberRepository memberRepository;
    
    // 원생 목록 조회 (반별)
    public List<KidResponse> findByClassroom(Long classroomId);
    
    // 원생 목록 조회 (학부모별)
    public List<KidResponse> findByParent(Long parentId);
    
    // 원생 상세 조회
    public KidResponse findById(Long id);
    
    // 원생 등록
    @Transactional
    public Long create(KidRequest request);
    
    // 원생 수정
    @Transactional
    public void update(Long id, KidRequest request);
    
    // 원생 삭제
    @Transactional
    public void delete(Long id);
    
    // 반 배정
    @Transactional
    public void assignClassroom(Long kidId, Long classroomId);
}
```

**파일 7**: `domain/kid/controller/KidApiController.java`
```java
@RestController
@RequestMapping("/api/v1/kids")
@RequiredArgsConstructor
public class KidApiController {
    
    private final KidService kidService;
    
    // GET /api/v1/kids?classroomId={id}
    @GetMapping
    public ResponseEntity<ApiResponse<List<KidResponse>>> findAll(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long parentId);
    
    // GET /api/v1/kids/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KidResponse>> findById(@PathVariable Long id);
    
    // POST /api/v1/kids
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@Valid @RequestBody KidRequest request);
    
    // PUT /api/v1/kids/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody KidRequest request);
    
    // DELETE /api/v1/kids/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id);
}
```

**체크리스트**:
- [ ] Gender enum: MALE, FEMALE
- [ ] Kid 엔티티: Classroom, Parent 연관관계
- [ ] Kid 엔티티: getAge() 메서드 (만 나이 계산)
- [ ] Repository: classroomId, parentId로 조회
- [ ] Service: CRUD + assignClassroom 메서드
- [ ] Controller: 5개 엔드포인트

---

#### 5-2. 원생 관리 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] kid/list.html, kid/detail.html
- [ ] 관련 CSS/JS

### Step 6: 출석 관리 구현 (Day 9-10)

#### 6-1. Attendance 도메인

**파일 1**: `domain/attendance/entity/AttendanceStatus.java`
```java
@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {
    PRESENT("출석"),
    ABSENT("결석"),
    LATE("지각"),
    EARLY_LEAVE("조퇴");
    
    private final String description;
}
```

**파일 2**: `domain/attendance/entity/Attendance.java`
```java
@Entity
@Table(name = "attendance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"kid_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kid_id", nullable = false)
    private Kid kid;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;
    
    private LocalTime dropOffTime;  // 등원 시간
    
    private LocalTime pickUpTime;   // 하원 시간
    
    @Column(length = 255)
    private String note;  // 비고 (결석 사유 등)
    
    // 정적 팩토리 메서드
    public static Attendance create(Kid kid, LocalDate date, AttendanceStatus status);
    
    // 비즈니스 메서드
    public void updateStatus(AttendanceStatus status);
    public void recordDropOff(LocalTime time);
    public void recordPickUp(LocalTime time);
    public void updateNote(String note);
    
    // 출석 여부 확인
    public boolean isPresent();
}
```

**파일 3**: `domain/attendance/repository/AttendanceRepository.java`
```java
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, AttendanceRepositoryCustom {
    
    Optional<Attendance> findByKidIdAndDate(Long kidId, LocalDate date);
    
    List<Attendance> findByKidIdAndDateBetween(Long kidId, LocalDate startDate, LocalDate endDate);
    
    List<Attendance> findByDateAndKidClassroomId(LocalDate date, Long classroomId);
}
```

**파일 4**: `domain/attendance/repository/AttendanceRepositoryCustom.java`
```java
public interface AttendanceRepositoryCustom {
    
    // 월별 출석 통계 조회
    List<AttendanceStatistics> findMonthlyStatistics(Long classroomId, int year, int month);
}
```

**파일 5**: `domain/attendance/dto/AttendanceRequest.java`
```java
@Getter
@NoArgsConstructor
public class AttendanceRequest {
    
    @NotNull(message = "원생 ID는 필수입니다")
    private Long kidId;
    
    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;
    
    @NotNull(message = "출석 상태는 필수입니다")
    private AttendanceStatus status;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime dropOffTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime pickUpTime;
    
    private String note;
}
```

**파일 6**: `domain/attendance/dto/AttendanceResponse.java`
```java
@Getter
@Builder
public class AttendanceResponse {
    private Long id;
    private Long kidId;
    private String kidName;
    private LocalDate date;
    private AttendanceStatus status;
    private LocalTime dropOffTime;
    private LocalTime pickUpTime;
    private String note;
    
    public static AttendanceResponse from(Attendance attendance);
}
```

**파일 7**: `domain/attendance/dto/DailyAttendanceResponse.java`
```java
@Getter
@Builder
public class DailyAttendanceResponse {
    private LocalDate date;
    private Long classroomId;
    private String classroomName;
    private int totalCount;
    private int presentCount;
    private int absentCount;
    private List<AttendanceResponse> attendances;
}
```

**파일 8**: `domain/attendance/service/AttendanceService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final KidRepository kidRepository;
    private final ClassroomRepository classroomRepository;
    
    // 일별 출석 조회
    public DailyAttendanceResponse findDailyAttendance(Long classroomId, LocalDate date);
    
    // 출석 등록/수정 (Upsert)
    @Transactional
    public void saveAttendance(AttendanceRequest request);
    
    // 일괄 출석 등록/수정
    @Transactional
    public void saveAllAttendance(List<AttendanceRequest> requests);
    
    // 등원 시간 기록
    @Transactional
    public void recordDropOff(Long kidId, LocalDate date, LocalTime time);
    
    // 하원 시간 기록
    @Transactional
    public void recordPickUp(Long kidId, LocalDate date, LocalTime time);
    
    // 월별 출석 통계
    public List<AttendanceStatistics> getMonthlyStatistics(Long classroomId, int year, int month);
}
```

**파일 9**: `domain/attendance/controller/AttendanceApiController.java`
```java
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {
    
    private final AttendanceService attendanceService;
    
    // GET /api/v1/attendance/daily?classroomId={id}&date={date}
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyAttendanceResponse>> getDailyAttendance(
            @RequestParam Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
    
    // POST /api/v1/attendance
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveAttendance(
            @Valid @RequestBody AttendanceRequest request);
    
    // POST /api/v1/attendance/batch
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> saveAllAttendance(
            @Valid @RequestBody List<AttendanceRequest> requests);
    
    // PATCH /api/v1/attendance/drop-off
    @PatchMapping("/drop-off")
    public ResponseEntity<ApiResponse<Void>> recordDropOff(
            @RequestParam Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
    
    // PATCH /api/v1/attendance/pick-up
    @PatchMapping("/pick-up")
    public ResponseEntity<ApiResponse<Void>> recordPickUp(
            @RequestParam Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
    
    // GET /api/v1/attendance/monthly?classroomId={id}&year={year}&month={month}
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<AttendanceStatistics>>> getMonthlyStatistics(
            @RequestParam Long classroomId,
            @RequestParam int year,
            @RequestParam int month);
}
```

**체크리스트**:
- [ ] AttendanceStatus enum: PRESENT, ABSENT, LATE, EARLY_LEAVE
- [ ] Attendance 엔티티: kid_id + date 유니크 제약조건
- [ ] Repository: 일별, 기간별 조회 메서드
- [ ] RepositoryCustom: 월별 통계 QueryDSL 구현
- [ ] Service: 일별 조회, 등록/수정, 일괄 처리, 등하원 기록
- [ ] Controller: 6개 엔드포인트

---

#### 6-2. 출석 관리 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] attendance/daily.html, attendance/monthly.html
- [ ] 관련 CSS/JS

### Step 7: 알림장 구현 (Day 11-12)

#### 7-1. Notepad 도메인

**파일 1**: `domain/notepad/entity/Notepad.java`
```java
@Entity
@Table(name = "notepad")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notepad extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kid_id")
    private Kid kid;  // NULL이면 반 전체 알림장
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private boolean isRead = false;
    
    // 정적 팩토리 메서드
    public static Notepad createForClass(Classroom classroom, Member writer, String title, String content);
    public static Notepad createForKid(Kid kid, Member writer, String title, String content);
    
    // 비즈니스 메서드
    public void update(String title, String content);
    public void markAsRead();
    
    // 반 전체 알림장 여부
    public boolean isClassNote();
}
```

**파일 2**: `domain/notepad/repository/NotepadRepository.java`
```java
public interface NotepadRepository extends JpaRepository<Notepad, Long>, NotepadRepositoryCustom {
    
    // 반별 알림장 목록
    List<Notepad> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    // 원생별 알림장 목록 (본인 + 반 전체)
    @Query("SELECT n FROM Notepad n WHERE n.kid.id = :kidId OR (n.classroom.id = :classroomId AND n.kid IS NULL) ORDER BY n.createdAt DESC")
    List<Notepad> findByKidIdOrClassroomId(@Param("kidId") Long kidId, @Param("classroomId") Long classroomId);
    
    // 작성자별 알림장 목록
    List<Notepad> findByWriterIdOrderByCreatedAtDesc(Long writerId);
}
```

**파일 3**: `domain/notepad/repository/NotepadRepositoryCustom.java`
```java
public interface NotepadRepositoryCustom {
    
    // 페이징 + 검색 조건
    Page<Notepad> searchNotepads(Long classroomId, Long kidId, String keyword, Pageable pageable);
}
```

**파일 4**: `domain/notepad/dto/NotepadRequest.java`
```java
@Getter
@NoArgsConstructor
public class NotepadRequest {
    
    private Long classroomId;  // 반 전체용
    
    private Long kidId;        // 개인용 (optional)
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
}
```

**파일 5**: `domain/notepad/dto/NotepadResponse.java`
```java
@Getter
@Builder
public class NotepadResponse {
    private Long id;
    private String title;
    private String content;
    private boolean isRead;
    private boolean isClassNote;
    private String writerName;
    private Long writerId;
    private String classroomName;
    private Long classroomId;
    private String kidName;
    private Long kidId;
    private LocalDateTime createdAt;
    
    public static NotepadResponse from(Notepad notepad);
}
```

**파일 6**: `domain/notepad/dto/NotepadListResponse.java`
```java
@Getter
@Builder
public class NotepadListResponse {
    private Long id;
    private String title;
    private boolean isRead;
    private boolean isClassNote;
    private String writerName;
    private String kidName;
    private LocalDateTime createdAt;
    
    public static NotepadListResponse from(Notepad notepad);
}
```

**파일 7**: `domain/notepad/service/NotepadService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotepadService {
    
    private final NotepadRepository notepadRepository;
    private final ClassroomRepository classroomRepository;
    private final KidRepository kidRepository;
    private final MemberRepository memberRepository;
    
    // 알림장 목록 조회 (반별)
    public List<NotepadListResponse> findByClassroom(Long classroomId);
    
    // 알림장 목록 조회 (원생별 - 학부모용)
    public List<NotepadListResponse> findByKid(Long kidId);
    
    // 알림장 상세 조회
    public NotepadResponse findById(Long id);
    
    // 알림장 작성
    @Transactional
    public Long create(Long writerId, NotepadRequest request);
    
    // 알림장 수정
    @Transactional
    public void update(Long id, Long writerId, NotepadRequest request);
    
    // 알림장 삭제
    @Transactional
    public void delete(Long id, Long writerId);
    
    // 읽음 처리
    @Transactional
    public void markAsRead(Long id);
    
    // 페이징 검색
    public Page<NotepadListResponse> search(Long classroomId, Long kidId, String keyword, Pageable pageable);
}
```

**파일 8**: `domain/notepad/controller/NotepadApiController.java`
```java
@RestController
@RequestMapping("/api/v1/notepads")
@RequiredArgsConstructor
public class NotepadApiController {
    
    private final NotepadService notepadService;
    
    // GET /api/v1/notepads?classroomId={id} 또는 ?kidId={id}
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotepadListResponse>>> findAll(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long kidId);
    
    // GET /api/v1/notepads/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotepadResponse>> findById(@PathVariable Long id);
    
    // POST /api/v1/notepads
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotepadRequest request);
    
    // PUT /api/v1/notepads/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotepadRequest request);
    
    // DELETE /api/v1/notepads/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails);
    
    // PATCH /api/v1/notepads/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id);
}
```

**체크리스트**:
- [ ] Notepad 엔티티: kid가 NULL이면 반 전체 알림장
- [ ] Notepad 엔티티: isClassNote() 메서드
- [ ] Repository: JPQL로 원생+반 전체 알림장 조회
- [ ] RepositoryCustom: 페이징 + 검색 QueryDSL 구현
- [ ] Service: CRUD + markAsRead 메서드
- [ ] Controller: 6개 엔드포인트 + `@AuthenticationPrincipal` 사용

---

#### 7-2. 알림장 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] notepad/list.html, notepad/write.html, notepad/detail.html
- [ ] 관련 CSS/JS

### Step 8: 공지사항 구현 (Day 13-14)

#### 8-1. Announcement 도메인

**파일 1**: `domain/announcement/entity/Announcement.java`
```java
@Entity
@Table(name = "announcement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE announcement SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Announcement extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kindergarten_id", nullable = false)
    private Kindergarten kindergarten;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private boolean isImportant = false;
    
    private LocalDateTime deletedAt;
    
    // 정적 팩토리 메서드
    public static Announcement create(Kindergarten kindergarten, Member writer, 
                                      String title, String content, boolean isImportant);
    
    // 비즈니스 메서드
    public void update(String title, String content, boolean isImportant);
    public void markAsImportant();
    public void unmarkAsImportant();
}
```

**파일 2**: `domain/announcement/repository/AnnouncementRepository.java`
```java
public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, AnnouncementRepositoryCustom {
    
    // 유치원별 공지사항 목록 (최신순)
    List<Announcement> findByKindergartenIdOrderByIsImportantDescCreatedAtDesc(Long kindergartenId);
    
    // 유치원별 중요 공지사항
    List<Announcement> findByKindergartenIdAndIsImportantTrueOrderByCreatedAtDesc(Long kindergartenId);
    
    // 유치원 + ID로 조회
    Optional<Announcement> findByIdAndKindergartenId(Long id, Long kindergartenId);
}
```

**파일 3**: `domain/announcement/repository/AnnouncementRepositoryCustom.java`
```java
public interface AnnouncementRepositoryCustom {
    
    // 페이징 + 검색
    Page<Announcement> searchAnnouncements(Long kindergartenId, String keyword, Pageable pageable);
}
```

**파일 4**: `domain/announcement/dto/AnnouncementRequest.java`
```java
@Getter
@NoArgsConstructor
public class AnnouncementRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    private boolean isImportant = false;
}
```

**파일 5**: `domain/announcement/dto/AnnouncementResponse.java`
```java
@Getter
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private boolean isImportant;
    private String writerName;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AnnouncementResponse from(Announcement announcement);
}
```

**파일 6**: `domain/announcement/dto/AnnouncementListResponse.java`
```java
@Getter
@Builder
public class AnnouncementListResponse {
    private Long id;
    private String title;
    private boolean isImportant;
    private String writerName;
    private LocalDateTime createdAt;
    
    public static AnnouncementListResponse from(Announcement announcement);
}
```

**파일 7**: `domain/announcement/service/AnnouncementService.java`
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final AnnouncementRepository announcementRepository;
    private final KindergartenRepository kindergartenRepository;
    private final MemberRepository memberRepository;
    
    // 공지사항 목록 조회
    public List<AnnouncementListResponse> findAll(Long kindergartenId);
    
    // 중요 공지사항만 조회
    public List<AnnouncementListResponse> findImportant(Long kindergartenId);
    
    // 공지사항 상세 조회
    public AnnouncementResponse findById(Long id);
    
    // 공지사항 작성
    @Transactional
    public Long create(Long kindergartenId, Long writerId, AnnouncementRequest request);
    
    // 공지사항 수정
    @Transactional
    public void update(Long id, Long writerId, AnnouncementRequest request);
    
    // 공지사항 삭제 (Soft Delete)
    @Transactional
    public void delete(Long id, Long writerId);
    
    // 페이징 검색
    public Page<AnnouncementListResponse> search(Long kindergartenId, String keyword, Pageable pageable);
}
```

**파일 8**: `domain/announcement/controller/AnnouncementApiController.java`
```java
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementApiController {
    
    private final AnnouncementService announcementService;
    
    // GET /api/v1/announcements?kindergartenId={id}
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnouncementListResponse>>> findAll(
            @RequestParam Long kindergartenId);
    
    // GET /api/v1/announcements/important?kindergartenId={id}
    @GetMapping("/important")
    public ResponseEntity<ApiResponse<List<AnnouncementListResponse>>> findImportant(
            @RequestParam Long kindergartenId);
    
    // GET /api/v1/announcements/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> findById(@PathVariable Long id);
    
    // POST /api/v1/announcements?kindergartenId={id}
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestParam Long kindergartenId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementRequest request);
    
    // PUT /api/v1/announcements/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AnnouncementRequest request);
    
    // DELETE /api/v1/announcements/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails);
}
```

**체크리스트**:
- [ ] Announcement 엔티티: Soft Delete 적용
- [ ] Announcement 엔티티: isImportant 필드
- [ ] Repository: 중요 공지 우선 정렬
- [ ] RepositoryCustom: 페이징 + 검색 QueryDSL 구현
- [ ] Service: CRUD + 중요 공지 조회
- [ ] Controller: 6개 엔드포인트

---

#### 8-2. 공지사항 화면
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] announcement/list.html, announcement/write.html, announcement/detail.html
- [ ] 관련 CSS/JS

### Step 9: 마무리 (Day 15-16)

#### 9-1. 대시보드 구현
> 🎨 **Cursor 담당** - 프론트엔드는 백엔드 완성 후 Cursor가 구현
- [ ] dashboard/index.html (역할별 분기)
- [ ] 원장: 전체 통계 (반 수, 원생 수, 오늘 출석률)
- [ ] 교사: 담당 반 정보, 오늘 출석, 미확인 알림장
- [ ] 학부모: 자녀 정보, 알림장, 출석 현황

---

#### 9-2. 테스트 코드 작성

**Service 단위 테스트 패턴**:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest(...);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any())).thenReturn(createMember());
        
        // when
        Long memberId = authService.signUp(request);
        
        // then
        assertThat(memberId).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_Fail_EmailExists() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);
        
        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
```

**Repository 테스트 패턴**:
```java
@DataJpaTest
@Import(QuerydslConfig.class)
class MemberRepositoryTest {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Test
    @DisplayName("이메일로 회원 조회")
    void findByEmail() {
        // given
        Member member = Member.create("test@test.com", "password", "테스트", "010-1234-5678", MemberRole.TEACHER);
        memberRepository.save(member);
        
        // when
        Optional<Member> found = memberRepository.findByEmail("test@test.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트");
    }
}
```

**통합 테스트 패턴**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("회원가입 -> 로그인 통합 테스트")
    void signUpAndLogin() throws Exception {
        // 1. 회원가입
        SignUpRequest signUpRequest = new SignUpRequest(...);
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
            .andExpect(status().isCreated());
        
        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest(...);
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
```

**체크리스트**:
- [ ] AuthServiceTest: signUp, login, refresh, logout 테스트
- [ ] MemberRepositoryTest: findByEmail, existsByEmail 테스트
- [ ] ClassroomServiceTest: CRUD 테스트
- [ ] AttendanceServiceTest: 일별 조회, 등록 테스트
- [ ] AuthIntegrationTest: 회원가입 → 로그인 통합 테스트

---

#### 9-3. Docker 환경 구성
> ✅ **이미 완료** (Step 1에서 진행)
- [x] docker/docker-compose.yml (MySQL + Redis)
- [ ] Dockerfile (애플리케이션 이미지 빌드용)

**Dockerfile 작성 예시**:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

#### 9-4. README.md 작성

**포함할 내용**:
```markdown
# 유치원 ERP

## 프로젝트 소개
- 유치원 통합 관리 시스템
- 원장, 교사, 학부모를 위한 서비스

## 기술 스택
- Backend: Spring Boot 3.x, JPA, QueryDSL
- Database: MySQL 8.0, Redis 7.x
- Frontend: Thymeleaf, Vanilla JS

## 실행 방법
1. Docker 환경 실행
2. 애플리케이션 실행
3. 접속: http://localhost:8080

## API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html

## 테스트 계정
| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| 원장 | principal@test.com | test1234 |
| 교사 | teacher@test.com | test1234 |
| 학부모 | parent@test.com | test1234 |
```

**체크리스트**:
- [ ] Dockerfile 작성
- [ ] README.md 작성
- [ ] (선택) Swagger 설정 추가

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

