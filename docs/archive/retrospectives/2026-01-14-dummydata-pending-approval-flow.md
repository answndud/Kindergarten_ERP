# 2026-01-14: 더미데이터 불일치 + 승인 대기 흐름/알림 프래그먼트 이슈 회고

## 문제 개요

**증상(사용자 관점)**
- `principal2@test.com`(이원장)으로 로그인했는데 알림장 작성 화면에서 "등록된 반이 없습니다"로 표시됨.
- 교사가 특정 유치원에 지원 신청했는데, 원장이 로그인해도 승인 대기 목록이 비어있음.
- 일부 화면에서 무한 로딩/DB 쿼리 폭주 발생.

**영향**
- 역할별 흐름(교사/학부모 승인 전 차단, 원장 승인)이 제대로 보이지 않아 기능이 멈춘 것처럼 보임.
- 로그가 과도하게 출력되고 DB에 불필요한 쿼리가 반복되어 디버깅이 어려워짐.

---

## 문제 원인

### 1) 더미데이터(DataLoader) 스킵으로 인한 “기대 데이터” 불일치
`DataLoader`는 `member`가 1개라도 있으면 더미데이터 로딩을 건너뛴다.
- 결과적으로 문서/가정(유치원 2개, 원장 2명, 반 4개)이 DB에 없거나 부분만 존재할 수 있음.
- 화면에서 `currentMember.kindergartenId` 기준으로 `/api/v1/classrooms?kindergartenId=...`를 호출하면 빈 리스트가 나와 "등록된 반이 없습니다"가 정상 동작이 된다.

관련: `src/main/java/com/erp/global/config/DataLoader.java`

### 2) 승인 대기 UX는 “유치원 매칭”이 핵심
원장 승인 대기 목록은 본질적으로 `principal.kindergartenId == application.kindergartenId`인 데이터만 보인다.
- 교사가 다른 유치원 ID로 신청했거나,
- 원장 계정의 `kindergarten_id`가 기대와 다르거나,
- 신청은 됐지만 다른 원장(다른 유치원)이 보는 경우
→ 원장 화면에서 "뜰 게 없는" 상태가 정상.

### 3) HTMX 프래그먼트 “self-load”로 인한 요청 폭주
알림 배지/목록 프래그먼트 자체에 `hx-trigger="load"` + `hx-swap="outerHTML"`가 들어가 있으면,
프래그먼트가 렌더링될 때마다 자기 자신을 다시 로딩하는 구조가 되어 요청이 반복된다.
- 결과적으로 `@ControllerAdvice`의 `currentMember` 조회가 매 요청마다 실행되어 member 조회 쿼리가 폭증.

### 4) 교사 지원 재신청(취소 후 재신청)과 DB 유니크 제약
`kindergarten_application`은 `(teacher_id, kindergarten_id)` 유니크 제약이 있어,
취소(CANCELLED) 레코드가 남아있으면 같은 유치원으로 재신청 시 INSERT가 실패한다.
- 해결은 "재신청 = UPDATE"로 다루거나, 상태/삭제 플래그를 유니크에 반영해야 한다.

---

## 해결/개선 조치

### 1) 승인 전 접근 제한 UX 강화
- 교사/학부모는 승인 전까지 `/applications/pending`에서만 신청/상태 확인 가능하도록 강제.
- 교사가 지원 신청을 하는 시점부터 `PENDING`으로 고정하여, 승인 전 일반 화면 접근을 막는다.

### 2) 원장 기본 화면에서 소속 유치원 가시화
- 홈 화면에서 원장(PRINCIPAL)에게 "관리 유치원" 정보를 표시하고 승인 대기 화면으로 바로 이동할 수 있게 제공.

### 3) 알림 프래그먼트 로딩 구조 변경
- 초기 로드는 placeholder에서만 수행하고,
- 서버가 반환하는 프래그먼트는 이벤트 기반 갱신(`notifications-changed`)만 수행하도록 조정.

### 4) 교사 지원 재신청 처리(취소/거절 후)
- 새 row INSERT가 아니라 기존 row를 PENDING으로 되돌리는 방식으로 재신청을 처리.

### 5) 학부모 입학 신청 중복/재신청 정책 확정
- 학부모는 "대기 중(PENDING) 입학 신청"이 있으면 추가 신청 불가.
- 동일 유치원에 취소/거절된 신청이 있을 경우, 재신청은 새 INSERT가 아니라 기존 row UPDATE로 처리.
- 동시성/무결성 강화를 위해 (parent_id, kindergarten_id) 유니크 제약을 추가.

---

## 운영/개발 프로세스 개선

### 1) 더미데이터 사용 정책 명확화
- 더미데이터를 기준으로 기능을 검증할 때는 "DB 초기화 후 DataLoader 실행"을 표준 절차로 문서화한다.
- 반/유치원/원장 매핑이 핵심인 기능(알림장/승인)에선 현재 로그인 사용자의 `kindergartenId`를 화면에 항상 노출해 디버깅 비용을 낮춘다.

### 2) 프래그먼트(HTMX) 체크리스트
- `hx-trigger="load"`가 포함된 요소가 `outerHTML`로 자기 자신을 교체하는 구조인지 반드시 확인.
- 이벤트 기반 갱신(예: `*-changed from:body`)을 기본으로 두고, 폴링은 보조로만 사용.

---

## 개발용 DB 초기화 가이드(요약)

### Docker(MySQL/Redis) 볼륨까지 완전 초기화
- `docker compose -f docker/docker-compose.yml down -v`
- `docker compose -f docker/docker-compose.yml up -d`
- 애플리케이션 재시작 후 Flyway 마이그레이션 적용 + DataLoader가 더미데이터를 생성한다.

주의: `-v`는 모든 데이터 삭제.
