# Environment Contract

이 문서는 실행 환경별 필수 환경 변수와 안전한 기본값 규칙의 SSOT입니다.

## 공통 원칙

- `application.yml`은 fail-closed 기준선입니다.
- `SPRING_PROFILES_ACTIVE`를 명시하지 않으면 애플리케이션은 부팅되지 않습니다.
- 로컬 인프라용 값과 앱 프로세스용 시크릿은 분리합니다.
- Swagger/OpenAPI, app-port Prometheus, demo seed는 기본 공개가 아니라 명시적 opt-in입니다.

## 1. local

로컬 개발은 아래 두 가지를 분리합니다.

1. Docker 인프라 환경변수
2. 앱 프로세스 환경변수

### Docker 인프라

- `docker/.env.example`를 `docker/.env`로 복사
- 대상 값
  - `MYSQL_ROOT_PASSWORD`
  - `MYSQL_DATABASE`
  - `MYSQL_USER`
  - `MYSQL_PASSWORD`
  - `GRAFANA_ADMIN_USER`
  - `GRAFANA_ADMIN_PASSWORD`

### 앱 프로세스

- 필수
  - `SPRING_PROFILES_ACTIVE=local`
- 선택
  - `JWT_SECRET`
    - 비우면 local 전용 fallback secret 사용
  - `APP_SEED_ENABLED=true`
    - 로컬 시드 데이터가 필요할 때만 켭니다.
  - `APP_SEED_LOG_CREDENTIALS=true`
    - 시드 계정 정보를 로그에 남겨야 할 때만 켭니다.
  - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
  - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`

## 2. demo

면접/시연용 실행입니다.

- 필수
  - `SPRING_PROFILES_ACTIVE=demo`
- 기본 동작
  - local 설정을 포함합니다.
  - 시드 데이터가 자동으로 활성화됩니다.
  - Swagger/OpenAPI와 app-port Prometheus를 명시적으로 엽니다.
- 선택
  - `JWT_SECRET`
    - 비우면 local 계층의 demo용 fallback secret 사용
  - OAuth client 환경변수

## 3. prod

운영은 모든 핵심 값을 환경 변수로 명시해야 합니다.

### 필수

- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`

### 선택

- `REDIS_PORT`
- `MANAGEMENT_SERVER_PORT`
- `MANAGEMENT_SERVER_ADDRESS`
- `NOTIFICATION_INCIDENT_WEBHOOK`
- `NOTIFICATION_EMAIL_FROM`
- `NOTIFICATION_PUSH_WEBHOOK`
- `NOTIFICATION_APP_WEBHOOK`

### prod 안전 조건

- `app.seed.enabled=false`
- `springdoc.api-docs.enabled=false`
- `springdoc.swagger-ui.enabled=false`
- `app.security.management-surface.public-api-docs=false`
- `app.security.management-surface.expose-prometheus-on-app-port=false`
- `jwt.cookie-secure=true`

## 4. 테스트

- 테스트는 `@ActiveProfiles("test")`를 사용합니다.
- JWT/OAuth/Redis/Testcontainers용 테스트 전용 값은 `src/test/resources/application-test.yml`에 고정합니다.

## 5. 체크리스트

배포 전에는 아래를 반드시 확인합니다.

1. `SPRING_PROFILES_ACTIVE`가 명시돼 있는가
2. `JWT_SECRET`가 실제 시크릿인가
3. Swagger/OpenAPI가 prod에서 비활성화돼 있는가
4. Prometheus가 app port가 아니라 management plane 또는 내부 경로로만 노출되는가
5. `app.seed.enabled`가 prod에서 꺼져 있는가
