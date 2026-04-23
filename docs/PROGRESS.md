# PROGRESS.md

기준일: 2026-04-23

## 문서 규칙

- 이 문서는 active 상태, blocker, 최근 검증, 다음 액션만 유지합니다.
- 작업이 완료되면 이 문서의 최종 상태, 검증, 결과를 정리해 `docs/COMPLETED.md`로 옮기고 active 본문에서는 바로 삭제합니다.
- 완료된 작업은 0건으로 유지하고, 진행 중이거나 아직 끝나지 않은 작업만 남깁니다.
- 모든 active 작업이 끝나면 작업 본문은 전부 비우고 `현재 active 작업 없음`만 남깁니다.

## 현재 상태 요약

- active 작업: Impeccable 기준 주요 화면 디자인 개선 workflow 수립
- 상태: phase별 실행 계획 작성 완료, 구현 미시작
- blocker: 보호 화면 screenshot 재검증은 demo seed 로그인 환경이 필요함
- 다음 액션: Phase 1 출결/출결 변경 요청 흐름 baseline screenshot 캡처 후 `$critique` 시작

## 최근 검증

- `npm run impeccable:detect:json -- --fast`
  - 결과: detector findings 35건 확인
  - 주요 유형: `single-font` 27건, `flat-type-hierarchy` 7건, `gray-on-color` 1건
- `./gradlew bootRun --args='--spring.profiles.active=demo'`
  - 결과: 서버 기동 확인
  - 제한: 당시 seed 활성화 환경이 완전하지 않아 보호 화면은 `/login`으로 리다이렉트됨
- Chrome headless public/auth 렌더링
  - 결과: `/`, `/login`, `/signup` screenshot/렌더링 메트릭 일부 확인
  - 제한: 보호 화면 screenshot은 Phase 1 시작 시 재캡처 필요

## Active Work Status

### Impeccable 디자인 개선 workflow

- `docs/PLAN.md`에 Phase 1-6 실행 계획 작성 완료
- phase는 한 번에 한 화면군 또는 한 사용자 흐름만 다루도록 분리함
- 각 phase에 목표, 대상 화면, skill 순서, 수정 범위, 하지 않을 것, 완료 기준, 검증 명령, screenshot 확인 지점을 기록함
- 구현은 아직 시작하지 않음

### 다음 액션

1. `.env`를 source 해서 demo seed 로그인 환경을 켭니다.
2. Docker MySQL/Redis와 Spring Boot demo 서버를 띄웁니다.
3. `/attendance`, `/attendance/monthly`, `/attendance-requests`의 desktop/mobile/narrow mobile baseline screenshot을 캡처합니다.
4. `$critique`로 Phase 1만 재평가하고, `$adapt`부터 수정 범위를 좁혀 진행합니다.
