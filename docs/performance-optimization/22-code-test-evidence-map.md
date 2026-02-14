# Code/Test Evidence Map

## 목적

문서 기반 성능 개선 내용을 실제 코드/테스트와 1:1로 연결해
"말한 개선이 코드로 증명되는가"를 빠르게 확인하기 위한 매핑 문서입니다.

## 성능 개선 증빙 매핑

| 주제 | 핵심 변경 코드 | 검증 테스트/근거 | 관련 문서 |
|---|---|---|---|
| Notepad N+1 제거 | `NotepadService`, `NotepadRepository` 집계 조회 전환 | `NotepadApiIntegrationTest.MarkAsReadTest#getClassroomNotepads_ReadCountReflected` | `03-notepad-readcount-nplusone.md` |
| Dashboard 집계 전환 | Dashboard 통계 집계 쿼리/캐시 경로 | `DashboardPerformanceStoryTest` 및 테스트 결과 XML | `04-dashboard-stats.md` |
| 인덱스 튜닝 | Flyway migration `V5__add_performance_indexes_for_dashboard_and_notepad.sql` | MySQL EXPLAIN 전/후 비교 | `05-index-tuning-dashboard-notepad.md`, `02-mysql-validation-report.md` |
| Redis refresh 최적화 | `refresh:{email}` 단일 키, set+TTL 규칙 | 재발급 성공/실패 시나리오, TTL 검증 케이스 | `06-redis-jwt.md`, `07-redis-adoption-story-script.md` |

## 로그/리포트 파일

- `build/test-results/test/TEST-com.erp.performance.NotepadPerformanceStoryTest.xml`
- `build/test-results/test/TEST-com.erp.performance.DashboardPerformanceStoryTest.xml`
- `build/k6-summary.json`

## 면접 답변 연결 문장

- "각 개선은 문서만 있는 게 아니라 테스트 클래스와 결과 파일로 바로 역추적 가능합니다."
- "MySQL EXPLAIN 캡처와 Flyway migration까지 연결해 DB 레벨 근거도 보유하고 있습니다."
