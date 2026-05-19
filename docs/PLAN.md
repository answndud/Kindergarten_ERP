# PLAN.md

## 기준일

- 기준일: 2026-05-19

## 문서 규칙

- 이 문서는 현재 active 작업 계획의 SSOT다.
- 진행 상태, blocker, 최근 검증, 다음 액션은 `docs/PROGRESS.md`에 기록한다.
- 완료된 작업은 active 문서에 남기지 않고 `docs/COMPLETED.md`로 archive한다.
- active 작업이 없으면 이 문서는 `현재 active 작업 없음`만 유지한다.
- 이번 문서는 장기 개선 roadmap을 active plan으로 유지한다. 각 phase를 완료할 때마다 해당 phase만 `docs/COMPLETED.md`로 archive하고, 다음 phase 진행 여부를 다시 판단한다.

## Active Work

### 목표

백엔드 신입 포트폴리오로 이미 제출 가능한 상태를 넘어, 운영 전환 리스크, 핵심 서비스 구조, 테스트 신뢰도, production-like 실행 증거, 기능 완성도를 순서대로 보강한다.
Phase 1~6을 모두 끝냈을 때는 "아직 할 수 있는 아이디어"가 아니라, 실제 운영 전환과 코드 구조 관점에서 면접관이 지적할 만한 P0/P1 리스크와 큰 리팩토링 여지를 닫는 것을 목표로 한다.

### 최종 완료 정의

아래 조건을 모두 만족해야 이 roadmap을 완료로 본다.

| 영역 | 완료 기준 |
| --- | --- |
| 운영 완성도 | cloud 미배포 사실은 유지하되, prod profile safety, secret/env contract, secure cookie/CORS, Swagger/Prometheus 비공개, seed 차단, readiness/rollback 문서가 코드/테스트/문서로 일치한다. |
| 리팩토링 여지 | 면접관이 열어볼 핵심 service에서 orchestration/state transition/audit/notification 책임이 설명 가능한 단위로 분리되고, 더 분해할 후보는 P2 이하의 선택 개선만 남는다. |
| 테스트 신뢰도 | 권한 경계, 세션 revoke, 입학 상태 전이, outbox dead-letter/retry, prod safety, 주요 성능 회귀를 깨뜨리면 targeted test 또는 manual quality에서 잡힌다. |
| 배포 대체 증거 | 실제 클라우드 비용 없이도 `bootJar`, compose config, prod safety dry-run, deployment runbook, rollback/containment가 하나의 production-like checklist로 재현 가능하다. |
| 기능 완성도 | outbox/audit/session 중 최소 1개 운영 기능을 실제 운영자가 쓸 수 있는 수준으로 보강하고, 나머지 기능 후보는 README에 과장하지 않는다. |
| 문서/포트폴리오 | README, evidence map, risk response, interview guide가 실제 구현/검증과 일치하고 stale한 최신 수치나 미완료 주장을 포함하지 않는다. |

### 완료 후에도 남길 수 있는 것

- 실제 클라우드 상시 운영은 비용 문제로 여전히 제외할 수 있다.
- 대규모 마이크로서비스 분리는 현재 규모에서 필수 완료 조건이 아니다.
- 디자인 시스템 고도화, 모바일 UI 완성, 실제 외부 알림 provider 계약은 P2/P3 후속으로 남길 수 있다.
- 단, 위 항목을 남길 경우 `docs/guides/risk-response.md`에 이유와 운영 전 조치가 명확해야 한다.

### 전체 범위 원칙

- `main` 브랜치 고정 운영을 유지한다.
- 한 번에 대규모 rewrite를 하지 않고 phase별로 작은 commit 단위로 닫는다.
- API/DB 계약은 extension over modification 원칙을 따른다.
- 운영 파괴 작업, DB clean/rebuild 전제, 강제 push, `--no-verify`는 금지한다.
- 사용자의 검증 최소화 선호를 반영하되, 보안/DB/상태 전이 변경은 targeted integration test를 생략하지 않는다.
- 각 phase는 `계획 갱신 -> 구현 -> 최소 검증 -> completed archive -> commit/push` 순서로 닫는다.
- 각 phase가 끝날 때마다 `P0/P1 남은 리스크`와 `P2/P3 후속으로 미룬 것`을 구분해 archive한다.

### 우선순위

1. 운영 전환 리스크 감소
2. 큰 서비스/워크플로우 구조 정리
3. 테스트 품질 강화
4. production-like 실행 증거 확보
5. 기능 완성도 확장

## Phase 1. 운영 전환 리스크 줄이기

### 목표

`prod` profile로 운영 전환할 때 seed, Swagger, Prometheus, CORS, secure cookie, secret, management surface가 잘못 열리는 리스크를 더 줄인다.
Phase 1 완료 후에는 "실제 서버만 없을 뿐, prod 전환 시 무엇을 막고 무엇을 확인해야 하는지"가 코드/테스트/문서에서 닫혀 있어야 한다.

### 세부 작업

| Step | 작업 | 파일/범위 | Acceptance Criteria |
| --- | --- | --- | --- |
| 1.1 | prod safety 현재 코드 재점검 | `StartupSafetyValidator`, `SecurityConfig`, `application-prod.yml`, `env-contract.md` | prod에서 seed/swagger/prometheus/JWT fallback/cookie secure 차단 근거가 문서와 코드에 일치 |
| 1.2 | prod-like smoke profile/명령 설계 | `docs/guides/deployment-guide.md`, `docs/guides/env-contract.md` | 실제 클라우드 없이도 prod profile 부팅 전 안전 설정을 확인하는 절차가 존재 |
| 1.3 | prod safety 테스트 보강 | `StartupSafetyValidatorTest`, observability/security integration tests | prod 위험 설정 조합에 대한 실패 테스트가 명확함 |
| 1.4 | CORS/secure cookie 운영 전환 문서 보강 | `risk-response.md`, `deployment-guide.md`, `env-contract.md` | 운영 도메인 확보 후 바꿔야 할 값과 확인 명령이 명확함 |
| 1.5 | README 표현 정리 | `README.md`, `evidence-map.md` | 미배포 약점이 숨김 없이 운영 준비도와 함께 설명됨 |
| 1.6 | P0/P1 운영 리스크 판정 | `risk-response.md`, `COMPLETED.md` | prod 전환 관련 남은 P0/P1 리스크 없음. 남은 항목은 실제 클라우드 계정/도메인 확보 같은 외부 의존성뿐 |

### 검증 계획

- `./gradlew test --tests "com.erp.global.config.StartupSafetyValidatorTest"`
- 필요 시 `./gradlew test --tests "com.erp.integration.ObservabilityIntegrationTest"`
- `git diff --check`
- `rg -n "prod|seed|Swagger|Prometheus|CORS_ALLOWED_ORIGINS|cookie-secure" README.md docs src/main/resources src/test/java`

### 리스크 및 대응

- 리스크: prod-like 실행을 실제 prod DB 없이 구성하면 과한 shell/script가 생길 수 있음.
- 대응: 우선 문서화와 validator/test 중심으로 닫고, 실제 compose smoke는 Phase 4에서 별도 처리한다.

## Phase 2. 큰 서비스/워크플로우 구조 정리

### 목표

면접관이 코드를 열었을 때 orchestration, 상태 전이, audit, notification 책임이 명확하게 보이도록 큰 service의 응집도를 높인다.
Phase 2 완료 후에는 "이 service는 너무 많은 일을 한다"는 P1급 리뷰를 피할 수 있어야 한다.

### 우선 후보

| 후보 | 현재 의심 지점 | 목표 구조 | Non-goals |
| --- | --- | --- | --- |
| `KidApplicationService` 주변 | 입학 신청 상태 전이, 알림, audit, 원생 생성 흐름이 여전히 면접 질문의 핵심 | 상태 전이/usecase 단위 helper 또는 workflow component로 더 분명하게 분리 | API 계약/DB schema 변경 |
| auth/session 영역 | cookie JWT, refresh, active session revoke가 여러 class에 분산 | session lifecycle 흐름을 추적하기 쉽게 naming/경계 정리 | 인증 정책 변경 |
| notification/outbox 영역 | outbox worker, retry, dead-letter, ops API의 설명 경로가 길 수 있음 | 운영 API와 delivery state transition 책임을 더 읽기 쉽게 정리 | 실제 provider 연동 |
| audit/export 영역 | auth audit/domain audit/export/archive purge가 기능별로 넓음 | export/purge/query 책임을 작게 유지 | audit 저장 포맷 변경 |

### 세부 작업

| Step | 작업 | 파일/범위 | Acceptance Criteria |
| --- | --- | --- | --- |
| 2.1 | service size/caller map 조사 | `rg`, `grep`, class별 public method 목록 | 실제 리팩토링 대상 1~2개만 선정 |
| 2.2 | 첫 리팩토링 tranche 선정 | `docs/PLAN.md`, `docs/PROGRESS.md` | 변경 범위와 테스트 범위가 phase 안에서 닫힘 |
| 2.3 | behavior-preserving extraction | 선택된 service/helper | public API와 DB schema 변경 없음 |
| 2.4 | targeted tests 유지/보강 | 관련 `*IntegrationTest` 또는 service test | 기존 주요 상태 전이 통과 |
| 2.5 | evidence map 갱신 | `docs/guides/evidence-map.md` | 리팩토링 후 확인 경로가 더 명확함 |
| 2.6 | 리팩토링 잔여 후보 등급화 | `docs/COMPLETED.md`, 필요 시 `risk-response.md` | 남은 구조 개선이 있더라도 P2/P3 선택 개선으로 설명 가능 |

### 검증 계획

- 선택 도메인 targeted integration test
- `./gradlew compileJava compileTestJava`
- `git diff --check`

### 리스크 및 대응

- 리스크: 리팩토링이 넓어져 기능 변경으로 번질 수 있음.
- 대응: 한 tranche는 한 도메인 책임만 다루고, public API/DB migration을 만들지 않는다.

## Phase 3. 테스트 품질 강화

### 목표

“테스트가 많다”가 아니라 “핵심 상태 전이와 보안 경계가 깨지면 바로 알 수 있다”는 구조로 보강한다.
Phase 3 완료 후에는 핵심 포트폴리오 주장에 대해 "테스트가 어디 있나요?"라는 질문에 즉시 답할 수 있어야 한다.

### 테스트 강화 후보

| 영역 | 보강 이유 | 후보 테스트 |
| --- | --- | --- |
| 입학 신청 상태 전이 | 포트폴리오 핵심 workflow | approve/waitlist/offer/accept/expire 실패 경계 |
| 세션 revoke | 보안 설명의 핵심 | 로그아웃/다른 기기 종료 후 refresh/access 차단 |
| outbox retry/dead-letter | 운영성 설명의 핵심 | retry 가능/불가능 상태, principal-only, idempotency |
| prod safety | 운영 전환 리스크 | prod 위험 설정 조합 실패 케이스 |
| performance story | 성능 주장 근거 | query count regression guard |

### 세부 작업

| Step | 작업 | 파일/범위 | Acceptance Criteria |
| --- | --- | --- | --- |
| 3.1 | 현재 테스트 matrix 작성 | `docs/guides/evidence-map.md` 또는 새 섹션 | 핵심 주장별 테스트 존재/부족이 보임 |
| 3.2 | P0 gap 1~2개 선정 | security/workflow 중심 | 가장 위험한 누락부터 처리 |
| 3.3 | targeted test 추가 | `src/test/java/com/erp/api`, `integration`, `performance` | 새 테스트가 실제 회귀를 잡는 실패 조건을 포함 |
| 3.4 | CI 분리 유지 | `.github/workflows/*` | push quick CI가 과도하게 느려지지 않음 |
| 3.5 | README/evidence 갱신 | README, evidence map | “무엇을 검증하는가”가 명확함 |
| 3.6 | 테스트 gap 최종 분류 | `evidence-map.md`, `COMPLETED.md` | P0/P1 주장에는 테스트 또는 명시적 manual quality 경로가 있음 |

### 검증 계획

- 새로 추가한 targeted test
- 필요 시 `./gradlew fastTest` 또는 `./gradlew integrationTest`
- `git diff --check`

### 리스크 및 대응

- 리스크: Testcontainers 기반 테스트가 늘어 CI 시간이 다시 증가.
- 대응: push CI에는 fast test만 유지하고 heavy test는 manual quality로 둔다.

## Phase 4. Production-like 실행 증거 확보

### 목표

실제 클라우드 배포 없이도 “운영 profile과 배포 자산을 최소한으로 검증했다”는 증거를 만든다.
Phase 4 완료 후에는 "배포는 안 했지만 운영 profile로 무엇을 확인했나요?"라는 질문에 명령과 결과로 답할 수 있어야 한다.

### 세부 작업

| Step | 작업 | 파일/범위 | Acceptance Criteria |
| --- | --- | --- | --- |
| 4.1 | production-like local smoke 범위 정의 | `docs/guides/deployment-guide.md` | 실제 secret 없이 실행하지 않는 항목과 mock 가능한 항목이 구분됨 |
| 4.2 | compose config 검증 정리 | `docker/*`, `deploy/*`, docs | local/prod compose config 해석 명령이 문서화됨 |
| 4.3 | bootJar + prod safety dry-run 설계 | docs 또는 script 후보 | 위험 설정이 validator에서 실패하는 절차가 설명됨 |
| 4.4 | rollback/containment runbook 보강 | deployment guide | 실패 시 되돌릴 파일/명령/환경변수 기준 명확 |
| 4.5 | README에 “미배포지만 검증한 것” 요약 | README, risk response | 미배포 약점과 대체 검증이 균형 있게 설명됨 |
| 4.6 | production-like checklist 고정 | `deployment-guide.md`, `evidence-map.md` | 제출 전 반복 가능한 checklist와 expected result가 있음 |

### 검증 계획

- `./gradlew bootJar`
- `docker compose --env-file docker/.env.example -f docker/docker-compose.yml config`
- `docker compose --env-file deploy/.env.prod.example -f deploy/docker-compose.prod.yml config`가 가능한지 파일 존재 여부부터 확인
- `git diff --check`

### 리스크 및 대응

- 리스크: 가짜 prod 실행이 실제 운영 검증처럼 과장될 수 있음.
- 대응: README와 risk response에서 “cloud 미배포”를 계속 명시한다.

## Phase 5. 기능 완성도 확장

### 목표

새 기능을 무작정 늘리지 않고, 현재 포트폴리오 스토리를 더 강하게 만드는 작은 운영 기능만 추가한다.
Phase 5 완료 후에는 기능 후보를 계속 늘리는 상태가 아니라, 최소 1개 운영 기능을 "실제 운영 도구처럼 쓸 수 있음" 수준으로 닫고 나머지는 명확히 후순위로 보낸다.

### 후보 기능 우선순위

| 순위 | 기능 | 이유 | 예상 변경 범위 | 검증 |
| --- | --- | --- | --- | --- |
| 5.1 | Outbox timeline/search/filter | dead-letter 운영 화면이 더 실제 운영 도구처럼 보임 | outbox API query params, repository, view | principal/teacher 권한, pagination/search tests |
| 5.2 | Audit filter UX/API polish | 감사 로그 설명력 강화 | audit query DTO/view | filter integration tests |
| 5.3 | Active session UX 보강 | 세션 revoke 스토리 강화 | session 화면/API 문구 | auth/session integration tests |
| 5.4 | Parent/Teacher empty/error state 개선 | 실제 사용자 흐름 완성도 | Thymeleaf templates | view endpoint tests |
| 5.5 | Notification channel drill-down | outbox 운영성 확장 | notification/outbox DTO/API/view | targeted outbox tests |

### 세부 작업

| Step | 작업 | 파일/범위 | Acceptance Criteria |
| --- | --- | --- | --- |
| 5.1 | 기능 후보 중 1개만 선정 | PLAN/PROGRESS | 포트폴리오 스토리 강화 효과가 가장 큰 기능 선택 |
| 5.2 | API 계약 먼저 정의 | controller DTO docs | 기존 API 깨지지 않음 |
| 5.3 | 최소 수직 slice 구현 | controller/service/repository/view | 한 화면/한 API 흐름이 끝까지 동작 |
| 5.4 | 권한/입력/상태 테스트 | integration tests | principal/teacher/parent 경계 확인 |
| 5.5 | README/screenshot 필요 여부 판단 | README/assets/docs | 새 기능이 실제로 보여줄 가치가 있으면 반영 |
| 5.6 | 기능 확장 잔여 후보 정리 | `risk-response.md`, `COMPLETED.md` | 남은 기능은 취업 포트폴리오 필수 미완료가 아니라 선택 확장으로 분류 |

### 검증 계획

- 선택 기능 targeted integration test
- `./gradlew compileJava compileTestJava`
- 필요 시 browser smoke
- `git diff --check`

### 리스크 및 대응

- 리스크: 기능 추가가 포트폴리오 메시지를 흐릴 수 있음.
- 대응: “운영성/보안/상태 전이” 스토리에 직접 기여하지 않으면 구현하지 않는다.

## Phase 6. 최종 정리와 제출 패키지 고정

### 목표

Phase 1~5 중 실제 완료한 작업만 기준으로 README, evidence map, risk response, interview guide를 다시 맞춘다.
Phase 6 완료 후에는 이 repository를 제출했을 때 과장된 주장, stale 수치, evidence 없는 핵심 주장, active 문서 잔여 작업이 없어야 한다.

### 세부 작업

| Step | 작업 | Acceptance Criteria |
| --- | --- | --- |
| 6.1 | 완료된 phase만 README에 반영 | 과장된 미완료 항목 없음 |
| 6.2 | evidence map 재정렬 | 주장별 코드/테스트 링크 최신화 |
| 6.3 | risk response 최신화 | 남은 리스크와 완료 리스크 구분 |
| 6.4 | interview guide 최종 압축 | 5분/10분 답변이 최신 구현과 일치 |
| 6.5 | final smoke/CI 상태 확인 | main clean, CI green, active docs 정리 |
| 6.6 | 최종 residual risk 선언 | 남은 것은 비용/외부 계정/운영 도메인 같은 외부 의존성 또는 P2/P3 선택 개선뿐 |

### 검증 계획

- `rg -n "TODO|후속|미완료|1m[0-9]+s|현재 active 작업 없음" README.md docs`
- `git diff --check`
- 필요 시 `./gradlew bootJar`

## 실행 순서

1. Phase 1 운영 전환 리스크 감소를 먼저 시작한다.
2. Phase 1 완료 후 `docs/COMPLETED.md`에 archive하고 commit/push한다.
3. Phase 2는 실제 service/caller map을 보고 범위를 1개 도메인으로 제한한다.
4. Phase 3은 Phase 2에서 건드린 도메인의 테스트 gap부터 보강한다.
5. Phase 4는 실제 클라우드 배포가 아니라 production-like 증거 확보로 제한한다.
6. Phase 5는 기능 후보 중 1개씩만 선택해 수직 slice로 닫는다.
7. 모든 phase는 완료 시 active 문서에서 제거하고 archive로 이관한다.
