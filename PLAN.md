# PLAN.md

## 작업명
- 후속 고도화 21차 (운영형 워크플로우 + 세션/알림 신뢰성 + 취업용 문서 패키지)

## 현재 배치 상태
- 완료: Batch A (`management plane 하드닝 + 활성 세션 관리`)
- 다음: Batch B (`notification_outbox` + retry/dead-letter + 외부 incident channel)

## 1) 목표 / 범위
- 포트폴리오 관점에서 가장 값이 큰 다음 개선 5개를 실제 코드/테스트/문서까지 포함해 순차 구현한다.
- 기능 추가 범위로 `domain_audit_log`를 도입해 핵심 업무 상태 변경의 운영 증적을 남긴다.
- 문서 범위는 단순 README 보강이 아니라, 채용 담당자/면접관이 빠르게 흡수할 수 있는 아키텍처/데모/케이스 스터디/채용용 패키지까지 포함한다.
- 구현 결과는 모두 "기능 구현 + 검증 + 결정 로그 + 인터뷰/데모 문서"까지 한 세트로 남긴다.

## 2) 세부 작업 단계
1. 운영 plane 하드닝
   - `management.server.port` 분리 가능 구조와 prod 보호 정책 설계
   - prod에서 Swagger/Prometheus 노출 정책 정리
   - health/readiness는 유지하되 management 접근 제어와 문서/데모 경로를 분리

2. 활성 세션 관리
   - 세션 메타데이터 저장 구조(`ip`, `userAgent`, `lastSeenAt`, `createdAt`) 도입
   - 내 세션 목록 조회 API/화면
   - 개별 세션 강제 종료 및 현재 세션/다른 기기 구분
   - 세션 lifecycle 문서화 및 회귀 테스트

3. 알림 신뢰성 고도화
   - `notification_outbox` 기반 비동기 dispatch 설계
   - retry / dead-letter / delivery status / incident channel(Slack/Webhook/Email) 도입
   - 인증 이상 징후 알림을 외부 채널로도 연결
   - 운영 runbook과 실패 시나리오 테스트 보강

4. 도메인 워크플로우 확장
   - 반 정원(capacity)과 입학/지원 waitlist 상태 도입
   - `WAITLISTED`, `OFFERED`, `OFFER_EXPIRED` 상태 설계
   - 입학 제안 수락/만료 배치 구현
   - 출결 요청/승인 워크플로우(학부모 요청 -> 교사 승인) 도입

5. 업무 감사 로그(`domain_audit_log`)
   - 입학 승인/거절, 교사 지원 승인/거절, 공지 수정/삭제, 소셜 연결 해제 등 핵심 상태 전이에 대한 audit 저장
   - actor / tenant / action / target / summary / metadata 저장
   - 원장용 조회 API/화면 또는 기존 감사 로그 콘솔과의 분리 정책 설계

6. 테스트/CI 신뢰성 보강
   - `fastTest`/`integrationTest`를 path include에서 JUnit Tag 또는 test suite 기반으로 전환
   - auth/security leaf unit test 보강
   - Redis/MySQL 장애 시 readiness/degraded behavior 검증
   - auth/export 경로 성능 smoke 및 scheduled job cluster-safe 전략 검토

7. 취업용 문서 패키지 재구성
   - 활성 아키텍처 문서 (`C4 + auth flow + audit/alert flow + ERD`)
   - 재현형 데모 문서 (`demo_preflight`, `demo_runbook`)
   - `auth incident response` 케이스 스터디
   - 채용용 랜딩 페이지(`hiring-pack`)와 핵심 스크린샷/시각 자료
   - README / docs index / interview script를 최신 상태로 재정렬

8. 배치 전략
   - 범위가 큰 만큼 4개 사용자 가치 배치로 쪼개서 진행
   - 각 배치는 코드 + 테스트 + docs + push + CI 확인까지 닫고 다음 배치로 이동
   - 예상 배치:
     - Batch A: 운영 plane 하드닝 + 활성 세션 관리
     - Batch B: 알림 outbox/retry + 외부 incident channel
     - Batch C: waitlist 입학/지원 + 출결 요청/승인 + domain audit log
     - Batch D: CI/tagging + failure mode/performance smoke + 아키텍처/데모/채용 문서 압축

## 3) 검증 계획
- 공통 컴파일/회귀
  - `./gradlew compileJava compileTestJava`
  - 배치별 대상 테스트 + `./gradlew test`
- CI/스위트 검증
  - `./gradlew fastTest integrationTest`
  - GitHub Actions run 확인
- 운영/문서 검증
  - YAML/JSON/mermaid/문서 링크 검증
  - `git diff --check`
- 성능/운영성 검증
  - auth/export/load 시나리오 재측정
  - degraded mode / scheduler / outbox retry 테스트
- 데모 검증
  - demo profile, monitoring overlay, runbook 재현 확인

## 4) 리스크 및 대응
- 범위가 커서 한 번에 구현하면 품질과 문서 밀도가 떨어질 수 있음
  - 대응: 4개 배치로 나누고, 배치마다 코드/테스트/문서를 동시에 마감한다
- waitlist/세션/outbox는 상태 전이와 동시성 리스크가 큼
  - 대응: 상태 다이어그램을 먼저 문서화하고 optimistic/pessimistic 전략과 idempotency 정책을 명시한다
- management plane 하드닝이 demo 편의성을 떨어뜨릴 수 있음
  - 대응: `demo/local`과 `prod`의 노출 정책을 분리하고 문서에서 명확히 구분한다
- 문서가 다시 과잉 분산될 수 있음
  - 대응: 새 문서는 `hiring-pack`, `architecture`, `case-studies`, `demo`에 압축하고 phase 문서는 의사결정 로그로만 유지한다
