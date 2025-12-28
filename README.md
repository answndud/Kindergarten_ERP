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

### Phase 1: 인증 시스템
- ✅ 회원가입 (이메일/비밀번호)
- ✅ 로그인 (JWT 기반)
- ✅ 역할 기반 접근 제어

### Phase 2: 유치원 & 반 관리
- ✅ 유치원 등록/수정
- ✅ 반 생성/수정/삭제
- ✅ 교사 배정
- ✅ 원생 등록/관리

### Phase 3: 출석 관리
- ✅ 일별 출석 체크
- ✅ 등/하원 시간 기록
- ✅ 결석 사유 입력
- ✅ 월별 출석 통계

### Phase 4: 알림장
- ✅ 알림장 작성 (교사)
- ✅ 알림장 확인 (학부모)
- ✅ 읽음 확인

### Phase 5: 공지사항
- ✅ 공지 작성/수정/삭제
- ✅ 중요 공지 설정

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

### Database
| 기술 | 버전 | 용도 |
|------|------|------|
| MySQL | 8.0 | 메인 RDB |
| Redis | 7.x | 캐시, 토큰 저장 |
| Flyway | - | DB 마이그레이션 |

### Frontend
| 기술 | 용도 |
|------|------|
| Thymeleaf | 템플릿 엔진 |
| Vanilla JavaScript | 인터랙션 |
| CSS3 | 스타일링 |

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
│   │   │   │   ├── security/        # 보안 (JWT)
│   │   │   │   └── common/          # 공통 클래스
│   │   │   └── domain/              # 도메인별 패키지
│   │   │       ├── member/          # 회원
│   │   │       ├── auth/            # 인증
│   │   │       ├── kindergarten/    # 유치원
│   │   │       ├── classroom/       # 반
│   │   │       ├── kid/             # 원생
│   │   │       ├── attendance/      # 출석
│   │   │       ├── notepad/         # 알림장
│   │   │       └── announcement/    # 공지사항
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
cd docker
docker-compose up -d

# 상태 확인
docker ps
```

### 3. 애플리케이션 실행

```bash
# 프로젝트 루트로 이동
cd ..

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
cd docker
docker-compose down

# 데이터 포함 완전 삭제
docker-compose down -v
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

### 유치원 (Kindergarten)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/kindergartens` | 유치원 등록 |
| GET | `/api/v1/kindergartens/{id}` | 유치원 조회 |
| PUT | `/api/v1/kindergartens/{id}` | 유치원 수정 |

### 반 (Classroom)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/classrooms` | 반 목록 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 |

### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 |
| POST | `/api/v1/kids` | 원생 등록 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |

### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/attendance/daily` | 일별 출석 조회 |
| POST | `/api/v1/attendance` | 출석 등록/수정 |
| GET | `/api/v1/attendance/monthly` | 월별 통계 |

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

---

## 📚 문서

| 문서 | 설명 |
|------|------|
| [project_idea.md](docs/project_idea.md) | 프로젝트 설계서 (상세 구현 가이드) |
| [project_diary.md](docs/project_diary.md) | 개발 일지 (면접 대비) |
| [project_summary.md](docs/project_summary.md) | 기존 프로젝트 분석 |
| [springboot_tutorial.md](docs/springboot_tutorial.md) | Spring Boot 튜토리얼 |

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

