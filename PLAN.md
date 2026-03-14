# PLAN.md

## 작업명
- 후속 고도화 20차 (감사 로그 denormalization + retention/archive 정책)

## 1) 목표 / 범위
- `auth_audit_log`에 `kindergarten_id`를 직접 저장해 principal 조회/export에서 `member` join을 제거한다.
- known email 실패 로그도 tenant에 안전하게 귀속할 수 있으면 조회 대상에 포함되도록 감사 로그 저장 로직을 보강한다.
- 오래된 감사 로그를 archive table로 이동하고, 더 오래된 archive는 purge하는 retention 정책과 스케줄러를 추가한다.
- README, 결정 로그, 진행 기록을 감사 로그 lifecycle 기준으로 갱신한다.

## 2) 세부 작업 단계
1. 감사 로그 schema denormalization
   - Flyway로 `auth_audit_log.kindergarten_id` 추가 및 backfill
   - active/archive 테이블용 인덱스 설계
   - 저장 서비스가 `memberId/email` 기준으로 `kindergartenId`를 해석해 함께 기록

2. 조회/export 쿼리 최적화
   - principal 조회/export가 `kindergarten_id` 기반으로 바로 필터링되게 repository query 단순화
   - known email 실패 로그가 principal 조회/export에 포함되는 회귀 테스트 추가

3. retention/archive 정책
   - archive table 생성
   - archive-after / delete-after / cron / batch-size 설정 추가
   - 스케줄러 서비스로 active -> archive 이동 및 archive purge 구현
   - 수동 호출 가능한 서비스 메서드와 테스트 추가

4. 테스트 / 문서 / 배포
   - 감사 로그 API/retention 서비스 회귀 테스트 보강
   - README, 결정 로그, 진행 기록 갱신
   - add/commit/push 및 GitHub Actions 결과 확인

## 3) 검증 계획
- 컴파일 검증
  - `./gradlew compileJava compileTestJava`
- 핵심 회귀 검증
  - `./gradlew test --tests "com.erp.api.AuthAuditApiIntegrationTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.integration.ObservabilityIntegrationTest" --tests "com.erp.integration.AuthAuditRetentionIntegrationTest"`
- 전체 테스트
  - `./gradlew test`
- 마이그레이션/문서 검증
  - `git diff --check`

## 4) 리스크 및 대응
- `kindergarten_id` backfill이 기존 감사 로그를 잘못 귀속할 수 있음
  - 대응: `member_id` 기준 row만 SQL backfill하고, 이메일 기반 귀속은 저장 시점에만 적용한다
- retention job이 과도하게 많은 row를 한 번에 옮겨 락을 오래 잡을 수 있음
  - 대응: batch-size 기반 반복 처리로 제한하고, active/archive purge를 분리한다
- scheduled job이 테스트나 local 부팅 시 예기치 않게 작동할 수 있음
  - 대응: `enabled` 설정을 분리하고, 테스트는 수동 서비스 호출로 검증한다
