# Dev TODO (Unfinished Work)

## In Progress (Working Tree)
- [ ] 알림장 목록 조회 안정화 (유치원/반/원생 필터)
  - `src/main/java/com/erp/domain/notepad/controller/NotepadController.java` 목록 API 정합성 확인
  - `src/main/resources/templates/notepad/notepad.html`과 파라미터/응답 구조 맞추기
  - Pageable + fetch join 사용 방식 재검토(필요 시 쿼리/로딩 전략 분리)

## Frontend TODO from code
- [ ] 교사 유치원 선택 기능 연결
  - `src/main/resources/templates/kindergarten/select.html`의 “교사-유치원 연결 API” TODO 처리
- [ ] 출석부 로드 기능 구현
  - `src/main/resources/templates/attendance/attendance.html`의 “Load attendance via API” TODO 처리
  - 일괄 변경 모달(bulk update) TODO 처리
- [ ] 알림장 작성 페이지의 반/원생 로딩 정리
  - `src/main/java/com/erp/domain/notepad/controller/NotepadViewController.java` TODO 처리(서버 주입 vs API 로드 방식 확정)

## Next (Phase 7: Application)
- [ ] 교사 지원/승인 워크플로우(KindergartenApplication) 구현
  - 설계: `docs/00_project/phase7_application.md`
- [ ] 학부모 입학 신청/승인 워크플로우(KidApplication) 구현
  - 승인 시 Kid 생성 + ParentKid 관계 생성

## Next (Phase 8: Notification)
- [ ] Notification 도메인 구현 + 미읽음 카운트/배지
  - 설계: `docs/00_project/phase8_notification.md`

## Consistency / Docs
- [ ] README/docs의 엔드포인트 문서와 실제 컨트롤러 매핑 정합성 점검(특히 attendance/notepad)
