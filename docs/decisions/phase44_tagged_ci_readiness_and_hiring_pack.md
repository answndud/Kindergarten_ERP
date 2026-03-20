# Phase 44. Tagged CI, Readiness Failure Mode, Hiring Pack

## 배경

이전 단계까지 기능/보안/운영성은 충분히 쌓였지만, 세 가지 약점이 남아 있었다.

1. CI의 `fastTest`/`integrationTest`는 패키지 경로 기반이라 테스트 성격과 실행 스위트가 정확히 맞지 않았다.
2. readiness probe는 "엔드포인트가 켜진다"는 사실만 검증했고, 실제 DB/Redis 장애에 반응하는지 설명 근거가 약했다.
3. 문서는 많아졌지만 채용 담당자/면접관이 빠르게 읽을 active SSOT 묶음이 부족했다.

이번 단계는 코드보다 "운영형 검증과 취업용 가시화"를 강화하는 데 집중했다.

## 결정

### 1) CI 스위트는 path include 대신 JUnit Tag로 분리

- `fastTest`: `@Tag("fast")`
- `integrationTest`: `@Tag("integration")`
- `performanceSmokeTest`: `@Tag("performance")`

이유:

- 패키지 경로는 테스트의 실제 실행 비용/성격을 보장하지 못한다.
- tagged suite는 리팩터링 후에도 테스트 의미를 유지하기 쉽다.
- performance smoke를 별도 job으로 빼면 운영 회귀와 일반 통합 테스트를 분리해 설명할 수 있다.

### 2) readiness는 `criticalDependencies`로 명시하고, liveness와 분리 검증

- readiness group에 `criticalDependencies`를 포함했다.
- `CriticalDependenciesHealthIndicator`는 DB/Redis를 직접 probe 한다.
- 통합 테스트에서는 contributor를 장애 상태로 바꿔 `readiness DOWN / liveness UP`을 검증한다.

이유:

- "probe가 있다"와 "probe가 운영 신호를 반영한다"는 다른 문제다.
- 외부 의존성 장애는 트래픽 수신 중단(readiness)과 프로세스 생존(liveness)을 구분해 다뤄야 한다.

### 3) 운영 콘솔 경로에 성능 smoke 추가

- auth audit list/export
- domain audit list/export

이유:

- 관리자 화면도 결국 운영자가 반복해서 쓰는 조회 경로다.
- 성능 스토리를 사용자 API에만 한정하지 않고, 운영 콘솔까지 확장한 점이 포트폴리오 신호가 된다.

### 4) active 문서를 hiring-pack 중심으로 압축

새 active SSOT:

- `docs/portfolio/architecture/system-architecture.md`
- `docs/portfolio/demo/demo-preflight.md`
- `docs/portfolio/demo/demo-runbook.md`
- `docs/portfolio/case-studies/auth-incident-response.md`
- `docs/portfolio/hiring-pack/backend-hiring-pack.md`

이유:

- 면접관은 결정 로그 40개를 순서대로 읽지 않는다.
- "읽는 순서"를 설계하는 것까지가 포트폴리오 완성도다.

## 검증

```bash
./gradlew --no-daemon compileJava compileTestJava
./gradlew --no-daemon fastTest
./gradlew --no-daemon integrationTest
./gradlew --no-daemon performanceSmokeTest
./gradlew --no-daemon test
```

## 면접 포인트

- `테스트를 패키지 경로가 아니라 tag 단위로 분리해 CI 신뢰성을 높였습니다.`
- `readiness는 DB/Redis 의존성을 반영하고, 장애 상황에서도 liveness는 분리해 검증했습니다.`
- `운영자 콘솔 list/export도 성능 smoke를 붙여 운영 경로 회귀를 잡습니다.`
- `문서는 history와 active SSOT를 분리하고, 채용용 진입 경로를 명시적으로 설계했습니다.`
