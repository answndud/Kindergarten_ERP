# Phase 35: 원장 전용 인증 감사 로그 조회 API 추가

> Update
>
> 이 문서는 조회 API를 처음 도입한 시점의 결정 로그입니다.
> 현재 상태는 `phase38_auth_audit_retention_and_denormalization.md`까지 반영되어,
> `auth_audit_log.kindergarten_id` 비정규화와 retention/archive 정책이 추가됐습니다.

## 배경

Phase 33에서 login/refresh/social link/unlink를 DB 감사 로그로 저장했지만,
그 상태만으로는 아직 반쪽이었습니다.

- 로그는 저장되지만 운영자가 조회할 수 없음
- tenant 경계를 고려한 조회 정책이 없음

즉 "감사 로그를 남긴다"까지는 했지만,
"운영에서 어떻게 보느냐"는 닫히지 않았습니다.

## 핵심 결정

1. **조회 API는 원장만 사용할 수 있다.**
   - 인증 이벤트는 민감 정보이므로 teacher/parent까지 열지 않습니다.

2. **당시 principal은 자기 유치원 소속 member 기반 로그만 조회했다.**
   - 이 시점에는 `auth_audit_log.kindergartenId`가 없었습니다.
   - 그래서 `memberId -> member.kindergarten` 기준으로 tenant 범위를 계산했습니다.

3. **익명 실패 로그는 principal 조회에서 제외한다.**
   - 예: 존재하지 않는 이메일 로그인 실패
   - 이런 이벤트는 특정 유치원에 안전하게 귀속할 수 없으므로 tenant API에서 제외합니다.

4. **필터는 운영자 관점 기준으로만 연다.**
   - `eventType`, `result`, `provider`, `email`, `from`, `to`
   - 로그 원문 export보다 우선순위가 높은 건 "빠르게 좁혀 보는 것"입니다.

## 구현 요약

### 1) 조회 서비스

- `AuthAuditLogQueryService`
  - requester를 `MemberService.getMemberByIdWithKindergarten`로 조회
  - 원장 + 유치원 소속 여부를 검증
  - 날짜는 `from` 시작일 포함, `to + 1 day` 미만으로 처리
  - 페이지 크기는 최대 100으로 제한

### 2) Repository (당시 기준)

- `AuthAuditLogRepository.searchByKindergartenId(...)`
  - `AuthAuditLog.memberId = Member.id`
  - `member.kindergarten.id = :kindergartenId`
  - optional filter
  - `createdAt desc, id desc`

### 3) Controller

- `GET /api/v1/auth/audit-logs`
  - role: `PRINCIPAL`
  - filters:
    - `eventType`
    - `result`
    - `provider`
    - `email`
    - `from`
    - `to`
    - `page`
    - `size`

## API 응답 예시

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 12,
        "memberId": 3,
        "email": "teacher@test.com",
        "provider": "GOOGLE",
        "eventType": "SOCIAL_UNLINK",
        "result": "FAILURE",
        "reason": "A010",
        "clientIp": "198.51.100.12",
        "createdAt": "2026-03-13T21:10:11"
      }
    ]
  }
}
```

## 테스트

- `AuthAuditApiIntegrationTest`
  - principal이 자기 유치원 로그만 보는지 검증
  - 다른 유치원 로그와 익명 실패 로그가 제외되는지 검증
  - eventType/result/provider/email/date 필터 검증
  - teacher forbidden 검증

## 검증

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.AuthAuditApiIntegrationTest"
git diff --check
```

## 인터뷰 포인트

- "왜 principal API에서 익명 실패 로그를 안 보여줬나요?"
  - tenant에 안전하게 귀속할 수 없는 로그를 열면 데이터 경계가 흐려지기 때문입니다.

- "왜 `kindergartenId`를 로그 테이블에 바로 저장하지 않았나요?"
  - 당시에는 최소 필드로 감사 로그를 먼저 도입하고, 조회 시 member 소속을 기준으로 tenant 범위를 계산했습니다.
  - 이후 phase38에서 denormalized `kindergartenId`를 추가해 조회 비용과 known email 실패 귀속 문제를 함께 정리했습니다.

- "왜 teacher는 못 보게 했나요?"
  - 인증 이벤트는 보안 데이터라서 운영 책임이 큰 principal까지만 열었습니다.
