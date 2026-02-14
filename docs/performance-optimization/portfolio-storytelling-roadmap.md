# Portfolio Storytelling Roadmap (Spring Boot Backend)

## Why This Document

이 문서는 "처음엔 성능이 좋지 않았지만, 체계적으로 개선했다"는 포트폴리오 스토리를
실제 코드/지표/문서로 연결하기 위한 실행 로드맵입니다.

핵심은 "잘 만든 아키텍처"를 보여주는 것이 아니라,
"문제를 발견하고 재현하고 개선하고 검증한 개발자"를 보여주는 것입니다.

## One-Line Interview Message

"기능 구현 후 성능 문제가 생겼고, p95/쿼리 수로 병목을 확인한 뒤,
N+1 제거와 집계 쿼리/캐시/인덱스 튜닝을 단계적으로 적용해
동일 시나리오에서 응답 시간을 유의미하게 줄였습니다."

## Execution Order (Recommended)

1. Baseline 계측 환경 만들기
2. Notepad 목록/읽음 처리 N+1 개선
3. Dashboard 통계 집계 최적화
4. Auth refresh token Redis 접근 최적화
5. 인덱스 튜닝 + Flyway migration
6. 문서/면접 스크립트 정리

## Current Progress (2026-02-14)

- [x] 성능 스토리텔링 로드맵 문서화
- [x] Notepad N+1 개선 1차 코드 반영
- [x] Baseline/After 정량 측정값 채우기 (Notepad, Dashboard)
- [x] Dashboard 통계 최적화 1차 적용
- [x] Redis refresh token 조회 경로 개선
- [x] 인덱스 튜닝 migration + EXPLAIN 비교
- [x] k6 부하 테스트 스크립트/가이드 추가

## Phase 0. Baseline (반드시 먼저)

### Scenario 정의

- 시나리오 A: 학부모 알림장 목록 진입 (`/notepad` 연관 API)
- 시나리오 B: 원장 대시보드 통계 조회
- 시나리오 C: 로그인 후 토큰 갱신(재발급)

### Data Volume 기준

- Kindergarten 1개
- Classroom 8개
- Kid 240명
- Notepad 8,000건
- Attendance 120,000건
- Member 500명
- Notification 30,000건

### Measurement 항목

- 평균 응답 시간 (avg)
- p95 응답 시간
- 요청당 SQL 쿼리 수
- (선택) DB CPU / slow query 개수

### Tooling

- 부하: k6 또는 JMeter
- 서버 로그: Hibernate SQL + 실행 시간
- DB: MySQL slow query log / EXPLAIN

## Phase 1. Notepad N+1 개선 (가장 좋은 대표 사례)

### Problem Signal

- `NotepadService`에서 목록 매핑 중 읽음 수 조회가 건별 반복될 가능성
- 학부모용 목록 구성 시 Kid별 Parent lookup이 반복될 가능성

### Expected Fix

- 읽음 수를 건별 조회 대신 집계 쿼리로 일괄 조회
- Parent 대상 계산을 루프성 repository 호출에서 배치 조회로 변경
- 필요한 연관 엔티티는 `EntityGraph` 또는 명시적 fetch 전략으로 통제

### Portfolio Point

- "기능은 맞게 동작했지만 데이터가 늘면서 병목이 드러났다"
- "코드를 갈아엎지 않고 조회 경로만 바꿔서 성능을 올렸다"

## Phase 2. Dashboard 통계 최적화

### Problem Signal

- 기간별 통계 계산 시 리스트 로딩 + 자바 계산 비중이 큼
- 요청당 count 쿼리 다발 + 메모리 계산 증가

### Expected Fix

- DB 집계 쿼리 우선(합계/건수/비율)
- 필요한 경우 1~5분 TTL 캐시 적용
- 날짜 경계/타임존 고정으로 재현 가능성 확보

### Portfolio Point

- "비즈니스 KPI 화면에서 서버 계산 비용을 줄여 p95를 안정화"

## Phase 3. Auth + Redis 개선

### Problem Signal

- refresh token 검증 과정에서 Redis key 접근 방식이 비효율적일 수 있음
- 키 설계가 조회 비용과 운영 복잡도에 영향

### Expected Fix

- 키 네이밍 단순화 및 직접 키 조회 기반으로 변경
- 토큰 저장/삭제 흐름의 O(1) 접근 보장

### Portfolio Point

- "보안 기능에서도 성능/운영성을 함께 최적화"

## Phase 4. Index Tuning + Migration

### Problem Signal

- 정렬/필터 조건과 인덱스 구성이 맞지 않으면 풀스캔/파일소트 발생

### Expected Fix

- 실제 느린 쿼리 기준 복합 인덱스 설계
- Flyway migration으로 이력 관리
- 개선 전/후 EXPLAIN 비교 첨부

### Portfolio Point

- "코드 최적화에서 멈추지 않고 DB 실행 계획까지 확인"

## Measurement Template (Copy & Fill)

| Scenario | Before avg | Before p95 | Before queries | After avg | After p95 | After queries | Delta |
|---|---:|---:|---:|---:|---:|---:|---:|
| Notepad list (parent) | - | - | - | - | - | - | - |
| Dashboard stats | - | - | - | - | - | - | - |
| Token refresh | - | - | - | - | - | - | - |

## Document Writing Template (per optimization)

각 문서는 아래 섹션을 동일하게 유지합니다.

1. 문제 재현 시나리오
2. 개선 전 측정값
3. 원인 분석
4. 개선 내용 (코드/쿼리/인덱스)
5. 개선 후 측정값
6. 트레이드오프
7. 면접 예상 질문 3~5개

## Interview Script Tips

- 숫자를 먼저 말한다. ("p95가 1.8s에서 620ms로 감소")
- 원인을 한 줄로 말한다. ("목록 N+1로 쿼리 수가 폭증")
- 해결은 두 단계로 말한다. ("조회 경로 정리 + 집계 쿼리")
- 트레이드오프를 인정한다. ("쿼리는 복잡해졌지만 유지보수는 문서화로 보완")

## Done Criteria

아래 4개를 만족하면 "포트폴리오용 성능 개선 완료"로 본다.

- 전/후 수치가 표로 남아 있음
- 코드 변경 이유가 문서에 연결되어 있음
- 트레이드오프/한계가 명시되어 있음
- 면접용 1분 요약 스크립트가 작성되어 있음
