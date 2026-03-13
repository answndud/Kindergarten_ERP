# Phase 33: 인증/소셜 감사 로그 도입

## 배경

지금까지 인증 보안은 많이 강화했습니다.

- refresh session rotation
- Redis rate limit
- trusted proxy 기반 client IP 해석
- OAuth2 계정 충돌/연결/해제 정책

하지만 운영 관점에서는 한 가지가 비어 있었습니다.

- 누가 로그인에 성공했는가
- 어떤 IP에서 refresh 실패가 반복됐는가
- 누가 소셜 계정을 연결하거나 해제했는가

이 이벤트들이 DB에 남지 않으면
보안 사고 분석, 운영 대응, 면접 설명 모두 약해집니다.

## 핵심 결정

1. **인증 감사 로그는 DB에 별도 테이블로 저장한다.**
   - Redis는 TTL 기반 운영 데이터에는 적합하지만
   - 장기 보존/조회/증빙에는 적합하지 않습니다.

2. **감사 로그 저장은 메인 인증 트랜잭션과 분리한다.**
   - 로그인 실패나 refresh 실패는 원래 트랜잭션이 rollback될 수 있습니다.
   - 그래도 실패 이벤트는 남아야 하므로 `REQUIRES_NEW`로 저장합니다.

3. **감사 로그 저장 실패가 인증 플로우를 깨뜨리면 안 된다.**
   - 감사 로그는 중요하지만 주 기능보다 우선할 수는 없습니다.
   - 저장 실패는 `warn`으로만 남기고 본래 요청은 계속 처리합니다.

4. **개인정보는 최소한만 저장한다.**
   - 비밀번호, access token, refresh token 원문은 저장하지 않습니다.
   - `memberId`, `email`, `provider`, `eventType`, `result`, `reason`, `clientIp`만 남깁니다.

5. **`member_id`는 FK로 묶지 않는다.**
   - 탈퇴/비활성화/이력 보존 상황에서도 감사 로그는 살아 있어야 합니다.
   - 감사 로그는 운영 증적이므로 원본 회원 row lifecycle과 느슨하게 결합합니다.

## 구현 요약

### 1) DB / Entity

- `V10__create_auth_audit_log.sql`
  - `auth_audit_log` 테이블 추가
  - `created_at`, `(member_id, created_at)`, `(event_type, created_at)` 인덱스 추가

- `AuthAuditLog`
  - 인증 이벤트 단위 row
  - provider/reason/clientIp는 nullable로 두어 refresh 실패나 익명 실패도 저장 가능하게 구성

- `AuthAuditEventType`
  - `LOGIN`, `REFRESH`, `SOCIAL_LINK`, `SOCIAL_UNLINK`

- `AuthAuditResult`
  - `SUCCESS`, `FAILURE`

### 2) 서비스 / 저장 정책

- `AuthAuditLogService`
  - `@Transactional(propagation = REQUIRES_NEW)`
  - login, refresh, social link, social unlink 성공/실패 저장 메서드 분리
  - email/reason/clientIp 정규화
  - 저장 실패는 `warn` 로깅 후 swallow

### 3) 인증 흐름 반영

- `AuthService`
  - 로컬 로그인 성공/실패 기록
  - refresh 성공/실패 기록
  - 실패 reason은 `ErrorCode` 코드 기준으로 남김

- `AuthApiController`
  - refresh cookie 자체가 없는 경우도 `A003` 실패 로그 저장

- `OAuth2AuthenticationSuccessHandler`
  - 소셜 로그인 성공 기록
  - 계정 충돌, 일반 실패 기록
  - social link success/failure 기록

- `MemberApiController`
  - social unlink success/failure 기록

## 저장 필드

| 필드 | 의미 |
|------|------|
| `memberId` | 식별 가능한 회원이면 저장, 익명 실패는 `null` 허용 |
| `email` | 운영 확인용 로그인 식별자 |
| `provider` | `LOCAL`, `GOOGLE`, `KAKAO` |
| `eventType` | `LOGIN`, `REFRESH`, `SOCIAL_LINK`, `SOCIAL_UNLINK` |
| `result` | `SUCCESS`, `FAILURE` |
| `reason` | 실패 코드 또는 정책 사유 (`A001`, `A005`, `provider-mismatch` 등) |
| `clientIp` | trusted proxy 정책을 거친 최종 client IP |
| `createdAt` | 감사 시점 |

## 테스트

- `AuthApiIntegrationTest`
  - 로그인 성공/실패 감사 로그 검증
  - refresh 실패 감사 로그 검증

- `MemberApiIntegrationTest`
  - social unlink 성공/실패 감사 로그 검증

- `OAuth2AuthenticationSuccessHandlerTest`
  - social login conflict 감사 로그 검증
  - social link success/failure 감사 로그 검증

## 테스트 설계 메모

감사 로그 저장은 `REQUIRES_NEW`이고,
통합 테스트 베이스는 `@Transactional`입니다.

MySQL 기본 격리 수준에서는 테스트 시작 시점 스냅샷 때문에
같은 테스트 트랜잭션 안에서 새로 커밋된 감사 로그가 보이지 않을 수 있습니다.

그래서 테스트 assertion은 `BaseIntegrationTest.readCommitted(...)` 헬퍼로
별도 트랜잭션에서 다시 읽도록 맞췄습니다.

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.global.security.oauth2.OAuth2AuthenticationSuccessHandlerTest"
git diff --check
```

## 인터뷰 포인트

- "왜 Redis가 아니라 DB에 저장했나요?"
  - rate limit과 session은 TTL 운영 데이터이고,
  - 감사 로그는 사후 분석/증빙 데이터라 성격이 다르기 때문입니다.

- "왜 FK를 걸지 않았나요?"
  - 회원 탈퇴 후에도 인증 이벤트 이력은 운영 증적로 남아야 하기 때문입니다.

- "왜 `REQUIRES_NEW`를 썼나요?"
  - 로그인 실패나 refresh 실패는 본 요청이 예외로 끝나더라도 감사 이벤트는 반드시 남겨야 하기 때문입니다.

- "감사 로그 실패가 메인 플로우를 막지 않게 한 이유는?"
  - 인증 가용성을 우선하되, 저장 실패는 `warn`으로 남겨 운영에서 추적 가능하게 했기 때문입니다.
