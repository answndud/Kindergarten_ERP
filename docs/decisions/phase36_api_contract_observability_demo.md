# Phase 36. API 계약 가시화 + 운영 콘솔 + Prometheus + Demo 진입점

## 배경

이전 단계까지 권한 경계, JWT 세션, 소셜 계정 lifecycle, 감사 로그 저장/조회 API, Testcontainers, CI는 정리되어 있었다.

하지만 포트폴리오 관점에서는 아직 네 가지 공백이 남아 있었다.

1. 실행 중인 API 계약 문서가 없어 외부에서 엔드포인트 구조를 빠르게 파악하기 어려웠다.
2. 인증 감사 로그는 API만 있고, 원장이 실제로 보는 운영 화면이 없었다.
3. 보안 이벤트는 DB에는 남았지만 Prometheus 같은 운영 메트릭으로는 보이지 않았다.
4. 데모 실행 경로가 `local` 기준 설명에 묶여 있어 시연 시작점이 모호했다.

즉, **좋은 내부 구현은 있었지만 "보여주는 방식"이 아직 약한 상태**였다.

## 이번 단계의 결정

### 1) Swagger/OpenAPI를 live contract로 추가

- `springdoc-openapi-starter-webmvc-ui`를 추가했다.
- `/swagger-ui.html`, `/v3/api-docs`를 공개 경로로 열었다.
- cookie 기반 인증 구조를 설명하는 `cookieAuth` security scheme을 문서에 포함했다.

의도는 정적 표를 또 하나 만드는 게 아니라,
**실행 중인 애플리케이션이 자기 계약을 직접 보여주게 만드는 것**이다.

## 2) 인증 감사 로그를 원장용 운영 화면까지 확장

- `/audit-logs` 뷰를 추가했다.
- 기존 `/api/v1/auth/audit-logs`를 그대로 사용해 필터/페이지네이션/배지 UI만 얹었다.
- 설정 화면과 헤더에서 원장만 진입 링크를 볼 수 있게 했다.

여기서 중요한 점은 새 권한 정책을 만드는 것이 아니라,
기존 principal 전용 API를 **운영 콘솔 UX로 닫은 것**이다.

## 3) 인증/소셜 이벤트를 Prometheus counter로도 수집

- `AuthAuditLogService`에서 로그인, refresh, 소셜 연결/해제 이벤트를 DB에 저장할 때
  같은 이벤트를 `erp.auth.events` meter로도 기록한다.
- Prometheus scrape에서는 `erp_auth_events_total`로 노출된다.
- tag는 `event_type`, `result`, `provider`만 사용했다.

`reason`은 audit DB에서는 중요하지만 Prometheus tag로 넣으면 cardinality가 불필요하게 커질 수 있어 제외했다.

## 4) `demo` 프로파일로 시연 시작점을 고정

- `spring.profiles.group.demo=local`로 `demo` 프로파일이 로컬 DB/Redis/OAuth 설정을 그대로 포함하게 했다.
- `application-demo.yml`에 Swagger UI 시연 친화 설정을 넣었다.
- local/demo seed data에 인증 감사 로그 샘플도 추가했다.

이제 면접/데모에서는 아래 한 줄로 시작하면 된다.

```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

## 구현 포인트

- `OpenApiConfig`
  - `api-v1` group
  - title/version/server/cookie security scheme 정의
- `AuthAuditLogViewController`, `authaudit/audit-logs.html`
  - 원장 전용 운영 화면
- `AuthAuditMetricsService`
  - low-cardinality auth event counter
- `PrometheusRegistryConfig`, `PrometheusScrapeController`
  - Prometheus registry/scrape fallback
- `RoleRedirectInterceptor`
  - `swagger-ui`, `v3/api-docs`, `actuator/prometheus` 같은 인프라 경로는 로그인 리다이렉트 대상에서 제외

특히 마지막 항목은 이번 단계에서 나온 실제 함정이었다.
Security 설정만 열어서는 충분하지 않았고,
뷰 인터셉터가 문서/메트릭 경로를 다시 `/login`으로 보내고 있었다.

## 검증

- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.api.AuthAuditApiIntegrationTest"`

주요 회귀 포인트:

- Swagger UI / OpenAPI JSON 공개 접근
- `/actuator/prometheus` 공개 접근 및 `erp_auth_events_total` 노출
- 원장의 `/audit-logs` 화면 접근 가능
- 비로그인/교사의 `/audit-logs` 접근 차단

## 인터뷰에서 강조할 포인트

1. "좋은 백엔드는 내부 구현뿐 아니라 계약과 운영 상태를 외부에서 빠르게 이해할 수 있어야 한다고 봤습니다."
2. "감사 로그는 저장 API에서 끝내지 않고 원장용 화면과 Prometheus counter까지 연결해 운영자가 실제로 보는 구조로 닫았습니다."
3. "문서/메트릭 공개 경로를 열 때 SecurityConfig만 보지 않고 MVC interceptor까지 같이 점검해야 한다는 점을 이번 단계에서 실제로 확인했습니다."
4. "`demo` 프로파일을 따로 둔 이유는 면접 시연을 local 개발 환경과 분리해서 시작점을 고정하기 위해서입니다."
