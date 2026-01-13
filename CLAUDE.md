# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Kindergarten ERP** is a management system for kindergarten operations targeting three user roles:
- **Principal** (원장): Overall management and approvals
- **Teacher** (교사): Daily operations (attendance, notepads, schedules)
- **Parent** (학부모): View notepads and attendance records

The project follows a "Simple is Best" philosophy - focused on core functionality with clean, intuitive UI.

## Development Commands

### Building & Running
```bash
# Build (skip tests)
./gradlew build -x test

# Run application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*MemberServiceTest"

# Run with coverage
./gradlew test jacocoTestReport
```

### Database (Flyway Migrations)
```bash
# Flyway migrations run automatically on startup
# To clean and rebuild (local only):
./gradlew clean flywayClean flywayMigrate
```

### Docker Setup
```bash
# Note: docker-compose.yml needs to be created - currently missing
# Intended setup:
cd docker
docker-compose up -d    # Start MySQL + Redis
docker-compose down     # Stop containers
```

## Architecture

### Technology Stack
- **Java 17** with Spring Boot 3.5.9
- **Spring Data JPA** with QueryDSL 5.0.0 for dynamic queries
- **Spring Security** + JWT (jjwt 0.12.6) for stateless authentication
- **MySQL 8.0** as primary database
- **Redis** for caching and token management
- **Flyway** for database migrations
- **Thymeleaf** for server-side rendering
- **HTMX** for dynamic hypermedia-driven interactions
- **Alpine.js** for lightweight client-side reactivity
- **Tailwind CSS** for utility-first styling (v3.4 via CDN)

### Package Structure (Domain-Driven Design)
```
com.erp/
├── ErpApplication.java          # Main entry point
├── global/                      # Cross-cutting concerns
│   ├── config/                  # Configuration classes
│   ├── security/                # JWT, Security utilities
│   ├── exception/               # Exception handling
│   ├── common/                  # Shared classes (BaseEntity, ApiResponse)
│   └── util/                    # Utilities
└── domain/                      # Business domains (each follows same pattern)
    ├── auth/                    # Authentication (signup, login, token refresh)
    ├── member/                  # Member management
    ├── kindergarten/            # Kindergarten entity
    ├── classroom/               # Class management
    ├── kid/                     # Student management
    ├── attendance/              # Attendance tracking
    ├── notepad/                 # Daily notepads
    └── announcement/            # Announcements
```

Each domain package contains: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

### Configuration Profiles
- `local`: Local development (application-local.yml)
- `prod`: Production environment (application-prod.yml)
- Default profile: `local` (set in application.yml)

### Key Application Properties
- Server port: `8080`
- Database: MySQL on `localhost:3306/erp_db`
- JWT secret: Must be set via `JWT_SECRET` environment variable (256-bit minimum)
- JWT access token validity: 15 minutes
- JWT refresh token validity: 7 days

### Domain Model Hierarchy
```
KINDERGARTEN (1:N) CLASSROOM (1:N) KID (1:N) ATTENDANCE
                                  (N:M)
                                  MEMBER (Parents, Teachers)
```

Key relationships:
- **Member**: Users with roles (PRINCIPAL, TEACHER, USER/parent)
- **Classroom**: Has many kids, assigned teachers
- **Kid**: Students linked to classroom and parents
- **Attendance**: Daily attendance records per kid

### Security Architecture
- **Stateless JWT**: Tokens stored in cookies, no server session
- **Role-based access**: Four roles - ADMIN, PRINCIPAL, TEACHER, USER
- **Authorization flow**:
  1. Login via `/api/v1/auth/login` (email/password) or OAuth2
  2. JWT generated and stored in HTTP-only cookie
  3. Subsequent requests validated via JWT filter
  4. SecurityContext populated with authentication

### API Design Patterns
- RESTful APIs under `/api/v1/`
- Separate controllers for API (`*ApiController`) and views (`*ViewController`)
- Standard response format: `ApiResponse<T>` with success/error
- QueryDSL for complex queries (especially filtering/pagination)

### Frontend Architecture (HTMX + Alpine + Tailwind)
- **HTMX**: Server-driven UI updates via HTML over HTTP
  - Use `hx-get`, `hx-post`, `hx-put`, `hx-delete` for REST calls
  - `hx-swap` for controlling response insertion (innerHTML, outerHTML, beforeend, etc.)
  - `hx-target` for specifying where to swap content
  - `hx-trigger` for custom event triggers
- **Alpine.js**: Client-side state and interactivity
  - `x-data` for component state
  - `x-show`, `x-if` for conditional rendering
  - `x-for` for loops
  - `x-model` for two-way binding
  - `@click`, `@submit` for event handling
- **Tailwind CSS**: Utility-first styling via CDN
  - Use `<script src="https://cdn.tailwindcss.com"></script>` in templates
  - Configure theme colors in `tailwind.config` object if needed
- **Thymeleaf Integration**:
  - Server-side templates with `th:fragment` for reusable components
  - HTMX can swap Thymeleaf fragments dynamically
  - Use `th:replace` or `th:insert` for partial updates

### Database Conventions
- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`
- OSIV disabled (`open-in-view: false`) for performance
- Batch fetch size: 100 (reduces N+1 queries)
- Soft delete pattern: `deleted_at` timestamp (not hardcoded `delete_flag`)

## Development Notes

### Current Project State
This is a **new project scaffold** with:
- Package structure defined but entities not yet implemented
- Configuration files ready
- Docker directory exists but `docker-compose.yml` is missing
- Comprehensive planning documentation in `docs/`

### Important Patterns from Documentation
1. **Controller Separation**: Split `*ApiController` (REST) from `*ViewController` (Thymeleaf)
2. **DTO Naming**: Use `*Request` for input, `*Response` for output
3. **Entity Naming**: Singular form (Member, Kid, not Members, Kids)
4. **Repository Pattern**: Interface + Impl for QueryDSL custom queries
5. **Exception Handling**: Global handler with `BusinessException` and `ErrorCode`

### Environment Variables
```bash
# Required for JWT
export JWT_SECRET="your-256-bit-secret-key-here-must-be-at-least-32-characters"

# Database (defaults in application-local.yml)
export DB_USERNAME=root
export DB_PASSWORD=root

# Redis (optional)
export REDIS_PASSWORD=
```

## Git workflow (mandatory)
중요 기능(사용자 가치 단위) 완료 시에는 반드시 /commitpush 를 실행해
"요약 → 메시지 후보 → 승인 후 add/commit/push" 절차로만 커밋/푸시한다.

### Testing Strategy
- Unit tests for each service layer
- Integration tests for authentication flows
- Test coverage reports via JaCoCo plugin

### Reference Documentation
- `docs/project_plan.md`: Overall development plan
- `docs/project_idea.md`: Detailed implementation guide
- `docs/project_summary.md`: Analysis of reference project (AiBayo)
- `docs/springboot_tutorial.md`: Spring Boot patterns and best practices
- `docs/00_project/`: **Design decision logs for portfolio interviews**

## Design Decision Documentation

This project is for **backend developer portfolio interviews**. Every technical decision must be documented for interview preparation.

### Documentation Structure
Create decision logs in `docs/00_project/` for each feature/phase:

```
docs/00_project/
├── phase0_setup.md           # Project setup decisions
├── phase1_auth.md            # Authentication & JWT decisions
├── phase2_kindergarten.md    # Kindergarten & Classroom decisions
├── phase3_kid.md             # Kid & Parent decisions
├── phase4_attendance.md      # Attendance decisions
├── phase5_notepad.md         # Notepad decisions
└── phase6_announcement.md    # Announcement decisions
```

### Decision Log Template (Korean)

```markdown
# [기능명] 기술 선택 및 구현 결정

## 개요
- 기능: [간단한 설명]
- 목표: [해결하려는 문제]

## 기술/라이브러리 선택

### [기술명]
**결정**: [선택한 기술]

**이유**:
1. [이유 1]
2. [이유 2]
3. [이유 3]

**대안 고려**:
- [대안]: [채택하지 않은 이유]

**변경 이력**:
- 2024-XX-XX: 초기 채택
- 2024-XX-XX: [변경사유]로 [이전기술] → [새기술]
```




### When to Document
- Before implementing: Write decision rationale
- After implementation: Update with actual outcomes
- When changing: Record change reason in "변경 이력"

### Interview Preparation
These logs directly answer interview questions like:
- "Why did you choose X over Y?"
- "What trade-offs did you consider?"
- "How would you improve this if you could restart?"
