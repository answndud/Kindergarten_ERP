# Phase 4: 출석부 기술 선택 결정

## 개요
- 단계: 출석부 관리 구현
- 목표: 원생의 일일 출석 관리, 등하원 시간 기록, 출석 통계
- 작업: Attendance 도메인

---

## 1. Attendance 엔티티 설계

### 결정
원생별 일일 출석 기록을 별도 엔티티로 관리

### 이유
1. **시계열 데이터**: 매일 출석 데이터가 누적되므로 별도 테이블 필요
2. **유연한 상태 관리**: PRESENT, ABSENT, LATE 등 상태 변경 용이
3. **통계 조회**: 월별 출석률, 패턴 분석 등에 활용
4. **감사 추적**: 언제 누가 출석을 수정했는지 기록

### 관계 설계
```
Kid (1:N) Attendance
  └─ 한 원생은 여러 날의 출석 기록
```

### 상태 값
- `PRESENT`: 출석
- `ABSENT`: 결석
- `LATE`: 지각
- `EARLY_LEAVE`: 조퇴
- `SICK_LEAVE`: 병결

### 변경 이력
- 2024-12-28: Attendance 엔티티 분리 결정

---

## 2. 등하원 시간 기록

### 결정
dropOffTime, pickUpTime 필드로 등하원 시간 기록

### 이유
1. **안전 관리**: 원생의 등하원 시간 추적
2. **통계 활용**: 평균 등하원 시간 분석
3. **연장 보호**: 늦게 하원하는 원생 관리

### 구현 방식
```java
// 등원 시간 기록
attendance.recordDropOff(LocalTime.of(9, 30));
attendance.recordPickUp(LocalTime.of(16, 45));
```

### 대안 고려
- **시간 기록 안 함**: 안전 관리가 어려움

### 변경 이력
- 2024-12-28: 등하원 시간 기록 채택

---

## 3. 출석 상태 수정

### 결정
updateAttendance() 메서드로 상태와 사유 수정

### 이유
1. **사유 관리**: 결석 사유 등을 기록
2. **날짜별 변경**: 당일 상태 수정 가능

### 구현 방식
```java
attendance.updateAttendance(AttendanceStatus.SICK_LEAVE, "감기로 결석");
```

### 대안 고려
- **불변 객체**: 새로운 Attendance 생성 (불필요한 객체 증가)

### 변경 이력
- 2024-12-28: 가변 상태 수정 채택

---

## 4. 고유 제약조건 (kid_id + date)

### 결정
한 원생은 하루에 출석 기록 하나

### 이유
1. **데이터 무결성**: 중복 출석 기록 방지
2. **비즈니스 로직**: 하루에 두 번 출석 불가능

### 구현 방식
```java
@UniqueConstraint(columnNames = {"kid_id", "date"})
```

### 변경 이력
- 2024-12-28: 유니크 제약조건 채택

---

## 5. 월별 통계 조회

### 결정
QueryDSL 또는 JPQL로 월별 통계 집계

### 이유
1. **성능**: N+1 쿼리 방지
2. **유연성**: 기간별 통계 확장 용이

### 구현 방식
```java
// 월별 출석 통계
@Query("SELECT new com.erp.domain.attendance.dto.response.MonthlyStatisticsResponse(" +
       "COUNT(a), " +
       "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), " +
       "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END)" +
       ") FROM Attendance a " +
       "WHERE a.kid.id = :kidId " +
       "AND a.date BETWEEN :startDate AND :endDate")
MonthlyStatisticsResponse getMonthlyStatistics(@Param("kidId") Long kidId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
```

### 대안 고려
- **애플리케이션에서 집계**: 메모리 사용량 증가

### 변경 이력
- 2024-12-28: DB 집계 채택

---

## 6. 출석부 API 설계

### 결정
RESTful API 설계

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | /api/v1/attendances/drop-off | 등원 기록 |
| POST | /api/v1/attendances/pick-up | 하원 기록 |
| GET | /api/v1/attendances/daily | 특정 날짜 출석 조회 |
| GET | /api/v1/attendances/monthly | 월별 통계 조회 |
| PUT | /api/v1/attendances/{id} | 출석 수정 |

### 이유
1. **표준 준수**: RESTful API 원칙
2. **명확한 의미**: URL로 기능 파악 가능

### 변경 이력
- 2024-12-28: RESTful API 채택

---

## 7. 권한 제어

### 결정
@PreAuthorize로 출석부 접근 제어

### 권한 규칙
- **생성/수정**: 원장, 교사
- **조회**: 원장, 교사, 학부모 (자녀만)

### 구현 방식
```java
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public void recordDropOff(Long kidId, LocalTime dropOffTime) { }

@PreAuthorize("hasRole('PARENT')")
public List<AttendanceResponse> getMyKidsAttendance(LocalDate date) { }
```

### 변경 이력
- 2024-12-28: 역할별 접근 제어 채택

---

## 8. 출석부 화면 UI

### 결정
HTMX + Alpine.js로 동적 출석부 화면

### 주요 기능
1. **일별 출석표**: 날짜별 출석 현황 표시
2. **등하원 버튼**: 버튼 클릭으로 시간 기록
3. **상태 변경**: 셀렉트박스로 상태 변경

### 이유
1. **실시간 업데이트**: HTMX로 부분 렌더링
2. **간단한 조작**: 클릭만으로 등하원 기록

### 변경 이력
- 2024-12-28: HTMX 기반 UI 채택

---

## 면접 예상 질문 답변

### Q: 왜 Attendance를 별도 엔티티로 만드셨나요?
> A: 원생별로 매일 출석 데이터가 누적되는 시계열 데이터입니다. Kid 엔티티에 포함하면 한 엔티티에 너무 많은 데이터가 들어가게 되고, 조회 성능도 저하됩니다. 별도 테이블로 분리하면 일별 조회, 월별 통계 등의 쿼리가 훨씬 간단해지고 인덱스 활용도 가능해집니다.

### Q: 등하원 시간을 기록하는 이유는요?
> A: 안전 관리와 통계 활용 때문입니다. 원생이 언제 등원했고 언제 하원했는지 기록하면 연장 보호 대상을 관리하기 좋고, 평균 등하원 시간을 분석해서 유치원 운영에도 활용할 수 있습니다.

### Q: 월별 통계는 어떻게 조회하나요?
> A: JPQL이나 QueryDSL로 집계 쿼리를 사용합니다. 애플리케이션에서 데이터를 가져와서 계산하면 메모리 사용량이 많아지고, 데이터가 늘어날수록 성능이 저하됩니다. DB에서 GROUP BY나 CASE WHEN으로 집계하면 인덱스를 활용해서 훨씬 효율적으로 조회할 수 있습니다.

### Q: 출석 상태는 어떻게 관리하나요?
> A: enum 타입으로 관리합니다. PRESENT, ABSENT, LATE, EARLY_LEAVE, SICK_LEAVE 등으로 명확하게 구분하고, updateAttendance() 메서드로 상태와 사유를 함께 수정할 수 있습니다. 이렇게 하면 결석 사유를 기록해서 나중에 참고할 수 있습니다.

---

## 다음 단계
Phase 5: 알림장 관리 구현

---

## 구현 현황 (Phase 4 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/attendance/entity/Attendance.java` | 출석 엔티티 (Kid, date, status, dropOffTime, pickUpTime) |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/attendance/repository/AttendanceRepository.java` | 출석 리포지토리 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/attendance/dto/request/DropOffRequest.java` | 등원 요청 |
| `domain/attendance/dto/request/PickUpRequest.java` | 하원 요청 |
| `domain/attendance/dto/response/AttendanceResponse.java` | 출석 정보 응답 |
| `domain/attendance/dto/response/DailyAttendanceResponse.java` | 일별 출석 응답 |
| `domain/attendance/dto/response/MonthlyStatisticsResponse.java` | 월별 통계 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/attendance/service/AttendanceService.java` | 출석 서비스 |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/attendance/controller/AttendanceController.java` | 출석 API | POST /api/v1/attendances/drop-off, /pick-up, GET /api/v1/attendances/daily, /monthly |

---

### 구현된 기능 상세

#### 백엔드

**1. Attendance 엔티티**
```java
// 필드: kid, date, status, dropOffTime, pickUpTime, note
// 연관관계: Kid (Many-to-One)
// 정적 팩토리: create(), createDropOff()
// 비즈니스 메서드: updateAttendance(), recordDropOff(), recordPickUp()
```

**2. Attendance 서비스**
- `recordDropOff()`: 등원 시간 기록
- `recordPickUp()`: 하원 시간 기록
- `getDailyAttendance()`: 특정 날짜 출석 조회
- `getMonthlyStatistics()`: 월별 통계 조회
- `updateAttendance()`: 출석 상태 수정

---

**Phase 4 완료일: 2025-01-14**
