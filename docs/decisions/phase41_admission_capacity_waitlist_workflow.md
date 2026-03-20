# Phase 41. 반 정원, Waitlist, Offer 기반 입학 워크플로우

## 1. 배경

이전 입학 신청 흐름은 `PENDING -> APPROVED/REJECTED/CANCELLED` 중심이었다.

즉 승인되면 바로 `Kid`를 만들고, 반 정원이나 제안 만료 같은 운영 현실은 모델에 없었다.

이 구조는 두 가지 한계를 가졌다.

1. 반이 이미 가득 찬 상태에서도 "승인"이라는 단일 액션만 존재해 운영 정책을 코드로 설명하기 어려웠다.
2. 포트폴리오 관점에서 상태 전이, 좌석 예약, 만료 처리 같은 백엔드 핵심 포인트가 약했다.

## 1-1. Before / After

| 구분 | Before | After |
|---|---|---|
| 반 모델 | 이름, 연령대 중심 | `capacity` 포함 |
| 신청 상태 | `PENDING/APPROVED/REJECTED/CANCELLED` | `WAITLISTED`, `OFFERED`, `OFFER_EXPIRED` 추가 |
| 승인 결과 | 즉시 `Kid` 생성 | waitlist 배치 -> offer -> parent accept 후 생성 |
| 좌석 계산 | 없음 | 재원 아동 + active offer를 같이 좌석 점유로 계산 |
| 만료 처리 | 없음 | `offerExpiresAt` + scheduled expire |

## 2. 이번 결정

### 2-1. 반 정원을 엔티티와 API에 올린다

- `Classroom.capacity`를 추가했다.
- 반 생성/수정 API는 정원을 함께 받도록 확장했다.
- 현재 재원 수보다 작은 값으로 정원을 줄이는 것은 차단한다.

즉 정원은 단순 표시값이 아니라, 입학/이동/반 수정의 실제 제약 조건이 된다.

### 2-2. 입학 신청은 단순 승인 대신 상태 전이 문제로 푼다

- `WAITLISTED`
  - 정원이 없어 바로 입학시킬 수 없을 때
- `OFFERED`
  - 자리가 나서 학부모에게 제안했지만 아직 수락하지 않았을 때
- `OFFER_EXPIRED`
  - offer 만료 시각을 넘겨 자동 만료된 상태

즉 "승인"만 있는 CRUD에서 "검토 -> 대기열 -> 제안 -> 수락/만료"가 있는 워크플로우로 바꿨다.

### 2-3. 좌석 계산은 재원 아동과 active offer를 함께 본다

- 이미 배정된 `Kid` 수만 보면, 동시에 여러 offer를 보내는 순간 oversell이 생길 수 있다.
- 그래서 `ClassroomCapacityService`는
  - 현재 반 소속 아동 수
  - 같은 반의 `OFFERED` 신청 수
  를 합쳐 좌석을 계산한다.

즉 offer 자체를 임시 좌석 예약으로 취급했다.

### 2-4. 수락 시점에만 실제 `Kid`를 만든다

- `OFFERED` 상태를 학부모가 수락해야 `Kid`를 생성한다.
- 이 시점까지는 신청서만 존재하고, 실제 원생 엔티티는 아직 없다.

이렇게 해서 "운영상 검토 중인 사람"과 "실제 재원 아동"을 구분했다.

## 3. 구현 포인트

### schema / entity

- `V13__add_admission_workflow_attendance_requests_and_domain_audit.sql`
  - `classroom.capacity`
  - `kid_application.assigned_classroom_id`
  - `waitlisted_at`, `offered_at`, `offer_expires_at`, `offer_accepted_at`, `decision_note`
  - offer 조회/만료용 인덱스 추가
- `KidApplication`
  - `placeOnWaitlist`, `offerSeat`, `acceptOffer`, `markOfferExpired`

### service

- `ClassroomCapacityService`
  - 반 row lock
  - 좌석 요약
  - 정원 초과/축소 검증
- `KidApplicationService`
  - `placeOnWaitlist`
  - `offer`
  - `acceptOffer`
  - `expireOffers`
- `KidService`
  - 직접 원생 생성/반 이동 시에도 정원 검증

### API

- `GET /api/v1/kid-applications/queue`
- `PUT /api/v1/kid-applications/{id}/waitlist`
- `PUT /api/v1/kid-applications/{id}/offer`
- `PUT /api/v1/kid-applications/{id}/accept-offer`

## 4. 트레이드오프

### 장점

- 반 정원과 운영 현실을 코드로 설명할 수 있다.
- 대기열/offer/수락/만료라는 상태 전이 설계를 포트폴리오로 보여줄 수 있다.
- offer를 좌석 예약으로 포함해 oversell 가능성을 줄였다.

### 비용

- 상태가 늘어나면서 API/문서/테스트 복잡도가 증가한다.
- 현재 waitlist 순위 정책은 FIFO에 가깝고, 우선순위 점수 모델은 아직 없다.
- offer 만료는 단순 scheduler 기반이라, 분산 스케줄링/cluster-safe 보강은 다음 단계다.

## 5. 검증

실행 명령:

```bash
./gradlew --no-daemon compileJava compileTestJava
./gradlew --no-daemon test --tests "com.erp.api.KidApplicationApiIntegrationTest" --tests "com.erp.api.ClassroomApiIntegrationTest" --tests "com.erp.api.KidApiIntegrationTest"
```

검증한 내용:

- 반 생성 시 정원 값 저장
- 정원 초과 상태에서 원생 직접 등록 차단
- 정원 초과 상태에서 반 이동 차단
- 입학 신청 waitlist 전환
- staff의 offer 전환과 parent 수락
- 다른 유치원 교사의 offer 차단
- review queue에서 `WAITLISTED`, `OFFERED` 조회

## 6. 면접에서 말할 포인트

- "입학 신청을 승인 버튼 하나로 끝내지 않고, 정원/대기열/offer/만료가 있는 상태 전이 문제로 바꿨습니다."
- "좌석은 재원 아동만이 아니라 active offer까지 예약으로 계산해 oversell을 막았습니다."
- "정원 검증을 입학 신청뿐 아니라 원생 직접 등록과 반 이동에도 공통으로 적용해 모델 일관성을 맞췄습니다."
