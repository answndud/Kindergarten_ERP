# PROGRESS.md

## 상태

- 이 문서는 현재 active 작업의 상태, blocker, 최근 검증, 다음 액션만 유지한다.
- 완료된 작업은 `docs/COMPLETED.md`로 archive하고 이 문서에 남기지 않는다.
- active 작업이 없으면 이 문서는 아래 상태만 유지한다.

## Active Progress

- 상태: Phase 5 기능 완성도 확장 완료. 다음 실행 후보는 Phase 6 최종 정리와 제출 패키지 고정.
- Blocker: 없음.
- 최근 검증: `/api/v1/notification-outbox/dead-letters?channel=EMAIL` 계약, repository query, 화면 채널 필터, 통합 테스트를 보강했다. `./gradlew test --tests "com.erp.api.NotificationOutboxOpsApiIntegrationTest"`와 `./gradlew compileJava compileTestJava`가 통과했다.
- 다음 액션: Phase 5 변경분을 commit/push한 뒤 Phase 6에서 README/evidence/risk/interview guide의 과장 또는 stale 표현을 최종 정리한다.
