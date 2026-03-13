# Phase 23: 캘린더 반복 일정/권한 정합성 보강

## 배경

- 캘린더 설계 문서에는 반복 일정이 "조회 시 동적으로 생성된다"고 적혀 있었지만, 실제 구현은 `repeatType`, `repeatEndDate`를 저장만 하고 목록 조회에서는 전혀 사용하지 않았다.
- 같은 문서에서는 유치원 전체 일정은 모두가 볼 수 있다고 설명했지만, 서비스 구현상 학부모는 `KINDERGARTEN` 범위 일정을 조회할 수 없었다.
- 이 상태는 면접에서 "문서와 코드 중 어느 쪽이 진짜인가"를 바로 질문받을 수 있는 리스크다.

## 목표

1. 반복 일정이 실제 API 응답에서 occurrence로 확장되도록 구현한다.
2. 유치원 전체 일정의 학부모 조회를 문서 의도에 맞게 정리한다.
3. 캘린더 도메인도 다른 보안 하드닝 배치와 같은 수준의 권한/회귀 테스트를 갖추게 만든다.

## 결정

1. 반복 일정은 엔티티를 추가로 늘리지 않고, 목록/오늘/다가오는 조회 시점에 occurrence를 동적으로 확장한다.
2. `repeatType != NONE`이면 `repeatEndDate`를 필수로 두고, 시작일보다 이른 종료일은 허용하지 않는다.
3. 학부모도 같은 유치원에 속해 있으면 `KINDERGARTEN` 범위 일정을 조회할 수 있게 한다.
4. 응답 계약은 유지하고, 반복 occurrence는 기존 `CalendarEventResponse` 구조에서 `startDateTime`, `endDateTime`만 occurrence 기준으로 바꿔 내려준다.

## 구현 요약

### 1) 반복 일정 조회 실제 구현

- `CalendarEventRepository`
  - 단순 `start/end` 겹침 조건만 보던 쿼리를 반복 일정까지 고려하도록 수정
  - `repeatType != NONE`인 경우 `repeatEndDate`와 조회 시작일을 기준으로 후보를 가져오도록 정리
- `CalendarEventService`
  - 조회된 이벤트를 `repeatType`에 따라 occurrence로 확장
  - `DAILY`, `WEEKLY`, `MONTHLY` 반복을 지원
  - 단건 상세 조회는 원본 엔티티를 유지하고, 목록성 API에서만 occurrence를 확장

### 2) 권한/가시성 정합화

- `CalendarEventService`
  - 학부모 기본 조회에도 `KINDERGARTEN` scope를 포함
  - `KINDERGARTEN` 범위 상세 조회 시 같은 유치원 소속이면 학부모도 허용
- `CalendarEventController`
  - `isAuthenticated()` 대신 세 역할을 명시한 `hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')`로 정리

### 3) 입력 검증 강화

- `repeatType != NONE`인데 `repeatEndDate`가 없으면 `400/C001`
- `repeatEndDate`가 시작일보다 이르면 `400/C001`
- `repeatType == NONE`인데 `repeatEndDate`만 들어오는 경우도 막는다

## 테스트

- `CalendarApiIntegrationTest`
  - 주간 반복 일정 조회 시 후속 occurrence가 실제 날짜로 응답되는지 검증
  - 학부모의 유치원 전체 일정 목록/상세 조회 성공 검증
  - 다른 유치원 일정 상세 접근 `403/CA002` 검증
  - `repeatEndDate` 누락 입력 `400/C001` 검증

## 검증 결과

- `./gradlew compileJava compileTestJava`
- `./gradlew test --tests "com.erp.api.CalendarApiIntegrationTest"`

둘 다 통과했다.

## 인터뷰 답변 포인트

### 왜 이 작업이 중요했는가

- 기능이 있다고 문서에 써두는 것보다, 실제 응답과 권한 모델이 그 설명을 따라가는지가 더 중요하다.
- 캘린더는 UI에서는 멀쩡해 보여도, 반복 일정과 역할별 조회가 틀어지면 실제 운영 신뢰도가 크게 떨어진다.

### 왜 occurrence를 DB row로 미리 펼치지 않았는가

- 반복 일정은 조회 범위에 따라 필요한 건수가 달라진다.
- 저장 시점에 매번 row를 복제하면 수정/삭제 비용이 커지므로, 현재 프로젝트 규모에서는 조회 시 확장이 더 단순하고 설명 가능성이 높다고 판단했다.

### 남겨둔 트레이드오프는 무엇인가

- 매우 오래된 반복 일정이 긴 범위 조회에 걸리면 서비스에서 occurrence를 확장하는 비용이 늘 수 있다.
- 현재는 단순성과 구현 명확성을 우선했고, 필요해지면 이후에는 occurrence projection 캐시나 범위 제한 정책을 검토할 수 있다.
