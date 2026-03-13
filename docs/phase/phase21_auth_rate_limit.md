# Phase 21: 인증 Rate Limit 하드닝

## 배경

- 세션 단위 refresh token과 rotation까지 넣었지만, 로그인과 refresh API 남용 자체를 막는 장치는 없었다.
- 이 상태에서는 비밀번호 대입(brute-force)이나 refresh endpoint 과호출에 대해 애플리케이션이 무방비다.
- 포트폴리오 관점에서도 "JWT를 썼다"보다 "인증 남용을 어떻게 제한했는가"가 더 실무적인 질문으로 이어진다.

## 목표

1. 로그인 API에 기본적인 brute-force 방어를 추가한다.
2. refresh API에도 과호출 제한을 넣어 token abuse를 줄인다.
3. 구현은 단순하게 유지하되, Redis를 활용해 운영 환경과 맞는 방식으로 적용한다.

## 결정

1. Redis 기반 fixed window rate limit을 사용했다.
2. 로그인은 두 축으로 제한했다.
   - IP 기준: 10분 동안 15회
   - 이메일 기준: 10분 동안 5회
3. refresh는 IP 기준으로 5분 동안 10회로 제한했다.
4. 초과 시 공통 에러 코드 `A006`과 HTTP `429 Too Many Requests`를 반환한다.

## 왜 fixed window를 선택했는가

- 프로젝트 철학이 `Simple is Best`이기 때문이다.
- sliding window나 token bucket이 더 정교할 수 있지만, 현재 범위에서는 Redis `INCR + EXPIRE`만으로도 설명 가능하고 유지보수가 쉽다.
- 면접에서는 "왜 지금 이 정도 복잡도로 멈췄는가"를 설명할 수 있는 쪽이 더 중요하다.

## 구현 요약

### 1) 전용 서비스 분리

- `AuthRateLimitService`
  - 로그인 IP 키
  - 로그인 이메일 키
  - refresh IP 키
  - `RedisTemplate` 기반 `increment + expire`

### 2) 컨트롤러/서비스 연결

- `AuthApiController`
  - `X-Forwarded-For`, `X-Real-IP`, `remoteAddr` 순으로 client IP 추출
- `AuthService`
  - `login` 진입 전에 로그인 rate limit 검사
  - `refreshAccessToken` 진입 전에 refresh rate limit 검사

### 3) 예외/로그 정리

- `ErrorCode.AUTH_RATE_LIMITED`
  - `429`, `A006`
- `GlobalExceptionHandler`
  - BusinessException 로그를 4xx는 `warn`, 5xx는 `error`로 조정

## 검증

- `AuthApiIntegrationTest`
  - 잘못된 비밀번호 5회까지는 `401`, 6회째는 `429`
  - refresh 10회까지는 성공, 11회째는 `429`
- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`

## 인터뷰 답변 포인트

### 왜 로그인만이 아니라 refresh도 제한했는가

- 공격자는 비밀번호뿐 아니라 refresh endpoint도 과호출할 수 있다.
- refresh는 인증 이후 경로라고 해서 안전한 게 아니라, 토큰 재발급 자체가 민감한 동작이다.

### 왜 IP와 이메일 둘 다 봤는가

- 이메일만 보면 여러 계정을 돌려가며 시도하는 공격에 약하다.
- IP만 보면 같은 사용자 계정 대상 집중 공격을 설명하기 어렵다.
- 그래서 단순하지만 서로 다른 축을 함께 두었다.

### 왜 성공 요청도 카운트에 포함했는가

- 현재 구현은 단순 fixed window이므로 성공/실패를 구분하지 않는다.
- 대신 한도를 보수적으로 설정해서 정상 사용자를 과하게 막지 않도록 조정했다.
- 더 정교한 실패 기반 정책은 후속 과제로 남길 수 있다.

## 트레이드오프

- 장점
  - 구현이 단순하고 Redis 인프라와 바로 맞물린다
  - brute-force와 refresh abuse에 대한 기본 방어선을 확보했다
  - 통합 테스트로 실제 제한 동작을 증명했다
- 단점
  - fixed window 특성상 경계 시점 버스트에 완전히 정교하지 않다
  - 성공/실패를 구분한 실패 전용 잠금 정책보다 거칠다
