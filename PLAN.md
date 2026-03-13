# PLAN.md

## 작업명
- 후속 고도화 2차 (원격 CI 확인 + 인증 rate limit 하드닝)

## 1) 목표 / 범위
- 방금 push한 Node24 호환 workflow가 GitHub Actions에서 실제로 정상 동작하는지 확인한다.
- 로그인/refresh API에 Redis 기반 rate limit을 추가해 brute-force 및 token abuse 대응을 보강한다.
- 테스트와 인터뷰용 문서까지 함께 정리한다.

## 2) 세부 작업 단계
1. 원격 CI 실행 확인
   - `2b3f858` 기준 GitHub Actions run 상태 확인
   - 실패 시 원인 파악 후 즉시 수정

2. 인증 rate limit 설계
   - 로그인/refresh에 적용할 키 전략, 윈도우, 임계치 정의
   - Redis 기반 fixed window 또는 유사한 단순 정책으로 구현

3. 코드/테스트 반영
   - controller/service에 rate limit 적용
   - 성공/실패/제한 초과 케이스 통합 테스트 추가

4. 문서화 및 검증
   - `docs/phase/`와 README 보강
   - `./gradlew compileJava compileTestJava`
   - 관련 통합 테스트 실행

## 3) 검증 계획
- GitHub Actions run 상태 확인
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`
  - `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml')"`
  - `git diff --check`

## 4) 리스크 및 대응
- rate limit이 너무 공격적이면 정상 사용자도 차단될 위험
  - 대응: 로그인/refresh 각각 보수적인 기본값으로 시작하고, IP와 이메일(또는 session) 축을 분리
- 테스트에서 Redis 상태가 케이스 간 섞일 위험
  - 대응: 기존 `BaseIntegrationTest`의 Redis flush를 활용하고, 제한 초과 테스트는 독립 시나리오로 작성
- 원격 CI run이 아직 진행 중일 수 있음
  - 대응: 먼저 상태를 확인하고, 코드 변경은 별도 커밋 단위로 분리
