# PLAN.md

## 작업명
- CI 자동화 (GitHub Actions + Testcontainers 테스트) + 인터뷰용 문서화

## 1) 목표 / 범위
- 이미 전환한 MySQL/Redis Testcontainers 기반 테스트 스택을 GitHub Actions에서 자동 실행되도록 연결한다.
- 저장소에 없는 CI 워크플로우를 새로 추가하고, 실패 시 디버깅 가능한 아티팩트 업로드까지 포함한다.
- README와 `docs/phase/`에 CI 전략과 인터뷰용 설명 포인트를 정리한다.
- 이번 세션 결과물은 CI 워크플로우 추가 + 문서 보강 + 설정 검증이다.

## 2) 세부 작업 단계
1. CI 현황 점검
   - 현재 저장소의 `.github/workflows` 유무와 기존 자동화 상태 확인
   - Testcontainers 기반 테스트를 GitHub Actions runner에서 실행하기 위한 전제 확인

2. 워크플로우 구현
   - GitHub Actions workflow 추가
   - Java/Gradle 캐시, `./gradlew test`, 실패 시 test report artifact 업로드 구성
   - 수동 실행(`workflow_dispatch`)과 PR/push 트리거 구성

3. 검증
   - YAML 문법/구조 검토
   - README/문서 설명과 실제 워크플로우 구성이 일치하는지 교차 점검

4. 문서화
   - `docs/phase/`에 CI 자동화 배경, GitHub Actions 설계, 면접 포인트 정리
   - README에 테스트/CI 전략을 짧게 연결

## 3) 검증 계획
- 워크플로우 구조 검토
  - `.github/workflows/ci.yml` 내용 점검
- 로컬 교차 검증
  - README/phase 문서가 실제 실행 명령(`./gradlew test`)과 일치하는지 확인

## 4) 리스크 및 대응
- GitHub Actions에서 Docker/Testcontainers 초기화가 느릴 위험
  - 대응: runner 기본 Docker 환경을 활용하고 Gradle 캐시를 활성화
- 워크플로우 실패 시 원인 파악이 어려울 위험
  - 대응: test report와 test-results를 artifact로 업로드
- README가 기능 나열만 하고 자동화 전략을 설명하지 못할 위험
  - 대응: CI/Testcontainers를 포트폴리오 관점의 신뢰성 포인트로 별도 서술
