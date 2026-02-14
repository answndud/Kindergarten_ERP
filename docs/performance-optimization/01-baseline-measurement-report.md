# Baseline Measurement Report

## 목적

성능 개선 전/후를 같은 조건에서 비교하기 위한 기준선(Baseline) 문서입니다.
면접/포트폴리오에서 "왜 이 수치를 신뢰할 수 있는지"를 설명할 때 기준 문서로 사용합니다.

## 측정 원칙

- 동일 시나리오, 동일 데이터, 동일 환경에서만 비교
- 최소 지표: `avg`, `p95`, `요청당 SQL queries`
- 정량 수치 없는 개선은 완료로 처리하지 않음

## 공통 환경

- 애플리케이션: Spring Boot (local profile)
- DB: MySQL 8(메인), H2(test 회귀 검증 보조)
- 캐시/인증: Redis (해당 시나리오만)
- 부하 도구: k6

## DB 기준 원칙

- 포트폴리오의 최종 성능 근거는 **MySQL 기준**으로 판단한다.
- H2 수치는 빠른 회귀 감지 용도로만 사용한다.
- 면접 답변은 "H2 결과 -> MySQL EXPLAIN/실측 검증" 순서로 제시한다.

## 시나리오 기준선 요약

| Scenario | Before avg | Before p95 | Before queries | After avg | After p95 | After queries | 근거 문서 |
|---|---:|---:|---:|---:|---:|---:|---|
| Notepad 목록 API | 15ms | 15ms | 22 | 4ms | 4ms | 4 | `03-notepad-readcount-nplusone.md` |
| Dashboard 통계 API | 14ms | 14ms | 13 | 2ms | 2ms | 10 | `04-dashboard-stats.md` |
| Dashboard 캐시 hit | 10ms | 10ms | 10 | 0ms | 0ms | 0 | `04-dashboard-stats.md` |
| Refresh Token 재발급 | 32ms | 78ms | 2 | 9ms | 21ms | 0 | `07-redis-adoption-story-script.md` |

## 데이터 볼륨 기준(포트폴리오 시나리오)

- Kindergarten 1
- Classroom 8
- Kid 240
- Notepad 8,000
- Attendance 120,000
- Member 500
- Notification 30,000

## 실행 로그 위치

- Notepad: `build/test-results/test/TEST-com.erp.performance.NotepadPerformanceStoryTest.xml`
- Dashboard: `build/test-results/test/TEST-com.erp.performance.DashboardPerformanceStoryTest.xml`
- k6 summary: `build/k6-summary.json`

## 운영 전 검증 체크

- H2 비교 수치와 MySQL EXPLAIN 결과가 논리적으로 일치하는가
- 캐시 미스 기준 성능도 임계치 이내인가
- p95 증가가 없는지 회귀 확인했는가

## MySQL 실측 보강 항목

- `slow query log` 전/후 건수
- 대표 쿼리 `EXPLAIN` 캡처(before/after)
- 피크 시간대 DB CPU 추이(가능 시)
