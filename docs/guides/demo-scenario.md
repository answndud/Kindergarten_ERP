# Demo Scenario Runbook

기준일: 2026-05-19

이 문서는 Kindergarten ERP를 면접/시연에서 빠르게 보여주기 위한 클릭 순서와 기대 화면을 정리합니다.

## 1. 실행 전제

```bash
cp docker/.env.example docker/.env
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d
SPRING_PROFILES_ACTIVE=demo ./gradlew bootRun
```

- `demo` 프로파일은 `local` 설정을 포함합니다.
- 시드 데이터는 자동 활성화됩니다.
- Swagger/OpenAPI와 app-port Prometheus는 demo에서만 열립니다.

## 2. Demo 계정

| 역할 | 계정 | 비밀번호 | 먼저 볼 화면 |
| --- | --- | --- | --- |
| 원장 | `principal@test.com` | `test1234!` | `/dashboard`, `/applications/pending`, `/notification-outbox` |
| 교사 | `teacher1@test.com` | `test1234!` | `/attendance`, `/attendance-requests`, `/calendar` |
| 학부모 | `parent1@test.com` | `test1234!` | `/applications/pending`, `/notifications`, `/notepad` |

## 3. 5분 시연

1. 원장으로 로그인합니다.
2. `/dashboard`에서 출석/회원/운영 지표가 비어 있지 않음을 보여줍니다.
3. `/applications/pending`에서 `PENDING`, `WAITLISTED`, `OFFERED` 상태가 함께 보이는 검토 큐를 설명합니다.
4. `/notification-outbox`에서 dead-letter 채널 summary와 retry 버튼을 보여줍니다.
5. `/audit-logs`, `/domain-audit-logs`에서 인증 이벤트와 업무 상태 변경 이력을 보여줍니다.

## 4. 10분 시연

1. 원장 로그인: `principal@test.com / test1234!`
2. 신청 처리 큐: 입학 신청, 대기열, offer 상태가 같은 큐에서 관리되는 구조를 설명합니다.
3. 알림 Outbox 운영: dead-letter 목록을 확인하고 하나를 retry합니다.
4. 캘린더: 유치원 전체 일정, 반 반복 일정, 개인 운영 점검 일정이 함께 조회되는 구조를 설명합니다.
5. 감사 로그: 인증 감사 로그와 업무 감사 로그가 분리된 이유를 설명합니다.
6. README 성능 표: Notepad/Dashboard query count와 CI 시간 단축 수치를 설명합니다.
7. Swagger: `/swagger-ui.html`에서 운영 API 설명을 보여줍니다.

## 5. 질문을 받았을 때 열 파일

| 질문 | 열 파일 |
| --- | --- |
| 인증/세션은 어떻게 관리하나요? | `src/main/java/com/erp/global/security/*`, `src/main/java/com/erp/domain/auth/service/*` |
| 권한/tenant 경계는 어디서 막나요? | `src/main/java/com/erp/global/security/access/AccessPolicyService.java` |
| 알림 실패는 어떻게 재처리하나요? | `src/main/java/com/erp/domain/notification/*`, `src/main/resources/templates/notifications/outbox.html` |
| 입학 workflow는 어디서 관리하나요? | `src/main/java/com/erp/domain/kidapplication/service/*` |
| 운영 환경 변수는 어디에 정리했나요? | `docs/guides/env-contract.md` |
| 완료 이력과 검증은 어디에 남기나요? | `docs/COMPLETED.md` |

## 6. 시연 실패 시 빠른 복구

- 로그인 실패: `demo` 프로파일인지, seed가 켜졌는지 확인합니다.
- 화면이 비어 있음: 기존 DB에 seed principal이 이미 있어 `DataLoader`가 skip됐을 수 있습니다. 로컬 DB를 새로 만들거나 seed 계정을 삭제한 뒤 다시 실행합니다.
- Outbox가 비어 있음: `/notification-outbox`는 demo seed의 dead-letter 샘플이 필요합니다.
- Swagger가 닫힘: `SPRING_PROFILES_ACTIVE=demo`인지 확인합니다.
- Redis/MySQL 연결 실패: `docker compose --env-file docker/.env -f docker/docker-compose.yml ps`로 컨테이너 상태를 확인합니다.
