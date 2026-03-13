# 2026-01-14: SweetAlert2 도입 + alert/modal UX 정리 회고

## 문제 개요

**증상(사용자 관점)**
- 화면마다 `alert()`/`confirm()` 스타일이 제각각이라 UX가 거칠고 투박하게 느껴짐.
- 일부 화면은 Tailwind로 만든 “페이지 내 모달”을 사용했는데, 마크업이 크고 재사용이 어려움.
- 동일한 승인/취소/삭제 흐름인데도 문구와 동작(확인/취소)이 일관되지 않음.

---

## 목표
- 전역적으로 **확인/성공/오류 다이얼로그 UX를 통일**한다.
- 페이지별 모달 HTML을 최소화하고, JS 호출로 관리한다.
- 라이브러리 로딩 실패 시에도 최소 기능(기본 alert/confirm)으로 폴백한다.

---

## 해결/개선 조치

### 1) `window.UI`(SweetAlert2 wrapper)로 호출 통일
- 성공/오류/확인/입력 다이얼로그를 `window.UI`로 제공.
- SweetAlert2가 로딩되어 있으면 예쁜 다이얼로그로, 아니면 기본 `window.alert/confirm`으로 폴백.

관련: `src/main/resources/static/js/app.js`

### 2) 폼 삭제/파괴적 액션 확인을 “데이터 속성”으로 처리
- 템플릿에서 `onsubmit="return confirm(...)"` 대신 `data-ui-confirm="..."` 사용.
- 공통 submit 이벤트 핸들러가 확인 다이얼로그를 띄우고, 확인 시에만 submit.

### 3) 잔여 `alert()` 제거 및 문구 정리
- `classroom`, `attendance`, `settings`, `signup`, `kindergarten` 등에서 남아있던 `alert()`를 `UI.*`로 교체.
- 문구를 “짧고 자연스럽게” 통일: 완료/실패/주의의 톤을 일관화.

### 4) 레이아웃에 SweetAlert2 CDN 추가
- 레이아웃을 사용하는 화면은 별도 추가 없이 SweetAlert2가 항상 로딩되도록 처리.

관련: `src/main/resources/templates/layout/default.html`

---

## 배운 점 / 주의사항
- SSR/HTMX 환경에서는 “프래그먼트에 스크립트를 넣는 방식”이 반복 로딩/중복 실행을 만들기 쉽다.
  - 공통 라이브러리/헬퍼는 레이아웃(또는 전역 include)로 1회 로딩하는 편이 안전하다.
- `onsubmit`에서 Promise를 반환할 수 없기 때문에, 비동기 confirm은 이벤트 리스너 패턴이 더 안정적이다.

---

## 후속 작업(선택)
- standalone 템플릿들에 중복으로 들어간 SweetAlert2/app.js include를 정리하거나, 점진적으로 `layout/default.html`로 통합.
- 토스트(비동기 작업 완료 알림)와 다이얼로그(사용자 확인) 역할 분리.
