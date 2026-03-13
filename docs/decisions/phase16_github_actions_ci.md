# Phase 16: GitHub Actions 기반 CI 자동화

## 배경

- MySQL/Redis Testcontainers 기반 테스트 스택까지 정리했지만, 로컬에서만 돌면 포트폴리오 설득력이 반쪽이다.
- 채용 관점에서는 "테스트를 작성했다"보다 "PR과 push마다 자동으로 검증되도록 운영했다"가 더 강한 시그널이다.
- 저장소에는 기존에 `.github/workflows` 자체가 없어서, 자동 검증 체인이 부재한 상태였다.

## 목표

1. GitHub Actions에서 전체 테스트 스위트를 자동 실행한다.
2. Testcontainers 기반 테스트가 CI runner에서도 그대로 동작하도록 한다.
3. 실패 시 원인 분석을 위해 test report를 artifact로 남긴다.

## 결정

1. 별도 서비스 컨테이너를 직접 정의하지 않고, GitHub hosted runner의 Docker 환경에서 Testcontainers를 그대로 사용한다.
2. Java 17 단일 버전 기준으로 `./gradlew test` 전체를 실행한다.
3. PR, push, 수동 실행(`workflow_dispatch`)을 모두 지원한다.
4. 실패 여부와 관계없이 test report를 업로드한다.

## 구현 요약

### 1) 워크플로우 추가

- `.github/workflows/ci.yml`
  - `push`, `pull_request`, `workflow_dispatch` 트리거
  - `ubuntu-latest` runner
  - `actions/setup-java@v4`로 Temurin 17 설치
  - `gradle/actions/setup-gradle@v4`로 Gradle 캐시 사용
  - `docker version`으로 Docker 사용 가능 여부 확인
  - `./gradlew --no-daemon test` 실행

### 2) 디버깅 편의성 확보

- `actions/upload-artifact@v4`
  - `build/reports/tests/test`
  - `build/test-results/test`
- CI 실패 시에도 리포트가 남아 원인 분석이 쉬워진다.

### 3) 브랜치 전략 반영

- `main`
- `develop`
- `codex/**`

개인 포트폴리오 프로젝트 특성상 주 개발 브랜치와 작업 브랜치에서 모두 자동 검증이 돌도록 구성했다.

## 왜 서비스 컨테이너 대신 Testcontainers를 유지했는가

- 로컬과 CI에서 같은 테스트 부트스트랩 경로를 유지할 수 있다.
- 테스트 코드가 직접 필요 인프라를 선언하므로, 환경 차이로 인한 드리프트가 줄어든다.
- 서비스 컨테이너와 테스트 코드의 설정이 이중화되지 않아 유지보수가 단순하다.

## 검증 포인트

1. GitHub Actions runner에서 Docker 사용 가능해야 한다.
2. `./gradlew test`가 CI에서도 MySQL/Redis Testcontainers를 통해 통과해야 한다.
3. 실패 시 artifact에 HTML 리포트와 raw test result가 남아야 한다.

## 인터뷰 답변 포인트

### 1) 왜 CI를 추가했는가

- 로컬에서만 테스트가 통과하는 프로젝트보다, 변경마다 자동 검증되는 프로젝트가 훨씬 신뢰도가 높다.
- 특히 권한/인증/출석 같은 핵심 기능은 회귀 위험이 커서 자동화가 필수라고 판단했다.

### 2) 왜 전체 테스트를 돌렸는가

- 포트폴리오에서는 부분 테스트보다 "저장소 전체가 일관되게 검증된다"는 메시지가 더 중요하다.
- 속도 최적화는 후속 과제로 두고, 먼저 신뢰성을 우선했다.

### 3) 실패 시 artifact 업로드까지 넣은 이유는 무엇인가

- CI는 성공 여부만 알려주면 반쪽이다.
- 실패했을 때 바로 분석 가능한 정보가 남아야 실제 팀 개발 경험에 가까운 자동화라고 생각했다.

## 트레이드오프

- 장점
  - PR/push마다 자동 검증
  - 로컬과 CI가 같은 Testcontainers 경로 사용
  - 실패 시 디버깅 자료 확보
- 단점
  - 실행 시간이 더 길다
  - Docker 의존성이 있는 runner가 필요하다

## 후속 과제

1. CI 시간을 줄이기 위해 빠른 테스트와 통합 테스트를 job 또는 태그 기준으로 분리
2. README에 workflow badge 연결
3. PR 템플릿에 "권한/보안/테스트 영향" 체크리스트 추가
