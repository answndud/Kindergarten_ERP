# 🏫 유치원 ERP (Kindergarten ERP)

> 유치원 통합 관리 시스템 - 원장, 교사, 학부모를 위한 효율적인 유치원 운영 솔루션

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [성능 개선 스토리](#-성능-개선-스토리)
- [문서](#-문서)

---

## 🎯 프로젝트 소개

**유치원 ERP**는 유치원 운영의 효율성을 높이기 위한 통합 관리 시스템입니다.

### 프로젝트 철학

```
"Simple is Best"
- 핵심 기능에 집중
- 깔끔하고 직관적인 UI
- 확장 가능한 구조
```

### 타겟 사용자

| 역할 | 설명 | 주요 기능 |
|------|------|----------|
| 👔 원장 | 유치원 총괄 관리자 | 전체 관리, 통계, 승인 |
| 👩‍🏫 교사 | 반 담당 교사 | 출석, 알림장, 일정 |
| 👨‍👩‍👧 학부모 | 원생의 보호자 | 알림장 확인, 출결 확인 |

---

## ✨ 주요 기능

### 인증 시스템
- ✅ 회원가입 (이메일/비밀번호)
- ✅ 로그인 (JWT 기반)
- ✅ 소셜 로그인 (Google, Kakao OAuth2)
- ✅ 역할 기반 접근 제어

### 유치원/반/원생 관리
- ✅ 유치원 등록/수정
- ✅ 반 생성/수정/삭제
- ✅ 교사 배정
- ✅ 원생 등록/관리
- ✅ 학부모-원생 연결

### 출석 관리
- ✅ 일별 출석 체크
- ✅ 등/하원 시간 기록
- ✅ 결석 사유 입력
- ✅ 월별 출석 통계
- ✅ 반별 월간 리포트

### 알림장
- ✅ 알림장 작성 (교사)
- ✅ 알림장 확인 (학부모)
- ✅ 읽음 확인

### 공지사항
- ✅ 공지 작성/수정/삭제
- ✅ 중요 공지 설정
- ✅ 검색/인기 공지 조회

### 지원/승인 워크플로우
- ✅ 교사 유치원 지원
- ✅ 학부모 입학 신청
- ✅ 승인/거절 워크플로우

### 알림 시스템
- ✅ 알림 생성/조회
- ✅ 읽음 처리
- ✅ 드롭다운 UI

### 일정/캘린더
- ✅ 유치원/반 단위 일정 CRUD
- ✅ 오늘/다가오는 일정 조회

### 대시보드
- ✅ 출석/회원/공지 지표 조회 API
- ✅ 통계 캐시 기반 대시보드 화면

---

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 LTS | 메인 언어 |
| Spring Boot | 3.5.9 | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| JWT (jjwt) | 0.12.6 | 토큰 인증 |
| OAuth2 Client | - | Google/Kakao 로그인 |

### Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 토큰 저장 |
| Flyway | - | DB 마이그레이션 |

### Frontend
| 기술 | 버전 | 용도 |
|------|------|------|
| Thymeleaf | - | 템플릿 엔진 (SSR) |
| HTMX | 1.9.x | 동적 HTML 업데이트 |
| Alpine.js | 3.x | 가벼운 클라이언트 상태 관리 |
| Tailwind CSS | 3.4 | 유틸리티 퍼스트 CSS 프레임워크 |

### DevOps
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 개발 환경 |
| Gradle | 빌드 도구 |

---

## 📁 프로젝트 구조

```
erp/
├── src/
│   ├── main/
│   │   ├── java/com/erp/
│   │   │   ├── ErpApplication.java
│   │   │   ├── global/              # 전역 설정
│   │   │   │   ├── config/          # 설정 클래스
│   │   │   │   ├── exception/       # 예외 처리
│   │   │   │   ├── security/        # 보안 (JWT/OAuth2)
│   │   │   │   └── common/          # 공통 클래스
│   │   │   └── domain/              # 도메인별 패키지
│   │   │       ├── member/          # 회원
│   │   │       ├── auth/            # 인증
│   │   │       ├── kindergarten/    # 유치원
│   │   │       ├── classroom/       # 반
│   │   │       ├── kid/             # 원생
│   │   │       ├── attendance/      # 출석
│   │   │       ├── notepad/         # 알림장
│   │   │       ├── announcement/    # 공지사항
│   │   │       ├── notification/    # 알림
│   │   │       ├── calendar/        # 일정
│   │   │       ├── dashboard/       # 대시보드
│   │   │       ├── kidapplication/  # 원생 입학 신청
│   │   │       └── kindergartenapplication/ # 교사 지원
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/        # Flyway 마이그레이션
│   └── test/
├── docker/
│   └── docker-compose.yml           # MySQL + Redis
├── docs/                             # 프로젝트 문서
│   ├── project_idea.md              # 설계서
│   ├── project_diary.md             # 개발 일지
│   └── springboot_tutorial.md       # Spring Boot 튜토리얼
└── build.gradle
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Docker & Docker Compose
- Gradle 8.x

### 1. 저장소 클론

```bash
git clone https://github.com/{username}/kindergarten-erp.git
cd kindergarten-erp
```

### 2. Docker 환경 실행

```bash
# MySQL + Redis 컨테이너 시작
docker compose -f docker/docker-compose.yml up -d

# 상태 확인
docker ps
```

### 3. 애플리케이션 실행

```bash
# 빌드 및 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build -x test
java -jar build/libs/erp-0.0.1-SNAPSHOT.jar
```

### 4. 접속

- 애플리케이션: http://localhost:8080
- MySQL: localhost:3306 (erp_db / erp_user / erp1234)
- Redis: localhost:6379

### 5. 종료

```bash
# Docker 컨테이너 종료
docker compose -f docker/docker-compose.yml down

# 데이터 포함 완전 삭제
docker compose -f docker/docker-compose.yml down -v
```

---

## 📡 API 문서

### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| GET | `/api/v1/auth/me` | 현재 로그인 회원 조회 |

### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PATCH | `/api/v1/members/profile` | 프로필 수정 |
| PATCH | `/api/v1/members/password` | 비밀번호 변경 |
| DELETE | `/api/v1/members/withdraw` | 회원 탈퇴 |

### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens` | 유치원 목록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |
| DELETE | `/api/v1/kindergartens/{id}` | 유치원 삭제 |

### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 |
| PUT | `/api/v1/classrooms/{id}/teacher` | 담임 배정 |
| DELETE | `/api/v1/classrooms/{id}/teacher` | 담임 해제 |

### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 |
| POST | `/api/v1/kids` | 원생 등록 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |
| GET | `/api/v1/kids/my-kids` | 내 원생 목록(학부모) |

### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily` | 일별 출석 조회 |
| POST | `/api/v1/attendance/upsert` | 출석 등록/수정 |
| POST | `/api/v1/attendance/bulk` | 출석 일괄 반영 |
| GET | `/api/v1/attendance/kid/{kidId}/monthly` | 월간 출석 목록 |
| GET | `/api/v1/attendance/classroom/{classroomId}/monthly-report` | 반별 월간 리포트 |

### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |

### 공지사항 (Announcement)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/announcements` | 공지 목록 |
| POST | `/api/v1/announcements` | 공지 작성 |
| PUT | `/api/v1/announcements/{id}` | 공지 수정 |
| DELETE | `/api/v1/announcements/{id}` | 공지 삭제 |
| GET | `/api/v1/announcements/search` | 공지 제목 검색 |
| PATCH | `/api/v1/announcements/{id}/important` | 중요 공지 토글 |

### 지원/신청 (Application)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergarten-applications` | 교사 유치원 지원 |
| GET | `/api/v1/kindergarten-applications/my` | 내 교사 지원서 목록 |
| PUT | `/api/v1/kindergarten-applications/{id}/approve` | 교사 지원서 승인 |
| PUT | `/api/v1/kindergarten-applications/{id}/reject` | 교사 지원서 거절 |
| POST | `/api/v1/kid-applications` | 학부모 입학 신청 |
| GET | `/api/v1/kid-applications/my` | 내 입학 신청 목록 |
| PUT | `/api/v1/kid-applications/{id}/approve` | 입학 신청 승인 |
| PUT | `/api/v1/kid-applications/{id}/reject` | 입학 신청 거절 |

### 알림 (Notification)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notifications` | 알림 목록 조회 |
| GET | `/api/v1/notifications/unread-count` | 안 읽은 개수 조회 |
| PUT | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 |

### 일정 (Calendar)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/calendar/events` | 일정 목록 조회 |
| POST | `/api/v1/calendar/events` | 일정 등록 |
| PUT | `/api/v1/calendar/events/{id}` | 일정 수정 |
| DELETE | `/api/v1/calendar/events/{id}` | 일정 삭제 |

### 대시보드 (Dashboard)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/dashboard/statistics` | 대시보드 통계 조회 |

---

## 🚀 성능 개선 스토리

이 프로젝트는 "처음부터 빠른 시스템"보다
"기능 구현 후 병목을 발견하고, 수치로 검증하며 개선"하는 과정을 포트폴리오로 구성했습니다.

### 개선 요약

| 대상 | 개선 전 | 개선 후 | 핵심 개선 |
|------|--------:|--------:|----------|
| Notepad 목록 조회 | queries 22, 15ms | queries 4, 4ms | 읽음 수 N+1 제거, 다건 집계 쿼리 전환 |
| Dashboard 통계 | queries 13, 14ms | queries 10, 2ms | 집계 쿼리 통합, 불필요한 목록 로딩 제거 |
| Dashboard 반복 조회 | queries 10, 10ms | queries 0, 0ms | 60초 TTL 캐시 적용 (`dashboardStatistics`) |

### 실행계획(EXPLAIN) 개선

- Notepad/Announcement 목록: `ALL + filesort` -> `ref/index`
- Attendance 집계: `ALL` -> `range`

### 동시성 부하 테스트(k6)

- Notepad list (VU 10, 30초): avg 20.72ms, p95 45.32ms, error 0.00%
- Dashboard stats (VU 5, 30초): avg 12.46ms, p95 27.88ms, error 0.00%
- 전체 p95(`http_req_duration`): 294.44ms

### 상세 문서

- 성능 최적화 문서는 저장소의 `docs/performance-optimization` 폴더에서 번호 순서(`00`부터)로 확인하세요.

---

## 📚 문서

| 문서 | 설명 |
|------|------|
| 프로젝트 문서 | `docs/` 폴더 참고 |
| 성능 최적화 문서 | `docs/performance-optimization/` 폴더 참고 (번호 순서) |

---

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Made with ❤️ for Kindergartens**
