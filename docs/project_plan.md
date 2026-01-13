# 🚀 유치원 ERP 개발 계획서

> 전체 개발 프로세스와 순서를 정리한 문서입니다.
> 이 계획에 따라 단계별로 구현을 진행합니다.

---

## 📋 목차

1. [개발 원칙](#개발-원칙)
2. [개발 단계 개요](#개발-단계-개요)
3. [상세 개발 계획](#상세-개발-계획)
4. [개발 체크리스트](#개발-체크리스트)

---

## 개발 원칙

```
1. 백엔드 우선: API → 엔티티 → 리포지토리 → 서비스 → 컨트롤러 순서
2. 테스트 주도: 핵심 로직은 단위 테스트 작성
3. 점진적 구현: 각 Phase가 완료된 후 다음 Phase 진행
4. 문서화: 완료된 기능은 README.md의 API 문서에 반영
```

---

## 개발 단계 개요

| Phase | 내용 | 주요 작업 | 우선순위 |
|-------|------|----------|----------|
| **0** | 프로젝트 설정 | 기초 설정, 공통 컴포넌트 | 🔴 필수 |
| **1** | 인증 시스템 | 회원가입, 로그인, JWT | 🔴 필수 |
| **2** | 유치원 & 반 | 유치원/반 관리, 교사 배정 | 🔴 필수 |
| **3** | 원생 관리 | 원생 등록, 학부모 연결 | 🔴 필수 |
| **4** | 출석 관리 | 일별 출석, 월별 통계 | 🟡 중요 |
| **5** | 알림장 | 알림장 작성, 조회, 읽음처리 | 🟡 중요 |
| **6** | 공지사항 | 공지 작성, 중요 공지 | 🟢 선택 |

---

## 상세 개발 계획

## Phase 0: 프로젝트 기초 설정

### 목표
개발에 필요한 기초 설정과 공통 컴포넌트 구현

### 작업 목록

#### 0-1. 데이터베이스 설정
- [ ] Docker Compose 작성 (MySQL + Redis)
- [ ] Flyway 마이그레이션 V1__init_schema.sql 작성
  - member 테이블
  - kindergarten 테이블
  - classroom 테이블
  - kid 테이블
  - attendance 테이블
  - notepad 테이블
  - announcement 테이블

#### 0-2. 공통 컴포넌트 구현
- [ ] `global/common/BaseEntity.java` - 생성일/수정일Auditing
- [ ] `global/common/ApiResponse.java` - 공통 응답 DTO
- [ ] `global/exception/ErrorCode.java` - 에러 코드 enum
- [ ] `global/exception/BusinessException.java` - 커스텀 예외
- [ ] `global/exception/GlobalExceptionHandler.java` - 전역 예외 처리

#### 0-3. 설정 클래스
- [ ] `global/config/JpaConfig.java` - JPA Auditing 활성화
- [ ] `global/config/QuerydslConfig.java` - QueryDSL 빈 등록
- [ ] `global/config/RedisConfig.java` - Redis 설정

#### 0-4. 프론트엔드 기초
- [ ] `templates/layout/default.html` - 기본 레이아웃
  - HTMX CDN (`<script src="https://unpkg.com/htmx.org@1.9.10"></script>`)
  - Alpine.js CDN (`<script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>`)
  - Tailwind CSS CDN (`<script src="https://cdn.tailwindcss.com"></script>`)
- [ ] `static/js/app.js` - HTMX/Alpine 전역 설정
- [ ] `static/css/custom.css` - Tailwind 커스텀 설정

---

## Phase 1: 인증 시스템

### 목표
회원가입, 로그인, JWT 인증 구현

### 작업 순서

#### 1-1. Member 도메인 (Entity)
```
domain/member/
├── entity/
│   ├── Member.java              # 회원 엔티티
│   ├── MemberRole.java          # 역할 enum (PRINCIPAL, TEACHER, PARENT)
│   └── MemberStatus.java        # 상태 enum (ACTIVE, INACTIVE, PENDING)
```

**체크리스트:**
- [ ] Member 엔티티: id, email, password, name, phone, role, status, kindergarten
- [ ] BaseEntity 상속
- [ ] Kindergarten과 연관관계 (@ManyToOne)
- [ ] 정적 팩토리 메서드: `create()`
- [ ] 비즈니스 메서드: `updateProfile()`, `changePassword()`, `activate()`, `deactivate()`

#### 1-2. Member 도메인 (Repository)
```
domain/member/repository/
├── MemberRepository.java                    # 인터페이스
├── MemberRepositoryCustom.java              # 커스텀 인터페이스
└── MemberRepositoryImpl.java                # QueryDSL 구현
```

**체크리스트:**
- [ ] `findByEmail()` - 이메일로 회원 조회
- [ ] `existsByEmail()` - 이메일 중복 확인
- [ ] QueryDSL용 커스텀 쿼리 (필요시)

#### 1-3. Member 도메인 (DTO)
```
domain/member/dto/
├── request/
│   ├── SignUpRequest.java       # 회원가입 요청
│   ├── LoginRequest.java        # 로그인 요청
│   └── UpdateProfileRequest.java # 프로필 수정 요청
└── response/
    ├── MemberResponse.java      # 회원 정보 응답
    └── LoginResponse.java       # 로그인 응답 (JWT 포함)
```

#### 1-4. JWT 구현
```
global/security/jwt/
├── JwtProperties.java           # JWT 설정 properties
├── JwtTokenProvider.java        # 토큰 생성/검증
└── JwtAuthenticationFilter.java # JWT 필터
```

**체크리스트:**
- [ ] JWT 토큰 생성 (Access + Refresh)
- [ ] JWT 토큰 검증
- [ ] JWT 토큰 갱신
- [ ] 쿠키에 토큰 저장 (HttpOnly, Secure)

#### 1-5. Member 도메인 (Service)
```
domain/member/service/
└── MemberService.java
```

**체크리스트:**
- [ ] `signUp()` - 회원가입 (비밀번호 암호화)
- [ ] `login()` - 로그인 (JWT 발급)
- [ ] `getMember()` - 회원 정보 조회
- [ ] `updateProfile()` - 프로필 수정
- [ ] `changePassword()` - 비밀번호 변경
- [ ] `withdraw()` - 회원 탈퇴 (Soft delete)

#### 1-6. Auth 도메인 (Controller)
```
domain/auth/controller/
├── AuthApiController.java       # API 컨트롤러
└── AuthViewController.java      # 뷰 컨트롤러
```

**체크리스트:**
- [ ] `POST /api/v1/auth/signup` - 회원가입
- [ ] `POST /api/v1/auth/login` - 로그인
- [ ] `POST /api/v1/auth/logout` - 로그아웃
- [ ] `POST /api/v1/auth/refresh` - 토큰 갱신
- [ ] `GET /login` - 로그인 페이지
- [ ] `GET /signup` - 회원가입 페이지

#### 1-7. Security 설정
```
global/config/
└── SecurityConfig.java
```

**체크리스트:**
- [ ] SecurityFilterChain 설정
- [ ] JWT 필터 추가
- [ ] 접근 권한 설정 (permitAll, hasRole)
- [ ] CORS 설정
- [ ] CSRF 비활성화 (JWT 사용 시)

#### 1-8. 템플릿 구현
```
templates/auth/
├── login.html                   # 로그인 페이지 (HTMX + Alpine)
└── signup.html                  # 회원가입 페이지 (HTMX + Alpine)
```

**체크리스트:**
- [ ] 로그인 폼 (이메일, 비밀번호)
- [ ] 회원가입 폼 (이름, 이메일, 비밀번호, 전화번호, 역할 선택)
- [ ] HTMX로 폼 제출 (`hx-post`, `hx-swap`)
- [ ] Alpine으로 유효성 검사 (`x-data`, `x-model`)
- [ ] Tailwind로 스타일링

---

## Phase 2: 유치원 & 반 관리

### 목표
유치원 정보 등록, 반 생성/수정/삭제, 교사 배정

### 작업 순서

#### 2-1. Kindergarten 도메인
```
domain/kindergarten/
├── entity/
│   └── Kindergarten.java
├── repository/
│   └── KindergartenRepository.java
├── dto/
│   ├── request/
│   │   └── KindergartenRequest.java
│   └── response/
│       └── KindergartenResponse.java
├── service/
│   └── KindergartenService.java
└── controller/
    └── KindergartenController.java
```

**체크리스트:**
- [ ] Kindergarten 엔티티: id, name, address, phone, openTime, closeTime
- [ ] BaseEntity 상속
- [ ] Member와 1:N 연관관계
- [ ] CRUD 서비스 메서드
- [ ] API: POST, GET, PUT `/api/v1/kindergartens/{id}`

#### 2-2. Classroom 도메인
```
domain/classroom/
├── entity/
│   └── Classroom.java
├── repository/
│   └── ClassroomRepository.java
├── dto/
│   ├── request/
│   │   └── ClassroomRequest.java
│   └── response/
│       └── ClassroomResponse.java
├── service/
│   └── ClassroomService.java
└── controller/
    └── ClassroomController.java
```

**체크리스트:**
- [ ] Classroom 엔티티: id, name, ageGroup, kindergarten, teacher
- [ ] Kindergarten과 N:1, Member(teacher)와 N:1
- [ ] Soft delete (deletedAt)
- [ ] CRUD 서비스 메서드
- [ ] API: GET, POST, PUT, DELETE `/api/v1/classrooms/{id}`
- [ ] 교사 배정 기능

#### 2-3. 템플릿 구현
```
templates/
├── kindergarten/
│   ├── register.html            # 유치원 등록
│   └── detail.html              # 유치원 상세
└── classroom/
    ├── list.html                # 반 목록 (HTMX로 동적 로드)
    ├── form.html                # 반 생성/수정 폼
    └── detail.html              # 반 상세
```

---

## Phase 3: 원생 관리

### 목표
원생 등록, 학부모-원생 연결, 반 배정

### 작업 순서

#### 3-1. Kid 도메인
```
domain/kid/
├── entity/
│   └── Kid.java
├── repository/
│   └── KidRepository.java
├── dto/
│   ├── request/
│   │   └── KidRequest.java
│   └── response/
│       └── KidResponse.java
├── service/
│   └── KidService.java
└── controller/
    └── KidController.java
```

**체크리스트:**
- [ ] Kid 엔티티: id, name, birthDate, gender, classroom, admissionDate
- [ ] Classroom과 N:1 연관관계
- [ ] Member(부모)와 N:M 연관관계 (ParentKid 중간 테이블)
- [ ] CRUD 서비스 메서드
- [ ] API: GET, POST, PUT, DELETE `/api/v1/kids/{id}`
- [ ] 반 배정 기능
- [ ] 학부모 연결 기능

#### 3-2. ParentKid 연결 엔티티
```
domain/kid/entity/
└── ParentKid.java               # 중간 테이블 엔티티
```

**체크리스트:**
- [ ] ParentKid 엔티티: id, kid, parent, relationship
- [ ] Kid와 N:1, Member와 N:1

#### 3-3. 템플릿 구현
```
templates/kid/
├── list.html                    # 원생 목록 (필터링, 검색)
├── form.html                    # 원생 등록/수정 폼
├── detail.html                  # 원생 상세
└── assign_parent.html           # 학부모 연결
```

---

## Phase 4: 출석 관리

### 목표
일별 출석 체크, 등/하원 시간 기록, 월별 통계

### 작업 순서

#### 4-1. Attendance 도메인
```
domain/attendance/
├── entity/
│   ├── Attendance.java
│   └── AttendanceStatus.java    # enum (PRESENT, ABSENT, LATE, etc)
├── repository/
│   └── AttendanceRepository.java
├── dto/
│   ├── request/
│   │   └── AttendanceRequest.java
│   └── response/
│       ├── AttendanceResponse.java
│       └── MonthlyStatisticsResponse.java
├── service/
│   └── AttendanceService.java
└── controller/
    └── AttendanceController.java
```

**체크리스트:**
- [ ] Attendance 엔티티: id, kid, date, status, dropOffTime, pickUpTime, note
- [ ] Kid와 N:1 연관관계
- [ ] 일별 출석 조회 (날짜, 반별 필터)
- [ ] 출석 등록/수정 (등교, 하교 시간)
- [ ] 월별 통계 (원생별 출석일, 결석일)
- [ ] API: GET `/api/v1/attendance/daily`, POST `/api/v1/attendance`
- [ ] API: GET `/api/v1/attendance/monthly`

#### 4-2. 템플릿 구현
```
templates/attendance/
├── daily.html                   # 일별 출석부 (HTMX로 날짜 이동)
├── monthly.html                 # 월별 통계
└── record.html                  # 출석 기록 폼
```

---

## Phase 5: 알림장

### 목표
알림장 작성 (교사), 조회 (학부모), 읽음 확인

### 작업 순서

#### 5-1. Notepad 도메인
```
domain/notepad/
├── entity/
│   └── Notepad.java
├── repository/
│   └── NotepadRepository.java
├── dto/
│   ├── request/
│   │   └── NotepadRequest.java
│   └── response/
│       └── NotepadResponse.java
├── service/
│   └── NotepadService.java
└── controller/
    └── NotepadController.java
```

**체크리스트:**
- [ ] Notepad 엔티티: id, classroom, kid, writer, title, content, isRead, photos
- [ ] Classroom, Kid, Member와 연관관계
- [ ] 알림장 작성 (교사)
- [ ] 알림장 목록 조회 (학부모 - 내 원생만)
- [ ] 알림장 상세 조회
- [ ] 읽음 처리 (`isRead` 플래그)
- [ ] API: GET, POST, PUT, DELETE `/api/v1/notepads/{id}`

#### 5-2. 템플릿 구현
```
templates/notepad/
├── list.html                    # 알림장 목록 (HTMX 무한 스크롤)
├── write.html                   # 알림장 작성 (WYSIWYG 에디터)
├── detail.html                  # 알림장 상세
└── fragments/
    └── notepad_card.html        # 알림장 카드 조각
```

---

## Phase 6: 공지사항

### 목표
공지 작성/수정/삭제, 중요 공지 설정

### 작업 순서

#### 6-1. Announcement 도메인
```
domain/announcement/
├── entity/
│   └── Announcement.java
├── repository/
│   └── AnnouncementRepository.java
├── dto/
│   ├── request/
│   │   └── AnnouncementRequest.java
│   └── response/
│       └── AnnouncementResponse.java
├── service/
│   └── AnnouncementService.java
└── controller/
    └── AnnouncementController.java
```

**체크리스트:**
- [ ] Announcement 엔티티: id, kindergarten, writer, title, content, isImportant
- [ ] Soft delete (deletedAt)
- [ ] 공지 작성 (원장, 교사)
- [ ] 공지 목록 조회 (중요 공지 상단)
- [ ] 공지 상세 조회
- [ ] API: GET, POST, PUT, DELETE `/api/v1/announcements/{id}`

#### 6-2. 템플릿 구현
```
templates/announcement/
├── list.html                    # 공지 목록
├── write.html                   # 공지 작성
└── detail.html                  # 공지 상세
```

---

## 개발 체크리스트

### 각 Phase 완료 기준

#### Phase 0 완료 기준
- [ ] Docker로 MySQL, Redis 실행 가능
- [ ] Flyway 마이그레이션 성공
- [ ] 애플리케이션 실행 시 에러 없음
- [ ] 기본 레이아웃 표시 정상

#### Phase 1 완료 기준
- [ ] 회원가입 가능 (이메일 중복 검증)
- [ ] 로그인 가능 (JWT 발급 및 쿠키 저장)
- [ ] JWT로 인증된 요청 가능
- [ ] 로그아웃 가능 (쿠키 삭제)
- [ ] 역할별 접근 제어 동작

#### Phase 2 완료 기준
- [ ] 유치원 등록 가능
- [ ] 반 생성/수정/삭제 가능
- [ ] 교사를 반에 배정 가능
- [ ] 본인 유치원 정보만 조회 가능

#### Phase 3 완료 기준
- [ ] 원생 등록 가능
- [ ] 원생을 반에 배정 가능
- [ ] 학부모를 원생에 연결 가능
- [ ] 학부모가 본인 원생만 조회 가능

#### Phase 4 완료 기준
- [ ] 일별 출석 조회 가능
- [ ] 출석 등록/수정 가능 (등/하원 시간)
- [ ] 월별 통계 조회 가능
- [ ] HTMX로 날짜 이동 시 새로고침 없이 동작

#### Phase 5 완료 기준
- [ ] 교사가 알림장 작성 가능
- [ ] 학부모가 본인 원생 알림장만 조회 가능
- [ ] 알림장 읽음 처리 가능
- [ ] 사진 첨부 가능

#### Phase 6 완료 기준
- [ ] 공지 작성/수정/삭제 가능
- [ ] 중요 공지 상단 표시
- [ ] 전체 공지 vs 반별 공지 구분

---

## 개발 팁

### 백엔드 개발 순서 (각 도메인)
```
1. Entity 작성 (필드, 연관관계)
2. Repository 인터페이스 작성
3. DTO 작성 (Request, Response)
4. Service 작성 (비즈니스 로직)
5. Controller 작성 (API 엔드포인트)
6. 단위 테스트 작성 (Service)
7. 통합 테스트 작성 (Controller)
```

### 프론트엔드 개발 순서 (각 화면)
```
1. 와이어프레임 작성 (HTML 구조)
2. Tailwind 클래스 적용 (스타일링)
3. Alpine.js로 상태 관리 (필요한 경우)
4. HTMX 속성 추가 (동적 업데이트)
5. Thymeleaf 변수 적용 (서버 데이터 바인딩)
```

### HTMX + Thymeleaf 패턴
```html
<!-- 서버에서 HTML 조각 반환 -->
<div id="item-list"
     hx-get="/api/v1/items"
     hx-trigger="load, searchChanged every 500ms"
     hx-swap="innerHTML">
  <!-- Thymeleaf 루프로 아이템 렌더링 -->
  <div th:each="item : ${items}" th:fragment="item-row">
    <span th:text="${item.name}"></span>
  </div>
</div>
```

### Alpine.js로 모달/토글
```html
<div x-data="{ open: false }">
  <button @click="open = true">열기</button>
  <div x-show="open" @click.away="open = false">
    모달 내용
  </div>
</div>
```

---

## 다음 단계

Phase 0부터 순서대로 진행합니다. 각 Phase가 완료되면 다음 Phase로 넘어가며, 완료된 기능은 README.md의 API 문서에 반영합니다.
