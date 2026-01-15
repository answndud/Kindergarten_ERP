# 2026-01-15: 원생 관리 UI 모달/스크립트 오류 해결

## 문제
- `/kids` 화면에서 원생 추가/편집 모달이 정상 동작하지 않음
- 콘솔 에러:
  - `UI is not defined`
  - `Cannot destructure property 'value' of (intermediate value)`
  - `Unexpected identifier 'buttonsStyling'`

## 원인
1. `app.js`에 남아 있던 잘못된 코드 블록으로 문법 오류 발생 → 이후 스크립트 로딩 실패
2. `kids.html`에서 `const { value: formValues } = await window.Swal.fire(...)` 패턴 사용
   - SweetAlert2 v11에서 `value`를 직접 디스트럭처링하면 `undefined` 발생
3. `UI` 전역이 로드되기 전에 인라인 스크립트가 실행됨

## 해결
1. `app.js`에서 중복/잔여 코드 제거 (문법 오류 해결)
2. `kids.html`에서 SweetAlert2 사용 패턴 변경
   - `const result = await Swal.fire(...)`
   - `if (!result.isConfirmed) return;`
   - `const formValues = result.value`
3. `kids.html`에 UI fallback 추가
   - `window.UI`가 없을 경우 `alert/confirm` 폴백 제공
   - `app.js` 로딩 실패에도 기본 동작 보장

## 결과
- 원생 추가/편집/삭제 모달 정상 동작 확인
- 브라우저 콘솔 에러 제거
- `/kids` 페이지에서 전체 CRUD 시나리오 정상 동작

## 영향 파일
- `src/main/resources/static/js/app.js`
- `src/main/resources/templates/kid/kids.html`
