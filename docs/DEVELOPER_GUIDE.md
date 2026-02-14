# Kindergarten ERP 개발자 가이드

이 문서는 이 저장소에서 기능을 추가/수정하는 개발자를 위한 실무 가이드입니다.
유저 관점 설명은 `docs/GUIDE.md`를 참고하세요.

---

## 1. 프로젝트 개요

- 프로젝트: Kindergarten ERP
- 핵심 역할: `PRINCIPAL`, `TEACHER`, `PARENT`
- 아키텍처: `domain/{controller,service,repository,entity,dto}` + `global/*`
- 기술 스택:
  - Java 17
  - Spring Boot 3.5.9
  - Spring Data JPA + QueryDSL
  - Spring Security + JWT(HTTP-only cookie)
  - MySQL 8, Redis
  - Thymeleaf + HTMX + Alpine.js + Tailwind(CDN)
  - Flyway

핵심 원칙은 **Simple is Best** 입니다.

---

## 2. 로컬 실행

## 인프라

```bash
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml down
```

## 빌드/실행

```bash
./gradlew clean build
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 테스트

```bash
./gradlew test
./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest"
./gradlew check
```

---

## 3. 디렉토리 구조와 책임

## 백엔드

- `src/main/java/com/erp/domain/*`
  - `controller`: API/뷰 진입점
  - `service`: 비즈니스 로직, 트랜잭션 경계
  - `repository`: 데이터 접근
  - `entity`: JPA 엔티티
  - `dto`: 요청/응답 객체
- `src/main/java/com/erp/global/*`
  - `config`: JPA, QueryDSL, Security, MVC Interceptor, Cache
  - `security`: JWT 필터/프로바이더/UserDetails
  - `exception`: `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`
  - `common`: `ApiResponse`, `BaseEntity`

## 프론트

- `src/main/resources/templates/*`: 역할 기반 SSR 화면
- `src/main/resources/static/js/app.js`: 공통 UI/SweetAlert2/알림 유틸
- HTMX로 fragment 단위 갱신, Alpine.js로 최소 상태 관리

## DB

- `src/main/resources/db/migration/V*.sql`: Flyway 마이그레이션

---

## 4. 인증/인가/리다이렉트 동작

## 인증

- API 로그인: `POST /api/v1/auth/login`
- JWT access/refresh 토큰은 쿠키 기반
- refresh token은 Redis TTL 저장

## 인가

- `@PreAuthorize` + `SecurityConfig` URL 규칙 병행
- 역할 enum은 반드시 `PRINCIPAL`, `TEACHER`, `PARENT`만 사용

## 상태 기반 강제 리다이렉트

`RoleRedirectInterceptor`가 다음을 강제합니다.

- 원장 + 유치원 미등록: `/kindergarten/create`
- 선생/학부모 + `PENDING` 또는 유치원 미배정: `/applications/pending`

신규 화면 추가 시 이 인터셉터 영향 범위를 먼저 점검하세요.

---

## 5. 도메인별 핵심 기능 맵

- `auth/member`: 가입/로그인/프로필/비밀번호/탈퇴
- `kindergarten/classroom`: 유치원/반 생성 및 운영
- `kid`: 원생 관리, 부모 연결
- `attendance`: 일별 출결, 특수 출결 상태, 월간 리포트
- `notepad`: 반/원생 알림장, 읽음 처리
- `announcement`: 공지 및 중요 공지
- `kindergartenapplication/kidapplication`: 지원/승인
- `notification`: 배지/목록/읽음/삭제
- `calendar`: 유치원/반/개인 일정
- `dashboard`: 통계(원장)

---

## 6. API/코드 스타일 규칙

## API

- prefix: `/api/v1/**`
- 응답: `ApiResponse<T>`
- 기존 계약은 수정보다 확장 우선

## DTO

- 요청: `*Request`
- 응답: `*Response` (가능하면 record 고려)
- 컨트롤러 입력 경계에 `@Valid`, `@NotNull`, `@NotBlank`

## 예외 처리

- 서비스에서 비즈니스 오류는 `BusinessException(ErrorCode)`
- 에러 포맷은 `ApiResponse.error` 계약 유지

## 트랜잭션/JPA

- 서비스 클래스 기본 `@Transactional(readOnly = true)` 권장
- 쓰기 메서드만 `@Transactional`
- OSIV OFF 전제 (`open-in-view=false`)
- Controller/View에서 lazy 초기화 의존 금지

---

## 7. DB 마이그레이션 규칙

- 위치: `src/main/resources/db/migration/`
- 파일명: `V{version}__{description}.sql`
- 운영 파괴 작업 금지
- 인덱스는 실제 조회 패턴 기준으로 추가
- soft delete 엔티티는 `deletedAt` 필터 일관성 유지

현재 주요 마이그레이션:

- `V1__init_schema.sql`: 초기 핵심 테이블
- `V2__add_application_workflow.sql`: 지원/승인/알림
- `V3__kid_application_unique_parent_kindergarten.sql`: 중복 신청 제약
- `V4__create_calendar_events.sql`: 캘린더
- `V5__add_performance_indexes_for_dashboard_and_notepad.sql`: 성능 인덱스

---

## 8. 테스트 전략

권장 우선순위:

1. Controller/API 변경: 통합 테스트 우선
2. 보안 변경: 성공/실패 권한 케이스 모두 추가
3. 성능 변경: 쿼리 수 또는 응답 시간 최소 1개 수치화

참고 테스트 클래스:

- `src/test/java/com/erp/api/*IntegrationTest.java`
- `src/test/java/com/erp/integration/PageAccessIntegrationTest.java`
- `src/test/java/com/erp/performance/*PerformanceStoryTest.java`

---

## 9. 문서화 규칙

기능/정책 변경 시 반드시 문서를 같이 갱신하세요.

- 기능 결정/변경: `docs/phase/`
- 트러블슈팅/회고: `docs/retrospective/`
- 성능 개선: `docs/performance-optimization/`

특히 성능 작업은 아래 순서를 지켜 기록합니다.

1. 재현 시나리오 정의
2. 개선 전 측정
3. 개선 적용
4. 개선 후 측정
5. 트레이드오프 문서화

---

## 10. 신규 기능 개발 체크리스트

1. 도메인 폴더에 controller/service/repository/dto/entity 배치
2. DTO 검증 어노테이션 적용
3. `@PreAuthorize` + `SecurityConfig` URL 권한 동시 확인
4. API 응답 `ApiResponse<T>` 유지
5. 필요 시 Flyway migration 추가
6. 통합 테스트 추가/수정
7. `docs/phase/` 변경 로그 작성
8. 성능 영향이 있으면 전/후 수치 기록

---

## 11. 현재 코드베이스에서 인지할 포인트

- 일부 레거시/전환 구간에서 URL 단수/복수 혼용이 존재합니다.
- 일부 API/뷰가 병행되어 있어 컨벤션 통일 작업 여지가 있습니다.
- `kindergarten/select` 는 안내 성격이 남아 있는 화면입니다.
- 공지사항 API 컨트롤러에는 임시 주석이 남아 있으므로 수정 시 인증 주체 연계를 먼저 점검하세요.

---

## 12. 참고 문서

- 사용자 가이드: `docs/GUIDE.md`
- 프로젝트 개요: `README.md`
- 단계별 결정 로그: `docs/phase/*.md`
- 성능 개선 스토리: `docs/performance-optimization/README.md`
