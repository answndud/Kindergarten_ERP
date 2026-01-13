# Phase 7: 지원/승인 워크플로우 기술 선택 결정

## 개요
- 단계: 지원/승인 워크플로우 구현
- 목표: 교사의 유치원 지원, 학부모의 원아 입학 신청, 승인 프로세스
- 작업: KindergartenApplication, KidApplication 도메인

---

## 1. KindergartenApplication 엔티티 설계

### 결정
교사의 유치원 지원을 별도 엔티티로 관리

### 이유
1. **승인 프로세스**: 지원 → 대기 → 승인/거절 상태 관리
2. **이력 추적**: 언제 누가 승인/거절했는지 기록
3. **통계 활용**: 지원 현황 파악

### 관계 설계
```
Member (TEACHER) (1:N) KindergartenApplication
Kindergarten (1:N) KindergartenApplication
Member (PRINCIPAL) (1:N) KindergartenApplication (processed_by)
```

### 상태 값
- `PENDING`: 승인 대기
- `APPROVED`: 승인 완료
- `REJECTED`: 거절
- `CANCELLED`: 취소

### 변경 이력
- 2025-01-14: KindergartenApplication 엔티티 분리 결정

---

## 2. KidApplication 엔티티 설계

### 결정
학부모의 원아 입학 신청을 별도 엔티티로 관리

### 이유
1. **승인 프로세스**: 신청 → 대기 → 승인 시 Kid 생성
2. **정보 수집**: 입학 전 원아 정보 미리 확보
3. **부모-유치원 매칭**: 적절한 반 배정

### 관계 설계
```
Member (PARENT) (1:N) KidApplication
Kindergarten (1:N) KidApplication
Classroom (N:1) KidApplication (희망 반)
Kid (1:1) KidApplication (승인 후 생성)
```

### 주요 필드
- `kidName`, `birthDate`, `gender`: 원생 정보
- `preferredClassroom`: 희망 반
- `kidId`: 승인 후 생성된 Kid ID

### 변경 이력
- 2025-01-14: KidApplication 엔티티 분리 결정

---

## 3. 승인 로직

### 결정
승인 시 즉시 상태 변경 및 연관 엔티티 생성

### 교사 지원 승인 흐름
```
1. KindergartenApplication.approve(principal)
2. Member.assignKindergarten(kindergarten)
3. Member.activateMember()
4. 다른 대기 지원서 자동 거절
```

### 입학 신청 승인 흐름
```
1. KidApplication.approve(classroom, processor)
2. Kid 엔티티 생성
3. ParentKid 관계 생성
4. Member.assignKindergarten() (부모)
5. Member.activateMember()
```

### 이유
1. **트랜잭션 일관성**: 한 트랜잭션에서 모두 처리
2. **데이터 무결성**: 승인과 동시에 관련 엔티티 생성

### 변경 이력
- 2025-01-14: 승인 시 즉시 처리 채택

---

## 4. 중복 지원 방지

### 결정
유니크 제약조건으로 중복 지원 방지

### 구현 방식
```java
@UniqueConstraint(columnNames = {"teacher_id", "kindergarten_id"})
```

### 비즈니스 로직
```java
// 이미 대기 중인 지원서가 있는지 확인
if (repository.existsByTeacherIdAndStatus(...)) {
    throw new BusinessException(PENDING_APPLICATION_EXISTS);
}
```

### 이유
1. **DB 무결성**: 중복 데이터 방지
2. **비즈니스 로직**: 한 번에 한 유치원에만 지원

### 변경 이력
- 2025-01-14: 유니크 제약조건 채택

---

## 5. 알림 연동

### 결정
지원/승인/거절 시 자동 알림 생성

### 알림 타입
- `KINDERGARTEN_APPLICATION_SUBMITTED`: 원장에게 지원 알림
- `KINDERGARTEN_APPLICATION_APPROVED`: 교사에게 승인 알림
- `KINDERGARTEN_APPLICATION_REJECTED`: 교사에게 거절 알림
- `KID_APPLICATION_SUBMITTED`: 원장/교사에게 신청 알림
- `KID_APPLICATION_APPROVED`: 학부모에게 승인 알림
- `KID_APPLICATION_REJECTED`: 학부모에게 거절 알림

### 구현 방식
```java
// 지원 시
notificationService.notifyWithLink(
    principal.getId(),
    NotificationType.KINDERGARTEN_APPLICATION_SUBMITTED,
    "새로운 교사 지원",
    content,
    "/applications/pending"
);
```

### 이유
1. **실시성**: 즉시 상황 파악
2. **사용자 경험**: 별도 확인 없이 알림으로 알림

### 변경 이력
- 2025-01-14: 이벤트 기반 알림 채택

---

## 6. 권한 제어

### 결정
역할별 접근 제어

### 권한 규칙
- **지원**: 해당 역할만 (TEACHER, PARENT)
- **승인/거절**: 원장, 교사 (자신의 유치원만)
- **조회**: 본인 또는 해당 유치원 소속원

### 구현 방식
```java
@PreAuthorize("hasRole('TEACHER')")
public Long apply(Long teacherId, KindergartenApplicationRequest request) { }

@PreAuthorize("hasRole('PRINCIPAL')")
public void approve(Long applicationId, Long principalId) { }
```

### 변경 이력
- 2025-01-14: 역할별 접근 제어 채택

---

## 7. 지원서 API 설계

### 결정
RESTful API 설계

### 교사 지원 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | /api/v1/kindergarten-applications | 지원 신청 |
| GET | /api/v1/kindergarten-applications/my | 내 지원 목록 |
| GET | /api/v1/kindergarten-applications/pending | 대기 지원 목록 (원장) |
| PUT | /api/v1/kindergarten-applications/{id}/approve | 승인 |
| PUT | /api/v1/kindergarten-applications/{id}/reject | 거절 |
| PUT | /api/v1/kindergarten-applications/{id}/cancel | 취소 |

### 입학 신청 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | /api/v1/kid-applications | 입학 신청 |
| GET | /api/v1/kid-applications/my | 내 신청 목록 |
| GET | /api/v1/kid-applications/pending | 대기 신청 목록 |
| PUT | /api/v1/kid-applications/{id}/approve | 승인 |
| PUT | /api/v1/kid-applications/{id}/reject | 거절 |
| PUT | /api/v1/kid-applications/{id}/cancel | 취소 |

### 변경 이력
- 2025-01-14: RESTful API 채택

---

## 8. 리다이렉트 로직

### 결정
로그인 후 역할/상태별 리다이렉트

### 리다이렉트 규칙
- **원장 (유치원 없음)**: `/kindergarten/create`
- **교사 (유치원 없음)**: `/kindergarten/select`
- **PENDING 상태**: `/applications/pending`
- **기본**: 역할별 대시보드

### 구현 방식
```java
// RedirectAuthenticationSuccessHandler
if (member.getRole() == PRINCIPAL && member.getKindergarten() == null) {
    return "/kindergarten/create";
}
```

### 인터셉터로 강제 이동
```java
// RoleRedirectInterceptor
if (shouldForceRedirect(member, uri)) {
    response.sendRedirect(redirectUrl);
    return false;
}
```

### 이유
1. **사용자 경험**: 바로 필요한 페이지로 안내
2. **데이터 무결성**: 필수 정보 입력 유도

### 변경 이력
- 2025-01-14: 리다이렉트 핸들러 + 인터셉터 채택

---

## 면접 예상 질문 답변

### Q: 왜 지원서를 별도 엔티티로 만드셨나요?
> A: 승인 프로세스를 관리하기 위해서입니다. 단순히 Member에 kindergarten_id를 바로 할당하면 승인 대기 상태를 표현할 수 없고, 언제 누가 승인했는지 이력도 남지 않습니다. KindergartenApplication 엔티티를 두면 지원 → 대기 → 승인/거절의 상태 변화를 추적할 수 있고, 승인 시점에 정확하게 유치원을 배정할 수 있습니다.

### Q: 입학 신청 승인 시 어떤 일이 일어나나요?
> A: 한 트랜잭션에서 KidApplication 상태를 APPROVED로 변경하고, Kid 엔티티를 생성하고, ParentKid 관계를 맺습니다. 그리고 학부모에게 유치원을 배정하고 활성화합니다. 이렇게 트랜잭션으로 묶어서 승인 실패 시 롤백이 가능하고, 모든 관련 데이터를 일관되게 관리할 수 있습니다.

### Q: 중복 지원은 어떻게 방지하나요?
> A: DB 레벨에서 유니크 제약조건을 걸고, 애플리케이션 레벨에서도 대기 중인 지원서가 있는지 확인합니다. (teacher_id, kindergarten_id) 조합으로 유니크 제약을 걸어서 물리적으로 중복을 방지하고, 서비스에서 대기 중인 지원서가 있으면 예외를 던져서 비즈니스 로직을 강제합니다.

---

## 다음 단계
Phase 8: 알림 시스템 구현

---

## 구현 현황 (Phase 7 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/kindergartenapplication/entity/KindergartenApplication.java` | 교사 지원 엔티티 |
| `domain/kindergartenapplication/entity/ApplicationStatus.java` | 지원 상태 enum |
| `domain/kidapplication/entity/KidApplication.java` | 입학 신청 엔티티 |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/kindergartenapplication/repository/KindergartenApplicationRepository.java` | 교사 지원 리포지토리 |
| `domain/kidapplication/repository/KidApplicationRepository.java` | 입학 신청 리포지토리 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/kindergartenapplication/dto/request/KindergartenApplicationRequest.java` | 지원 요청 |
| `domain/kindergartenapplication/dto/response/KindergartenApplicationResponse.java` | 지원 응답 |
| `domain/kidapplication/dto/request/KidApplicationRequest.java` | 입학 신청 요청 |
| `domain/kidapplication/dto/response/KidApplicationResponse.java` | 입학 신청 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/kindergartenapplication/service/KindergartenApplicationService.java` | 교사 지원 서비스 |
| `domain/kidapplication/service/KidApplicationService.java` | 입학 신청 서비스 |

#### 5. 컨트롤러
| 파일 | 설명 |
|------|------|
| `domain/kindergartenapplication/controller/KindergartenApplicationController.java` | 교사 지원 API |
| `domain/kidapplication/controller/KidApplicationController.java` | 입학 신청 API |

#### 6. Security
| 파일 | 설명 |
|------|------|
| `global/security/RedirectAuthenticationSuccessHandler.java` | 로그인 후 리다이렉트 |
| `global/security/RoleRedirectInterceptor.java` | 강제 리다이렉트 인터셉터 |
| `global/config/WebMvcConfig.java` | 인터셉터 등록 |

---

### 구현된 기능 상세

#### 백엔드

**1. KindergartenApplication 엔티티**
```java
// 필드: teacher, kindergarten, status, message, processedAt, rejectionReason, processedBy
// 연관관계: Member (교사, N:1), Kindergarten (N:1), Member (처리자, N:1)
// 비즈니스 메서드: approve(), reject(), cancel()
```

**2. KidApplication 엔티티**
```java
// 필드: parent, kindergarten, kidName, birthDate, gender, preferredClassroom, notes, kidId
// 연관관계: Member (학부모, N:1), Kindergarten (N:1), Classroom (희망 반, N:1)
// 비즈니스 메서드: approve(classroom, processor, kidId), reject(), cancel()
```

**3. 워크플로우**
- 교사가 유치원 검색 → 지원 → 원장 승인 → 유치원 배정
- 학부모가 원아 정보 입력 → 유치원 검색 → 입학 신청 → 교사/원장 승인 → Kid 생성

---

**Phase 7 완료일: 2025-01-14**
