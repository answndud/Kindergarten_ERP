# Phase 17: JWT Refresh Token 세션화와 Rotation

## 배경

- 기존 refresh token 저장 구조는 `refresh:{email}` 단일 키였다.
- 이 구조에서는 같은 계정으로 다른 기기에서 로그인하면 이전 refresh token이 조용히 덮어써졌다.
- refresh 요청도 access token만 재발급하고 refresh token은 그대로 유지해, 세션 수명과 탈취 대응 설명이 약했다.

## 목표

1. refresh token을 사용자 단위가 아니라 세션 단위로 저장한다.
2. refresh 시 refresh token rotation을 적용한다.
3. 로그아웃은 현재 세션만 정리하고, 탈퇴는 전체 세션을 정리하도록 분리한다.

## 결정

1. JWT claim에 `memberId`, `sessionId`, `tokenType`, `jti`를 추가했다.
2. Redis 키는 아래처럼 분리했다.
   - 개별 세션: `refresh:session:{memberId}:{sessionId}`
   - 세션 목록: `refresh:sessions:{memberId}`
3. refresh 요청은 같은 `sessionId`를 유지한 채 access/refresh token을 모두 재발급한다.
4. 현재 설계는 access token을 Redis 조회 없이 검증하는 대신, 로그아웃 후 다른 기기의 access token은 짧은 TTL 동안만 유효할 수 있는 trade-off를 유지했다.

## 구현 요약

### 1) 토큰 claim 확장

- `JwtTokenProvider`
  - access/refresh token 생성 시 `memberId`, `sessionId`, `tokenType`, `jti` 저장
  - `getMemberId`, `getSessionId`, `isRefreshToken` 유틸리티 추가

### 2) AuthService 세션 관리

- 로그인 시 세션별 UUID 생성
- refresh token을 세션 키에 TTL과 함께 저장
- refresh 시
  - JWT 서명/만료 확인
  - `tokenType=refresh` 확인
  - Redis 저장값 일치 확인
  - access token + refresh token rotation
- 로그아웃 시 현재 세션 키만 제거
- 회원 탈퇴 시 세션 목록을 순회하며 모든 refresh 세션 제거

### 3) 컨트롤러/필터 보강

- `/api/v1/auth/logout`
  - refresh cookie에서 현재 세션을 식별해 revoke
- `/api/v1/members/withdraw`
  - 탈퇴 처리 후 전체 refresh 세션 제거 + 현재 쿠키 만료
- `JwtFilter`
  - 탈퇴/비활성 사용자 access token이 남아 있어도 예외를 터뜨리지 않고 인증 실패로 처리

## 검증

- `AuthApiIntegrationTest`
  - 로그인 후 refresh token이 세션 키에 저장되는지 검증
  - refresh 시 같은 세션 ID로 새 refresh token이 rotation 되는지 검증
  - 로그아웃이 현재 세션만 정리하는지 검증
- `MemberApiIntegrationTest`
  - 회원 탈퇴 시 모든 refresh 세션이 제거되고 쿠키가 만료되는지 검증

## 인터뷰 답변 포인트

### 왜 email 단일 키를 버렸는가

- 멀티 디바이스 환경에서는 계정 단위 키가 세션을 표현하지 못한다.
- 면접에서 중요한 건 "로그인 가능"보다 "세션이 무엇인지 모델링했는가"다.

### 왜 refresh rotation을 넣었는가

- 탈취된 refresh token의 재사용 가능 시간을 줄이기 위해서다.
- rotation 없이 장기 토큰만 유지하면 세션 설명이 취약해진다.

### 왜 access token blacklist까지는 하지 않았는가

- 현재 프로젝트는 access token 검증을 Redis 조회 없이 처리해 API 경로를 단순하게 유지한다.
- 대신 refresh revoke는 즉시 반영되고, access token은 짧은 TTL 안에서만 남는 구조다.

## 트레이드오프

- 장점
  - 멀티 디바이스 세션 모델이 명확하다
  - refresh 탈취 대응력이 좋아진다
  - 로그아웃/탈퇴 정책을 코드로 설명할 수 있다
- 단점
  - Redis 키 수가 늘어난다
  - access token 즉시 전역 강제 만료는 별도 blacklist 없이 불가능하다
