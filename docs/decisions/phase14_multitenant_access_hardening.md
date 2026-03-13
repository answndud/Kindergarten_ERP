# Phase 14: 멀티테넌시 권한 경계 하드닝

## 배경

- 기존 구현은 인증과 역할 체크는 있었지만, "같은 유치원 소속인지", "내 자녀인지", "내가 실제 수신자인지" 같은 데이터 경계 검증이 도메인별로 빠져 있었다.
- 그 결과 로그인만 된 사용자가 다른 유치원의 원생, 출석, 알림장, 공지사항, 알림 데이터를 `id` 기반으로 조회하거나 수정할 수 있는 IDOR(Insecure Direct Object Reference) 위험이 있었다.
- 포트폴리오 관점에서도 이 문제는 치명적이다. 기능이 많아도 데이터 경계가 약하면 "운영 감각 없는 CRUD 프로젝트"로 평가되기 쉽다.

## 문제 재현 시나리오

1. 다른 유치원 교사가 `/api/v1/kids/{kidId}`로 타 유치원 원생 상세 조회
2. 다른 유치원 원생에 대해 `/api/v1/attendance`로 출석 생성
3. 다른 유치원 학부모가 `/api/v1/notepads/{id}`로 알림장 상세 조회
4. 다른 유치원 학부모가 `/api/v1/announcements/{id}`로 공지사항 조회
5. 원장/교사가 `/api/v1/notifications`에 타 유치원 `receiverId`를 넣어 알림 발송
6. soft delete 된 알림을 `id`로 다시 조회

## 결정

1. 컨트롤러에서 인증 사용자 `memberId`를 서비스 계층으로 명시적으로 전달한다.
2. 서비스 계층에서 공통 접근 정책을 강제하는 `AccessPolicyService`를 도입한다.
3. 권한을 읽기(`read`)와 관리(`manage`)로 분리한다.
4. 상세 조회 계열에서도 `deletedAt` 필터를 일관 적용한다.

## 구현 요약

### 1) 공통 접근 정책 도입

- `AccessPolicyService`를 추가해 아래 정책을 한 곳에서 관리했다.
  - 같은 유치원 소속 여부
  - 교직원(원장/교사) 전용 관리 권한
  - 학부모-원생 연결 여부
  - 반 접근 가능 여부
  - 알림장/공지사항/알림 수신 대상 검증

### 2) 서비스 계층 권한 하드닝

- `KidService`
  - 원생 생성/조회/수정/삭제, 반 변경, 학부모 연결/해제에 요청자 기준 검증 추가
- `ClassroomService`
  - 반 조회/수정/삭제, 담임 배정/해제 시 같은 유치원 검증 추가
- `AttendanceService`
  - 출석 생성/조회/수정/삭제, 일괄 처리, 월간 통계/리포트에 요청자 검증 추가
- `NotepadService`
  - 알림장 생성/조회/수정/삭제, 읽음 처리에 대상자 검증 추가
  - 학부모는 내 자녀/내 반 범위만 읽을 수 있도록 제한
- `AnnouncementService`
  - 공지사항 상세/목록/검색/인기/중요 조회에 같은 유치원 검증 추가
- `NotificationService`
  - 생성 시 발신자와 수신자 소속 유치원 검증 추가
  - 상세/읽음/삭제 시 soft delete 된 알림 재접근 차단

### 3) 컨트롤러 정합성 정리

- API 컨트롤러와 View 컨트롤러가 모두 요청자 `memberId`를 서비스로 전달하도록 수정했다.
- 권한 검증을 컨트롤러에서 흩뿌리지 않고 서비스 계층에 수렴시켜, HTMX/Thymeleaf/API 경로가 달라도 같은 정책이 적용되도록 맞췄다.

## 왜 서비스 계층에서 막았는가

- 컨트롤러 단의 역할 체크만으로는 `id` 기반 객체 접근을 안전하게 보장할 수 없다.
- 같은 서비스가 API/뷰 컨트롤러 양쪽에서 재사용되기 때문에, 경계 검증은 가장 아래 공통 실행 지점인 서비스 계층에 두는 편이 일관성과 유지보수성이 높다.
- 특히 학부모는 "로그인 가능"과 "해당 자녀 데이터 접근 가능"이 전혀 다른 조건이라, 도메인 객체를 조회한 뒤 관계를 검증하는 정책이 필요했다.

## 테스트 증빙

아래 회귀 테스트를 추가해 실제 취약 경로를 실패 케이스로 고정했다.

- `KidApiIntegrationTest`
  - 다른 유치원 교사의 원생 상세 조회 차단
- `AttendanceApiIntegrationTest`
  - 다른 유치원 원생에 대한 출석 등록 차단
- `NotepadApiIntegrationTest`
  - 다른 유치원 학부모의 알림장 열람 차단
  - 다른 유치원 교사의 알림장 삭제 차단
- `AnnouncementApiIntegrationTest`
  - 다른 유치원 학부모의 공지사항 조회 차단
- `NotificationApiIntegrationTest`
  - 다른 유치원 사용자 대상 알림 발송 차단
  - soft delete 이후 알림 재조회 차단

실행 명령:

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.api.KidApiIntegrationTest" \
  --tests "com.erp.api.AttendanceApiIntegrationTest" \
  --tests "com.erp.api.NotepadApiIntegrationTest" \
  --tests "com.erp.api.AnnouncementApiIntegrationTest" \
  --tests "com.erp.api.NotificationApiIntegrationTest"
```

결과:

- 컴파일 성공
- 관련 통합 테스트 통과

## 인터뷰 답변 포인트

### 1) 이번 개선의 핵심은 무엇인가

- "역할 기반 인증"에서 끝내지 않고, "데이터 소유/소속 기반 인가"까지 내려간 점이 핵심이다.
- 원장/교사/학부모의 권한 차이보다 더 중요한 건 "어느 유치원 데이터인가"와 "정말 내 자녀 데이터인가"였다.

### 2) 왜 읽기와 관리를 분리했는가

- 학부모는 내 자녀의 알림장/출석을 읽을 수는 있어야 하지만, 수정/삭제 권한까지 가져서는 안 된다.
- 교직원도 같은 유치원 안에서는 관리 가능하지만 다른 유치원 데이터에는 접근하면 안 된다.
- 그래서 `read`와 `manage`를 분리해야 역할과 데이터 경계를 함께 표현할 수 있다.

### 3) 어떤 시그널을 보여주려 했는가

- 단순 CRUD보다 "보안 경계 설계", "서비스 계층 정책화", "회귀 테스트로 재발 방지"를 보여주려 했다.
- 포트폴리오에서 이 작업은 기능 추가보다 신뢰성 향상에 더 큰 의미가 있다.

## 트레이드오프

- 장점
  - 도메인별 권한 누락을 공통 정책으로 수렴
  - API/뷰 경로가 달라도 동일한 권한 모델 적용
  - 면접에서 설명 가능한 보안 설계 포인트 확보
- 단점
  - 서비스 시그니처에 `requesterId`가 추가되어 변경 범위가 넓어짐
  - 권한 검증이 늘어나 테스트 데이터 설계가 더 엄격해짐

## 남은 개선 과제

1. H2 + Mock Redis 기반 통합 테스트를 MySQL/Redis Testcontainers 기반으로 전환
2. 쿼리 단위에서도 `kindergarten_id` 조건을 더 강하게 밀어 넣어 애플리케이션 검증과 DB 조회 범위를 이중화
3. 권한 정책을 문서뿐 아니라 아키텍처 다이어그램으로도 정리해 README/포트폴리오 소개 자료와 연결
