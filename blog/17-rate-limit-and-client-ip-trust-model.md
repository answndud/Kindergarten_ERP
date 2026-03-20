# [Spring Boot 포트폴리오] 17. 로그인 남용 방어: Rate Limit과 Client IP 신뢰 모델

## 1. 이번 글에서 풀 문제

인증이 동작한다고 끝이 아닙니다.  
이제 “남용”을 막아야 합니다.

이 프로젝트에서 추가로 풀어야 했던 문제는 아래였습니다.

- 로그인 brute-force를 어떻게 줄일까?
- refresh 요청 폭주를 어떻게 막을까?
- `X-Forwarded-For` 헤더를 아무나 넣으면 rate limit을 우회하지 않을까?

이 문제를 해결하기 위해 아래 두 축을 추가했습니다.

- `AuthRateLimitService`
- `ClientIpResolver`

## 2. 먼저 알아둘 개념

### 2-1. Fixed Window Rate Limit

정해진 시간 창 안에서 요청 수를 제한하는 가장 단순한 방식입니다.

이 프로젝트는 설명 가능성과 구현 단순성을 위해 이 방식을 택했습니다.

### 2-2. IP와 이메일 두 축 제한

로그인 남용은 하나의 축만으로 막기 어렵습니다.

- IP만 보면 공유망 환경이 문제
- 이메일만 보면 여러 IP 분산 공격이 문제

그래서 이 프로젝트는 로그인에 대해 IP와 이메일 두 축을 같이 봅니다.

### 2-3. Trusted Proxy

`X-Forwarded-For`는 프록시가 붙여 주는 헤더지만,  
아무 요청자나 임의로 넣을 수 있기 때문에 무조건 신뢰하면 안 됩니다.

즉, **어떤 프록시를 신뢰할지**를 코드로 정해야 합니다.

## 3. 이번 글에서 다룰 파일

```text
- src/main/java/com/erp/domain/auth/service/AuthRateLimitService.java
- src/main/java/com/erp/global/security/ClientIpResolver.java
- src/main/java/com/erp/global/security/ClientIpProperties.java
- src/main/java/com/erp/domain/auth/controller/AuthApiController.java
- src/main/java/com/erp/domain/auth/service/AuthService.java
- src/test/java/com/erp/api/AuthApiIntegrationTest.java
- docs/decisions/phase21_auth_rate_limit.md
- docs/decisions/phase24_auth_client_ip_trust_model.md
- docs/decisions/phase25_login_rate_limit_policy_refinement.md
```

## 4. 설계 구상

```mermaid
flowchart TD
    A["AuthApiController"] --> B["ClientIpResolver"]
    B --> C["신뢰 가능한 프록시면 X-Forwarded-For 사용"]
    B --> D["아니면 remoteAddr 사용"]
    A --> E["AuthRateLimitService"]
    E --> F["Redis counters"]
```

핵심 기준은 아래였습니다.

1. 로그인은 IP와 이메일 두 축을 본다
2. refresh는 IP 기준으로 제한한다
3. 성공 로그인은 이메일 실패 카운터를 지운다
4. forwarded header는 trusted proxy일 때만 믿는다

## 5. 코드 설명

### 5-1. `AuthRateLimitService`: 단순하지만 설명 가능한 제한

[AuthRateLimitService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthRateLimitService.java)의 핵심 메서드는 아래입니다.

- `validateLoginAllowed(...)`
- `recordLoginFailure(...)`
- `clearLoginFailures(...)`
- `validateRefreshAllowed(...)`
- `consumeSlot(...)`

로그인 정책은 아래입니다.

- IP: 10분에 15회
- 이메일: 10분에 5회

refresh 정책은 아래입니다.

- IP: 5분에 10회

### 5-2. 왜 성공 로그인 시 이메일 카운터를 지우는가

초기 정책은 성공 로그인도 누적 예산을 소모하는 문제가 있었습니다.

그래서 현재는

- 실패만 기록하고
- 성공하면 이메일 실패 카운터는 지웁니다

즉, 운영 UX까지 고려한 정책으로 다듬었습니다.

### 5-3. `ClientIpResolver`: 헤더 스푸핑을 그대로 믿지 않는다

[ClientIpResolver.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/global/security/ClientIpResolver.java)의 핵심 메서드는 아래입니다.

- `resolve(...)`
- `isTrustedProxy(...)`
- `extractForwardedFor(...)`
- `fallback(...)`

흐름은 아래입니다.

1. 먼저 `remoteAddr` 확인
2. trusted proxy가 아니면 그것만 사용
3. trusted proxy면 `X-Forwarded-For`, `X-Real-IP` 순으로 확인

즉, 헤더 자체보다 **신뢰 모델**이 핵심입니다.

### 5-4. `AuthApiController`와 `AuthService` 연결

[AuthApiController.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/controller/AuthApiController.java)는
로그인/refresh 진입 전에 `clientIpResolver.resolve(request)`를 통해 client IP를 확보합니다.

[AuthService.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/auth/service/AuthService.java)는 이 값을 받아

- rate limit
- audit log
- anomaly alert

까지 한 흐름으로 처리합니다.

## 6. 실제 흐름

```mermaid
sequenceDiagram
    participant Client as 요청자
    participant Resolver as ClientIpResolver
    participant Rate as AuthRateLimitService
    participant Auth as AuthService

    Client->>Resolver: request
    Resolver-->>Rate: trusted IP 해석 결과
    Rate->>Auth: 허용 / 차단
    Auth-->>Client: 로그인 성공 또는 429
```

## 7. 테스트로 검증하기

대표 검증은 `AuthApiIntegrationTest`입니다.

- 로그인 연속 실패 시 `429`
- refresh 요청 연속 시 제한
- 성공 로그인 후 이메일 실패 카운터 초기화
- 신뢰하지 않는 프록시에서 forwarded header 스푸핑 우회 불가

그리고 설계 변천은 아래 문서로 이어집니다.

- [phase21_auth_rate_limit.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase21_auth_rate_limit.md)
- [phase24_auth_client_ip_trust_model.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase24_auth_client_ip_trust_model.md)
- [phase25_login_rate_limit_policy_refinement.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase25_login_rate_limit_policy_refinement.md)

## 8. 회고

이 단계의 핵심 교훈은 아래입니다.

1. rate limit은 “있다”보다 “어떤 기준으로 계산하는가”가 중요하다
2. IP를 본다면 반드시 trust model을 같이 설계해야 한다

즉, 보안 기능은 체크리스트처럼 붙이는 것이 아니라  
운영 환경을 가정하고 설계해야 합니다.

## 9. 취업 포인트

- “Redis fixed-window rate limit으로 로그인과 refresh 남용을 제한했습니다.”
- “로그인 성공은 실패 예산에서 제외해 UX를 보정했고, refresh는 별도 정책으로 관리했습니다.”
- “`X-Forwarded-For`를 무조건 믿지 않고 trusted proxy 모델을 코드로 분리했습니다.”
