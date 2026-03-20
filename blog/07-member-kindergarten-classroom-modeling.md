# [Spring Boot 포트폴리오] 07. `Member`, `Kindergarten`, `Classroom`으로 첫 관계를 어떻게 모델링했는가

## 1. 이번 글에서 풀 문제

도메인 모델링을 처음 할 때 가장 많이 하는 실수는 CRUD 화면 기준으로 엔티티를 나누는 것입니다.

예를 들어

- 회원 테이블
- 유치원 테이블
- 반 테이블

을 그냥 각각 독립적으로 만들고 끝내 버립니다.

하지만 Kindergarten ERP에서는 이 세 엔티티가 서로의 규칙을 만들어 냅니다.

- 회원은 역할을 가진다
- 회원은 유치원에 소속될 수 있다
- 반은 반드시 유치원에 속한다
- 반에는 담임 교사가 배정될 수 있다

즉, 이 세 엔티티는 단순 마스터 데이터가 아니라  
**권한과 운영 흐름의 뼈대**입니다.

## 2. 먼저 알아둘 개념

### 2-1. 정적 팩토리 메서드

정적 팩토리 메서드는 `new` 대신 `create(...)` 같은 메서드로 객체를 만드는 방식입니다.

이 프로젝트에서는 거의 모든 핵심 엔티티가 이 방식을 씁니다.

장점은 생성 규칙을 한 곳에 모을 수 있다는 점입니다.

### 2-2. 연관관계

이 세 엔티티의 핵심 연관관계는 아래입니다.

- `Member -> Kindergarten`
- `Classroom -> Kindergarten`
- `Classroom -> Member(teacher)`

즉, 회원은 유치원에 소속되고, 반은 유치원과 교사에 연결됩니다.

### 2-3. Soft Delete

삭제를 바로 물리 삭제하지 않고 `deletedAt`을 남기는 방식입니다.

이 프로젝트에서는 `Member`, `Classroom`이 soft delete를 고려합니다.

## 3. 이번 글에서 다룰 파일

```text
- src/main/java/com/erp/domain/member/entity/Member.java
- src/main/java/com/erp/domain/member/entity/MemberRole.java
- src/main/java/com/erp/domain/member/entity/MemberStatus.java
- src/main/java/com/erp/domain/kindergarten/entity/Kindergarten.java
- src/main/java/com/erp/domain/classroom/entity/Classroom.java
- src/main/java/com/erp/domain/member/service/MemberService.java
- src/main/java/com/erp/domain/classroom/service/ClassroomService.java
- src/test/java/com/erp/api/ClassroomApiIntegrationTest.java
- src/test/java/com/erp/api/MemberApiIntegrationTest.java
- docs/decisions/phase00_setup.md
- docs/decisions/phase41_admission_capacity_waitlist_workflow.md
```

## 4. 설계 구상

이 세 엔티티는 이 프로젝트의 “조직 구조”를 표현합니다.

```mermaid
flowchart TD
    A["Kindergarten"] --> B["Classroom"]
    A --> C["Member"]
    C --> D["PRINCIPAL / TEACHER / PARENT"]
    B --> E["Teacher 배정"]
```

핵심 설계 기준은 아래였습니다.

1. 역할은 `MemberRole` enum으로 제한한다
2. 회원은 유치원에 속할 수 있지만, 역할마다 쓰임새는 다르다
3. 반은 유치원 없이 존재할 수 없다
4. 반 정원과 교사 배정 규칙은 `Classroom`이 가진다

## 5. 코드 설명

### 5-1. `Member`: 이 프로젝트의 모든 사용자 시작점

[Member.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/member/entity/Member.java)의 핵심 필드는 아래입니다.

- `email`
- `password`
- `name`
- `phone`
- `role`
- `status`
- `kindergarten`

핵심 생성 메서드는 두 개입니다.

- `create(...)`
  - 로컬 계정 생성
- `createSocial(...)`
  - 소셜 로그인 계정 생성

핵심 비즈니스 메서드는 아래입니다.

- `assignKindergarten(...)`
- `updateProfile(...)`
- `changePassword(...)`
- `activateMember()`
- `markPending()`
- `withdraw()`

즉, `Member`는 단순 회원 테이블이 아니라  
인증 방식, 역할, 상태, 소속을 함께 관리하는 중심 엔티티입니다.

### 5-2. `Kindergarten`: 소속과 경계를 만드는 엔티티

[Kindergarten.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kindergarten/entity/Kindergarten.java)는

- `name`
- `address`
- `phone`
- `openTime`
- `closeTime`

을 가집니다.

핵심 메서드는 아래입니다.

- `create(...)`
- `update(...)`

언뜻 단순해 보이지만, 이 엔티티가 중요한 이유는  
이후 거의 모든 접근 제어가 “같은 유치원 소속인가?” 기준으로 흘러가기 때문입니다.

즉, `Kindergarten`은 단순 마스터 데이터가 아니라  
**테넌트 경계의 기준 엔티티**입니다.

### 5-3. `Classroom`: 유치원과 교사를 묶는 단위

[Classroom.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/classroom/entity/Classroom.java)의 핵심 필드는 아래입니다.

- `kindergarten`
- `name`
- `ageGroup`
- `capacity`
- `teacher`
- `deletedAt`

핵심 생성/수정 메서드는 아래입니다.

- `create(kindergarten, name, ageGroup)`
- `create(kindergarten, name, ageGroup, capacity)`
- `update(...)`

핵심 비즈니스 메서드는 아래입니다.

- `assignTeacher(...)`
- `removeTeacher()`
- `remainingSeats(...)`
- `canResizeTo(...)`
- `softDelete()`

즉, `Classroom`은 단순 반 이름이 아니라

- 교사 배정
- 정원
- soft delete

규칙까지 품고 있습니다.

## 6. 실제 흐름

이 세 엔티티는 실제로 아래 순서로 연결됩니다.

```mermaid
sequenceDiagram
    participant Principal as 원장
    participant Member as Member
    participant KG as Kindergarten
    participant Classroom as Classroom

    Principal->>Member: 회원가입 / 활성화
    Principal->>KG: 유치원 생성
    Principal->>Member: assignKindergarten(...)
    Principal->>Classroom: create(...)
    Principal->>Classroom: assignTeacher(...)
```

즉, 이 구조가 먼저 있어야

- 교사 지원 승인
- 원생 반 배정
- 출결/알림장/일정 기능

이 자연스럽게 붙습니다.

## 7. 테스트로 검증하기

이 구조는 API 테스트에서도 바로 검증됩니다.

- `ClassroomApiIntegrationTest`
  - 반 생성, 수정, 교사 배정, 정원 관련 흐름
- `MemberApiIntegrationTest`
  - 회원 프로필, 비밀번호, 소셜/로컬 계정 흐름

즉, 도메인 모델이 실제 API 계약으로 바로 이어집니다.

또한 최근에는 [phase41_admission_capacity_waitlist_workflow.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase41_admission_capacity_waitlist_workflow.md)에서
`Classroom.capacity`가 waitlist/offer 워크플로우와도 연결됐습니다.

## 8. 회고

처음에는 `Classroom`에 정원이 없었습니다.  
하지만 프로젝트가 운영형 워크플로우로 커지면서 정원이 반드시 필요해졌습니다.

이건 중요한 교훈입니다.

- 엔티티는 처음부터 완벽하지 않아도 된다
- 하지만 확장될 방향을 막아 두면 나중에 더 힘들어진다

`Classroom`이 유치원과 교사, 정원까지 함께 품고 있었기 때문에  
나중에 waitlist 기능을 붙이기 훨씬 쉬웠습니다.

## 9. 취업 포인트

이 글에서 강조할 포인트는 아래입니다.

- “회원, 유치원, 반을 단순 테이블이 아니라 권한과 소속 구조의 시작점으로 모델링했습니다.”
- “`MemberRole`, `MemberStatus`를 enum으로 제한해 역할/상태 규칙을 코드에 녹였습니다.”
- “`Classroom`은 초기에 단순 반 관리였지만, 이후 정원과 배정 워크플로우 확장까지 고려할 수 있는 구조로 키웠습니다.”
