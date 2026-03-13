# Phase 34: 운영 관측성 baseline 추가

## 배경

보안, 권한, 테스트, 감사 로그까지 강화한 뒤에도
운영 관점에서는 한 가지가 더 필요했습니다.

- 서비스가 살아 있는지
- 트래픽 받을 준비가 됐는지
- 요청 하나를 로그에서 어떻게 추적할지

이 세 가지가 없으면 면접에서
"기능은 많지만 운영 관측성은 없는 프로젝트"로 보일 수 있습니다.

## 핵심 결정

1. **Actuator는 health/info만 외부 공개한다.**
   - 운영 체크에 필요한 최소 엔드포인트만 열고
   - 나머지 actuator 엔드포인트는 기존 인증 규칙에 맡깁니다.

2. **liveness/readiness probe를 분리한다.**
   - "프로세스가 죽었는지"와
   - "트래픽을 받아도 되는지"는 다른 질문입니다.

3. **모든 요청에 correlation id를 부여한다.**
   - 클라이언트가 `X-Correlation-Id`를 보내면 재사용
   - 없으면 서버가 생성
   - 응답 헤더에도 다시 내려서 요청-로그를 연결할 수 있게 합니다.

4. **요청 로그는 key=value 형태로 남긴다.**
   - `method`, `uri`, `status`, `durationMs`, `clientIp` 수준으로만 기록
   - body/토큰/비밀번호 같은 민감 정보는 남기지 않습니다.

## 구현 요약

### 1) 의존성 / 설정

- `spring-boot-starter-actuator` 추가
- `application.yml`
  - `management.endpoints.web.exposure.include=health,info`
  - `management.endpoint.health.probes.enabled=true`
  - `management.health.livenessstate.enabled=true`
  - `management.health.readinessstate.enabled=true`

### 2) 보안

- `SecurityConfig`
  - `/actuator/health`
  - `/actuator/health/**`
  - `/actuator/info`
  를 공개 경로에 추가

### 3) 로깅

- `CorrelationIdFilter`
  - `X-Correlation-Id` 처리
  - MDC에 `correlationId` 저장
  - 응답 헤더에 다시 설정

- `RequestLoggingFilter`
  - 요청 완료 시 structured log 기록
  - `method`, `uri`, `status`, `durationMs`, `clientIp`

- `logback-spring.xml`
  - 로그 패턴에 `correlationId=%X{correlationId}` 추가

## 테스트

- `ObservabilityIntegrationTest`
  - `/actuator/health` 공개 접근 검증
  - `/actuator/health/readiness` 활성화 검증
  - correlation id header echo 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest"
git diff --check
```

## 인터뷰 포인트

- "왜 actuator를 다 공개하지 않았나요?"
  - 운영 확인에 필요한 최소 범위만 공개하고, 나머지는 정보 노출을 줄이기 위해 닫았기 때문입니다.

- "왜 correlation id가 필요한가요?"
  - 브라우저, 서버 로그, reverse proxy 로그를 같은 요청 단위로 묶기 위해서입니다.

- "왜 structured logging을 넣었나요?"
  - 사람이 읽는 로그를 넘어, 나중에 검색/집계/추적 가능한 로그 형태를 만들기 위해서입니다.
