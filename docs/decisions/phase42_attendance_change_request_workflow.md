# Phase 42. 학부모 출결 변경 요청과 교사 승인 워크플로우

## 1. 배경

이전 출결 도메인은 교사/원장이 직접 `Attendance`를 작성하거나 수정하는 구조였다.

하지만 실제 운영에서는 학부모가 결석/병결/지각 사유를 먼저 제출하고, 교사나 원장이 이를 검토하는 흐름이 흔하다.

포트폴리오 관점에서도 "출석 수정 API"보다 "요청 -> 검토 -> 승인/거절" 구조가 더 실무형 백엔드 이야기를 만든다.

## 1-1. Before / After

| 구분 | Before | After |
|---|---|---|
| 학부모 개입 | 출결 조회 위주 | 변경 요청 생성 가능 |
| 출결 모델 | `Attendance` 단일 aggregate | `Attendance` + `AttendanceChangeRequest` 분리 |
| 승인 큐 | 없음 | 교사/원장 승인 대기 큐 |
| 이력 | 최종 출결만 남음 | 요청/승인/거절/취소 이력 보존 |

## 2. 이번 결정

### 2-1. 요청 aggregate를 별도로 둔다

- 최종 상태는 `Attendance`
- 승인 전 요청 상태는 `AttendanceChangeRequest`

즉 학부모가 곧바로 출결 record를 수정하지 못하게 하고,
review queue를 반드시 통과하도록 했다.

### 2-2. 학부모 요청과 staff 검토의 책임을 분리한다

- 학부모는 자기 자녀에 대해서만 요청 생성/조회/취소
- 교사/원장은 같은 유치원 요청에 대해서만 승인/거절

이렇게 해서
"누가 요청했는가"와
"누가 최종 반영했는가"를 따로 추적할 수 있게 했다.

### 2-3. 승인 시점에만 실제 출결을 upsert한다

- 요청 생성 시에는 `Attendance`를 건드리지 않는다.
- 승인 시점에 `AttendanceService.upsertAttendance(...)`를 호출한다.

즉 승인 전 요청과 최종 출결 snapshot을 섞지 않았다.

## 3. 구현 포인트

### schema / entity

- `attendance_change_request`
  - `kindergarten_id`, `classroom_id` 비정규화
  - `requester_id`, `reviewed_by`
  - `attendance_id`
  - `requested_status`, `note`, `rejection_reason`
  - `PENDING / APPROVED / REJECTED / CANCELLED`
- `AttendanceChangeRequest`
  - `approve`, `reject`, `cancel`

### service / access policy

- `AttendanceChangeRequestService`
  - 학부모 요청 생성
  - staff 승인/거절
  - 학부모 취소
  - staff pending queue 조회
- `AccessPolicyService`
  - 학부모-자녀 관계 검증
  - 같은 유치원 staff 검토 검증
  - 본인 요청 조회/취소 검증

### view / API

- `POST /api/v1/attendance-requests`
- `GET /api/v1/attendance-requests/my`
- `GET /api/v1/attendance-requests/pending`
- `POST /api/v1/attendance-requests/{id}/approve`
- `POST /api/v1/attendance-requests/{id}/reject`
- `POST /api/v1/attendance-requests/{id}/cancel`
- `/attendance-requests` 운영 화면 추가

## 4. 트레이드오프

### 장점

- 학부모가 최종 출결 데이터를 직접 바꾸지 못해 권한 경계가 명확하다.
- 승인/거절/취소 이력을 남겨 운영 책임 추적이 가능하다.
- 요청 aggregate 덕분에 review queue를 독립적으로 설명할 수 있다.

### 비용

- 단순 출결 CRUD보다 aggregate 하나가 더 생겨 복잡도가 증가한다.
- 현재는 요청 사유/상태 중심이고 첨부파일 증빙까지는 지원하지 않는다.
- 같은 날짜의 중복 `PENDING` 요청만 막고, 더 복잡한 merge 정책은 아직 없다.

## 5. 검증

실행 명령:

```bash
./gradlew --no-daemon compileJava compileTestJava
./gradlew --no-daemon test --tests "com.erp.api.AttendanceChangeRequestApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest"
```

검증한 내용:

- 학부모의 자기 자녀 출결 요청 생성
- 다른 유치원 원생 대상 요청 차단
- 교사의 승인/거절
- 다른 유치원 교사의 검토 차단
- parent/staff `/attendance-requests` 화면 렌더링

## 6. 면접에서 말할 포인트

- "학부모가 곧바로 `Attendance`를 수정하지 못하게 하고, 요청 aggregate를 별도로 둬 review queue를 만들었습니다."
- "승인 전 요청과 최종 출결 snapshot을 분리해서 이력과 권한 경계를 동시에 지켰습니다."
- "이 흐름 덕분에 단순 출석 CRUD가 아니라, 역할 간 상호작용이 있는 운영형 워크플로우를 설명할 수 있게 됐습니다."
