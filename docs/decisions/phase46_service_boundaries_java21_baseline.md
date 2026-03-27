# Phase 46: 신청서 권한 경계 보강, 출결 요청 중복 가드, Java 21 기준선

## 배경

- `KidApplication`, `KindergartenApplication` 상세 조회는 역할보다 범위가 넓어, 같은 유치원 소속이라는 이유만으로 타인의 신청서를 볼 수 있는 여지가 있었다.
- `AttendanceChangeRequest` 생성은 선조회(`exists`) 후 저장하는 구조라, 병렬 요청이나 더블클릭 시 동일 원생/날짜의 `PENDING` 요청이 중복될 수 있었다.
- 대시보드 캐시는 출석/공지 변경만 무효화하고 있어, 입학 승인/offer 수락/교사 승인처럼 통계값을 바꾸는 흐름에서는 stale 데이터가 남을 수 있었다.
- 빌드와 CI는 아직 Java 17 기준이어서 현재 포트폴리오 기준선과 도구 체인을 Java 21로 올릴 필요가 있었다.

## 목표

1. 신청서 상세 조회를 `본인 + 승인권자` 중심으로 좁혀 개인정보 노출 여지를 줄인다.
2. 출결 변경 요청 생성은 서비스 검증 + DB 제약을 함께 사용해 중복 `PENDING` 생성 경로를 막는다.
3. 대시보드 캐시를 실제 통계 변경 경로까지 일관되게 무효화한다.
4. 로컬/CI/문서/블로그의 Java 기준선을 21로 통일한다.

## 결정

### 1) 신청서 상세 조회는 역할만이 아니라 소유권/승인권자로 제한한다

- `KidApplication`
  - 허용: 신청한 학부모 본인, 같은 유치원의 교사/원장
  - 차단: 같은 유치원의 다른 학부모
- `KindergartenApplication`
  - 허용: 지원한 교사 본인, 해당 유치원의 원장
  - 차단: 같은 유치원의 다른 교사, 학부모

구현은 컨트롤러의 coarse role gate와 서비스 계층의 object-level access policy를 함께 사용한다.

### 2) 출결 요청 중복은 DB가 마지막으로 막는다

- 서비스는 기존처럼 `existsByKidIdAndDateAndStatus(...)`로 빠르게 1차 차단한다.
- DB에는 `PENDING`일 때만 값이 채워지는 generated column을 두고 `kid_id + pending_request_date` unique 제약을 추가한다.
- 저장 시 `DataIntegrityViolationException`이 발생하면 `ATTENDANCE_CHANGE_REQUEST_ALREADY_PENDING`으로 번역한다.

즉, “선검사 + DB 최종 보장” 구조로 닫는다.

### 3) 대시보드 캐시는 실제 통계 변경 시점에서 비운다

아래 경로에서 `dashboardStatistics` 캐시를 직접 비운다.

- 입학 즉시 승인
- 입학 offer 수락
- 교사 지원 승인

waitlist/offer/reject처럼 통계값을 직접 바꾸지 않는 경로는 이번 배치에서 제외한다.

### 4) Java 21을 현재 기준선으로 삼되, 로컬은 21 이상 JDK를 허용한다

- `build.gradle`은 `sourceCompatibility/targetCompatibility = 21`과 `--release 21`로 표준화한다.
- CI는 Temurin 21로 고정한다.
- 로컬은 Java 21 이상 JDK에서도 21 타깃으로 컴파일되도록 허용한다.

이렇게 하면 CI 기준은 단단하게 유지하면서, 개발자 로컬 환경의 상위 JDK도 수용할 수 있다.

## 구현 요약

- `AccessPolicyService`
  - `validateKidApplicationReadAccess(...)`
  - `validateKindergartenApplicationReadAccess(...)`
- `KidApplicationController`, `KindergartenApplicationController`
  - 상세 조회 endpoint에 명시적 `@PreAuthorize` 추가
- `KidApplicationService`, `KindergartenApplicationService`
  - 상세 조회 권한 검증을 서비스 계층으로 이동/정리
  - 승인 경로의 대시보드 캐시 무효화 추가
- `KindergartenApplicationRepository`
  - `findByIdAndDeletedAtIsNull(...)`
  - `findByIdAndDeletedAtIsNullForUpdate(...)`
- `KindergartenApplication`
  - 상태 전이 예외를 `BusinessException(AP011)`로 통일
- `V14__guard_pending_attendance_change_requests.sql`
  - 중복 `PENDING` 요청 방지용 generated column + unique 제약
- `AttendanceChangeRequestService`
  - save 시 `DataIntegrityViolationException` -> `AT005` 번역
- `build.gradle`, `.github/workflows/ci.yml`
  - Java 21 기준선과 CI Java 21 전환

## 검증 포인트

1. 같은 유치원의 다른 학부모는 `KidApplication` 상세를 볼 수 없어야 한다.
2. 같은 유치원의 다른 교사는 `KindergartenApplication` 상세를 볼 수 없어야 한다.
3. 동일 원생/날짜로 `PENDING` 출결 요청을 두 번 만들 수 없어야 한다.
4. 입학 승인/offer 수락/교사 승인 후 대시보드 통계 캐시가 stale하지 않아야 한다.
5. 빌드/CI/문서가 Java 21 기준으로 일치해야 한다.

## 인터뷰 답변 포인트

### 1) 왜 상세 조회 권한을 더 좁혔나요?

- 목록 권한보다 상세 권한이 넓으면 개인정보 노출이 생길 수 있어서, 서비스 계층에서 `역할 + 소유권/승인권자`까지 같이 검증했습니다.

### 2) 왜 출결 요청 중복을 DB까지 내렸나요?

- 서비스의 선조회만으로는 race condition을 완전히 막을 수 없어서, MySQL unique 제약을 마지막 방어선으로 둔 뒤 애플리케이션 예외로 번역했습니다.

### 3) 왜 Java 21로 올렸나요?

- 현재 LTS 기준선으로 언어/도구 체인을 맞추고, CI와 문서, 블로그 설명까지 같은 기준으로 정리하기 위해서입니다.

## 트레이드오프

- `KidApplication`은 같은 유치원의 교사도 상세 조회를 허용한다.
  - 이유: 현재 검토 큐와 reviewer 역할이 teacher까지 열려 있기 때문이다.
  - 더 보수적으로 가려면 이후 principal-only 상세 조회로 추가 축소할 수 있다.

- 출결 요청 중복 가드는 generated column을 사용한다.
  - 이유: MySQL에서 partial unique index를 직접 쓸 수 없어서, 상태 기반 유일성 제약을 우회적으로 모델링해야 했기 때문이다.
