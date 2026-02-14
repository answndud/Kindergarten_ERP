# Performance Optimization Portfolio

## Overview

이 폴더는 유치원 ERP의 성능 개선을 "포트폴리오 스토리"로 설명하기 위한 문서 모음입니다.
핵심 메시지는 아래 1문장으로 일관되게 유지합니다.

> "처음엔 느렸고(문제 재현 + 수치), 원인을 좁혀서(분석), 설계를 바꿨고(개선), 다시 측정해 실제로 빨라졌다(전/후 비교)."

## Storytelling First Rule

성능 문서는 기술 나열보다 "문제 해결 과정"이 먼저 보이도록 작성합니다.

1. 재현 시나리오 정의 (누가, 어떤 화면/API, 어느 데이터 규모)
2. 개선 전 측정 (응답 시간 + 쿼리 수)
3. 원인 분석 (N+1, 불필요한 full scan, 중복 집계 등)
4. 개선안 적용 (코드/쿼리/인덱스/캐시)
5. 개선 후 동일 조건 재측정
6. 트레이드오프와 차후 과제 정리

## Documents

- `portfolio-storytelling-roadmap.md`: 포트폴리오용 성능 개선 로드맵(우선순위/측정표/면접 포인트)
- `notepad-readcount-nplusone.md`: 알림장 목록 N+1 개선 1차 적용 기록
- `index-tuning-dashboard-notepad.md`: Dashboard/Notepad 인덱스 튜닝 및 EXPLAIN 비교
- `load-test-k6.md`: k6 기반 동시성 부하 테스트 가이드
- `notifications.md`: 알림 시스템 성능/UX 개선 요약
- `list-loading.md`: 목록/HTMX 로딩 전략 개선
- `dashboard-stats.md`: 대시보드 통계 계산 개선
- `redis-jwt.md`: Redis + JWT 쿠키 기반 인증 성능/보안 선택 이유
- `jpa-fetching.md`: OSIV off, batch fetch, QueryDSL 선택 이유
- `soft-delete-flyway.md`: Soft delete, Flyway 도입 이유와 성능 관점
- `frontend-ssr-htmx.md`: SSR + HTMX + Alpine.js 선택 이유

## Recommended Reading Order

1. `portfolio-storytelling-roadmap.md`로 전체 전략/순서를 먼저 이해
2. 기능별 문서(`list-loading.md`, `dashboard-stats.md`, `notifications.md`)로 개선 사례 확인
3. 기반 설계 문서(`jpa-fetching.md`, `redis-jwt.md`, `soft-delete-flyway.md`)로 선택 이유 정리
4. 각 문서의 예상 질문/답변을 면접 스크립트로 압축

## Measurement Policy (Mandatory)

- 같은 시나리오/같은 데이터/같은 환경에서 전/후를 비교한다.
- 최소 지표: `p95 응답 시간`, `평균 응답 시간`, `요청당 쿼리 수`.
- 수치는 문서에 표 형태로 남긴다.
- 정량 수치가 없는 성능 개선은 "후보"로만 기록하고 "완료"로 표시하지 않는다.

## Update Checklist

성능 개선 PR 또는 작업 단위 완료 시 아래를 반드시 갱신합니다.

- 개선 대상 문서 (예: `list-loading.md`)
- 로드맵 현황 (`portfolio-storytelling-roadmap.md`)
- 필요 시 트러블슈팅 (`docs/retrospective/`)
