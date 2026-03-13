# PLAN.md

## 작업명
- 후속 고도화 5차 (인증 Client IP 신뢰 모델 하드닝)

## 1) 목표 / 범위
- 인증 rate limit에서 사용하는 Client IP 해석 로직을 신뢰 가능한 프록시 기준으로 정리한다.
- 임의 클라이언트가 `X-Forwarded-For`, `X-Real-IP`를 조작해 로그인/refresh rate limit을 우회하지 못하게 한다.
- 인터뷰에서 설명 가능한 운영형 보안 결정으로 문서화하고 회귀 테스트를 추가한다.

## 2) 세부 작업 단계
1. 현재 IP 해석 경로 점검
   - `AuthApiController`, rate limit 테스트, 설정 파일을 검토한다.
   - 어떤 조건에서 전달 헤더를 신뢰할지 정책을 결정한다.

2. Client IP resolver 구현
   - trusted proxy 여부를 판단하는 resolver/properties를 추가한다.
   - trusted proxy인 경우에만 `X-Forwarded-For`, `X-Real-IP`를 사용하도록 변경한다.

3. 회귀 테스트 추가
   - 임의 remote address에서 조작된 forwarded header가 무시되는지 검증한다.
   - loopback 또는 trusted proxy에서는 전달 헤더가 반영되는지 검증한다.

4. 문서화 및 검증
   - `README.md`, `docs/phase/`에 보안 의도와 운영 트레이드오프를 정리한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 프록시 신뢰 기준을 너무 엄격하게 잡으면 실제 reverse proxy 환경에서 원본 IP를 못 읽을 수 있음
  - 대응: loopback은 기본 신뢰하고, 추가 trusted proxy는 설정으로 열어둔다
- forwarded header 파싱을 잘못 구현하면 정상 요청이 `unknown`으로 묶일 수 있음
  - 대응: 유효하지 않은 헤더는 무시하고 `remoteAddr`로 안전하게 fallback 한다
- 테스트가 구현 세부에 과도하게 묶이면 유지보수가 어려움
  - 대응: rate limit 결과(429/미발생) 기준으로 검증하고 내부 key 문자열에는 의존하지 않는다
