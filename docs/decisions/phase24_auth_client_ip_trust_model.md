# Phase 24: 인증 Client IP 신뢰 모델 하드닝

## 배경

- 이전 배치에서 로그인/refresh API에 Redis 기반 rate limit을 추가했지만, `AuthApiController`는 `X-Forwarded-For`, `X-Real-IP`를 모든 요청에서 그대로 신뢰하고 있었다.
- 이 상태에서는 일반 클라이언트도 요청 헤더를 임의로 바꿔 같은 원격 주소에서 계속 다른 IP처럼 보이게 만들 수 있다.
- 즉 "rate limit을 구현했다"는 사실은 있었지만, 실제 운영 관점에서는 우회 가능성이 남아 있었다.

## 목표

1. 인증 API가 client IP를 해석할 때 trusted proxy를 거친 요청만 전달 헤더를 신뢰하게 만든다.
2. 헤더 스푸핑으로 로그인/refresh rate limit을 우회할 수 없도록 한다.
3. reverse proxy 환경에서 왜 이런 기준이 필요한지 면접에서 설명 가능하게 만든다.

## 결정

1. `ClientIpResolver`를 별도 컴포넌트로 분리한다.
2. loopback 주소는 기본 trusted proxy로 간주한다.
3. 추가 trusted proxy는 `app.security.client-ip.trusted-proxies` 설정으로 확장 가능하게 둔다.
4. trusted proxy가 아닌 경우에는 `X-Forwarded-For`, `X-Real-IP`를 무시하고 `remoteAddr`만 사용한다.

## 구현 요약

### 1) 공통 resolver 도입

- `ClientIpProperties`
  - `app.security.client-ip.trusted-proxies` 설정 바인딩
- `ClientIpResolver`
  - trusted proxy 여부 판단
  - trusted proxy일 때만 `X-Forwarded-For`, `X-Real-IP` 사용
  - 그 외에는 `remoteAddr` fallback

### 2) Auth API 연결

- `AuthApiController`
  - 로그인과 refresh에서 직접 헤더를 읽는 로직 제거
  - 공통 `ClientIpResolver`를 사용하도록 변경

### 3) 테스트 보강

- `AuthApiIntegrationTest`
  - untrusted remote address에서 매 요청마다 다른 `X-Forwarded-For`를 넣어도 IP rate limit이 유지되는지 검증
  - loopback proxy에서는 forwarded header가 실제 client IP로 사용되는지 검증

## 검증 결과

- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`

둘 다 통과했다.

## 인터뷰 답변 포인트

### 왜 이 작업이 필요한가

- `X-Forwarded-For`는 reverse proxy가 붙여 주는 헤더일 뿐, 애플리케이션이 아무 요청에서나 그대로 믿으면 보안 장치가 된다기보다 우회 포인트가 된다.
- 특히 rate limit, 감사 로그, 이상 징후 탐지처럼 IP를 기준으로 동작하는 기능은 "어떤 프록시를 신뢰하는가"가 설계의 일부다.

### 왜 loopback만 기본 신뢰했는가

- 개인 프로젝트에서는 가장 흔한 배치가 같은 호스트의 reverse proxy 또는 로컬 개발 프록시다.
- 기본값은 보수적으로 두고, 실제 배포 프록시 IP는 설정으로 추가하는 편이 운영상 더 안전하다.

### 트레이드오프는 무엇인가

- trusted proxy를 설정하지 않으면 일부 배포 환경에서 원본 client IP 대신 proxy IP가 잡힐 수 있다.
- 하지만 아무 프록시나 신뢰하는 것보다, 명시적 신뢰 목록으로 운영하는 편이 보안 스토리와 실무 설명력 모두 더 낫다.
