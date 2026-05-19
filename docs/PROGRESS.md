# PROGRESS.md

## 상태

- 이 문서는 현재 active 작업의 상태, blocker, 최근 검증, 다음 액션만 유지한다.
- 완료된 작업은 `docs/COMPLETED.md`로 archive하고 이 문서에 남기지 않는다.
- active 작업이 없으면 이 문서는 아래 상태만 유지한다.

## Active Progress

- 상태: Phase 2 첫 tranche 완료. 다음 실행 후보는 Phase 3 테스트 품질 강화.
- Blocker: 없음.
- 최근 검증: Phase 1 변경분을 commit/push했다. service size/caller map 조사 결과 `KidApplicationService`가 포트폴리오 핵심 workflow이면서 review 상태 전이 orchestration을 직접 들고 있어 첫 리팩토링 tranche로 선정했다. 승인/대기/제안/수락/거절 workflow를 `KidApplicationReviewService`로 추출했고 `compileJava compileTestJava`, `KidApplicationApiIntegrationTest`가 통과했다.
- 다음 액션: Phase 2 tranche를 commit/push한 뒤, Phase 3에서 방금 분리한 입학 신청 workflow의 테스트 gap을 우선 점검한다.
