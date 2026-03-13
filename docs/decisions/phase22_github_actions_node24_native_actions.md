# Phase 22: GitHub Actions Node24 네이티브 Action 전환

## 배경

- `4915d4c` push 후 GitHub Actions run `23046637472`은 성공했다.
- 하지만 두 job 모두 runner annotation으로 Node.js 20 deprecation 경고가 계속 남았다.
- 이전 배치에서 추가한 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true`는 opt-in 경로 확보에는 의미가 있었지만, annotation 자체를 제거하지는 못했다.

## 목표

1. GitHub Actions workflow를 Node24 네이티브 action major 기준으로 정리한다.
2. 임시 opt-in 환경변수 의존도를 없애고, workflow 자체를 장기 유지 가능한 상태로 만든다.
3. 면접에서 "경고를 봤고, 원인을 좁히고, 임시 대응 후 구조적 대응까지 마쳤다"는 스토리를 설명 가능하게 만든다.

## 결정

1. 아래 action major를 최신 안정 major로 올린다.
   - `actions/checkout@v5`
   - `actions/setup-java@v5`
   - `gradle/actions/setup-gradle@v5`
   - `actions/upload-artifact@v6`
2. `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24` 환경변수는 제거한다.
3. workflow 구조나 job 분리는 건드리지 않고, action runtime 호환성만 정리한다.

## 구현 요약

- `.github/workflows/ci.yml`
  - `actions/checkout@v4` -> `actions/checkout@v5`
  - `actions/setup-java@v4` -> `actions/setup-java@v5`
  - `gradle/actions/setup-gradle@v4` -> `gradle/actions/setup-gradle@v5`
  - `actions/upload-artifact@v4` -> `actions/upload-artifact@v6`
  - top-level `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24` 제거

## 왜 opt-in 환경변수를 제거했는가

- opt-in은 "러너 기본 런타임을 바꿔도 지금 workflow가 깨지지 않는지"를 미리 보는 임시 장치였다.
- 하지만 annotation이 계속 남는다면 운영 문서상으로는 여전히 미해결 상태처럼 보인다.
- action major 자체를 올릴 수 있는 시점이 확인됐으므로, workflow 본체를 정리하는 편이 더 명확하다.

## 검증

1. 기존 원격 run `23046637472`에서 경고 재현을 확인했다.
2. workflow YAML 파싱 검증
3. `git diff --check` 검증
4. push 후 새 GitHub Actions run에서 annotation 제거 여부를 최종 확인할 예정

## 인터뷰 답변 포인트

### 왜 한 번 더 CI를 손봤는가

- 경고를 본 시점에서 멈추지 않고, "임시 우회"와 "구조적 수정"을 분리해서 처리했다.
- 이런 식으로 변경 리스크를 작은 배치로 나누면 운영 자동화도 안정적으로 고도화할 수 있다.

### 왜 action major 업그레이드를 별도 배치로 분리했는가

- action 버전 업그레이드는 런타임 변경과 입력 계약 변경 가능성이 있어, 인증 rate limit 같은 기능 배치와 섞지 않는 편이 추적이 쉽다.
- 포트폴리오에서도 "문제를 발견하고, 범위를 줄여 해결했다"는 의사결정이 드러난다.

## 참고한 공식 릴리스

- `actions/checkout` v5 release
- `actions/setup-java` v5 release
- `gradle/actions` v5 release
- `actions/upload-artifact` v6 release
