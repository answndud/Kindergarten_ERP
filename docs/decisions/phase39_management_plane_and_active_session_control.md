# Phase 39. Management Plane 하드닝과 활성 세션 제어

## 1. 배경

이전 배치까지 JWT refresh token은 세션 단위로 분리되어 있었지만, 두 가지 운영상 빈틈이 남아 있었다.

1. refresh token만 revoke해도 이미 발급된 access token은 TTL 동안 계속 유효했다.
2. Swagger/OpenAPI, Prometheus 같은 운영/계약 endpoint가 local/demo 편의 기준으로 열려 있었고, prod 노출 정책이 문서와 코드에서 분리돼 있었다.

포트폴리오 관점에서 이 상태는 "세션 관리 UI는 있지만 실제 보안 제어는 약하다", "운영 관측성은 있지만 노출면 통제가 약하다"는 인상을 줄 수 있다.

## 2. 이번 결정

### 2-1. Redis 세션 레지스트리를 access token 검증의 SSOT로 승격

- 기존 `refresh:session:{memberId}:{sessionId}`와 `refresh:sessions:{memberId}`를 유지한다.
- 여기에 `refresh:session:meta:{memberId}:{sessionId}`를 추가해 기기/IP/User-Agent/최근 활동/만료 시각을 저장한다.
- JWT 필터는 access token 서명 검증 뒤, `memberId + sessionId`로 Redis 세션 활성 여부를 추가 확인한다.
- 세션이 revoke되면 refresh token뿐 아니라 해당 access token 인증도 즉시 끊긴다.

### 2-2. 활성 세션 관리 API/UI 추가

- `GET /api/v1/auth/sessions`
  - 현재 세션 포함 전체 활성 세션 목록
- `DELETE /api/v1/auth/sessions/{sessionId}`
  - 특정 기기 세션 종료
- `DELETE /api/v1/auth/sessions/others`
  - 현재 기기를 제외한 모든 세션 종료

설정 화면에는 API 기반 세션 목록과 "다른 기기 로그아웃" UI를 붙여, 인증 설계를 사용자가 실제로 제어할 수 있게 했다.

### 2-3. 운영 plane은 profile별로 분리

- `prod`에서는 `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`
- `prod`에서는 `management.server.port`, `management.server.address`로 management plane 분리
- 감사 로그 화면의 Swagger 링크도 설정값을 읽어 조건부 렌더링

즉 local/demo의 시연 편의성과 prod의 노출 통제를 같은 코드베이스에서 profile로 나눴다.

## 3. 구현 포인트

### 인증/세션

- `AuthSessionRegistryService`
  - 세션 메타데이터 저장
  - refresh rotation 시 TTL/메타데이터 갱신
  - 세션 목록 조회/정렬
  - 개별 세션/다른 기기/전체 세션 revoke
- `JwtFilter`
  - access token 검증 후 Redis 세션 활성 여부 확인
  - 정적 자원/운영 경로를 제외한 요청에서 `lastSeenAt` 갱신
- `AuthService`
  - 토큰 발급/rotation/revoke를 세션 레지스트리 SSOT에 위임
- `AuthApiController`
  - 활성 세션 조회/종료 API 추가

### 운영 plane

- `application-prod.yml`
  - Swagger 비활성화
  - management port/address 분리
- `AuthAuditLogViewController`, `authaudit/audit-logs.html`
  - API 계약 링크 조건부 노출

## 4. 트레이드오프

### 장점

- 세션 revoke가 실제 인증 제어가 된다.
- 사용자에게 "내 계정이 어디서 로그인돼 있는가"를 보여줄 수 있다.
- 운영 문서/화면/설정이 prod 기준과 더 일치한다.

### 비용

- JWT 필터가 Redis 세션 레지스트리를 읽기 때문에, 인증 경로가 Redis에 더 의존한다.
- 이 프로젝트에서는 가용성을 위해 Redis 오류 시 access token 검증을 fail-open으로 두었다.
- 대신 이 선택은 문서화하고, 이후 장애 모드 검증 배치에서 다시 다룬다.

## 5. 검증

실행 명령:

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"
```

검증한 내용:

- 세션 목록 API가 현재 세션/다른 기기 세션을 함께 반환
- 특정 세션 revoke 시 해당 access token이 즉시 401 처리
- `DELETE /sessions/others`가 현재 세션만 남김
- settings 화면에 활성 세션 관리 섹션 노출
- OAuth2 로그인 성공 핸들러 시그니처 변경 회귀 없음

## 6. 면접에서 말할 포인트

- "refresh token만 지우는 세션 관리는 반쪽짜리라서, access token도 Redis 세션 레지스트리에 묶었습니다."
- "세션 관리 UI를 붙일 때 보안 제어가 실제로 먹는지까지 통합 테스트로 고정했습니다."
- "운영 편의 때문에 열어 둔 Swagger/Prometheus를 prod까지 그대로 두지 않고, profile별 노출 정책으로 분리했습니다."
