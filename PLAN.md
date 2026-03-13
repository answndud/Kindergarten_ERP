# PLAN.md

## 작업명
- 후속 고도화 17차 (`docs/` 정보 구조 개편)

## 1) 목표 / 범위
- `docs/` 하위 문서를 active/portfolio/archive 기준으로 다시 분류한다.
- 루트의 혼재된 문서를 `guides`, `portfolio`, `decisions`, `archive` 구조로 정리한다.
- `README.md`, `docs/README.md`, `AGENTS.md`, 개발 가이드가 새 경로를 SSOT로 가리키게 맞춘다.
- 더 이상 현재 문서가 아닌 설계서/일지/튜토리얼/초기 TODO는 archive로 이동한다.

## 2) 세부 작업 단계
1. 새 문서 체계 확정
   - active docs: `docs/guides`, `docs/portfolio`, `docs/decisions`
   - archive docs: `docs/archive`

2. 실제 파일 이동
   - `docs/interview` -> `docs/portfolio/interview`
   - `docs/performance-optimization` -> `docs/portfolio/performance`
   - `docs/phase` -> `docs/decisions`
   - 루트 문서와 `requirements`, `retrospective`는 archive 또는 guides로 이동

3. 진입점/규칙 수정
   - `docs/README.md` 인덱스 추가
   - `README.md`, `AGENTS.md`, `docs/guides/developer-guide.md` 경로 갱신

4. 검증 및 배포
   - `git diff --check`
   - add/commit/push
   - GitHub Actions run 시작 여부 확인

## 3) 검증 계획
- 문서 검증
  - `git diff --check`

## 4) 리스크 및 대응
- 문서 이동 후 링크가 깨질 수 있음
  - 대응: `README.md`, `docs/README.md`, `AGENTS.md`, 개발 가이드의 active 링크를 우선 갱신한다
- archive로 이동한 문서를 실수로 SSOT로 다시 참조할 수 있음
  - 대응: `docs/README.md`에서 active docs와 archive docs를 명시적으로 분리한다
- 향후 에이전트가 예전 폴더를 계속 사용할 수 있음
  - 대응: `AGENTS.md`의 문서화 규칙을 새 경로 기준으로 갱신한다
