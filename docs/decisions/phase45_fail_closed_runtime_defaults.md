# Phase 45. Fail-Closed Runtime Defaults

## 배경

프로젝트가 local/demo 중심으로 성장하면서 `application.yml`의 기본값이 개발 편의에 치우쳤습니다.
이 상태에서는 운영 배포에서 profile 또는 secret 주입이 한 번만 어긋나도 local 편의 설정으로 부팅될 수 있었습니다.

핵심 문제는 아래였습니다.

- 기본 활성 프로파일이 `local`
- 공통 `JWT_SECRET` fallback
- Swagger/OpenAPI, app-port Prometheus 공개 기본값
- local seed/data loader가 profile만으로 자동 활성화
- README와 demo/runbook이 “편의 기본값”을 전제로 서술

## 결정

### 1. 공통 설정은 fail-closed 기준선으로 둔다

- `application.yml`에서는 기본 활성 프로파일을 제거한다.
- `application.yml`에서는 Swagger/OpenAPI와 app-port Prometheus를 기본 비활성화한다.
- `application.yml`에서는 `app.seed.enabled=false`를 기준선으로 둔다.

### 2. local/demo 편의는 명시적 opt-in으로 연다

- `application-local.yml`에서만 Swagger/OpenAPI와 app-port Prometheus를 연다.
- demo는 `demo -> local` profile group 위에서 `app.seed.enabled=true`로 시연용 seed를 명시적으로 켠다.
- local seed는 `APP_SEED_ENABLED=true`일 때만 동작한다.

### 3. 운영은 부팅 단계에서 위험한 설정을 거부한다

- `StartupSafetyValidator`를 추가해 profile 미지정 부팅을 막는다.
- `prod`에서는 아래 조건이면 부팅을 거부한다.
  - `JWT_SECRET` 미설정 또는 placeholder
  - 공개 API 문서 노출
  - app-port Prometheus 노출
  - seed 활성화
  - `jwt.cookie-secure=false`

### 4. 문서/블로그도 같은 메시지로 맞춘다

- README, env contract, developer guide, demo preflight/runbook, hiring pack을 함께 수정한다.
- 관련 블로그 글은 “편의 기본값”이 아니라 “기본 닫힘 + 명시적 활성화” 관점으로 다시 설명한다.

## 결과

- 앱은 `SPRING_PROFILES_ACTIVE`를 명시하지 않으면 부팅되지 않는다.
- local/demo에서만 문서와 metrics, seed data가 의도적으로 열린다.
- prod는 기본 공개가 아니라 내부 운영 surface 중심으로 설명된다.
- 설정, 실행 명령, 데모 문서, 블로그 메시지가 같은 기준을 공유하게 된다.

## 트레이드오프

- `./gradlew bootRun`만으로 바로 뜨는 경험은 사라진다.
- local 시드가 기본 자동 적재가 아니므로 처음 설정이 한 단계 늘어난다.
- 대신 profile/secret 실수로 인한 fail-open 위험을 줄이고, 포트폴리오 메시지도 더 단단해진다.
