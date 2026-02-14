# Cache Policy and Invalidation

## 목적

성능 최적화에서 캐시 도입/운영 규칙(TTL, 키 설계, 무효화)을 표준화합니다.

## 캐시 적용 대상

### Dashboard 통계

- 키: `dashboardStatistics:{kindergartenId}:{range}`
- TTL: 60초
- 전략: 조회 빈도 높고 쓰기 대비 읽기 비율이 높은 구간에 적용

### Refresh Token (Redis)

- 키: `refresh:{email}`
- TTL: Refresh 만료시간과 동일
- 전략: 단일 키 O(1) 조회/삭제, 패턴 검색 금지

## 무효화 정책

### Dashboard

- 출석 쓰기(생성/수정) 시 관련 통계 캐시 무효화
- 공지 쓰기(생성/수정/삭제) 시 관련 통계 캐시 무효화

### Refresh Token

- 로그아웃 시 즉시 삭제
- 재로그인/재발급 시 기존 키 덮어쓰기 + TTL 재설정

## 금지 규칙

- `KEYS` 기반 패턴 조회 금지
- TTL 없는 영구 키 저장 금지
- 경로별 다른 직렬화 포맷 혼용 금지

## 검증 체크

- 저장 직후 TTL 존재 여부 테스트
- 캐시 hit/miss 성능 비교 테스트
- 무효화 후 즉시 최신 값 반영 테스트

## 트레이드오프

- 장점: 응답시간/p95 안정화, DB read 감소
- 단점: 정합성/무효화 복잡도 증가, 운영 규칙 미준수 시 장애 가능
