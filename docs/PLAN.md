# PLAN.md

기준일: 2026-04-23

## 문서 규칙

- 이 문서는 active roadmap만 유지합니다.
- 완료된 작업은 `docs/COMPLETED.md`에 archive한 뒤 여기서 바로 삭제합니다.
- 세션 시작 시 기본으로 읽는 문서는 `docs/PLAN.md`, `docs/PROGRESS.md`, `docs/guides/developer-guide.md`, `docs/guides/env-contract.md`입니다.
- active 문서에 완료된 작업을 남기지 않습니다.
- 모든 active 작업이 끝나면 작업 본문은 전부 비우고 `현재 active 작업 없음`만 남깁니다.

## 범위 원칙

- 문서 운영 SSOT는 `docs/README.md`, `docs/PLAN.md`, `docs/PROGRESS.md`, `docs/COMPLETED.md`, `docs/guides/*`만 사용합니다.
- 루트 `PLAN.md`, `PROGRESS.md`는 더 이상 사용하지 않습니다.
- 블로그 작업 SSOT는 계속 루트의 `BLOG_PLAN.md`, `BLOG_PROGRESS.md`를 사용합니다.
- 새 작업은 코드보다 먼저 이 문서에 목표, 검증 계획, 리스크를 기록하고 시작합니다.
- Impeccable 작업은 전역 설정을 변경하지 않고 repo-local `.impeccable.md`, `.agents/skills/*`, `package.json` script만 사용합니다.

## Active Work

| 상태 | 우선순위 | 작업 | 완료 기준 | 의존성 |
|---|---|---|---|---|
| `planned` | `P1` | Impeccable 기준 주요 화면 디자인 개선 workflow | phase별 screenshot, detector, compile 검증을 통과하고 결과를 `docs/COMPLETED.md`에 archive | demo seed 로그인 가능 환경 |

## Impeccable 디자인 개선 Workflow

### 목표

- Kindergarten ERP의 주요 화면을 `.impeccable.md` 기준에 맞춰 점진적으로 개선합니다.
- 한 번에 전체 UI를 갈아엎지 않고, 한 phase마다 한 화면군 또는 한 사용자 흐름만 다룹니다.
- Simple is Best 원칙을 유지하면서 모바일 작업성, 정보 위계, 폼 상태, 접근성, 운영 화면 밀도를 개선합니다.

### 현재 평가 요약

- `npm run impeccable:detect:json -- --fast` 기준 detector findings: 35건.
- 주요 유형: `single-font` 27건, `flat-type-hierarchy` 7건, `gray-on-color` 1건.
- 가장 큰 리스크는 모바일 운영 화면의 작은 터치 타깃, 과도한 카드/표면 중첩, 화면별 폼/상태 문구 불일치입니다.
- `dashboard`, `audit`, `settings`, `attendance-requests` 계열은 `rounded-2xl/3xl`, `shadow-sm`, `bg-white`, border가 반복되어 ERP 화면보다 generic SaaS card UI처럼 보입니다.
- public/auth 화면은 렌더링 확인 완료. 보호 화면은 demo seed 로그인 환경을 먼저 맞춘 뒤 screenshot 재확인이 필요합니다.

### 공통 실행 규칙

- 각 phase 시작 전 `docs/PROGRESS.md`에 시작 상태, 대상 화면, 검증 계획을 갱신합니다.
- 각 phase는 지정된 화면군 외 파일을 대형 리팩터링하지 않습니다.
- shared CSS/JS 수정은 해당 phase 화면군에 필요한 최소 변경만 허용합니다.
- 디자인 토큰 추출은 한 phase 안에서 반복을 줄이는 수준으로만 진행하고, 별도 대형 디자인 시스템 개편은 하지 않습니다.
- 모든 phase는 desktop/mobile screenshot 비교를 남긴 뒤 완료합니다.
- 완료된 phase는 결과, 검증 명령, screenshot 경로, 남은 이슈를 `docs/COMPLETED.md`에 archive하고 `docs/PLAN.md`에서는 제거하거나 상태를 갱신합니다.

### 공통 검증 명령

```bash
npm run impeccable:detect -- --fast
npm run impeccable:detect:json -- --fast
./gradlew compileJava compileTestJava
```

로컬 렌더링 검증이 필요한 phase:

```bash
set -a
source .env
set +a
docker compose --env-file docker/.env.example -f docker/docker-compose.yml up -d mysql redis
./gradlew bootRun
```

검증 후 임시 인프라 중지:

```bash
docker compose --env-file docker/.env.example -f docker/docker-compose.yml stop mysql redis
```

### Screenshot 공통 기준

- Desktop: 1440x900
- Tablet: 768x1024
- Mobile: 390x844
- Narrow mobile: 320x568
- 각 화면은 기본 상태, empty state, loading/error가 있는 경우 해당 상태, keyboard focus 이동 상태를 확인합니다.

## Phase 1: 출결/출결 변경 요청 흐름

### 목표

- 교사와 원장이 모바일에서 출결 확인, 상태 변경, 변경 요청 검토를 빠르게 처리할 수 있게 합니다.
- 작은 버튼, 과도한 표 스크롤, 반복 카드 표면을 줄이고 핵심 액션을 먼저 보이게 합니다.

### 대상 화면

- `/attendance`
- `/attendance/monthly`
- `/attendance-requests`

### 사용할 Impeccable skill 순서

1. `$critique`: 현재 출결 흐름의 계층, 액션 위치, 모바일 사용성 평가
2. `$adapt`: 390px/320px 모바일 레이아웃과 터치 타깃 보강
3. `$layout`: 출결 상태, 필터, 액션의 정보 위계 재배치
4. `$clarify`: 출결 상태, empty/error/loading 문구 개선
5. `$polish`: focus, spacing, 버튼 상태, detector 잔여 이슈 정리

### 수정 범위

- 출결 관련 Thymeleaf templates/fragments.
- 출결 화면에서만 쓰이는 CSS/JS.
- 공통 CSS는 touch target, focus, table/card helper처럼 출결 화면에 직접 필요한 최소 변경만 허용.

### 하지 않을 것

- 출결 API 계약 변경.
- 출결 상태 enum/도메인 로직 변경.
- 월간 통계 쿼리나 성능 최적화.
- 다른 업무 화면의 카드/테이블까지 같이 변경.

### 완료 기준

- 모바일에서 주요 출결 액션 터치 영역이 44px 이상이거나 동등한 조작성을 확보합니다.
- 390px 화면에서 핵심 상태, 원아명, 액션이 가로 스크롤 없이 판단 가능합니다.
- monthly 표는 desktop 밀도를 유지하되 mobile에서는 요약/스크롤 안내가 명확합니다.
- 출결 empty/error/loading state가 다음 행동을 안내합니다.
- detector 결과가 phase 시작 전보다 악화되지 않습니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 출결 관련 테스트:

```bash
./gradlew test --tests "*Attendance*"
```

### Screenshot 확인 지점

- `/attendance`: desktop, mobile, narrow mobile
- `/attendance/monthly`: desktop, mobile horizontal behavior
- `/attendance-requests`: pending list, empty state, approve/reject action focus

## Phase 2: 입학/소속 신청 처리 흐름

### 목표

- 원장/교사/학부모가 입학 또는 소속 신청 상태와 다음 행동을 명확히 이해하게 합니다.
- 승인/반려/대기/제안 상태의 시각 체계를 통일하고, 신청 처리 화면의 과한 표면을 줄입니다.

### 대상 화면

- `/applications/pending`
- `/kindergarten/create`
- `/kindergarten/select`

### 사용할 Impeccable skill 순서

1. `$critique`: 신청 상태 흐름과 사용자별 판단 지점 평가
2. `$layout`: 신청 카드/목록/필터의 우선순위 재구성
3. `$clarify`: 승인, 반려, 대기, 제안, empty state 문구 개선
4. `$adapt`: 모바일 신청 카드와 액션 버튼 정리
5. `$polish`: spacing, focus, 상태 badge, detector 잔여 이슈 정리

### 수정 범위

- application/kindergarten 관련 templates/fragments.
- 신청 상태 badge와 안내 문구.
- 해당 화면에서 쓰이는 작은 JS interaction.

### 하지 않을 것

- 신청 workflow 도메인 상태 변경.
- 승인/반려 API payload 변경.
- 알림 발송 정책 변경.
- 원아 생성/소속 배정 로직 변경.

### 완료 기준

- 각 신청 상태가 색상만이 아니라 텍스트와 배치로도 구분됩니다.
- 승인/반려 같은 primary action이 모바일에서 접히거나 묻히지 않습니다.
- empty state가 “무엇을 하면 되는지”를 안내합니다.
- 320px에서도 신청 카드의 이름, 상태, 주요 액션이 겹치지 않습니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 신청 관련 테스트:

```bash
./gradlew test --tests "*Application*"
```

### Screenshot 확인 지점

- `/applications/pending`: pending list, empty state, action focus
- `/kindergarten/create`: form validation state
- `/kindergarten/select`: list, empty/none selected state

## Phase 3: 원아/반 관리 흐름

### 목표

- 원장과 교사가 원아, 반, 보호자 연결 정보를 빠르게 스캔하고 안전하게 수정하게 합니다.
- 목록/상세/폼 사이의 정보 구조를 맞추고 SweetAlert 기반 동적 폼의 접근성 리스크를 줄입니다.

### 대상 화면

- `/kids`
- `/kids/{id}`
- `/kids/new`
- `/kids/{id}/edit`
- `/classrooms`

### 사용할 Impeccable skill 순서

1. `$critique`: 목록, 상세, 편집 흐름의 사용성 평가
2. `$layout`: 원아/반 정보의 scan pattern과 액션 그룹 재배치
3. `$adapt`: 모바일 카드, action menu, form touch target 보강
4. `$clarify`: 보호자 연결, 반 배정, 삭제/해제 confirm 문구 개선
5. `$polish`: modal focus, spacing, detector 잔여 이슈 정리

### 수정 범위

- kid/classroom 관련 templates/fragments.
- 원아/반 화면의 SweetAlert HTML과 버튼 상태.
- 원아/반 화면 전용 JS helper.

### 하지 않을 것

- 원아/반 API 계약 변경.
- 보호자 연결 권한 정책 변경.
- classroom assignment 도메인 검증 변경.
- 출결/알림장 화면까지 함께 수정.

### 완료 기준

- 원아 목록에서 이름, 반, 보호자, 상태, 주요 액션을 1차 스캔으로 확인할 수 있습니다.
- mobile에서 action이 과밀하지 않고 menu 또는 grouped action으로 정리됩니다.
- 동적 modal/form은 label, focus, error 상태가 명확합니다.
- 삭제/연결 해제 같은 위험 액션이 시각적으로 구분됩니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 원아/반 관련 테스트:

```bash
./gradlew test --tests "*Kid*" --tests "*Classroom*"
```

### Screenshot 확인 지점

- `/kids`: populated list, empty state, mobile action grouping
- `/kids/{id}`: parent connection section, detail actions
- `/kids/new`, `/kids/{id}/edit`: validation, focus state
- `/classrooms`: teacher assignment, empty teacher state

## Phase 4: 알림장/공지/캘린더 커뮤니케이션 흐름

### 목표

- 교사/원장이 작성하고 학부모가 읽는 커뮤니케이션 화면의 가독성과 작성 안정성을 높입니다.
- 목록, 상세, 작성, 수정 화면의 hierarchy를 맞추고 generic empty state를 줄입니다.

### 대상 화면

- `/notepad`
- `/notepad/write`
- `/notepad/{id}`
- `/notepad/{id}/edit`
- `/announcements`
- `/announcement/write`
- `/announcement/{id}`
- `/announcement/{id}/edit`
- `/calendar`

### 사용할 Impeccable skill 순서

1. `$critique`: 읽기/작성 흐름과 hierarchy 평가
2. `$typeset`: 제목, 본문, 메타 정보의 글자 크기/무게/행간 정리
3. `$layout`: 목록, 상세, 작성 form의 리듬 정리
4. `$clarify`: empty/error/loading, 저장/수정/삭제 copy 개선
5. `$adapt`: 모바일 작성 form과 calendar/list 전환 보강
6. `$polish`: focus, spacing, detector gray-on-color/flat hierarchy 정리

### 수정 범위

- notepad, announcement, calendar templates/fragments.
- 커뮤니케이션 화면에서 쓰이는 상태 badge, form helper, empty state.
- `announcement/announcements.html`의 `gray-on-color` detector issue.

### 하지 않을 것

- 알림장/공지 API 계약 변경.
- 첨부파일, 댓글, 읽음 확인 같은 새 기능 추가.
- 캘린더 도메인 로직이나 일정 반복 규칙 추가.
- 전체 typography 시스템을 한 번에 교체.

### 완료 기준

- 목록에서 제목, 대상, 작성자, 작성일, 읽음/상태 정보의 우선순위가 명확합니다.
- 작성 form은 모바일에서 입력/저장/취소가 안정적으로 조작됩니다.
- 공지 detector `gray-on-color` 이슈가 해소됩니다.
- empty state가 화면 성격에 맞는 다음 행동을 제공합니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 커뮤니케이션 관련 테스트:

```bash
./gradlew test --tests "*Notepad*" --tests "*Announcement*" --tests "*Calendar*"
```

### Screenshot 확인 지점

- `/notepad`: list, empty state, mobile filters
- `/notepad/write`: validation, focus, submit loading
- `/announcements`: detector color fix, list hierarchy
- `/announcement/write`: form spacing, mobile submit
- `/calendar`: desktop calendar/list, mobile calendar/list, event modal

## Phase 5: 원장 대시보드/감사 로그 흐름

### 목표

- 원장이 오늘 처리해야 할 일, 이상 상태, 최근 추세를 더 빠르게 판단하게 합니다.
- dashboard와 audit/log 화면의 과도한 KPI 카드/필터 표면을 줄이고 데이터 밀도를 높입니다.

### 대상 화면

- `/dashboard`
- `/audit-logs`
- `/domain-audit-logs`
- `/notifications`

### 사용할 Impeccable skill 순서

1. `$critique`: 원장 의사결정 흐름과 dashboard density 평가
2. `$distill`: 중복 카드, 장식 표면, 과한 round/shadow 제거
3. `$layout`: KPI, alert, table, filter의 우선순위 재배치
4. `$adapt`: 모바일 dashboard/log filter interaction 보강
5. `$clarify`: audit/filter/notification empty state와 label 개선
6. `$polish`: focus, table readability, detector 잔여 이슈 정리

### 수정 범위

- dashboard, authaudit, domainaudit, notifications templates/fragments.
- filter panel, KPI card, table density, status badge.
- dashboard/log 화면에 필요한 shared utility class 최소 변경.

### 하지 않을 것

- dashboard API 통계 계약 변경.
- audit retention, security policy, logging schema 변경.
- 성능 최적화 쿼리 변경.
- 차트 라이브러리 도입 또는 대형 시각화 추가.

### 완료 기준

- dashboard 첫 화면에서 pending action과 anomaly/alert가 card 반복보다 먼저 보입니다.
- audit table은 desktop에서 정보 밀도를 유지하고 mobile에서 filter/table 조작이 명확합니다.
- filter는 큰 card 표면보다 빠른 조건 변경에 초점을 둡니다.
- notifications는 badge/text/action의 터치와 focus 상태가 명확합니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 dashboard/audit/notification 관련 테스트:

```bash
./gradlew test --tests "*Dashboard*" --tests "*Audit*" --tests "*Notification*"
```

### Screenshot 확인 지점

- `/dashboard`: first viewport, mobile stacked state
- `/audit-logs`: populated table, filter open/closed, empty result
- `/domain-audit-logs`: populated table, mobile filter behavior
- `/notifications`: unread/read filters, mobile list, empty state

## Phase 6: 인증/계정 설정 흐름

### 목표

- 로그인, 회원가입, 프로필, 계정 설정 화면을 안정적인 trust surface로 정리합니다.
- generic auth card 느낌을 줄이고, 소셜 연결/비밀번호/세션 관리의 위험도를 명확히 표시합니다.

### 대상 화면

- `/login`
- `/signup`
- `/profile`
- `/settings`

### 사용할 Impeccable skill 순서

1. `$critique`: auth/account trust flow 평가
2. `$clarify`: error, password, social link/unlink, session copy 개선
3. `$adapt`: signup role selector, social buttons, settings mobile layout 보강
4. `$distill`: settings nested card와 과한 surface 정리
5. `$layout`: 계정 정보, 보안, 연결 계정, 세션 영역 재배치
6. `$polish`: focus, loading, disabled, detector 잔여 이슈 정리

### 수정 범위

- auth/profile/settings templates.
- auth/account 화면 전용 JS와 message copy.
- settings 내부 social/password/session section layout.

### 하지 않을 것

- 인증/JWT/OAuth2 보안 로직 변경.
- 계정 생성/비밀번호 변경 API 계약 변경.
- 새 OAuth provider 추가.
- 브라우저 저장 비밀번호, global config, 사용자 홈 설정 변경.

### 완료 기준

- 로그인/회원가입 form error가 원인과 다음 행동을 안내합니다.
- signup role selector가 mobile에서 오탭 없이 조작됩니다.
- settings는 nested card가 줄고 보안 위험 액션이 명확히 분리됩니다.
- keyboard tab 순서와 focus ring이 끊기지 않습니다.

### 검증 명령

```bash
npm run impeccable:detect -- --fast
./gradlew compileJava compileTestJava
```

필요 시 auth/member view 관련 테스트:

```bash
./gradlew test --tests "*Auth*" --tests "*Member*" --tests "*ViewEndpointTest*"
```

### Screenshot 확인 지점

- `/login`: normal, error, loading, mobile
- `/signup`: role selector, validation, mobile/narrow mobile
- `/profile`: edit modal/form, mobile
- `/settings`: password/social/session sections, danger action focus, mobile

## 전체 완료 기준

- 각 phase의 screenshot 확인 지점이 desktop/mobile/narrow mobile에서 겹침 없이 통과합니다.
- detector findings가 baseline 35건보다 줄거나, 남는 항목의 사유가 문서화됩니다.
- `./gradlew compileJava compileTestJava`가 통과합니다.
- 변경된 화면군에 해당하는 선택 테스트가 통과하거나, 실행하지 못한 사유가 `docs/COMPLETED.md`에 기록됩니다.
- `docs/COMPLETED.md`에 phase별 결과, 검증 명령, screenshot 경로, 남은 리스크가 archive됩니다.

## 다음 실행 순서

1. `docs/PROGRESS.md`에 Phase 1 시작 상태를 기록합니다.
2. demo seed 로그인 환경에서 `/attendance`, `/attendance/monthly`, `/attendance-requests` screenshot baseline을 캡처합니다.
3. `$critique`로 Phase 1 화면군을 재평가한 뒤 `$adapt`부터 작은 패치 단위로 수정합니다.
4. Phase 1 완료 후 검증 결과를 `docs/COMPLETED.md`로 archive하고 Phase 2로 넘어갑니다.
