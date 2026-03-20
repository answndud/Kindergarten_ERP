# Docs Index

이 폴더는 "현재 바로 써야 하는 문서"와 "기록으로 남겨야 하는 문서"를 분리한 문서 인덱스입니다.

## Start Here

처음 보는 사람은 아래 순서로 읽으면 됩니다.

1. `README.md`
2. `docs/portfolio/hiring-pack/backend-hiring-pack.md`
3. `docs/portfolio/architecture/system-architecture.md`
4. `docs/portfolio/demo/demo-preflight.md`
5. `docs/portfolio/demo/demo-runbook.md`
6. `docs/portfolio/case-studies/auth-incident-response.md`

## Active Docs

### Guides

- `docs/guides/developer-guide.md`
  - 개발/구조/테스트/문서화 규칙
- `docs/guides/user-guide.md`
  - 역할별 사용 흐름

### Portfolio

- `docs/portfolio/hiring-pack/`
  - 채용 담당자/면접관용 진입 문서
- `docs/portfolio/architecture/`
  - 현재 구조를 설명하는 아키텍처 SSOT
- `docs/portfolio/demo/`
  - 시연 전 체크리스트와 실제 데모 runbook
- `docs/portfolio/case-studies/`
  - auth incident response 같은 대표 사례
- `docs/portfolio/interview/`
  - 인터뷰 1장 요약, 데모 스크립트, Q&A 스피킹 노트
- `docs/portfolio/performance/`
  - 성능 개선 스토리, 측정 리포트, 면접용 성능 자료

### Decisions

- `docs/decisions/`
  - 기능/보안/테스트/운영 관련 결정 로그
  - 최신 개선 흐름은 `phase14` 이후 문서를 우선 보면 됩니다.
  - 최근 배치는 `phase44_tagged_ci_readiness_and_hiring_pack.md`까지 이어집니다.
  - 인증 이상 징후/모니터링/외부 incident 전달 흐름은 `phase37 -> phase40` 순서로 읽는 것을 권장합니다.
  - 운영형 워크플로우 확장은 `phase39 -> phase40 -> phase41 -> phase42 -> phase43 -> phase44` 순서로 읽는 것을 권장합니다.

## Archive

현재 운영 SSOT는 아니지만 보존 가치가 있는 문서는 archive로 이동했습니다.

- `docs/archive/legacy/`
  - 초기 설계서, 개발 일지, 과거 계획서, 튜토리얼
- `docs/archive/requirements/`
  - 초기 TODO/요구사항 메모
- `docs/archive/retrospectives/`
  - 트러블슈팅, 회고, 디버깅 기록

## Rules

- 새 기능/정책 결정 로그: `docs/decisions/`
- 성능 최적화 스토리: `docs/portfolio/performance/`
- 인터뷰 자료: `docs/portfolio/interview/`
- 더 이상 SSOT가 아닌 참고 기록: `docs/archive/`
