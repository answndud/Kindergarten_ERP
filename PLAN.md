# PLAN.md

## 작업명
- 후속 고도화 6차 (로그인 Rate Limit 정책 정교화)

## 1) 목표 / 범위
- 로그인 rate limit이 정상 사용자 경험을 불필요하게 해치지 않도록 정책을 정교화한다.
- 로그인 성공은 카운트하지 않고 실패만 누적하도록 바꾼다.
- 성공 로그인 시 이메일 기준 실패 카운터를 초기화해 brute-force 방어와 정상 로그인 UX를 분리한다.

## 2) 세부 작업 단계
1. 현재 로그인 rate limit 흐름 점검
   - `AuthService`, `AuthRateLimitService`, `AuthApiIntegrationTest`를 확인한다.
   - 성공/실패가 어떤 타이밍에 카운트되는지 정리한다.

2. 정책 정교화 구현
   - 로그인 진입 시에는 현재 실패 횟수만 확인한다.
   - 인증 실패 시에만 로그인 IP/이메일 카운터를 증가시킨다.
   - 인증 성공 시 이메일 기준 실패 카운터를 초기화한다.

3. 회귀 테스트 추가
   - 반복 성공 로그인은 rate limit에 걸리지 않는지 검증한다.
   - 일부 실패 후 성공 로그인 시 이메일 실패 카운터가 초기화되는지 검증한다.

4. 문서화 및 검증
   - `README.md`, `docs/phase/`에 정책 변경 이유와 운영 관점 트레이드오프를 정리한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 실패 카운터를 너무 많이 초기화하면 공격 흔적이 과도하게 지워질 수 있음
  - 대응: 성공 시에는 이메일 기준 실패 카운터만 지우고, IP 실패 카운터는 유지한다
- validate/record 타이밍이 엇갈리면 임계치 계산이 흔들릴 수 있음
  - 대응: 서비스 흐름을 `사전 확인 -> 인증 -> 실패 기록 또는 성공 초기화` 순으로 고정한다
- 기존 테스트가 "모든 로그인 시도 누적" 전제에 묶여 있을 수 있음
  - 대응: 실패만 누적되는 시나리오와 성공 로그인 비차단 시나리오를 함께 추가해 정책 자체를 고정한다
