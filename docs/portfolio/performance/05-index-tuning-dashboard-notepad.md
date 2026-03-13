# Index Tuning: Dashboard + Notepad

## 문제 재현 시나리오

- Notepad 목록: `GET /api/v1/notepads/classroom/{id}`
- Dashboard 통계: `/api/v1/dashboard/statistics` 내부 집계 쿼리
- Announcement 목록: 유치원 공지 최신순/중요도순 정렬

기능은 동작했지만, SQL 실행계획 기준으로 `ALL` 스캔/`Using filesort`가 보여
데이터 증가 시 병목 가능성이 높은 상태였습니다.

## 개선 전 EXPLAIN

### 1) Notepad 반별 목록

```sql
EXPLAIN SELECT n.id, n.classroom_id, n.kid_id, n.created_at
FROM notepad n
WHERE n.classroom_id = 1 AND n.kid_id IS NULL
ORDER BY n.created_at DESC
LIMIT 20;
```

- type: `ALL`
- key: `NULL`
- Extra: `Using where; Using filesort`

### 2) Announcement 목록

```sql
EXPLAIN SELECT a.id, a.kindergarten_id, a.is_important, a.created_at
FROM announcement a
WHERE a.kindergarten_id = 1 AND a.deleted_at IS NULL
ORDER BY a.is_important DESC, a.created_at DESC
LIMIT 20;
```

- type: `ALL`
- key: `NULL`
- Extra: `Using where; Using filesort`

### 3) Dashboard 출석 집계

```sql
EXPLAIN SELECT COUNT(a.id)
FROM attendance a
JOIN kid k ON a.kid_id = k.id
JOIN classroom c ON k.classroom_id = c.id
WHERE c.kindergarten_id = 1
  AND a.date BETWEEN '2025-01-01' AND '2025-01-31'
  AND a.status IN ('PRESENT', 'LATE');
```

- attendance 접근 type: `ALL`
- key: `NULL`
- Extra: `Using where; Using join buffer (hash join)`

## 개선 내용

Flyway migration 추가: `src/main/resources/db/migration/V5__add_performance_indexes_for_dashboard_and_notepad.sql`

```sql
CREATE INDEX idx_notepad_classroom_kid_created
    ON notepad (classroom_id, kid_id, created_at DESC);

CREATE INDEX idx_notepad_kid_created
    ON notepad (kid_id, created_at DESC);

CREATE INDEX idx_announcement_kind_deleted_important_created
    ON announcement (kindergarten_id, deleted_at, is_important DESC, created_at DESC);

CREATE INDEX idx_attendance_date_status_kid
    ON attendance (date, status, kid_id);
```

## 개선 후 EXPLAIN

### 1) Notepad 반별 목록

- type: `ref`
- key: `idx_notepad_classroom_kid_created`
- Extra: `Using where; Using index`

### 2) Announcement 목록

- type: `ref`
- key: `idx_announcement_kind_deleted_important_created`
- Extra: `Using where; Using index`
- `Using filesort` 제거

### 3) Dashboard 출석 집계

- attendance 접근 type: `range`
- key: `idx_attendance_date_status_kid`
- Extra: `Using where; Using index`

## 포트폴리오 스토리텔링 포인트

1. "코드 최적화 뒤에도 DB 실행계획을 확인해 병목 가능성을 제거했다"
2. "증상(ALL/filesort) -> 인덱스 설계 -> 실행계획 개선(ref/range)으로 이어지는 과정"을 설명할 수 있다
3. "단순히 인덱스를 늘린 게 아니라, 실제 where/order/join 패턴에 맞춰 설계했다"

## 트레이드오프

- 인덱스 추가로 쓰기 성능/스토리지 비용은 증가한다.
- 다만 본 프로젝트는 조회 트래픽(목록/통계)이 핵심이므로 read 최적화를 우선했다.
- 향후 데이터 증가 시 slow query log 기반으로 인덱스 정리를 주기적으로 수행한다.
