# PLAN.md

## 작업명
- 후속 고도화 마감 점검 (원격 CI 확인 + GitHub Actions Node24 호환)

## 1) 목표 / 범위
- 방금 push한 split CI workflow가 GitHub Actions에서 실제로 통과하는지 확인한다.
- 통과 후 runner annotation으로 확인된 Node.js 20 deprecation 경고를 제거해 Node 24 기본 전환 전에 workflow 호환성을 확보한다.
- 필요 시 workflow와 문서를 보강하고, YAML/로컬 검증까지 마친다.

## 2) 세부 작업 단계
1. 원격 CI 실행 확인
   - `ff5b683` 기준 GitHub Actions run 상태 확인
   - job별 결과 및 artifact 업로드 여부 확인

2. Node24 호환 반영
   - GitHub runner annotation 기준으로 Node 20 deprecation 대응
   - workflow env 또는 action 버전 조정으로 Node 24 경로 확보

3. 검증 및 기록
   - YAML 파싱 검증
   - `PROGRESS.md`에 원격 run 결과와 후속 조치 기록

## 3) 검증 계획
- GitHub Actions run 상태 확인
- 로컬 검증
  - `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml')"`
  - `git diff --check`
- 후속 push 시 새 workflow run 재확인

## 4) 리스크 및 대응
- Node 24 강제 전환 대응이 현재 action 조합과 충돌할 위험
  - 대응: 먼저 runner가 안내한 공식 env 플래그를 적용하고, 이후 action major 업그레이드는 별도 배치로 분리
- workflow 변경 후 경고는 줄어도 기능이 깨질 위험
  - 대응: YAML 파싱과 원격 run 재확인까지 한 세트로 검증
