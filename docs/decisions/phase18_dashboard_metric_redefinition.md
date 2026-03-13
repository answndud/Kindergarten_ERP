# Phase 18: 대시보드 지표 산식 보정

## 배경

- 기존 출석률은 `전체 원생 수 x 일수`를 분모로 사용해 주말과 입소 전 기간까지 포함했다.
- 기존 공지 열람률은 `sum(viewCount) / totalMembers`라서 같은 사용자가 여러 번 열어도 비율이 올라갔다.
- 이 상태의 숫자는 "보여주기용 지표"는 되지만, 운영 지표로 설명하기 어렵다.

## 목표

1. 출석률 분모를 실제 등원 가능 인원/일 기준으로 보정한다.
2. 공지 열람률을 고유 열람 기준으로 바꿔 100% 초과와 중복 집계 왜곡을 제거한다.
3. 정확도 개선 후에도 대시보드 집계 쿼리 수를 관리 가능한 수준으로 유지한다.

## 결정

1. 출석률은 `활성 원생-학교일(active kid-school days)`을 분모로 재정의했다.
   - 주말 제외
   - `admissionDate` 이전 제외
   - `deletedAt` 이후 제외
2. 공지 열람률은 `고유 구성원-공지 열람(unique member-announcement reads)`을 분자로 재정의했다.
   - 분모: `활성 구성원 수 x 활성 공지 수`
   - 분자: `announcement_view`의 unique row 수
3. 정확도 개선으로 쿼리가 늘어나지 않도록 집계 쿼리를 다시 묶었다.

## 구현 요약

### 1) 출석률

- `KidRepository.findDashboardKidSummaries`
  - 입소일/삭제일만 가져오는 경량 projection
- `AttendanceRepository.findPresentOrLateCountsByKindergartenAndDateBetween`
  - 30일 범위 present/late를 날짜별로 한 번에 집계
- `DashboardService`
  - 7일/30일 출석률을 동일한 집계 결과에서 재사용
  - weekday 기준으로만 분모/분자를 계산

### 2) 공지 고유 열람률

- `announcement_view` 테이블 추가
  - `(announcement_id, viewer_id)` unique constraint
- `AnnouncementService`
  - 공지 상세 조회 시 raw `viewCount`는 유지
  - 고유 열람은 `announcement_view`에 1회만 기록
- `AnnouncementRepository.findDashboardStats`
  - 총 공지 수와 고유 열람 수를 한 번에 집계

### 3) UI/문구 정리

- 대시보드 카드를 `공지 열람률`에서 `공지 고유 열람률`로 변경
- 면접에서 "왜 100%를 넘지 않는가"를 명확하게 설명할 수 있게 정리

## 검증

- `DashboardApiIntegrationTest`
  - 같은 학부모의 중복 조회는 1회로만 집계되는지 검증
  - 주말/입소 전 기간이 출석률 분모에서 제외되는지 검증
- `DashboardPerformanceStoryTest`
  - 레거시 대비 집계 쿼리 수: `13 -> 5`
  - 첫 조회 기준 응답 시간: `30ms -> 9ms`
  - 캐시 hit 시 쿼리 수: `5 -> 0`

## 인터뷰 답변 포인트

### 왜 출석률 분모를 바꿨는가

- KPI는 계산 가능한 것보다 신뢰 가능한 것이 중요하다.
- 주말과 입소 전 기간을 분모에 넣으면 숫자는 나오지만 의사결정에 쓰기 어렵다.

### 왜 공지 조회수를 그대로 열람률에 쓰지 않았는가

- 조회수는 관심도 지표이고, 열람률은 도달률 지표다.
- 둘을 같은 값으로 쓰면 같은 사람이 여러 번 연 경우 비율이 왜곡된다.

### 정확도 개선이 성능 악화로 이어지지 않았는가

- 단순히 계산식을 복잡하게 만든 게 아니라 projection과 집계 쿼리 통합으로 다시 최적화했다.
- 그래서 이번 변경은 "정확도 개선"과 "쿼리 수 절감"을 같이 보여주는 사례가 된다.

## 트레이드오프

- 장점
  - 대시보드 숫자를 운영 지표로 설명할 수 있다
  - 공지 열람률이 100%를 넘지 않는다
  - 성능 스토리와 정확도 스토리를 동시에 확보했다
- 단점
  - `announcement_view` 저장 공간이 추가된다
  - 휴원일/공휴일은 아직 별도 캘린더와 연동하지 않았다
