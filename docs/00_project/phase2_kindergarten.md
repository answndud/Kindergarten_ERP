# Phase 2: 유치원 & 반 관리 기술 선택 결정

## 개요
- 단계: 유치원 & 반 관리 구현
- 목표: 유치원 정보 등록, 반 생성/관리, 교사 배정
- 작업: Kindergarten, Classroom 도메인

---

## 1. Kindergarten 엔티티 설계

### 결정
유치원을 별도 엔티티로 분리하여 Member와 N:1 관계

### 이유
1. **다중 유치원**: 하나의 시스템으로 여러 유치원 관리 가능
2. **확장성**: 유치원별 설정, 정보 확장 용이
3. **데이터 정규화**: 유치원 정보를 회원 테이블에 중복 저장 방지
4. **승인 프로세스**: 원장이 유치원 등록 후 교사/학부모 승인 가능

### 관계 설계
```
Member (N:1) Kindergarten (1)
  └─ 여러 회원이 하나의 유치원에 소속
  └─ 원장은 유치원 생성, 교사/학부모는 승인 후 배정
```

### 대안 고려
- **Member에 통합**: 유치원 정보를 Member에 포함
  - 단점: 한 회원이 여러 유치원 소속 불가, 확장성 낮음

### 변경 이력
- 2024-12-28: 별도 엔티티로 분리 결정

---

## 2. Classroom 엔티티 설계

### 결정
반(Classroom)을 별도 엔티티로 Kindergarten와 N:1 관계

### 이유
1. **명확한 관계**: 한 유치원에 여러 반 존재
2. **유연한 연결**: 교사 변경 시 반은 유지
3. **Soft Delete**: deletedAt으로 삭제된 반 관리

### 관계 설계
```
Kindergarten (1:N) Classroom (N:1) Teacher(Member)
                                      (N:1)
                                        Kid
```

### Soft Delete 방식
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

// 삭제 시 deletedAt 설정, 실제 삭제 안 함
```

### 대안 고려
- **Hard Delete**: 실제로 DB에서 삭제
  - 단점: 데이터 복구 불가, 참조 무결성 위배

### 변경 이력
- 2024-12-28: Soft Delete 채택

---

## 3. 교사-반 연결: 일대일 관계

### 결정
한 교사는 한 반만 담당 (1:1)

### 이유
1. **단순성**: 담임 교사는 한 반만 담당
2. **명확한 책임**: 반별 책임 교사 명확
3. **실무에 맞춤**: 대부분의 유치원이 한 반 한 교사

### 확장성 고려
- 추후 부교사, 보조 교사 기능 필요 시 다대다 테이블 추가
- 지금은 MVP로 단순하게 1:1 구현

### 대안 고려
- **N:M (교사-반 중간테이블)**: 한 교사가 여러 반 담당
  - 복잡도 증가, 현재는 불필요

### 변경 이력
- 2024-12-28: 1:1 관계 채택

---

## 4. 권한 검증: @PreAuthorize

### 결정
Spring Security의 @PreAuthorize로 메서드별 권한 검증

### 이유
1. **선언적 보안**: 메서드 위에 어노테이션으로 권한 명시
2. **유연한 제어**: hasRole, hasAnyRole로 복합 조건 가능
3. **코드 간결**: 별도 if문으로 권한 체크 불필요

### 사용 예시
```java
@PreAuthorize("hasRole('PRINCIPAL')")
public void createKindergarten() { }

@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public void viewClassroom() { }
```

### 대안 고려
- **Controller에서 검증**: 각 메서드에서 if(role == ...) 체크
  - 단점: 코드 중복, 누락 가능

### 변경 이력
- 2024-12-28: @PreAuthorize 채택

---

## 5. 유효성 검증: @Valid

### 결정
Controller의 @RequestBody에 @Valid 추가

### 이유
1. **자동 검증**: DTO의 @NotBlank, @Pattern 등 자동 검증
2. **일관된 에러**: GlobalExceptionHandler에서 일관된 응답
3. **코드 간결**: 별도 if문으로 검증 불필요

### 흐름
```
요청 → @Valid 검증 → 실패 시 MethodArgumentNotValidException
    → GlobalExceptionHandler → ApiResponse.error 반환
```

### 대안 고려
- **Service에서 검증**: 각 필드별 if문 체크
  - 단점: 코드 중복

### 변경 이력
- 2024-12-28: @Valid 채택

---

## 6. DTO 레코드 사용

### 결정
Project Lombok을 사용하지 않고 직접 DTO 작성

### 이유
1. **명확성**: 필드명과 getter/setter 명확
2. **제어**: 검증 로직, 변환 로직 추가 용이
3. **안전성**: Lombok 버전 호환성 이슈 방지

### Request/Response 분리
```java
// Request
SignUpRequest { email, password, name, ... }
LoginRequest { email, password }

// Response
MemberResponse { id, email, name, role, ... }
TokenResponse { accessToken, refreshToken }
```

### 대안 고려
- **Entity 직접 반환**: 보안 문제 (password 노출)
- **Map 반환**: 타입 안전하지 않음

### 변경 이력
- 2024-12-28: 직접 DTO 작성

---

## 7. HTTP 메서드 매핑

### 결정
RESTful 원칙에 따라 HTTP 메서드 매핑

| 메서드 | 용도 | 예시 |
|--------|------|------|
| POST | 생성 | POST /api/v1/kindergartens |
| GET | 조회 | GET /api/v1/kindergartens/{id} |
| PUT | 전체 수정 | PUT /api/v1/kindergartens/{id} |
| PATCH | 부분 수정 | (추후 사용 가능) |
| DELETE | 삭제 | DELETE /api/v1/classrooms/{id} |

### 이유
1. **표준 준수**: RESTful API 표준
2. **명확한 의미**: HTTP 메서드로 의미 파악
3. **캐싱**: GET은 캐싱 가능, POST/PATCH/DELETE는 불가

### 변경 이력
- 2024-12-28: RESTful 매핑 채택

---

## 8. Soft Delete 구현

### 결정
deletedAt 필드로 Soft Delete 구현

### 이유
1. **데이터 보존**: 삭제된 데이터 복구 가능
2. **참조 무결성**: 다른 테이블에서 참조 시 에러 방지
3. **감사 추적**: 삭제 기록 유지

### 구현 방식
```java
// 삭제 시
public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}

// 조회 시 (QueryDSL)
.where(classroom.deletedAt.isNull())
```

### 대안 고려
- **Hard Delete**: 실제 DB에서 삭제
  - 단점: 데이터 복구 불가, 참조 무결성 문제

### 변경 이력
- 2024-12-28: Soft Delete 채택

---

## 9. 연관관계 매핑: LAZY 로딩

### 결정
모든 연관관계에 FetchType.LAZY 사용

### 이유
1. **성능**: 필요한 데이터만 조회
2. **N+1 방지**: EntityGraph나 QueryDSL로 명시적 조인
3. **OSIV 비활성화**: 트랜잭션 밖에서 lazy loading 방지

### 예시
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "kindergarten_id")
private Kindergarten kindergarten;

// 조회 시 필요한 데이터만 명시적 로드
@EntityGraph(attributePaths = {"kindergarten"})
List<Classroom> findAll();
```

### 대안 고려
- **EAGER**: 항상 함께 로딩
  - 단점: 불필요한 조인, 성능 저하

### 변경 이력
- 2024-12-28: LAZY 로딩 채택

---

## 10. 예외 처리: 커스텀 예외

### 결정
비즈니스 로직 오류 시 BusinessException 던지기

### 예시
```java
// 유치원이 없을 때
throw new BusinessException(ErrorCode.KINDERGARTEN_NOT_FOUND);

// 이미 담임 교사가 있을 때
throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_HAS_TEACHER);
```

### 이유
1. **일관된 에러**: GlobalExceptionHandler에서 ApiResponse로 변환
2. **명확한 코드**: ErrorCode enum으로 에러 분류
3. **디버깅**: 로그에 명확한 에러 원인 남김

### 대안 고려
- **IllegalArgumentException**: 세부 에러 구분 어려움
- **HTTP STATUS 직접 반환**: 코드 중복

### 변경 이력
- 2024-12-28: BusinessException 채택

---

## 면접 예상 질문 답변

### Q: 왜 Kindergarten를 별도 엔티티로 만드셨나요?
> A: 다중 유치원 관리를 고려했습니다. 하나의 시스템으로 여러 유치원을 관리할 수 있게 구현했습니다. 또한 확장성도 고려했습니다. 유치원별로 설정이나 정보가 다를 수 있는데, 별도 엔티티로 분리하면 확장이 용이합니다. 만약 Member에 통합했다면 한 회원이 여러 유치원에 소속하거나, 유치원 정보를 수정할 때 불편했을 것입니다.

### Q: Soft Delete를 사용한 이유는요?
> A: 데이터 보존과 참조 무결성 때문입니다. 실제로 삭제해버리면 다른 테이블에서 이 데이터를 참조할 때 에러가 발생할 수 있습니다. 그리고 삭제된 데이터라도 통계나 감사에 필요할 수 있어서 복구 가능하게 Soft Delete를 구현했습니다. deletedAt 필드로 삭제 시점을 기록하고, 조회 시에는 이 값이 null인 것만 조회합니다.

### Q: 왜 LAZY 로딩을 사용했나요?
> A: 성능 최적화를 위해서입니다. EAGER 로딩을 사용하면 항상 연관된 엔티티도 함께 조회하는데, 필요하지 않을 때도 조회하게 돼서 성능 저하가 발생합니다. LAZY 로딩으로 필요한 시점에만 데이터를 가져오고, EntityGraph나 QueryDSL으로 명시적으로 조인해서 N+1 문제도 방지합니다.

### Q: 교사와 반의 관계를 1:1로 한 이유는요?
> A: 실무를 고려했습니다. 대부분의 유치원에서 한 교사가 한 반을 담당합니다. N:M으로 구현하면 여러 교사가 여러 반을 담당할 수 있지만, 그만큼 복잡도가 증가합니다. MVP로는 단순하게 1:1로 구현하고, 추후 부교사나 보조 교사 기능이 필요하면 다대다 테이블로 확장할 계획입니다.

### Q: 권한 검증은 어디서 하시나요?
> A: Service 계층에서 @PreAuthorize 어노테이션으로 검증합니다. 메서드 실행 전에 Spring Security가 권한을 체크하고, 권한이 없으면 403 에러를 반환합니다. Controller나 Service 메서드 안에서 if문으로 체크하지 않아도 돼서 코드가 간결해지고, 보안 로직이 한 곳에 집중됩니다.

---

## 다음 단계
Phase 3: 원생 관리 구현

---

## 구현 현황 (Phase 2 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/kindergarten/entity/Kindergarten.java` | 유치원 엔티티 |
| `domain/classroom/entity/Classroom.java` | 반 엔티티 (Soft Delete) |

#### 2. 리포지토리
| 파일 | 설명 | 주요 메서드 |
|------|------|----------|
| `domain/kindergarten/repository/KindergartenRepository.java` | 유치원 리포지토리 | findByName, findAllByOrderByNameAsc, existsByName |
| `domain/classroom/repository/ClassroomRepository.java` | 반 리포지토리 | findByKindergartenIdAndDeletedAtIsNull, findByIdAndDeletedAtIsNull, countByKindergartenIdAndDeletedAtIsNull |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/kindergarten/dto/request/KindergartenRequest.java` | 유치원 등록/수정 요청 (시간 파싱) |
| `domain/kindergarten/dto/response/KindergartenResponse.java` | 유치원 정보 응답 (record) |
| `domain/classroom/dto/request/ClassroomRequest.java` | 반 등록/수정 요청 |
| `domain/classroom/dto/response/ClassroomResponse.java` | 반 정보 응답 (record) |

#### 4. 서비스
| 파일 | 설명 | 주요 메서드 |
|------|------|----------|
| `domain/kindergarten/service/KindergartenService.java` | 유치원 서비스 | register, getKindergarten, getAllKindergartens, updateKindergarten, deleteKindergarten |
| `domain/classroom/service/ClassroomService.java` | 반 서비스 | createClassroom, getClassroom, getClassroomsByKindergarten, updateClassroom, deleteClassroom, assignTeacher, removeTeacher |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/kindergarten/controller/KindergartenController.java` | 유치원 API | POST /api/v1/kindergartens, GET /api/v1/kindergartens/{id}, PUT /api/v1/kindergartens/{id}, DELETE /api/v1/kindergartens/{id} |
| `domain/classroom/controller/ClassroomController.java` | 반 API | POST /api/v1/classrooms, GET /api/v1/classrooms/{id}, PUT /api/v1/classrooms/{id}, DELETE /api/v1/classrooms/{id}, PUT /api/v1/classrooms/{id}/teacher |

---

### 구현된 기능 상세

#### 백엔드

**1. Kindergarten 엔티티**
```java
// 필드: name, address, phone, openTime, closeTime
// 정적 팩토리: create(name, address, phone, openTime, closeTime)
// 비즈니스 메서드: update(), setPrincipal()
```
- BaseEntity 상속 (createdAt, updatedAt)
- Member와 N:1 연관관계 (한 유치원에 여러 회원 소속)

**2. Classroom 엔티티**
```java
// 필드: kindergarten, name, ageGroup, teacher, deletedAt
// 연관관계: Kindergarten(N:1), Member(교사, 1:1)
// Soft Delete: deletedAt 필드
```
- 정적 팩토리: `create(kindergarten, name, ageGroup)`
- 비즈니스 메서드: `update()`, `assignTeacher()`, `removeTeacher()`, `softDelete()`, `restore()`
- 검증 메서드: `canAssignTeacher()`, `canDelete(kidsCount)`

**3. Kindergarten 서비스**
- `register()`: 유치원명 중복 확인, 생성
- `getKindergarten()`: ID로 조회 (없으면 404)
- `getAllKindergartens()`: 이름순 정렬 조회
- `updateKindergarten()`: 정보 수정
- `deleteKindergarten()`: Hard Delete (실제 삭제)

**4. Classroom 서비스**
- `createClassroom()`: 반 생성
- `getClassroom()`: ID로 조회 (Soft Delete 반영)
- `getClassroomsByKindergarten()`: 유치원별 반 목록 (삭제 제외)
- `updateClassroom()`: 반 이름, 연령대 수정
- `deleteClassroom()`: Soft Delete (원생 있으면 에러)
- `assignTeacher()`: 담임 교사 배정 (역할 확인, 이미 담당 있으면 에러)
- `removeTeacher()`: 담임 교사 해제

**5. 권한 제어 (@PreAuthorize)**
```java
// 유치원 등록/수정/삭제: 원장만 가능
@PreAuthorize("hasRole('PRINCIPAL')")

// 반 생성/수정/삭제: 원장, 교사 가능
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")

// 반 조회: 모두 가능 (인증만 필요)
```

**6. Soft Delete 구현**
```java
// 삭제 시 deletedAt 설정
public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}

// 조회 시 deletedAtIsNull()로 필터링
@Query("SELECT c FROM Classroom c WHERE c.deletedAt IS NULL")
```

---

### 확인 가능한 상태

#### 실행 방법
```bash
# 1. Docker 컨테이너 실행
cd docker && docker-compose up -d && ..

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. 접속
http://localhost:8080
```

#### API 테스트

**1. 유치원 등록 (원장 권한 필요)**
```bash
curl -X POST http://localhost:8080/api/v1/kindergartens \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "해바라기유치원",
    "address": "서울시 강남구",
    "phone": "0212345678",
    "openTime": "09:00",
    "closeTime": "18:00"
  }'
```

**2. 유치원 목록 조회**
```bash
curl -X GET http://localhost:8080/api/v1/kindergartens \
  -b cookies.txt
```

**3. 반 생성 (교사 권한 필요)**
```bash
curl -X POST http://localhost:8080/api/v1/classrooms \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "kindergartenId": 1,
    "name": "해바라기반",
    "ageGroup": "5세반"
  }'
```

**4. 반 목록 조회**
```bash
# 전체 조회
curl -X GET http://localhost:8080/api/v1/classrooms \
  -b cookies.txt

# 유치원별 조회
curl -X GET "http://localhost:8080/api/v1/classrooms?kindergartenId=1" \
  -b cookies.txt
```

**5. 담임 교사 배정**
```bash
curl -X PUT http://localhost:8080/api/v1/classrooms/1/teacher?teacherId=2 \
  -b cookies.txt
```

**6. 반 삭제**
```bash
curl -X DELETE http://localhost:8080/api/v1/classrooms/1 \
  -b cookies.txt
```

---

### Phase 2 완료 체크리스트
- [x] Kindergarten 엔티티 (BaseEntity 상속)
- [x] Classroom 엔티티 (Soft Delete)
- [x] Kindergarten N:1 Member 연관관계
- [x] Classroom N:1 Kindergarten 연관관계
- [x] Classroom 1:1 Teacher(Member) 연관관계
- [x] KindergartenRepository (findByName, existsByName)
- [x] ClassroomRepository (Soft Delete 쿼리)
- [x] KindergartenService (등록, 조회, 수정, 삭제)
- [x] ClassroomService (생성, 조회, 수정, Soft Delete, 교사 배정)
- [x] KindergartenController (@PreAuthorize 권한 제어)
- [x] ClassroomController (CRUD + 교사 배정)

---

**Phase 2 완료일: 2024-12-28**
