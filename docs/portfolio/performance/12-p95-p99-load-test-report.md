# p95/p99 Load Test Report

## 목적

부하 테스트 가이드(`11-load-test-k6.md`)와 별도로,
실제 실행 결과를 버전 단위로 누적 기록하기 위한 리포트입니다.

## 테스트 버전

- 날짜: 2026-02-14
- 스크립트: `scripts/performance/k6-auth-notepad-dashboard.js`
- 결과 파일: `build/k6-summary.json`

## 시나리오

- `notepad_list`: VU 10 / 30s
- `dashboard_stats`: VU 5 / 30s

## 결과 요약

| Scenario | Avg | p95 | p99 | Error Rate | 비고 |
|---|---:|---:|---:|---:|---|
| Notepad list | 20.72ms | 45.32ms | - | 0.00% | 목록 API 안정적 |
| Dashboard stats | 12.46ms | 27.88ms | - | 0.00% | 통계 API 안정적 |
| 전체 `http_req_duration` | - | 294.44ms | - | 0.00% | 로그인/혼합 트래픽 포함 |

> 현재 수집 데이터에 p99가 없으면 `-`로 기록하고, 다음 실행부터 p99를 필수 수집합니다.

## 해석

- Notepad/Dashboard 단일 시나리오의 p95는 낮은 수준으로 유지
- 전체 지표 p95가 상대적으로 큰 이유는 인증/혼합 요청이 포함되기 때문
- 다음 단계는 시나리오별 p99와 인증 경로 p95 분리 리포팅

## 다음 실행 체크리스트

- [ ] p99 지표 출력 포함
- [ ] warm-up 구간 제외 기준 명시
- [ ] 같은 데이터셋으로 전/후 비교
- [ ] 실패 요청 샘플(응답코드/엔드포인트) 첨부
