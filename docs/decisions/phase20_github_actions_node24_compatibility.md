# Phase 20: GitHub Actions Node 24 호환 선제 대응

## 배경

- `ff5b683` push 후 GitHub Actions run `23045682877`은 성공했다.
- 하지만 두 job 모두 runner annotation으로 Node.js 20 deprecation 경고가 출력됐다.
- GitHub 안내에 따르면 2026년 6월 2일부터 JavaScript action 기본 런타임이 Node 24로 전환된다.

## 목표

1. 현재 workflow를 Node 24 실행 경로로 미리 올린다.
2. 불필요한 경고를 줄이고, 향후 runner 기본 전환 리스크를 낮춘다.
3. action major 업그레이드는 별도 배치로 분리해 변경 범위를 작게 유지한다.

## 결정

1. workflow 상단에 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true`를 추가했다.
2. 이번 배치에서는 `actions/checkout`, `actions/setup-java`, `actions/upload-artifact`, `gradle/actions/setup-gradle` 버전 자체는 건드리지 않았다.
3. 이유는 현재 workflow가 이미 성공하는 상태에서, 먼저 runner가 제공한 공식 opt-in 경로를 적용하는 편이 가장 안전하기 때문이다.

## 구현 요약

- `.github/workflows/ci.yml`
  - top-level `env`
  - `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true`

## 왜 action 버전 업그레이드를 바로 하지 않았는가

- action major 업그레이드는 changelog 확인과 추가 회귀 검증이 필요하다.
- 지금 당장 중요한 것은 "곧 깨질 수 있는 기본 런타임 전환"에 선제 대응하는 것이다.
- 그래서 이번에는 호환 경로를 먼저 확보하고, 버전 업그레이드는 독립 배치로 남겼다.

## 검증

1. GitHub Actions 기존 run `23045682877` 성공 확인
2. YAML 파싱 검증
3. 후속 push 후 새 workflow run에서 경고 감소 여부 재확인 예정

## 인터뷰 답변 포인트

### 왜 성공한 CI를 또 손봤는가

- 운영 자동화는 "지금 성공한다"보다 "곧 바뀌는 플랫폼 조건에도 버틸 준비가 되어 있는가"가 중요하다.
- deprecation 경고는 아직 장애가 아니지만, 미리 처리하면 운영 감각을 보여주기 좋다.

### 왜 가장 작은 수정부터 적용했는가

- 경고 원인이 runner 런타임 전환 예고였기 때문에, 먼저 공식 opt-in 경로를 쓰는 게 리스크가 가장 작다.
- action 자체를 한 번에 올리는 건 변경 면적이 더 크므로 후속으로 나누는 편이 합리적이다.
