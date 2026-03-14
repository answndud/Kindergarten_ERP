# Phase 38: 감사 로그 tenant 비정규화 + retention/archive 정책

## 배경

Phase 37까지 오면서 인증 감사 로그는

- DB 저장
- 원장 전용 조회/export API
- 운영 화면
- 반복 로그인 실패 알림
- Grafana 관측

까지 닫혔습니다.

하지만 운영 관점에서 두 가지가 남아 있었습니다.

1. principal 조회/export가 여전히 `member -> kindergarten` join에 의존해서, 조회 비용과 known email 실패 귀속이 아쉬웠습니다.
2. 감사 로그가 계속 쌓이기만 하고, active/archived lifecycle이 없었습니다.

즉 "로그를 본다"까지는 됐지만,
"로그를 오래 운영한다"와 "tenant 범위를 더 싸게 계산한다"는 아직 미완성이었습니다.

## 목표

- `auth_audit_log.kindergarten_id`를 직접 저장해 principal 조회/export에서 tenant join을 제거한다.
- `memberId`가 없는 known email 로그인 실패도 write-time에 tenant를 안전하게 귀속한다.
- 오래된 감사 로그를 archive table로 이동하고, 더 오래된 archive는 purge하는 retention 정책을 추가한다.

## 1) `kindergarten_id` 비정규화

추가 변경:

- `V11__denormalize_auth_audit_log_and_add_retention_archive.sql`
  - `auth_audit_log.kindergarten_id` 추가
  - 기존 row는 `member_id -> member.kindergarten_id` 기준 backfill
  - tenant 조회 패턴용 인덱스 추가

저장 정책:

- `AuthAuditLogService`
  - `memberId`가 있으면 `findKindergartenIdById(...)`로 귀속
  - `memberId`가 없고 email이 known member면 `findKindergartenIdByEmail(...)`로 귀속
  - 완전히 익명인 실패는 `kindergartenId = null`

조회/export 정책:

- `AuthAuditLogRepository.searchByKindergartenId(...)`
  - 더 이상 `member` join 없이 `log.kindergartenId = :kindergartenId`로 필터링
- 결과적으로 principal API는
  - 자기 유치원 로그만 보되
  - known email 실패처럼 `memberId`가 없는 이벤트도 안전하게 포함할 수 있게 됨

왜 write-time 비정규화인가?

- 조회 때마다 tenant join을 반복하는 것보다 단순합니다.
- export와 운영 화면이 같은 테이블 조건을 그대로 재사용할 수 있습니다.
- 익명 실패를 억지로 노출하지 않고, 안전하게 귀속되는 범위만 넓힐 수 있습니다.

## 2) retention/archive 정책

추가 구성:

- active table: `auth_audit_log`
- archive table: `auth_audit_log_archive`
- 설정:
  - `app.security.auth-audit-retention.enabled`
  - `archive-after`
  - `delete-after`
  - `cron`
  - `batch-size`

정책:

- active 로그는 `created_at` 기준 `archive-after`가 지나면 archive table로 이동
- archive 로그는 `archived_at` 기준 `delete-after`가 지나면 purge
- 배치 처리로 큰 락을 피하고, local/test에서는 `executeRetention()`으로 수동 검증 가능

구현:

- `AuthAuditRetentionService`
  - `@Scheduled` 실행
  - batch 단위 `active -> archive`
  - batch 단위 old archive purge
  - 결과는 `AuthAuditRetentionResult(archivedCount, purgedCount)`로 노출

왜 soft delete가 아니라 archive table인가?

- 운영 조회는 최근 로그 중심이므로 active table을 가볍게 유지하는 편이 단순합니다.
- 보존 가치가 있는 로그는 archive에 남기고, active 쿼리는 계속 짧게 유지할 수 있습니다.
- purge 기준을 `archived_at`으로 별도 가져가면 total retention을 명확하게 설명할 수 있습니다.

## 3) 테스트

추가/보강:

- `AuthAuditApiIntegrationTest`
  - principal이 same-tenant attributed log만 조회하는지 검증
  - known email 로그인 실패 저장 시 `kindergartenId`가 채워지는지 검증
- `AuthAuditRetentionIntegrationTest`
  - 오래된 active 로그 archive 이동 검증
  - 오래된 archive purge 검증
- `TestData.cleanup()`
  - `auth_audit_log_archive` 정리 추가

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.AuthAuditApiIntegrationTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.integration.AuthAuditRetentionIntegrationTest"
./gradlew test
git diff --check
```

## 면접에서 강조할 포인트

1. "감사 로그를 남기는 것에서 끝내지 않고, tenant 필터 비용과 데이터 lifecycle까지 같이 설계했습니다."
2. "known email 실패는 write-time `kindergarten_id`로 귀속하고, 완전히 익명인 실패만 계속 제외해서 보안 경계를 유지했습니다."
3. "운영 로그는 active/archived lifecycle이 필요해서 archive table + purge scheduler로 관리 정책까지 닫았습니다."
4. "이 단계부터는 기능 추가보다 운영 비용과 장기 보존 정책을 다루는 백엔드 이야기로 확장됩니다."
