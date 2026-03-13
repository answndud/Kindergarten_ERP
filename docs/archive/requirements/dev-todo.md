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
- [x] 알림 배지/리스트 자동 갱신
  - 전략 확정: **폴링 유지 + 이벤트 기반 즉시 갱신**
  - `app.js`에서 30초 주기 자동 갱신 + `visibilitychange/focus` 시 즉시 갱신
  - 알림 드롭다운 오픈 시 `notifications-changed` 트리거로 목록 즉시 동기화

### 3. 원생(Kid) 관리 기능
- [x] 원생 목록/조회 화면
  - `/kids` (ViewController) + API(`KidApiController`) 구현
  - 반별 원생 목록, 이름/생년월일 검색
- [x] 원생 생성/편집 화면
  - `/kids/new`, `/kids/{id}/edit` 페이지 추가 (`kid/kid-form.html`)
  - `POST /api/v1/kids`, `PUT /api/v1/kids/{id}` API 연동
- [x] 학부모-자녀 관계(ParentKid) 관리
  - 학부모가 자녀를 조회할 수 있는 API/화면 확인
  - 자녀 입학 신청 시 `ParentKid` 생성 로직 검토

### 4. 교사 유치원 선택 기능 연결
- [x] `/kindergarten/select`에서 교사 지원 신청 연결
  - 선택 화면에서 `/api/v1/kindergarten-applications` POST 호출
  - 선택 메시지 입력(SweetAlert2) + 신청 성공 시 `/applications/pending` 이동

## Frontend TODO from code
- [x] 알림장 작성 페이지의 반/원생 로딩 정리
  - `NotepadViewController`의 TODO 제거, API 로드 전략으로 확정
  - 화면(`notepad/write`)에서 반/원생을 API로 동적 로딩

## Consistency / Docs
- [x] 주요 API 통합 테스트 보강
  - `CalendarApiIntegrationTest` 추가
  - `NotificationApiIntegrationTest` 추가
- [ ] 최근 회고 문서 업데이트
  - `docs/retrospective/`에 이번 SweetAlert2 마이그레이션/알림장 복구/대시보드 동기화 회고 추가

## Next Focus (2026-02-21)
- [x] API 문서(README)와 실제 컨트롤러 매핑 재동기화
- [x] CORS 허용 메서드 PATCH 반영
- [x] 목록 API 필수 필터 누락 시 400 응답 명시화 (`/api/v1/kids`, `/api/v1/classrooms`)
- [x] 입학 신청 승인 시 ParentKid 관계값 요청 기반 반영
- [x] 지원/교실/유치원 API 통합 테스트 보강
