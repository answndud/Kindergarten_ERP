# PROGRESS.md

## 상태

- 이 문서는 현재 active 작업의 상태, blocker, 최근 검증, 다음 액션만 유지한다.
- 완료된 작업은 `docs/COMPLETED.md`로 archive하고 이 문서에 남기지 않는다.
- active 작업이 없으면 이 문서는 아래 상태만 유지한다.

## Active Progress

- 상태: Phase 1 운영 전환 리스크 감소 완료. 다음 실행 후보는 Phase 2 큰 서비스/워크플로우 구조 정리.
- Blocker: 없음.
- 최근 검증: `StartupSafetyValidatorTest`, `ObservabilityIntegrationTest`, `ManagementSurfaceOptInIntegrationTest`, `bootJar`, local/prod compose config, `git diff --check` 통과. prod CORS wildcard/non-HTTPS 차단과 `.env.prod.example` 기반 compose dry-run을 보강했다.
- 다음 액션: Phase 1 변경분을 commit/push한 뒤 Phase 2 service size/caller map 조사를 시작한다.
