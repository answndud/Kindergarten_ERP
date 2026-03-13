# PLAN.md

## 작업명
- 후속 고도화 4차 (캘린더 반복 일정/권한 정합성 보강)

## 1) 목표 / 범위
- 캘린더 도메인에서 문서와 실제 구현이 어긋난 부분을 정리한다.
- 반복 일정(`repeatType`, `repeatEndDate`)이 목록/오늘/다가오는 조회에서 실제로 동작하도록 구현한다.
- 유치원 전체 일정의 학부모 조회 정책을 문서 의도에 맞게 정리하고, 권한 회귀 테스트와 인터뷰용 문서를 함께 남긴다.

## 2) 세부 작업 단계
1. 캘린더 구현/문서 정합성 점검
   - `CalendarEventService`, repository, API 테스트를 점검한다.
   - 문서상 반복 일정/유치원 전체 일정 조회 규칙과 현재 구현 차이를 정리한다.

2. 반복 일정 조회 구현
   - 반복 일정이 조회 범위에 걸릴 때 occurrence가 실제 응답으로 확장되도록 구현한다.
   - 반복 입력 검증(`repeatType != NONE` 시 `repeatEndDate` 필수 등)을 추가한다.

3. 권한/가시성 정합화
   - 학부모도 같은 유치원의 `KINDERGARTEN` 일정은 조회 가능하도록 정리한다.
   - 교차 유치원 일정 상세 접근 실패 회귀 테스트를 추가한다.

4. 문서화 및 검증
   - `README.md`, `docs/phase/`에 구현 배경과 면접 포인트를 정리한다.
   - `./gradlew compileJava compileTestJava`
   - `./gradlew test --tests "com.erp.api.CalendarApiIntegrationTest"`
   - `git diff --check`

## 3) 검증 계획
- 로컬 검증
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.CalendarApiIntegrationTest"`
  - `git diff --check`

## 4) 리스크 및 대응
- 반복 일정 조회를 서비스에서 잘못 확장하면 같은 일정이 누락되거나 중복될 수 있음
  - 대응: 주간 반복/유치원 전체/교차 유치원 접근 실패를 통합 테스트로 고정
- 기존 데이터 중 `repeatType != NONE`인데 `repeatEndDate`가 비어 있을 수 있음
  - 대응: 신규 입력은 엄격히 막고, 조회는 null-safe 하게 처리해 기존 데이터로 인한 장애를 피함
- 응답 계약을 크게 바꾸면 화면이 깨질 수 있음
  - 대응: 기존 `CalendarEventResponse` 구조는 유지하고, occurrence 확장 시 날짜 필드만 바꾼다
