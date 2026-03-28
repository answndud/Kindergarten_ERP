# 🏫 유치원 ERP (Kindergarten ERP)

> 유치원 통합 관리 시스템 - 원장, 교사, 학부모를 위한 효율적인 유치원 운영 솔루션

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)
[![Backend CI](https://github.com/answndud/Kindergarten_ERP/actions/workflows/ci.yml/badge.svg)](https://github.com/answndud/Kindergarten_ERP/actions/workflows/ci.yml)

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

이 저장소는 기능 구현 자체보다 아래의 백엔드 역량을 포트폴리오로 증명하는 데 초점을 맞췄습니다.

- 멀티테넌시 권한 경계 하드닝
- Redis 기반 JWT refresh session 분리와 rotation
- Redis 세션 레지스트리 기반 활성 세션 조회/강제 종료
- MySQL/Redis Testcontainers 기반 통합 테스트
- JUnit `@Tag` 기반 Gradle test task와 `fast / integration / performance smoke` CI 분리
- Swagger/OpenAPI 기반 API 계약 문서
- Actuator health/readiness, `criticalDependencies`, Prometheus, correlation id, request structured logging
- DB 기반 인증 감사 로그, `kindergarten_id` 비정규화 tenant 필터, 원장 전용 조회/export API, 감사 로그 운영 화면
- 반 정원(capacity), 입학 waitlist/offer/offer expiry, 학부모 출결 요청 승인처럼 상태 전이가 있는 운영형 워크플로우
- 별도 `domain_audit_log` 기반 업무 감사 로그와 원장 전용 조회 화면
- 반복 로그인 실패 감지, principal in-app alert, `notification_outbox` 기반 incident webhook 전달
- 감사 로그 archive/purge retention scheduler
- Grafana 대시보드까지 포함한 로컬 monitoring overlay
- 정확도와 쿼리 수를 함께 관리한 대시보드 지표 개선
- `demo` 프로파일 기반 시연용 seed/bootstrap

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
- ✅ 세션 단위 Refresh Token 저장 및 Rotation
- ✅ 활성 세션 목록 조회, 개별 세션 종료, 다른 기기 일괄 로그아웃
- ✅ Redis 기반 로그인/토큰 갱신 Rate Limit
- ✅ DB 기반 로그인/refresh/소셜 연결 감사 로그
- ✅ `kindergarten_id` 기반 tenant 감사 로그 필터링
- ✅ 원장 전용 인증 감사 로그 조회 API
- ✅ 원장 전용 인증 감사 로그 CSV export
- ✅ 원장 전용 인증 감사 로그 화면
- ✅ 반복 로그인 실패 감지 및 원장 시스템 알림
- ✅ `notification_outbox` 기반 외부 채널 비동기 전달
- ✅ retry/backoff/dead-letter 기반 알림 전달 상태 관리
- ✅ 인증 이상 징후 incident webhook fan-out
- ✅ 감사 로그 archive/purge retention scheduler
- ✅ 소셜 로그인 (Google, Kakao OAuth2)
- ✅ 소셜 계정 자동 연결 금지 및 충돌 안내
- ✅ 설정 화면 기반 명시적 소셜 계정 연결
- ✅ 다중 소셜 provider 연결 (Google + Kakao)
- ✅ 소셜 전용 계정의 로컬 비밀번호 설정
- ✅ 마지막 로그인 수단을 보호하는 소셜 연결 해제
- ✅ 같은 provider 소셜 계정 교체 금지 및 동일 계정 재연결만 허용
- ✅ 역할 기반 접근 제어

### 유치원/반/원생 관리
- ✅ 유치원 등록/수정
- ✅ 반 생성/수정/삭제
- ✅ 반 정원(capacity) 설정 및 축소 검증
- ✅ 교사 배정
- ✅ 원생 등록/관리
- ✅ 학부모-원생 연결

### 출석 관리
- ✅ 일별 출석 체크
- ✅ 등/하원 시간 기록
- ✅ 결석 사유 입력
- ✅ 월별 출석 통계
- ✅ 반별 월간 리포트
- ✅ 학부모 출결 변경 요청 생성/취소
- ✅ 교사/원장 출결 변경 요청 승인/거절

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
- ✅ 반 정원 기반 waitlist 처리
- ✅ 입학 offer 발송 / 학부모 수락 / 만료 배치

### 인증/보안
- ✅ 세션 단위 refresh token rotation
- ✅ Redis 기반 auth rate limit
- ✅ trusted proxy 기준 client IP 해석
- ✅ access token을 Redis 세션 레지스트리에 묶은 즉시 revoke
- ✅ 로그인 실패 전용 rate limit 정책
- ✅ DB 기반 auth/social audit trail
- ✅ Swagger UI / OpenAPI JSON 계약 문서
- ✅ Actuator health/info/prometheus 및 liveness/readiness probe
- ✅ `criticalDependencies` 기반 DB/Redis readiness probe
- ✅ Prometheus scrape endpoint와 auth event counter
- ✅ Grafana provisioning 기반 운영 대시보드
- ✅ audit console list/export 성능 smoke 테스트
- ✅ prod profile에서 management port 분리 및 Swagger 비공개화
- ✅ correlation id 응답 헤더 및 request structured logging
- ✅ OAuth2 principal 런타임 안전성 보강
- ✅ OAuth2 이메일 충돌 시 임시 세션 정리 및 명시적 안내
- ✅ 세션 기반 OAuth2 link intent와 단일 provider 연결 정책
- ✅ 소셜 계정의 로컬 로그인 전환(password bootstrap)
- ✅ 계정 잠금 방지를 반영한 소셜 연결 해제 정책

### 업무 감사/운영 워크플로우
- ✅ 입학 waitlist/offer/offer expiry 업무 감사 로그
- ✅ 출결 변경 요청 제출/승인/거절/취소 업무 감사 로그
- ✅ 공지 수정/삭제 및 승인 워크플로우 상태 전이 로그
- ✅ 원장 전용 업무 감사 로그 조회 API
- ✅ 원장 전용 업무 감사 로그 화면 및 CSV export

### 알림 시스템
- ✅ 알림 생성/조회
- ✅ 읽음 처리
- ✅ 드롭다운 UI

### 일정/캘린더
- ✅ 유치원/반/개인 일정 CRUD
- ✅ 반복 일정 occurrence 조회
- ✅ 오늘/다가오는 일정 조회

### 대시보드
- ✅ 출석/회원/공지 지표 조회 API
- ✅ 입소일/주말을 반영한 출석률 계산
- ✅ 공지 고유 열람률 집계
- ✅ 통계 캐시 기반 대시보드 화면

---

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 LTS | 메인 언어 |
| Spring Boot | 3.5.9 | 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| Spring Security | - | 인증/인가 |
| Spring Boot Actuator | - | health/info, 운영 관측성 |
| Springdoc OpenAPI | 2.8.16 | Swagger UI, API 계약 문서 |
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
| GitHub Actions | CI 자동 검증 |
| Prometheus | 메트릭 스크래핑 |
| Grafana | 운영 대시보드 |
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
│   │   │       ├── domainaudit/     # 업무 감사 로그
│   │   │       └── kindergartenapplication/ # 교사 지원
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/        # Flyway 마이그레이션
│   └── test/
├── docker/
│   ├── docker-compose.yml           # MySQL + Redis
│   ├── docker-compose.monitoring.yml # Prometheus + Grafana
│   └── monitoring/                  # Prometheus/Grafana provisioning
├── docs/                             # 문서 인덱스
│   ├── README.md                    # 문서 시작점
│   ├── guides/                      # 개발/사용 가이드
│   ├── portfolio/                   # 인터뷰/성능 스토리
│   ├── decisions/                   # 기능/보안/운영 결정 로그
│   └── archive/                     # 레거시/회고/초기 설계 보관
└── build.gradle
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose
- Gradle 8.x
- 실행 전 [환경 변수 계약](./docs/guides/env-contract.md)을 먼저 확인하세요.

### 1. 저장소 클론

```bash
git clone https://github.com/{username}/kindergarten-erp.git
cd kindergarten-erp
```

### 2. Docker 환경 실행

```bash
# 최초 1회: local infra 예제 파일 준비
cp docker/.env.example docker/.env

# MySQL + Redis 컨테이너 시작
cp docker/.env.example docker/.env
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d

# 상태 확인
docker ps
```

### 3. 애플리케이션 실행

```bash
# local 개발 실행
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

- `local`은 Swagger/OpenAPI와 app-port Prometheus를 개발 편의용으로 명시적으로 엽니다.
- 시드 데이터는 기본으로 올라오지 않습니다. 필요하면 아래처럼 켭니다.

```bash
SPRING_PROFILES_ACTIVE=local APP_SEED_ENABLED=true ./gradlew bootRun
```

### 3-1. 데모 프로파일 실행

```bash
export SPRING_PROFILES_ACTIVE=demo
./gradlew bootRun
```

- `demo` 프로파일은 `local` 설정을 포함하고, 시연용 seed data를 함께 올립니다.
- 주요 계정 예시
  - 원장: `principal@test.com / test1234!`
  - 교사: `teacher1@test.com / test1234!`
  - 학부모: `parent1@test.com / test1234!`
- 시연 직후 바로 확인할 경로
  - 이 경로들은 `local`/`demo`처럼 명시적으로 노출한 환경에서만 보입니다.
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - 출결 요청 화면: `http://localhost:8080/attendance-requests`
  - 인증 감사 로그 화면: `http://localhost:8080/audit-logs`
  - 업무 감사 로그 화면: `http://localhost:8080/domain-audit-logs`
  - Prometheus scrape: `http://localhost:8080/actuator/prometheus`

### 3-2. 운영 profile management plane

- `prod`에서는 Swagger/OpenAPI를 비활성화하고, management endpoint를 별도 포트로 분리합니다.
- `prod` 실행 전에는 [환경 변수 계약](./docs/guides/env-contract.md)의 필수 항목을 모두 채워야 합니다.
- 기본값
  - 앱 포트: `8080`
  - management 포트: `9091`
  - management bind address: `127.0.0.1`
- 필요하면 `MANAGEMENT_SERVER_PORT`, `MANAGEMENT_SERVER_ADDRESS` 환경변수로 조정할 수 있습니다.

### 3-3. Monitoring Overlay 실행

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml -f docker/docker-compose.monitoring.yml up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Grafana 계정은 `docker/.env`의 `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD`를 사용합니다.
- Grafana는 provisioning으로 `Kindergarten ERP Observability` 대시보드를 자동 로드합니다.
- 로컬 compose 포트는 기본적으로 `127.0.0.1`에만 바인딩됩니다. 외부 공개가 필요하면 `docker/.env`의 bind/port 값을 명시적으로 바꾸세요.
- 애플리케이션은 host에서 `local` 또는 `demo` 프로파일로 `http://localhost:8080`에서 실행 중이라고 가정합니다.

### 3-4. 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 빠른 단위/서비스 테스트
./gradlew fastTest

# Testcontainers 기반 통합 테스트
./gradlew integrationTest

# 운영 성능 smoke 테스트
./gradlew performanceSmokeTest
```

- 통합 테스트는 MySQL/Redis Testcontainers 기반으로 실행됩니다.
- 로컬 테스트 실행에는 Docker Desktop 또는 Docker Engine이 필요합니다.
- CI는 `fastTest`, `package-smoke`, `integrationTest`, `performanceSmokeTest`를 분리해 실행합니다.
- `package-smoke`는 `bootJar` 생성, JAR 구조 확인, compose config 해석까지 검증하고 산출물을 artifact로 업로드합니다.
- CI workflow action은 Node24 네이티브 major(`checkout@v5`, `setup-java@v5`, `setup-gradle@v5`, `upload-artifact@v6`)로 유지합니다.

### 4. 접속

- 애플리케이션: http://localhost:8080
- MySQL: localhost:3306 (`docker/.env` 기준, 기본 localhost bind)
- Redis: localhost:6379 (`docker/.env` 기준, 기본 localhost bind)
- Prometheus: http://localhost:9090 (`docker/.env` 기준, 기본 localhost bind)
- Grafana: http://localhost:3000 (`docker/.env` 기준, 기본 localhost bind)

### 5. 종료

```bash
# Docker 컨테이너 종료
docker compose --env-file docker/.env -f docker/docker-compose.yml down

# 데이터 포함 완전 삭제
docker compose --env-file docker/.env -f docker/docker-compose.yml down -v

# monitoring overlay까지 함께 종료
docker compose --env-file docker/.env -f docker/docker-compose.yml -f docker/docker-compose.monitoring.yml down -v
```

---

## 📡 API 문서

실행 중인 애플리케이션에서는 아래 live contract를 바로 확인할 수 있습니다.
단, 이 경로는 `local`/`demo`처럼 명시적으로 연 환경에서만 사용할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 쿠키 기반 인증을 쓰기 때문에 같은 origin에서 로그인 후 Swagger `Try it out`으로 보호 API를 바로 호출할 수 있습니다.
- 단, `prod` profile에서는 Swagger/OpenAPI를 비활성화해 운영 노출면을 줄입니다.

아래 표는 주요 엔드포인트를 빠르게 훑기 위한 요약입니다.

### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 세션 기반 토큰 rotation |
| GET | `/api/v1/auth/me` | 현재 로그인 회원 조회 |
| GET | `/api/v1/auth/sessions` | 활성 세션 목록 조회 |
| DELETE | `/api/v1/auth/sessions/{sessionId}` | 특정 세션 종료 |
| DELETE | `/api/v1/auth/sessions/others` | 다른 기기 세션 일괄 종료 |
| GET | `/api/v1/auth/audit-logs/export` | 원장용 감사 로그 CSV export |

### 회원 (Member)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| GET | `/api/v1/members/parents` | 학부모 목록 조회 |
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
| GET | `/api/v1/classrooms/{id}` | 반 단건 조회 |
| POST | `/api/v1/classrooms` | 반 생성 |
| PUT | `/api/v1/classrooms/{id}` | 반 수정 |
| DELETE | `/api/v1/classrooms/{id}` | 반 삭제 |
| PUT | `/api/v1/classrooms/{id}/teacher` | 담임 배정 |
| DELETE | `/api/v1/classrooms/{id}/teacher` | 담임 해제 |

### 원생 (Kid)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/kids` | 원생 목록 |
| GET | `/api/v1/kids/{id}` | 원생 상세 |
| GET | `/api/v1/kids/page` | 원생 목록(페이지) |
| GET | `/api/v1/kids/classroom-counts` | 반별 원생 수 조회 |
| POST | `/api/v1/kids` | 원생 등록 |
| PUT | `/api/v1/kids/{id}` | 원생 수정 |
| PUT | `/api/v1/kids/{id}/classroom` | 반 배정 변경 |
| POST | `/api/v1/kids/{id}/parents` | 학부모 연결 |
| DELETE | `/api/v1/kids/{id}/parents/{parentId}` | 학부모 연결 해제 |
| DELETE | `/api/v1/kids/{id}` | 원생 삭제 |
| GET | `/api/v1/kids/my-kids` | 내 원생 목록(학부모) |

### 출석 (Attendance)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/attendance` | 출석 생성 |
| GET | `/api/v1/attendance/daily` | 일별 출석 조회 |
| POST | `/api/v1/attendance/upsert` | 출석 등록/수정 |
| POST | `/api/v1/attendance/bulk` | 출석 일괄 반영 |
| GET | `/api/v1/attendance/{id}` | 출석 단건 조회 |
| GET | `/api/v1/attendance/kid/{kidId}` | 원생 날짜별 조회 |
| GET | `/api/v1/attendance/kid/{kidId}/monthly` | 월간 출석 목록 |
| GET | `/api/v1/attendance/kid/{kidId}/statistics` | 월간 출석 통계 |
| GET | `/api/v1/attendance/classroom/{classroomId}/monthly-report` | 반별 월간 리포트 |
| POST | `/api/v1/attendance-requests` | 학부모 출결 변경 요청 생성 |
| GET | `/api/v1/attendance-requests/my` | 내 출결 변경 요청 목록 |
| GET | `/api/v1/attendance-requests/pending` | 승인 대기 출결 요청 목록 |
| GET | `/api/v1/attendance-requests/{id}` | 출결 변경 요청 상세 |
| POST | `/api/v1/attendance-requests/{id}/approve` | 출결 변경 요청 승인 |
| POST | `/api/v1/attendance-requests/{id}/reject` | 출결 변경 요청 거절 |
| POST | `/api/v1/attendance-requests/{id}/cancel` | 출결 변경 요청 취소 |
| PUT | `/api/v1/attendance/{id}` | 출석 수정 |
| POST | `/api/v1/attendance/kid/{kidId}/drop-off` | 등원 처리 |
| POST | `/api/v1/attendance/kid/{kidId}/pick-up` | 하원 처리 |
| POST | `/api/v1/attendance/kid/{kidId}/absent` | 결석 처리 |
| POST | `/api/v1/attendance/kid/{kidId}/late` | 지각 처리 |
| POST | `/api/v1/attendance/kid/{kidId}/early-leave` | 조퇴 처리 |
| POST | `/api/v1/attendance/kid/{kidId}/sick-leave` | 병결 처리 |
| DELETE | `/api/v1/attendance/{id}` | 출석 삭제 |

### 알림장 (Notepad)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notepads` | 알림장 목록 |
| GET | `/api/v1/notepads/{id}` | 알림장 상세 |
| GET | `/api/v1/notepads/classroom/{classroomId}` | 반별 알림장 목록 |
| GET | `/api/v1/notepads/kid/{kidId}` | 원생별 알림장 목록 |
| GET | `/api/v1/notepads/parent` | 학부모용 알림장 목록 |
| POST | `/api/v1/notepads` | 알림장 작성 |
| PUT | `/api/v1/notepads/{id}` | 알림장 수정 |
| DELETE | `/api/v1/notepads/{id}` | 알림장 삭제 |
| POST | `/api/v1/notepads/{id}/read` | 알림장 읽음 처리 |

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
| GET | `/api/v1/kindergarten-applications/pending` | 유치원 대기 지원서 목록 |
| GET | `/api/v1/kindergarten-applications/{id}` | 교사 지원서 상세 |
| PUT | `/api/v1/kindergarten-applications/{id}/approve` | 교사 지원서 승인 |
| PUT | `/api/v1/kindergarten-applications/{id}/reject` | 교사 지원서 거절 |
| PUT | `/api/v1/kindergarten-applications/{id}/cancel` | 교사 지원서 취소 |
| POST | `/api/v1/kid-applications` | 학부모 입학 신청 |
| GET | `/api/v1/kid-applications/my` | 내 입학 신청 목록 |
| GET | `/api/v1/kid-applications/pending` | 유치원 대기 입학 신청 목록 |
| GET | `/api/v1/kid-applications/queue` | review queue 입학 신청 목록 |
| GET | `/api/v1/kid-applications/{id}` | 입학 신청 상세 |
| PUT | `/api/v1/kid-applications/{id}/approve` | 입학 신청 승인 |
| PUT | `/api/v1/kid-applications/{id}/waitlist` | 입학 신청 waitlist 전환 |
| PUT | `/api/v1/kid-applications/{id}/offer` | 입학 offer 발행 |
| PUT | `/api/v1/kid-applications/{id}/accept-offer` | 학부모 입학 offer 수락 |
| PUT | `/api/v1/kid-applications/{id}/reject` | 입학 신청 거절 |
| PUT | `/api/v1/kid-applications/{id}/cancel` | 입학 신청 취소 |

### 업무 감사 로그 (Domain Audit)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/domain-audit-logs` | 원장용 업무 감사 로그 조회 |
| GET | `/api/v1/domain-audit-logs/export` | 원장용 업무 감사 로그 CSV export |

### 알림 (Notification)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notifications` | 알림 목록 조회 |
| GET | `/api/v1/notifications/{id}` | 알림 상세 조회 |
| GET | `/api/v1/notifications/unread` | 미읽음 알림 목록 |
| GET | `/api/v1/notifications/unread-count` | 안 읽은 개수 조회 |
| PUT | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 |
| PUT | `/api/v1/notifications/read-all` | 알림 전체 읽음 처리 |
| DELETE | `/api/v1/notifications/{id}` | 알림 삭제 |

### 일정 (Calendar)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/calendar/events` | 일정 목록 조회 |
| GET | `/api/v1/calendar/events/{id}` | 일정 상세 조회 |
| GET | `/api/v1/calendar/events/today` | 오늘 일정 조회 |
| GET | `/api/v1/calendar/events/upcoming` | 다가오는 일정 조회 |
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
| Dashboard 통계 | queries 13, 30ms | queries 5, 9ms | 정확도 보정 + 집계 쿼리 통합 |
| Dashboard 반복 조회 | queries 5, 12ms | queries 0, 0ms | 60초 TTL 캐시 적용 (`dashboardStatistics`) |

### 실행계획(EXPLAIN) 개선

- Notepad/Announcement 목록: `ALL + filesort` -> `ref/index`
- Attendance 집계: `ALL` -> `range`

### 동시성 부하 테스트(k6)

- Notepad list (VU 10, 30초): avg 20.72ms, p95 45.32ms, error 0.00%
- Dashboard stats (VU 5, 30초): avg 12.46ms, p95 27.88ms, error 0.00%
- 전체 p95(`http_req_duration`): 294.44ms

### 상세 문서

- 성능 최적화 문서는 저장소의 `docs/portfolio/performance` 폴더에서 번호 순서(`00`부터)로 확인하세요.

---

## ✅ 테스트 & CI

- 로컬 전체 검증은 `./gradlew test`로 유지합니다.
- 통합 테스트는 H2 mock 환경이 아니라 MySQL/Redis Testcontainers를 사용합니다.
- GitHub Actions는 `fastTest`, `package-smoke`, `integrationTest`, `performanceSmokeTest`를 별도 job으로 실행합니다.
- `package-smoke`는 bootJar 생성과 compose config 해석을 함께 검증합니다.
- Swagger/OpenAPI와 Prometheus scrape도 통합 테스트로 공개 경로를 회귀 검증합니다.
- 초기 CI 실패 원인이었던 `gradle-wrapper.jar` 추적 누락도 복구했습니다.
- Node 20 deprecation annotation 대응을 위해 workflow action을 Node24 네이티브 major로 올렸습니다.
- 실패 시 `fastTest`/`integrationTest` 리포트를 각각 artifact로 업로드하도록 구성했습니다.
- 패키징 단계는 실행 가능한 bootJar를 artifact로 업로드해 배포 단위를 함께 검증합니다.

---

## 📚 문서

### 인터뷰 바로 보기

| 문서 | 설명 |
|------|------|
| 문서 인덱스 | `docs/README.md` |
| Hiring Pack | `docs/portfolio/hiring-pack/backend-hiring-pack.md` |
| 시스템 아키텍처 | `docs/portfolio/architecture/system-architecture.md` |
| 데모 Preflight | `docs/portfolio/demo/demo-preflight.md` |
| 데모 Runbook | `docs/portfolio/demo/demo-runbook.md` |
| Auth Incident Response Case Study | `docs/portfolio/case-studies/auth-incident-response.md` |
| 인터뷰 1장 요약 | `docs/portfolio/interview/interview_one_pager.md` |
| 면접 예상 질문/답변 스크립트 | `docs/portfolio/interview/interview_qa_script.md` |

### 상세 결정 로그

| 문서 | 설명 |
|------|------|
| 프로젝트 문서 인덱스 | `docs/README.md` |
| 성능 최적화 문서 | `docs/portfolio/performance/` 폴더 참고 (번호 순서) |
| 권한 경계 하드닝 | `docs/decisions/phase14_multitenant_access_hardening.md` |
| Testcontainers 테스트 전환 | `docs/decisions/phase15_testcontainers_integration_test_stack.md` |
| GitHub Actions CI 자동화 | `docs/decisions/phase16_github_actions_ci.md` |
| JWT 세션 회전 설계 | `docs/decisions/phase17_jwt_refresh_session_rotation.md` |
| 대시보드 지표 보정 | `docs/decisions/phase18_dashboard_metric_redefinition.md` |
| CI 복구 및 job 분리 | `docs/decisions/phase19_ci_fast_integration_split.md` |
| GitHub Actions Node24 호환 | `docs/decisions/phase20_github_actions_node24_compatibility.md` |
| 인증 Rate Limit 하드닝 | `docs/decisions/phase21_auth_rate_limit.md` |
| GitHub Actions Node24 네이티브 전환 | `docs/decisions/phase22_github_actions_node24_native_actions.md` |
| 캘린더 반복 일정/권한 정합성 보강 | `docs/decisions/phase23_calendar_recurrence_access_alignment.md` |
| 인증 Client IP 신뢰 모델 하드닝 | `docs/decisions/phase24_auth_client_ip_trust_model.md` |
| 로그인 Rate Limit 정책 정교화 | `docs/decisions/phase25_login_rate_limit_policy_refinement.md` |
| OAuth2 Principal 런타임 안전성 보강 | `docs/decisions/phase26_oauth2_principal_runtime_safety.md` |
| OAuth2 계정 충돌 정책/UX 정합화 | `docs/decisions/phase27_oauth2_account_conflict_policy.md` |
| 명시적 소셜 계정 연결 플로우 | `docs/decisions/phase28_explicit_social_account_linking.md` |
| 소셜 전용 계정 로컬 비밀번호 설정 | `docs/decisions/phase29_social_password_bootstrap.md` |
| 소셜 계정 연결 해제 정책 | `docs/decisions/phase30_social_account_unlink_policy.md` |
| 소셜 계정 다중 연결 구조 정규화 | `docs/decisions/phase31_member_social_account_normalization.md` |
| 소셜 provider 식별자 불변 정책 | `docs/decisions/phase32_social_provider_identity_immutability.md` |
| 인증/소셜 감사 로그 도입 | `docs/decisions/phase33_auth_social_audit_log.md` |
| 운영 관측성 baseline | `docs/decisions/phase34_operability_observability_baseline.md` |
| 인증 감사 로그 조회 API | `docs/decisions/phase35_auth_audit_query_api.md` |
| API 계약/운영 콘솔/Prometheus/demo 진입점 | `docs/decisions/phase36_api_contract_observability_demo.md` |
| 감사 로그 export/인증 이상 징후 알림/Grafana 대시보드 | `docs/decisions/phase37_auth_audit_export_alerting_dashboard.md` |
| 감사 로그 tenant 비정규화/retention/archive | `docs/decisions/phase38_auth_audit_retention_and_denormalization.md` |
| management plane 하드닝/활성 세션 제어 | `docs/decisions/phase39_management_plane_and_active_session_control.md` |
| notification outbox/retry/incident webhook | `docs/decisions/phase40_notification_outbox_and_incident_channel.md` |
| 반 정원/waitlist/offer 입학 워크플로우 | `docs/decisions/phase41_admission_capacity_waitlist_workflow.md` |
| 학부모 출결 변경 요청/승인 워크플로우 | `docs/decisions/phase42_attendance_change_request_workflow.md` |
| 업무 감사 로그(domain audit log) | `docs/decisions/phase43_domain_audit_log.md` |
| tagged CI/readiness failure mode/hiring pack | `docs/decisions/phase44_tagged_ci_readiness_and_hiring_pack.md` |
| fail-closed 기본 설정/management surface | `docs/decisions/phase45_fail_closed_runtime_defaults.md` |
| 신청서 권한 경계/출결 요청 DB 가드/Java 21 기준선 | `docs/decisions/phase46_service_boundaries_java21_baseline.md` |
| outbox atomic claim/compose localhost binding/package smoke | `docs/decisions/phase47_outbox_atomic_claim_and_ops_contract.md` |

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
