# Frontend Perceived Performance Metrics

## 목적

백엔드 지표(avg/p95/queries)와 별도로,
사용자가 실제로 체감하는 프론트 성능(TTFB/전환 시간)을 기록합니다.

## 측정 대상

- Notepad 목록 진입
- Dashboard 통계 조회
- 알림 드롭다운 갱신

## 측정 항목

- TTFB (Time To First Byte)
- 목록/통계 영역 갱신 완료 시간
- 전체 리로드 여부(Yes/No)
- 사용자 체감 지연(정성 메모)

## 측정 방법

1. Chrome DevTools Network에서 TTFB 기록
2. HTMX 요청 단위로 응답시간과 DOM 반영 시간 측정
3. 동일 시나리오 10회 반복 후 평균/상위값 기록

## 결과 템플릿

| Scenario | Before TTFB | After TTFB | Before UI Update | After UI Update | 비고 |
|---|---:|---:|---:|---:|---|
| Notepad list | - | - | - | - | |
| Dashboard stats | - | - | - | - | |
| Notifications dropdown | - | - | - | - | |

## 현재 관찰(정성)

- HTMX 부분 갱신으로 전체 페이지 리로드가 줄어 체감 지연 감소
- 스크롤 위치 유지/필터 전환 경험이 개선
- 알림 배지/목록 갱신의 즉시성이 향상

## 후속 과제

- Lighthouse/Web Vitals(LCP, INP) 측정 추가
- 프론트 체감 지표와 백엔드 p95를 같은 시나리오로 연결
