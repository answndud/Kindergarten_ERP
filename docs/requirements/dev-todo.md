# Dev TODO (Unfinished Work)

## Completed (Recent)
- [x] 알림장 목록 조회 안정화 (유치원/반/원생 필터)
  - HTMX 프래그먼트(`notepad/fragments/list.html`)로 목록 렌더링
  - API 기반 필터(반/원생) 및 로딩 전략 정리
- [x] 알림 markAllAsRead 500 수정
  - `NotificationRepository`에 `@Modifying` 추가하여 UPDATE 쿼리 실행
- [x] SweetAlert2 UI 마이그레이션
  - `window.UI` 헬퍼로 알림/확인/입력 다이얼로그 통일
  - 페이지별 모달/`alert()` 제거 (classroom/attendance/settings/signup 등)
- [x] 대시보드 통계 실제 API 동기화
  - Alpine 컴포넌트 `dashboardStats()`로 오늘 알림장/출석률/승인 대기/공지사항 수 표시
  - 통계 카드 클릭 시 각 페이지 이동
  - 중복 “빠른 메뉴” 섹션 제거
- [x] Phase 7 Application pending flow
  - `/applications/pending` 화면(HTMX) 추가: 교사 지원/원생 입학 신청 대기 리스트
  - `KindergartenApplication`, `KidApplication` 유니크/재신청 정책 구현
  - 승인/거절/취소 UI 및 API 연결

## In Progress (Working Tree)

## Next (Priority Order)

### 1. 출결(Attendance) 로드/저장 실제 기능 연결
- [x] `/attendance` 화면 “Load attendance via API” 구현
  - `AttendanceController`의 `/daily` API를 호출하여 날짜/반별 출결 정보 로드
  - 선택된 날짜/반 기준으로 원생 목록과 상태 표시
- [x] 출결 저장 로직 확인/구현
  - “저장하기” 버튼이 실제 `/api/v1/attendance` 엔드포인트를 호출
  - 출석/결석/지각/조퇴 상태별 요청 포맷 일치성 확인
- [x] 일괄 변경(bulk update) 모달 기능
  - 반 전체 원생에 대한 일괄 상태 변경 UX 구현 (SweetAlert2)
  - 각 원생별 `/api/v1/attendance` POST 호출 (API 추가 없이 개별 요청으로 구현)

### 2. 알림(Notification) 이벤트 연결 (Phase 8)
- [x] 승인/거절 시 알림 생성
  - `KindergartenApplicationService`에서 이미 구현됨 (`notifyTeacherAboutApproval/reject`, `notifyPrincipalAboutApplication`)
  - `KidApplicationService`에서 이미 구현됨 (`notifyParentAboutApproval/rejection`, `notifyStaffAboutApplication`)
- [x] 알림장 작성 시 알림 생성 (초안 구현됨, 반영향/테스트 필요)
  - `NotepadService.createNotepad()`에 원생별 알림 시 `kid.getParents()`를 통해 학부모에게 알림 생성 로직 추가
  - `Kid.getParents()`가 없어 `parents` 리스트를 사용하여 학부모 ID 추출 및 알림 발송
- 알림 배지/리스트 자동 갱신
  - 현재는 `notifications-changed` 이벤트 기반 HTMX 프래그먼트 갱신이 동작
  - 서버에서 이벤트를 발행(WebSocket/SSE)하거나 단순 폴링 방식 유지 결정
- [ ] 알림 배지/리스트 자동 갱신
  - 현재 `notifications-changed` 이벤트 기반 HTMX 프래그먼트 갱신이 동작하는지 확인
  - 서버에서 이벤트를 발행(WebSocket/SSE)하거나, 단순 폴링 방식 유지 결정

### 3. 원생(Kid) 관리 기능
- [ ] 원생 목록/조회 화면
  - `/kids` (ViewController) + API(`KidApiController`) 구현
  - 반별 원생 목록, 이름/생년월일 검색
- [ ] 원생 생성/편집 화면
  - `/kids/new`, `/kids/{id}/edit` 폼
  - `KidApiController`의 `POST /kids`, `PUT /kids/{id}` 연결
- [ ] 학부모-자녀 관계(ParentKid) 관리
  - 학부모가 자녀를 조회할 수 있는 API/화면 확인
  - 자녀 입학 신청 시 `ParentKid` 생성 로직 검토

### 4. 교사 유치원 선택 기능 연결
- [ ] `/kindergarten/select`에서 “교사-유치원 연결” API 구현
  - 현재는 TODO로 되어 있음; `KindergartenApiController`에 연결 엔드포인트 추가
  - 교사가 유치원을 선택/변경할 수 있는 흐름 구현

## Frontend TODO from code
- [ ] 알림장 작성 페이지의 반/원생 로딩 정리
  - `NotepadViewController`에서 서버 주입 vs API 로드 방식 확정
  - 현재는 이미 SweetAlert2 팝업으로 구현됨; 불필요한 주입 제거 필요 시 정리

## Consistency / Docs
- [ ] README/docs의 엔드포인트 문서와 실제 컨트롤러 매핑 정합성 점검
  - 특히 `/api/v1/attendance/*`, `/api/v1/notepads/*`
  - 각 엔드포인트의 파라미터/권한/응답 포맷 일치성 확인
- [ ] 최근 회고 문서 업데이트
  - `docs/retrospective/`에 이번 SweetAlert2 마이그레이션/알림장 복구/대시보드 동기화 회고 추가

