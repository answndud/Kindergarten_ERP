# Docs Index

이 폴더는 현재 로컬 기준의 문서 운영 SSOT입니다.
active 문서와 완료 archive를 분리해, 다음 세션에서도 바로 이어서 작업할 수 있게 유지합니다.

## Start Here

새 세션에서 먼저 읽는 순서는 아래와 같습니다.

1. `docs/PLAN.md`
2. `docs/PROGRESS.md`
3. `docs/guides/developer-guide.md`
4. `docs/guides/env-contract.md`
5. `docs/COMPLETED.md` (과거 맥락이 필요할 때만)

## Active Docs

- `docs/PLAN.md`
  - 현재 active 계획과 우선순위
- `docs/PROGRESS.md`
  - 현재 상태, blocker, 최근 검증, 다음 액션
- `docs/COMPLETED.md`
  - 완료된 작업의 상세 archive

## Guides

- `docs/guides/developer-guide.md`
  - 개발/구조/검증/문서화 기준
- `docs/guides/env-contract.md`
  - local/demo/prod 실행 환경 변수와 안전한 기본값 계약
- `docs/guides/user-guide.md`
  - 역할별 사용 흐름
- `docs/guides/deployment-guide.md`
  - 초보자용 배포 절차와 운영 자산 설명

## Rules

- active 작업은 `docs/PLAN.md`, `docs/PROGRESS.md`에만 남깁니다.
- 완료된 작업은 active 문서에 남기지 않고 `docs/COMPLETED.md`로 옮깁니다.
- 현재 로컬에 존재하지 않는 legacy 문서 트리는 SSOT로 취급하지 않습니다.
- 블로그 작업 SSOT는 별도로 루트의 `BLOG_PLAN.md`, `BLOG_PROGRESS.md`를 사용합니다.
