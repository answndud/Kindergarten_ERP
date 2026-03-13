# Phase 19: CI 복구와 Fast/Integration Job 분리

## 배경

- Phase 16에서 GitHub Actions를 추가했지만, 첫 실제 run은 실패했다.
- 원인은 `gradle/wrapper/gradle-wrapper.jar`가 `.gitignore`의 후행 `*.jar` 규칙에 다시 잡혀 runner에 포함되지 않은 것이었다.
- 또한 모든 테스트를 한 job에서 실행하면 피드백 속도와 실패 지점 분리가 약했다.

## 목표

1. GitHub runner에서 Gradle wrapper가 정상 동작하도록 복구한다.
2. CI를 `fastTest`와 `integrationTest` job으로 분리한다.
3. job별 report artifact를 남겨 실패 분석 동선을 더 명확히 한다.

## 결정

1. `.gitignore`에 `!gradle/wrapper/gradle-wrapper.jar` 예외를 후행 `*.jar` 규칙 뒤에도 명시해 wrapper 추적을 복구했다.
2. Gradle에 아래 태스크를 추가했다.
   - `fastTest`: `com.erp.domain/**`
   - `integrationTest`: `com.erp.api/**`, `com.erp.integration/**`, `com.erp.performance/**`, `ErpApplicationTests`
3. GitHub Actions job도 같은 기준으로 분리했다.

## 구현 요약

### 1) CI blocker 복구

- `.gitignore`
  - wrapper jar 예외 규칙 추가
- 결과
  - 로컬에서는 `gradle/wrapper/gradle-wrapper.jar`가 untracked로 드러나고, 다음 commit부터 runner에도 포함된다

### 2) Gradle 태스크 분리

- `build.gradle`
  - `fastTest`
  - `integrationTest`
- 로컬에서는 기존 `./gradlew test` 전체 검증 경로를 그대로 유지한다.

### 3) GitHub Actions 분리

- `Fast Checks`
  - Docker 없이 빠른 단위/서비스 테스트 실행
- `Integration Suite`
  - Docker availability 확인
  - Testcontainers 기반 통합/성능 테스트 실행
- artifact
  - `fast-test-reports`
  - `integration-test-reports`

## 검증

- 첫 원격 실패 로그에서 `Unable to access jarfile ... gradle-wrapper.jar` 원인을 확인
- 로컬에서 `./gradlew compileJava compileTestJava` 성공
- 로컬에서 `Auth/Member/Dashboard` 핵심 회귀 테스트 통과
- 분리 태스크 기준으로도 전체 검증이 가능하도록 구성

## 인터뷰 답변 포인트

### 왜 CI를 다시 손봤는가

- CI를 "있다"로 끝내지 않고, 실제 첫 실행 실패까지 확인하고 복구한 경험이 더 실무적이다.
- 포트폴리오에서도 자동화의 존재보다 운영 가능한 자동화인지가 더 중요하다.

### 왜 fast/integration으로 나눴는가

- 빠른 실패 피드백과 무거운 인프라 테스트를 같은 job에 넣으면 둘 다 애매해진다.
- 먼저 가벼운 회귀를 빠르게 확인하고, 이어서 Testcontainers 기반 신뢰성 검증을 분리하는 편이 해석이 쉽다.

### 왜 `test`를 없애지 않았는가

- 개발자 로컬 경험은 단순해야 한다.
- 그래서 기본 `./gradlew test`는 전체 검증으로 남기고, CI만 분리 실행 전략을 사용했다.

## 트레이드오프

- 장점
  - wrapper 누락 같은 운영 실수를 실제로 복구했다
  - CI 실패 위치가 더 빨리 드러난다
  - artifact가 job별로 분리되어 원인 파악이 쉽다
- 단점
  - workflow 구조가 조금 더 길어졌다
  - 캐시가 있어도 runner 두 개가 각각 빌드를 수행한다
