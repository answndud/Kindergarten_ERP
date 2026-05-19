# Backend Portfolio Interview Guide

기준일: 2026-05-19

이 문서는 면접관이 Kindergarten ERP를 빠르게 평가할 때 볼 수 있는 백엔드 포트폴리오 설명 가이드입니다.

## 1. 5분 설명 루트

1. `README.md`의 "핵심 문제와 해결"과 "수치로 검증한 개선"을 먼저 봅니다.
2. `docs/COMPLETED.md`에서 작업 archive를 확인합니다.
3. 코드에서는 `global/security`, `global/exception`, `domain/notification`, `domain/calendar`, `domain/attendance`를 확인합니다.
4. 테스트에서는 `src/test/java/com/erp/api/*IntegrationTest.java`, `src/test/java/com/erp/integration/*`, `src/test/java/com/erp/performance/*`를 확인합니다.

## 2. 이번 강화 작업의 의도

### 입력/예외 하드닝

- 문제: 잘못된 query/path parameter, 필수 파라미터 누락, 날짜 변환 오류가 generic 500으로 떨어질 수 있었습니다.
- 결정: Spring MVC 입력 경계 예외를 `GlobalExceptionHandler`에서 명시적으로 400 `ApiResponse.error`로 닫았습니다.
- 검증: 출석 월 조회 invalid month, missing year, invalid year type 통합 테스트를 추가했습니다.
- 트레이드오프: 모든 `IllegalArgumentException`을 400으로 잡지 않고, MVC 입력 오류와 날짜 오류 중심으로 제한했습니다.

### 캘린더 반복/범위 안전장치

- 문제: 반복 일정 확장 로직이 `CalendarEventService`에 섞여 있었고, 긴 조회 범위를 제한하지 않았습니다.
- 결정: `RecurrenceExpander`로 반복 occurrence 확장을 분리하고, 일정 목록 조회 기간을 최대 366일로 제한했습니다.
- 검증: fast unit test로 반복 확장을 고정하고, 통합 테스트로 366일 초과 조회가 400인지 확인했습니다.
- 트레이드오프: 신규 반복 규칙을 추가하지 않고 기존 `DAILY/WEEKLY/MONTHLY` 동작 보존에 집중했습니다.

### Notification Outbox 운영 API

- 문제: outbox는 retry/backoff/dead-letter 상태 전이를 갖췄지만, 운영자가 실패 건을 확인하고 재시도하는 API가 없었습니다.
- 결정: 원장 전용 `/api/v1/notification-outbox` 운영 API를 추가해 summary, dead-letter 목록, 수동 retry를 제공합니다.
- 검증: 원장 성공, 교사 접근 차단, dead-letter 재시도 상태 전이를 통합 테스트로 확인했습니다.
- 트레이드오프: 실제 외부 채널 발송 정책은 건드리지 않고, 실패한 outbox를 다시 `PENDING`으로 돌리는 운영면만 확장했습니다.

### 코드 탐색 노이즈 제거

- 문제: 비어 있는 `MemberRepositoryCustom/Impl` 스텁과 로컬 swap/backup 파일이 코드 탐색을 방해했습니다.
- 결정: 사용되지 않는 custom repository 스텁을 제거하고 로컬 ignored 작업파일을 삭제했습니다.
- 검증: 컴파일과 targeted tests로 repository wiring 회귀를 확인합니다.

## 3. 면접에서 강조할 답변 포인트

- 보안: HTTP-only cookie JWT, Redis refresh session, active session revoke, fail-closed profile.
- 권한: `PRINCIPAL`, `TEACHER`, `PARENT` 역할과 유치원 tenant 경계를 API/service/test에서 함께 검증.
- 운영성: audit log, outbox retry/dead-letter, Prometheus/Grafana, readiness, structured logging.
- 성능: Notepad/Dashboard N+1 제거, query count와 응답 시간 전후 비교, push CI 경량화.
- 테스트: Testcontainers 기반 MySQL/Redis 통합 테스트, fast/integration/performance smoke 분리.
- 문서화: `docs/PLAN.md`, `docs/PROGRESS.md`, `docs/COMPLETED.md`로 active/archive 상태 관리.

## 4. 남은 리스크와 후속 개선

- 실제 클라우드 배포는 비용 문제로 아직 실행하지 않았고, 배포 자산과 runbook 중심으로 준비된 상태입니다.
- Notification Outbox 운영 API는 API 중심이며, 별도 운영 화면은 아직 없습니다.
- `KidApplicationService`는 기능이 많아 향후 더 작은 workflow service로 나누면 테스트 locality가 좋아집니다.
- CORS allowed origins는 현재 단순 local 기준이며, 실제 배포 전 property 기반으로 분리하는 것이 좋습니다.
