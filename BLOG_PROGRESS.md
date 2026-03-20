# BLOG_PROGRESS.md

## 작업명
- 취업용 개발 블로그 시리즈 기획 2차 (전체 코드 재스캔 + 코드 기반 마스터 플랜 고도화)

## 진행 로그

| 시간 (KST) | 상태 | 수행 내용 | 다음 액션 |
|---|---|---|---|
| 2026-03-20 15:05 | DONE | 사용자 요청에 따라 블로그 작업은 기존 `PLAN.md`, `PROGRESS.md`와 분리하기로 결정 | 블로그 전용 SSOT 파일 생성 |
| 2026-03-20 15:09 | DONE | 루트 `blog/` 폴더와 `blog/README.md`, `blog/00_series_plan.md`, `blog/_post_template.md`를 생성해 작업 공간을 만들었다 | 블로그 전용 계획/진행 파일 생성 |
| 2026-03-20 15:12 | DONE | 기존 개발 SSOT인 `PLAN.md`, `PROGRESS.md`를 HEAD 기준으로 복구했다 | `BLOG_PLAN.md`, `BLOG_PROGRESS.md` 작성 |
| 2026-03-20 15:15 | DONE | 블로그 작업 전용 SSOT로 `BLOG_PLAN.md`, `BLOG_PROGRESS.md`를 작성하고 `blog/README.md`에 새 경로를 연결했다 | 실제 블로그 본문 작성 우선순위에 따라 `blog/01-*` 문서부터 집필 시작 |
| 2026-03-20 15:31 | DONE | 전체 코드와 문서 체계를 다시 스캔했다. `build.gradle`, `docker/*`, `application*.yml`, `global/config`, `global/security`, `domain/*`, `src/test/java`, `docs/decisions`, `docs/portfolio`를 기준으로 블로그 근거 지도를 다시 만들었다 | 얕은 개요형 계획을 실제 코드 기반 마스터 플랜으로 전면 재작성 |
| 2026-03-20 15:43 | DONE | `BLOG_PLAN.md`를 글 단위 마스터 플랜으로 재작성했다. 각 글마다 핵심 질문, 실제 파일, 클래스/메서드, 테스트/문서 근거, 입문자 포인트, 취업 포인트를 고정했다 | `blog/00_series_plan.md`를 마스터 플랜의 공개용 요약 인덱스로 재정렬 |
| 2026-03-20 15:47 | DONE | `blog/00_series_plan.md`를 마스터 플랜과 일치하도록 요약 인덱스로 다시 작성했고, 남아 있던 이전 초안 내용을 제거했다 | 포맷 검증 후 커밋/푸시 |
| 2026-03-20 16:02 | DONE | 사용자 피드백을 반영해 `BLOG_PLAN.md`를 다시 강화했다. 코드베이스 스냅샷, 소스 맵, 개발 연대기, 26개 글 구성, 집필 운영 규칙과 우선순위를 보강해 블로그 집필용 SSOT로 재정의했다 | 포맷 검증 후 커밋/푸시 |
| 2026-03-20 16:18 | DONE | 첫 본문 주제를 `settings.gradle`, `build.gradle`, `ErpApplication`으로 고정하고 관련 코드, CI 설정, 결정 로그를 다시 읽었다 | `blog/02-gradle-spring-boot-bootstrap.md` 초안 작성 |
| 2026-03-20 16:31 | DONE | `blog/02-gradle-spring-boot-bootstrap.md` 초안을 작성했다. 빌드 뼈대, 의존성 설계, Java 17 선택, 테스트 태스크 진화, CI 연결까지 입문자용 설명과 취업 포인트를 함께 정리했다 | 포맷 검증 후 add/commit/push |
| 2026-03-20 16:43 | DONE | 두 번째 본문 주제를 Docker 개발 환경으로 고정하고 `docker-compose.yml`, `docker-compose.monitoring.yml`, `docker/.env`, `application-local.yml`, `application.yml`, `demo-preflight.md`를 다시 읽었다 | `blog/03-docker-mysql-redis-dev-environment.md` 초안 작성 |
| 2026-03-20 16:57 | DONE | `blog/03-docker-mysql-redis-dev-environment.md` 초안을 작성했다. MySQL/Redis 기본 스택, monitoring overlay 분리, 앱은 호스트에서 실행하고 인프라만 컨테이너화하는 구조, `application-local.yml`과의 연결을 입문자용 설명과 취업 포인트로 정리했다 | 포맷 검증 후 add/commit/push |

## 현재 상태 요약
- 현재 단계: `DONE`
- 활성 작업: 없음
- 블로커: 없음
