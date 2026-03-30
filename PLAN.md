# PLAN.md

## 작업명
- 초보자용 배포 전략/가이드 문서화 + 배포 자산 추가 + 원격 반영

## 1) 목표 / 범위
- 현재 Kindergarten ERP를 스프링부트 백엔드 취업 포트폴리오 관점에서 가장 유리한 배포 방식으로 정리한다.
- 초보자도 이 문서 하나만 보고 `계정 생성 -> 인프라 준비 -> 환경변수 세팅 -> 첫 배포 -> 검증 -> 운영/롤백`까지 따라갈 수 있게 만든다.
- 비용은 2026-03-30 기준 공식 가격/공식 문서 기반 추정으로 시나리오별 비교 표를 제공한다.
- 문서에서 멈추지 않고 실제 배포용 자산(`Dockerfile`, `deploy/*`, `.github/workflows/cd.yml`)까지 저장소에 추가한다.
- 시크릿이 들어갈 수 있는 파일 패턴은 `.gitignore`에 명시적으로 반영한다.
- 최종적으로 작업 브랜치에서 commit/push까지 마친다.

## 2) 세부 작업 단계
1. 현재 문서 구조와 배포 관련 기존 내용을 확인한다.
   - `docs/README.md`
   - `docs/guides/developer-guide.md`
   - `docs/guides/env-contract.md`
   - `README.md`
   - `application*.yml`, `docker/docker-compose*.yml`, `.github/workflows/ci.yml`
2. 배포 가이드의 대상 구조를 고정한다.
   - 추천안: `EC2 + RDS MySQL + Redis on EC2 + Caddy + GitHub Actions CD`
   - 대안: Lightsail 저비용안, ElastiCache 추가안, PaaS 빠른 배포안
3. 새 문서를 작성한다.
   - 배포 전략 선택 이유
   - 아키텍처 다이어그램
   - 계정 생성 가이드(AWS/GitHub/OAuth)
   - 인프라 생성 순서
   - 환경변수 작성 가이드
   - 첫 배포 절차
   - 운영/백업/롤백/장애 대응
   - 비용 시나리오 비교
4. 실제 배포 자산을 추가한다.
   - `Dockerfile`
   - `.dockerignore`
   - `deploy/docker-compose.prod.yml`
   - `deploy/Caddyfile`
   - `deploy/.env.prod.example`
   - `.github/workflows/cd.yml`
   - `.gitignore` 보강
5. 문서 인덱스를 갱신한다.
   - `docs/README.md`에 신규 가이드 링크 추가
6. 문서/설정 정합성을 검증한다.
   - `git diff --check`
   - 새 문서 링크/경로 검토
   - `bootJar`
   - prod compose config
   - CD workflow YAML 파싱
7. 명시된 작업 브랜치에서 stage/commit/push를 수행한다.

## 3) 검증 계획
- `git diff --check`
- `rg -n "deployment-guide" docs README.md`
- 경로/명령/환경변수 이름이 현재 코드와 맞는지 수동 점검
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew --no-daemon bootJar`
- `docker compose --env-file deploy/.env.prod -f deploy/docker-compose.prod.yml config`
- `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/cd.yml')"`

## 4) 리스크 및 대응
- 가격 정보는 시간이 지나면 변할 수 있다.
  - 대응: 문서에 기준일과 공식 출처를 명시하고, 비용은 추정치로 표기한다.
- CD workflow를 잘못 트리거하면 작업 브랜치 push만으로도 운영 배포가 실행될 수 있다.
  - 대응: `cd.yml`은 `main` push와 수동 실행만 허용한다.
- `.env.prod`, PEM 키, 인증서 파일이 실수로 커밋될 수 있다.
  - 대응: `.gitignore`에 운영 비밀 파일 패턴을 추가하고 예제 파일만 커밋한다.
- OAuth 공급자 콘솔 UI는 바뀔 수 있다.
  - 대응: 버튼 명칭보다 반드시 넣어야 하는 redirect URI와 필수 값 중심으로 설명하고 공식 문서 링크를 함께 남긴다.
