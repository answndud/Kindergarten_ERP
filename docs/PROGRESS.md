# PROGRESS.md

## 상태

- 이 문서는 현재 active 작업의 상태, blocker, 최근 검증, 다음 액션만 유지한다.
- 완료된 작업은 `docs/COMPLETED.md`로 archive하고 이 문서에 남기지 않는다.
- active 작업이 없으면 이 문서는 아래 상태만 유지한다.

## Active Progress

- 상태: Phase 4 production-like 실행 증거 확보 완료. 다음 실행 후보는 Phase 5 기능 완성도 확장.
- Blocker: 없음.
- 최근 검증: Phase 3 tranche를 commit/push했다. `docs/guides/production-like-checklist.md`를 추가해 prod safety, observability, bootJar, local/prod compose config dry-run 명령과 기대 결과를 고정했다.
- 다음 액션: Phase 4 변경분을 commit/push한 뒤 Phase 5 후보 중 outbox 운영 기능 확장을 1개 수직 slice로 선정한다.
