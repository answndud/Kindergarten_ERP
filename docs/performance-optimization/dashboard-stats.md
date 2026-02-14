# 대시보드 통계 계산 최적화

## 문제
- 날짜 기준이 브라우저 로컬 시간에 의존해 통계가 어긋났다.
- 오늘 기준만 제공돼 최근 7일/30일 흐름을 파악하기 어려웠다.
- 통계 계산에서 목록 로딩(원생/공지) + 다중 count 쿼리가 발생해 요청당 쿼리 수가 많았다.

## 개선
- 기준 시간을 KST(Asia/Seoul)로 고정.
- 최근 7일/30일 토글을 추가해 기간별 집계 제공.
- 승인 대기는 현행 유지(현재 대기 건수), 나머지는 기간 필터로 집계.
- 출석률 계산을 `PRESENT + LATE` 단일 집계 쿼리로 통합.
- 공지 열람률 계산을 공지 목록 로딩 대신 `SUM(view_count)` 집계 쿼리로 전환.
- 원생 목록 로딩 대신 전체 원생 수 count를 재사용해 계산 비용 축소.
- 대시보드 통계 결과에 60초 TTL 캐시(`dashboardStatistics`)를 적용.
- 출석/공지 쓰기 경로에서 대시보드 캐시를 선택적으로 무효화해 정합성 유지.

## 효과(정성)
- 날짜 경계 혼선이 줄어 통계 신뢰도 상승.
- 사용자에게 단기/중기 흐름을 빠르게 제공.
- 같은 통계 화면에서 서버 계산 경로가 단순해져 응답시간 변동폭이 감소.

## 효과(정량)

- 실행 일시: 2026-02-14
- 테스트: `com.erp.performance.DashboardPerformanceStoryTest`
- DB: H2(test profile)

| Scenario | Before queries | After queries | Before elapsed | After elapsed |
|---|---:|---:|---:|---:|
| Dashboard statistics aggregation | 13 | 10 | 14ms | 2ms |
| Dashboard statistics (cache miss -> hit) | 10 | 0 | 10ms | 0ms |

> 측정 로그: `build/test-results/test/TEST-com.erp.performance.DashboardPerformanceStoryTest.xml`

## 정합성 검증

- `DashboardPerformanceStoryTest.dashboardCacheEvictedOnAttendanceWrite`
  - 출석 쓰기 후 `todayAttendanceCount`가 즉시 갱신되는지 검증
- `DashboardPerformanceStoryTest.dashboardCacheEvictedOnAnnouncementWrite`
  - 공지 생성 후 `totalAnnouncements`가 즉시 갱신되는지 검증

## 선택 이유
- 사용자/운영 기준 시간대(KST)를 명확히 해 데이터 일관성을 확보.
- 간단한 기간 토글로 추가 UI/UX 비용을 줄임.
- "화면 로직은 유지하고, 집계 경로만 교체"해 리스크를 낮추면서 성능을 개선.
- 반복 조회가 많은 대시보드 특성에 맞춰 짧은 TTL 캐시를 적용해 피크 구간 부하를 완화.

## 예상 질문/답변
1) Q: 왜 기간 필터를 7일/30일로 제한했나요?
   A: 사용 빈도가 높은 단기/중기 범위를 우선 제공하고, 복잡도를 낮추기 위해서입니다.
2) Q: 출석률 계산이 느려지지 않나요?
   A: 출석 상태를 단일 집계 쿼리로 통합해 요청당 쿼리 수를 줄였습니다.
3) Q: 승인 대기는 왜 기간 필터를 적용하지 않나요?
   A: 승인 대기는 현재 처리해야 할 건수를 보여주는 것이 목적이기 때문입니다.
4) Q: KST 기준을 고정한 이유는?
   A: 운영 환경이 한국이므로 일관된 날짜 경계를 제공하기 위해서입니다.
5) Q: 향후 확장 계획은?
   A: TTL 캐시를 도입해 피크 시간대 통계 호출을 추가 최적화할 계획입니다.
