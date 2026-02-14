# MySQL Validation Report

## 목적

H2 기반 테스트 수치를 MySQL 실행계획으로 교차 검증해,
"개선이 실제 운영 DB에서도 유효한가"를 확인하는 문서입니다.

## 검증 범위

- Notepad 목록 조회
- Announcement 목록 조회
- Dashboard 출석 집계

## 검증 방법

1. 개선 전 SQL 기준 EXPLAIN 수집
2. 인덱스/쿼리 개선 적용
3. 동일 SQL로 EXPLAIN 재수집
4. `type`, `key`, `rows`, `Extra` 비교

## 실행 명령 예시

```bash
# MySQL 접속
mysql -u root -p

# 대표 쿼리 EXPLAIN
EXPLAIN SELECT n.id, n.classroom_id, n.kid_id, n.created_at
FROM notepad n
WHERE n.classroom_id = 1 AND n.kid_id IS NULL
ORDER BY n.created_at DESC
LIMIT 20;

# 최근 slow query 확인(환경별 경로 상이)
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';
```

## EXPLAIN 비교 요약

| Query | Before | After | 핵심 변화 |
|---|---|---|---|
| Notepad 목록 | `type=ALL`, `Using filesort` | `type=ref`, `Using index` | 풀스캔/파일소트 제거 |
| Announcement 목록 | `type=ALL`, `Using filesort` | `type=ref`, `Using index` | 정렬 비용 감소 |
| Attendance 집계 | `type=ALL` | `type=range`, `Using index` | 날짜/상태 범위 인덱스 활용 |

## 검증 체크포인트

- `type`: `ALL` -> `ref/range` 전환 여부
- `key`: 의도한 복합 인덱스 사용 여부
- `rows`: 스캔 대상 건수 감소 여부
- `Extra`: `Using filesort` 제거 여부

## 적용 인덱스

- `idx_notepad_classroom_kid_created`
- `idx_notepad_kid_created`
- `idx_announcement_kind_deleted_important_created`
- `idx_attendance_date_status_kid`

## 결론

- 코드 레벨 개선(N+1 제거, 집계 쿼리 전환)이 MySQL 실행계획 개선으로 이어짐
- H2 수치는 회귀 감지용으로 사용하고, 최종 타당성은 MySQL EXPLAIN으로 보완

## 운영 관측 지표(권장)

- API별 p95 (Notepad/Dashboard/Auth)
- DB CPU(피크 구간)
- slow query 발생 건수(일/주)
- 인덱스 추가 후 write latency 변화

## 후속 과제

- slow query log 주간 리포트 운영
- 실제 rows 변화량 추적 자동화
- 인덱스 write cost 모니터링(삽입/수정 경로)
