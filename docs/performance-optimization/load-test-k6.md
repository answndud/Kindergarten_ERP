# k6 부하 테스트 가이드 (Notepad / Dashboard)

## 목적

포트폴리오 성능 스토리에서 "단일 요청 최적화"를 넘어
"동시 요청에서도 안정적인지"를 보여주기 위한 부하 테스트 가이드입니다.

## 스크립트 위치

- `scripts/performance/k6-auth-notepad-dashboard.js`

## 사전 조건

1. 애플리케이션 실행: `./gradlew bootRun --args='--spring.profiles.active=local'`
2. MySQL/Redis 실행: `docker compose -f docker/docker-compose.yml up -d`
3. 테스트 계정 및 데이터 준비
   - 학부모 계정: Notepad 목록 조회 가능
   - 원장 계정: Dashboard 통계 조회 가능

## 실행 예시

### 1) 기본 실행

```bash
k6 run scripts/performance/k6-auth-notepad-dashboard.js
```

### 2) 환경변수 지정 실행

```bash
BASE_URL=http://localhost:8080 \
PARENT_EMAIL=parent@test.com \
PARENT_PASSWORD=test1234 \
PRINCIPAL_EMAIL=principal@test.com \
PRINCIPAL_PASSWORD=test1234 \
CLASSROOM_ID=1 \
k6 run scripts/performance/k6-auth-notepad-dashboard.js
```

## 시나리오 구성

- notepad_list
  - VU 10, 30초 고정
  - 로그인 후 반별 알림장 목록 조회
- dashboard_stats
  - VU 5, 30초 고정
  - 로그인 후 대시보드 통계 조회

## 기본 임계값

- `http_req_failed < 1%`
- `http_req_duration p95 < 800ms`

## 결과 정리 템플릿

| Scenario | VU | Duration | Avg | p95 | Error Rate |
|---|---:|---:|---:|---:|---:|
| Notepad list | 10 | 30s | TBD | TBD | TBD |
| Dashboard stats | 5 | 30s | TBD | TBD | TBD |

## 포트폴리오 작성 팁

1. 코드 개선 전/후를 같은 스크립트로 비교한다.
2. 평균값보다 p95를 먼저 강조한다.
3. 실패율과 함께 제시해 안정성도 함께 설명한다.
