# AGENTS.md

이 파일은 Claude Code / Codex / opencode 등 코딩 에이전트가 이 저장소에서 작업할 때의 **최신/짧은 단일 진실 소스(SSOT)** 입니다.
(긴 배경/설명은 `CLAUDE.md`, `docs/`에 유지)

## Project Overview
- **Kindergarten ERP**: 유치원 운영 관리 시스템 (원장/교사/학부모)
- 철학: **Simple is Best**
- SSR: Thymeleaf + HTMX + Alpine.js + Tailwind(CDN)

## Current Snapshot (2026-01-14)
- `README.md` 기준 Phase 1~5(인증~공지) 구현 완료로 정리됨.
- Phase 7(지원/승인), Phase 8(알림)은 `docs/00_project/`에 설계/결정 로그가 있고 구현은 진행 예정.
- 워킹트리에 “알림장 목록 API + 템플릿 동적 로딩” 관련 변경이 존재.

## Key Commands
```bash
# Docker (MySQL + Redis)
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml down

# Build (skip tests)
./gradlew build -x test

# Run
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=local'

# Tests
./gradlew test
./gradlew test --tests "*MemberServiceTest"
./gradlew test jacocoTestReport
```

## Docs Rule (필수)
- 주요 기능(사용자 가치 단위) 추가/변경 시 `docs/00_project/`에 결정 로그 작성
- 트러블슈팅/회고는 `docs/retrospective/`에 기록

## Git Workflow (필수)
- 사용자 가치 단위 완료 시: “요약 → 메시지 후보 → 승인 후 add/commit/push”
- 강제 푸시/리베이스/--no-verify 등은 명시적 요청 없이는 금지

## Architecture Snapshot
- Java 17 + Spring Boot 3.5.9
- Spring Data JPA + QueryDSL 5.0.0
- Spring Security + JWT (jjwt 0.12.6)
- MySQL 8.0, Redis, Flyway
- OSIV 비활성화, batch fetch size 100
- DDD 패키지: `domain/{controller,service,repository,entity,dto}`

## Roles / Security
- Role enum: `PRINCIPAL`, `TEACHER`, `PARENT` (`src/main/java/com/erp/domain/member/entity/MemberRole.java`)
- JWT: HTTP-only cookie 기반 Stateless
- Refresh token: Redis 저장(TTL)
- 리다이렉트/인터셉터 기반 “필수 정보 입력 유도” 패턴 있음
  - 참고: `docs/retrospective/2025-01-14-interceptor-redirect-bug.md`

## API / Controller Conventions
- REST API: `/api/v1/` 아래
- `*ApiController`와 `*ViewController` 분리
- DTO: `*Request`, `*Response`
- Entity: 단수형
- 기존 API 계약은 **Extension over Modification** 우선

## Frontend Rules (Thymeleaf/HTMX)
- 템플릿에서 ID/데이터 하드코딩 금지: 드롭다운/목록은 API로 동적 로드
  - 참고: `docs/retrospective/2025-01-14-notepad-classroom-not-found.md`
- API 경로는 실제 컨트롤러 매핑 기준으로 맞춘다
  - 예: 출석 API base path는 `/api/v1/attendance` (`src/main/java/com/erp/domain/attendance/controller/AttendanceController.java`)

## DB / Migration
- Flyway: `src/main/resources/db/migration/`
- 네이밍: `V{version}__{description}.sql`
- Soft delete: `deletedAt` 패턴 유지

## Stability Notes
- Pageable 조회에 `JOIN FETCH` 사용 시 카운트/중복/페이지 깨짐 위험이 있으니 주의(필요 시 쿼리 분리/EntityGraph 고려)
