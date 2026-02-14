# Query Inventory and Owner

## 목적

핵심 성능 쿼리의 위치/책임자/튜닝 상태를 한눈에 관리하기 위한 문서입니다.

## 핵심 쿼리 인벤토리

| Domain | Query 목적 | 위치(예시) | 현 상태 | 다음 액션 |
|---|---|---|---|---|
| Notepad | 목록 + readCount | `NotepadService`, `NotepadRepository` | N+1 제거 완료, 집계 전환 완료 | MySQL rows 추적 자동화 |
| Dashboard | 출석/공지 통계 집계 | `DashboardService`, 통계 repository | 집계 쿼리 통합 완료 | p99 지표 추가 |
| Announcement | 중요도/최신순 목록 | announcement repository | 인덱스 튜닝 완료 | 데이터 증가 시 재검토 |
| Attendance | 기간/상태 집계 | attendance repository | range scan 유도 완료 | 조건 확장 시 인덱스 점검 |
| Auth/Redis | refresh 조회/검증 | auth service + redis access | 단일 키 O(1) 전환 완료 | 멀티 디바이스 정책 검토 |

## 오너십 규칙

- 서비스 레벨 변경 시: 해당 도메인 서비스 담당자 리뷰 필수
- 인덱스 변경 시: DB 실행계획(EXPLAIN) 캡처 필수
- 캐시 변경 시: 무효화 테스트와 TTL 테스트 동시 제출

## 문서 연동

- `03-notepad-readcount-nplusone.md`
- `04-dashboard-stats.md`
- `05-index-tuning-dashboard-notepad.md`
- `07-redis-adoption-story-script.md`
- `02-mysql-validation-report.md`
