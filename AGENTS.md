# AGENTS.md

이 문서는 이 저장소에서 작업하는 코딩 에이전트용 SSOT입니다.
긴 배경은 `docs/`에 유지하고, 에이전트 작업 규칙은 이 파일을 우선합니다.

## 1) Project Snapshot

- 프로젝트: Kindergarten ERP (유치원 운영 관리 시스템)
- 주요 사용자: 원장(PRINCIPAL), 교사(TEACHER), 학부모(PARENT)
- 철학: **Simple is Best**
- 기술: Java 17, Spring Boot 3.5.9, JPA, QueryDSL, Security, JWT, Flyway
- DB/Infra: MySQL 8, Redis
- 화면: Thymeleaf + HTMX + Alpine.js + Tailwind(CDN)
- 아키텍처: `domain/{controller,service,repository,entity,dto}` + `global/*`
- JPA 기본: OSIV OFF, `default_batch_fetch_size=100`

## 2) Rule Files Scan (Cursor / Copilot)

- `.cursor/rules/`: 없음
- `.cursorrules`: 없음
- `.github/copilot-instructions.md`: 없음

현재 저장소에는 Cursor/Copilot 전용 규칙 파일이 없으므로, 본 문서 규칙을 따른다.

## 3) Build / Run / Verify Commands

```bash
# Docker (MySQL + Redis)
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml down

# Build
./gradlew clean build
./gradlew build -x test

# Run
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=local'

# Compile/Check
./gradlew compileJava compileTestJava
./gradlew check
```

## 4) Test Commands (Single Test 포함)

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest"

# Nested 클래스
./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest$CreateAttendanceTest"

# 특정 테스트 메서드
./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest.createAttendance_Success_Principal"

# 패턴 기반 실행
./gradlew test --tests "*MemberServiceTest"

# 캐시 무시 재실행
./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --rerun
```

## 5) Code Style Guidelines

### 5.1 Formatting / General

- Java 17 기준 문법 사용 (record/var 허용, 가독성 우선).
- 들여쓰기 4 spaces.
- 메서드는 한 가지 책임에 집중하고 길어지면 private 메서드로 분리.
- 주석은 "왜"가 없으면 추가하지 않는다.

### 5.2 Imports

- 기존 코드에 wildcard import가 존재하므로 파일 단위 일관성 우선.
- 신규 코드에서는 explicit import를 권장.
- FQCN(`com.erp...`) 반복 사용은 지양하고 import로 정리.

### 5.3 Types / DTO

- 요청 DTO: `*Request`, 응답 DTO: `*Response`.
- 응답 DTO는 record 사용을 기본으로 고려.
- API 응답 래핑은 `ApiResponse<T>`를 기본 표준으로 유지.
- `@Valid`, `@NotNull`, `@NotBlank` 검증을 컨트롤러 입력 경계에 적용.

### 5.4 Naming

- Class: PascalCase
- method/field: camelCase
- constant: UPPER_SNAKE_CASE
- Entity 이름은 단수형 유지 (`Member`, `Kid`, `Attendance`).
- 컨트롤러는 용도 분리:
  - API: `*Controller` 또는 `*ApiController`
  - View: `*ViewController`

### 5.5 Error Handling

- 비즈니스 오류는 서비스 계층에서 `BusinessException(ErrorCode)`로 처리.
- 공통 예외 응답은 `GlobalExceptionHandler` 규격을 따른다.
- ErrorCode 추가 시 status/code/message를 반드시 함께 정의.
- 기존 에러 포맷(`ApiResponse.error`)과 계약을 깨지 않는다.

### 5.6 Transaction / Persistence

- 서비스 클래스는 기본 `@Transactional(readOnly = true)` 권장.
- 쓰기 메서드만 `@Transactional`로 명시.
- OSIV OFF 전제이므로 Controller/View에서 lazy 초기화 의존 금지.
- 페이징 + fetch join은 count/중복 위험이 크므로 신중히 사용.

### 5.7 Repository / Query

- 조회 패턴이 단순하면 Spring Data 메서드명.
- 복잡한 조건/동적 검색은 QueryDSL 또는 custom repository로 분리.
- N+1 가능성은 로그/쿼리 수로 확인 후 EntityGraph/쿼리 분리 적용.
- Soft delete 엔티티는 `deletedAt` 필터 일관 적용.

### 5.8 Security

- 역할 enum은 `PRINCIPAL`, `TEACHER`, `PARENT`만 사용.
- JWT는 HTTP-only cookie 기반 stateless.
- Refresh token은 Redis TTL 저장.
- 새 endpoint 추가 시 URL 권한(`SecurityConfig`) + 메서드 권한(`@PreAuthorize`) 동시 점검.

## 6) API / Frontend Conventions

- REST API는 `/api/v1/**` 하위로 유지.
- 기존 API 계약은 **Extension over Modification** 우선.
- 템플릿에서 ID/데이터 하드코딩 금지, API 동적 로딩 우선.
- HTMX는 전체 페이지 리로드보다 부분 갱신을 우선 적용.

## 7) DB / Migration Rules

- 위치: `src/main/resources/db/migration/`
- 파일명: `V{version}__{description}.sql`
- 운영 파괴 작업 금지(특히 clean/rebuild 계열).
- 인덱스는 실제 조회 패턴을 기준으로 추가하고 문서화.

## 8) Testing & Quality Rules

- 변경 코드에는 최소 1개 이상 검증(단위/통합) 추가 또는 보강.
- Controller 변경 시 통합 테스트 우선.
- 보안 변경 시 권한 성공/실패 케이스를 함께 검증.
- 성능 관련 변경은 "쿼리 수"와 "응답 시간" 중 최소 하나 이상 수치 확인.

## 9) Docs Rules (Mandatory)

- 주요 기능 추가/변경 시 `docs/00_project/`에 결정 로그 작성.
- 트러블슈팅/회고는 `docs/retrospective/`에 기록.
- 성능 최적화는 `docs/performance-optimization/`에 전/후 비교를 기록.

## 10) Git Workflow (Mandatory)

- 사용자 가치 단위 완료 시:
  1) 변경 요약
  2) 커밋 메시지 후보
  3) 승인 후 add/commit/push
- 강제 푸시/리베이스/`--no-verify`는 명시 요청 없이는 금지.

## 11) Portfolio-Focused Performance Story Rule

- 이 프로젝트는 "처음엔 느렸고, 개선했다"를 보여주는 포트폴리오가 목표다.
- 따라서 성능 작업은 아래 순서를 반드시 따른다:
  1) 재현 시나리오 정의
  2) 개선 전 측정(쿼리 수/응답 시간)
  3) 개선 적용
  4) 개선 후 동일 시나리오 재측정
  5) 결과/트레이드오프 문서화
- 문서에는 정량(숫자) + 정성(왜 이 선택을 했는지) 둘 다 포함한다.
