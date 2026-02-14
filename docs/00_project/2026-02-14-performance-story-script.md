# 2026-02-14 성능 개선 스크립트 (면접/발표용)

## 30초 버전

처음에는 기능 구현을 우선해서 알림장/대시보드 API가 데이터 증가 시 느려질 수 있는 구조였습니다.
그래서 먼저 같은 시나리오로 성능을 측정했고, N+1 제거, 집계 쿼리 통합, Redis 토큰 조회 경로 단순화,
그리고 인덱스 튜닝까지 단계적으로 적용했습니다.
결과적으로 알림장 목록은 쿼리 수가 22에서 4로 줄었고, 대시보드 통계는 13에서 10으로 줄면서
응답 시간도 14ms에서 2ms 수준으로 개선됐습니다.

## 1분 버전

이 프로젝트는 처음부터 완벽한 성능을 목표로 하지 않았고,
"기능을 만들고, 병목을 찾아서, 근거 있게 개선한다"는 흐름 자체를 포트폴리오로 설계했습니다.

첫 번째 병목은 알림장 목록에서 읽음 수를 건별로 조회하던 N+1 패턴이었습니다.
페이지 결과 N건마다 읽음 조회가 반복되어 쿼리 수가 선형으로 늘어났고,
읽음 수 집계를 다건 쿼리 1회로 바꿔서 22 -> 4 쿼리로 줄였습니다.

두 번째는 대시보드 통계였습니다.
기존에는 출석/공지 계산 경로가 분산되어 있었는데,
출석은 PRESENT+LATE 단일 집계 쿼리로 통합하고 공지는 SUM(view_count)로 전환해
통계 API의 쿼리 수를 13 -> 10으로 줄였습니다.
동일 테스트 시나리오에서 응답 시간은 14ms -> 2ms로 확인했습니다.

세 번째는 인증 경로입니다.
Redis refresh token 접근에서 패턴 키 검색을 제거하고
`refresh:{email}` 단일 키 O(1) 조회/삭제 경로로 정리했습니다.
성능뿐 아니라 운영 안정성 측면에서도 이점이 있습니다.

마지막으로 실행계획(EXPLAIN)을 기준으로 인덱스를 보강했습니다.
Notepad/Announcement는 ALL + filesort에서 ref/index 기반으로,
Attendance 집계는 ALL에서 range 스캔으로 바꿔서
"코드 개선 + DB 실행계획 개선"까지 연결된 스토리로 마무리했습니다.

## 기술 질문 대응 포인트

### Q1. 왜 캐시보다 N+1 제거를 먼저 했나요?

캐시는 증상을 완화하지만 근본 원인은 조회 경로였습니다.
먼저 N+1을 제거해서 기본 비용을 낮춘 뒤, 필요할 때 캐시를 올리는 순서가 안전하다고 판단했습니다.

### Q2. 인덱스는 어떤 기준으로 추가했나요?

실제 where/order/join 패턴을 기준으로 잡았습니다.
특히 목록 API 정렬 조건과 대시보드 집계 조건에 맞춘 복합 인덱스로 설계했고,
EXPLAIN에서 ALL/filesort 제거 여부를 확인했습니다.

### Q3. 트레이드오프는 뭐였나요?

인덱스가 늘면서 쓰기 비용과 저장공간이 늘어납니다.
하지만 이 시스템은 조회 트래픽이 더 중요해 read 최적화를 우선했고,
추후에는 slow query log 기반으로 주기적으로 인덱스를 정리할 계획입니다.

## 실제 수치 요약

- Notepad 목록: queries 22 -> 4, elapsed 15ms -> 4ms
- Dashboard 통계: queries 13 -> 10, elapsed 14ms -> 2ms

## 관련 문서

- `docs/performance-optimization/notepad-readcount-nplusone.md`
- `docs/performance-optimization/dashboard-stats.md`
- `docs/performance-optimization/redis-jwt.md`
- `docs/performance-optimization/index-tuning-dashboard-notepad.md`
- `docs/performance-optimization/portfolio-storytelling-roadmap.md`
