# [Spring Boot 포트폴리오] 08. `Kid`, `ParentKid`, `Attendance`로 첫 업무 Aggregate를 만드는 법

## 1. 이번 글에서 풀 문제

회원, 유치원, 반까지 만들었다고 해서 실제 서비스가 되는 것은 아닙니다.  
이제 “업무”가 들어와야 합니다.

Kindergarten ERP에서 가장 먼저 업무 규칙이 강하게 드러나는 곳이 바로 이 세 엔티티입니다.

- `Kid`
- `ParentKid`
- `Attendance`

이 세 가지가 묶이면서 아래 문제가 생깁니다.

- 원생은 어느 반에 속하는가?
- 원생은 어떤 학부모와 연결되는가?
- 출석은 하루에 한 번만 있어야 하지 않는가?
- 결석/지각/조퇴 같은 상태는 어디서 관리해야 하는가?

즉, 이 단계부터는 “마스터 데이터”가 아니라  
**업무 Aggregate**를 어떻게 설계할지가 중요해집니다.

## 2. 먼저 알아둘 개념

### 2-1. Aggregate

Aggregate는 함께 변경되고 함께 검증되어야 하는 객체 묶음이라고 이해하면 됩니다.

이 프로젝트에서는

- `Kid`
- `ParentKid`
- `Attendance`

가 강하게 연결됩니다.

### 2-2. 중간 엔티티

`ParentKid`는 단순 다대다 연결이 아니라,  
관계의 의미(`FATHER`, `MOTHER`, `GUARDIAN` 등)를 담는 중간 엔티티입니다.

즉, `@ManyToMany`로 끝내지 않고 별도 엔티티로 승격시킨 것입니다.

### 2-3. 상태 enum

출석은 문자열이 아니라 enum으로 관리하는 편이 안전합니다.

이 프로젝트는 `AttendanceStatus`로 아래를 구분합니다.

- `PRESENT`
- `ABSENT`
- `LATE`
- `EARLY_LEAVE`
- `SICK_LEAVE`

## 3. 이번 글에서 다룰 파일

```text
- src/main/java/com/erp/domain/kid/entity/Kid.java
- src/main/java/com/erp/domain/kid/entity/ParentKid.java
- src/main/java/com/erp/domain/kid/entity/Relationship.java
- src/main/java/com/erp/domain/attendance/entity/Attendance.java
- src/main/java/com/erp/domain/attendance/entity/AttendanceStatus.java
- src/main/java/com/erp/domain/kid/service/KidService.java
- src/main/java/com/erp/domain/attendance/service/AttendanceService.java
- src/test/java/com/erp/api/KidApiIntegrationTest.java
- src/test/java/com/erp/api/AttendanceApiIntegrationTest.java
- docs/decisions/phase09_kid_management.md
- docs/decisions/phase04_attendance.md
```

## 4. 설계 구상

이 단계의 핵심 관계는 아래처럼 볼 수 있습니다.

```mermaid
flowchart TD
    A["Classroom"] --> B["Kid"]
    B --> C["ParentKid"]
    C --> D["Parent(Member)"]
    B --> E["Attendance"]
```

설계 기준은 아래였습니다.

1. 원생은 반드시 반에 속한다
2. 학부모와 원생은 별도 관계 엔티티로 연결한다
3. 출석은 원생과 날짜 기준으로 유일해야 한다
4. 출석 상태 변경 메서드는 엔티티 안에 둔다

## 5. 코드 설명

### 5-1. `Kid`: 반 배정과 보호자 연결의 중심

[Kid.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/entity/Kid.java)의 핵심 필드는 아래입니다.

- `classroom`
- `name`
- `birthDate`
- `gender`
- `admissionDate`
- `deletedAt`
- `parents`

핵심 메서드는 아래입니다.

- `create(...)`
- `update(...)`
- `assignClassroom(...)`
- `addParent(...)`
- `removeParent(...)`
- `hasParent(...)`

즉, `Kid`는 단순 원생 정보가 아니라  
반 소속과 보호자 연결의 중심 엔티티입니다.

### 5-2. `ParentKid`: 관계 자체를 도메인으로 올린다

[ParentKid.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/kid/entity/ParentKid.java)는

- `kid`
- `parent`
- `relationship`
- `createdAt`

을 가집니다.

핵심 메서드는 아래입니다.

- `create(...)`
- `changeRelationship(...)`

이 구조가 좋은 이유는 `@ManyToMany`로는 담기 어려운 의미를 담을 수 있기 때문입니다.

- 단순 연결이 아니라 “어떤 보호자인가?”
- 연결 생성 시각은 언제인가?

즉, 관계도 하나의 도메인 정보로 다룹니다.

### 5-3. `Attendance`: 출석 상태를 엔티티 메서드로 다룬다

[Attendance.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/attendance/entity/Attendance.java)의 핵심 필드는 아래입니다.

- `kid`
- `date`
- `status`
- `dropOffTime`
- `pickUpTime`
- `note`

핵심 생성 메서드는 아래입니다.

- `create(...)`
- `createDropOff(...)`

핵심 비즈니스 메서드는 아래입니다.

- `updateAttendance(...)`
- `recordDropOff(...)`
- `recordPickUp(...)`
- `markAbsent(...)`
- `markLate(...)`
- `markEarlyLeave(...)`
- `markSickLeave(...)`

즉, 출석 상태 전이 규칙을 서비스에 흩뿌리지 않고 엔티티 메서드로 모았습니다.

### 5-4. 날짜 유일성은 엔티티와 스키마 둘 다에서 보장한다

[Attendance.java](/Users/alex/project/kindergarten_ERP/erp/src/main/java/com/erp/domain/attendance/entity/Attendance.java)는

```java
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kid_id", "date"})
})
```

를 가집니다.

그리고 `V1__init_schema.sql`에서도 `UNIQUE KEY uk_kid_date (kid_id, date)`를 둡니다.

즉, “하루 한 원생 한 출석” 규칙을

- JPA 매핑
- 실제 DB 스키마

둘 다에서 잡은 것입니다.

## 6. 실제 흐름

이 세 엔티티는 실제 업무에서 아래처럼 움직입니다.

```mermaid
sequenceDiagram
    participant Principal as 원장/교사
    participant Kid as Kid
    participant Relation as ParentKid
    participant Attendance as Attendance

    Principal->>Kid: 원생 등록
    Principal->>Relation: 학부모 연결
    Principal->>Attendance: 일별 출석 생성
    Principal->>Attendance: 결석/지각/조퇴 처리
```

즉, 원생 관리와 출석 관리는 따로 떨어진 기능이 아니라  
같은 Aggregate 안에서 자연스럽게 이어집니다.

## 7. 테스트로 검증하기

이 구조는 통합 테스트에서도 바로 검증됩니다.

- `KidApiIntegrationTest`
  - 원생 생성, 수정, 학부모 연결, 반 이동
- `AttendanceApiIntegrationTest`
  - 출석 생성, 수정, 결석/지각/조퇴 처리

또한 설계 의도는 아래 결정 로그와 연결됩니다.

- [phase09_kid_management.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase09_kid_management.md)
- [phase04_attendance.md](/Users/alex/project/kindergarten_ERP/erp/docs/decisions/phase04_attendance.md)

즉, 도메인 모델, API, 테스트, 결정 로그가 한 방향을 봅니다.

## 8. 회고

이 단계에서 중요한 교훈은 두 가지입니다.

1. 단순 연결도 의미가 있으면 별도 엔티티로 올려야 한다
2. 상태 전이는 서비스보다 엔티티 메서드에 가까운 경우가 많다

`ParentKid`를 별도 엔티티로 만든 선택과  
`Attendance` 안에 상태 변경 메서드를 둔 선택은 둘 다 이후 확장에 유리했습니다.

## 9. 취업 포인트

이 글에서 면접에 연결할 수 있는 포인트는 아래입니다.

- “학부모-원생 관계를 단순 다대다가 아니라 의미를 가진 중간 엔티티로 모델링했습니다.”
- “출석은 원생+날짜 unique 규칙을 JPA와 DB 양쪽에서 보장했습니다.”
- “결석/지각/조퇴 같은 상태 전이를 엔티티 메서드에 모아 비즈니스 규칙을 응집시켰습니다.”
