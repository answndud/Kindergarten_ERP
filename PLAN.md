# PLAN.md

## 작업명
- 후속 고도화 3차 (GitHub Actions Node24 네이티브 전환)

## 1) 목표 / 범위
- 최신 `main` CI run 통과 상태를 확인하고 잔여 경고를 정리한다.
- GitHub Actions workflow에서 Node 20 deprecation annotation을 발생시키는 action 버전을 Node24 네이티브 버전으로 올린다.
- README와 `docs/phase/`에 변경 이유와 면접 설명 포인트를 함께 남긴다.

## 2) 세부 작업 단계
1. 원격 CI 재확인
   - `4915d4c` 기준 GitHub Actions run 상태 및 annotation 확인
   - 경고 재현 여부를 근거로 다음 조치 범위 확정

2. Workflow action 버전 정비
   - `checkout`, `setup-java`, `setup-gradle`, `upload-artifact`의 최신 안정 major 검토
   - workflow를 Node24 네이티브 action 기준으로 갱신

3. 문서/기록 반영
   - README와 `docs/phase/`에 조치 배경, 결과, 면접 답변 포인트 정리
   - `PROGRESS.md`에 원격 run 결과와 후속 리스크 기록

4. 로컬 검증
   - workflow YAML 파싱
   - `git diff --check`

## 3) 검증 계획
- 원격 검증
  - `gh run view 23046637472 --repo answndud/kindergarten_ERP`
- 로컬 검증
  - `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml')"`
  - `git diff --check`

## 4) 리스크 및 대응
- action major 업그레이드 시 입력 파라미터나 기본 동작이 달라질 수 있음
  - 대응: 기존 사용 옵션과 release note 범위만 활용하고, workflow 구조는 최소 수정
- 로컬에서는 remote runner annotation 재현이 불가능함
  - 대응: 로컬에서는 YAML/format만 검증하고, push 후 GitHub Actions run으로 최종 확인
- 일부 action이 여전히 경고를 남길 수 있음
  - 대응: 공식 release 기준으로 올릴 수 있는 action만 먼저 올리고, 남는 항목은 후속 이슈로 분리
