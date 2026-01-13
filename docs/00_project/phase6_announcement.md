# Phase 6: 공지사항 기술 선택 결정

## 개요
- 단계: 공지사항 관리 구현
- 목표: 원장이 전체 공지사항 작성, 중요 공지 관리
- 작업: Announcement 도메인

---

## 1. Announcement 엔티티 설계

### 결정
유치원별 공지사항을 별도 엔티티로 관리

### 이유
1. **다중 유치원**: 하나의 시스템으로 여러 유치원 관리
2. **권한 분리**: 각 유치원별 공지사항 격리
3. **통계 활용**: 조회수 추적

### 관계 설계
```
Kindergarten (1:N) Announcement
Member (1:N) Announcement (작성자: 원장)
```

### 주요 필드
- `isImportant`: 중요 공지 여부
- `viewCount`: 조회수

### 변경 이력
- 2024-12-28: Announcement 엔티티 분리 결정

---

## 2. 중요 공지 표시

### 결정
isImportant 불리언 필드로 관리

### 이유
1. **단순함**: 별도 카테고리 없이 바로 표시
2. **UI 구현**: 상단 고정, 배경색 등으로 시각적 구분

### 구현 방식
```java
@Column(name = "is_important")
private Boolean isImportant = false;

public void markAsImportant() {
    this.isImportant = true;
}
```

### 대안 고려
- **우선순위 필드**: 1~5 숫자 (복잡도 증가)

### 변경 이력
- 2024-12-28: 불리언 isImportant 채택

---

## 3. 조회수 기능

### 결정
viewCount 필드로 자동 증가

### 이유
1. **통계 활용**: 인기 있는 공지사항 파악
2. **단순함**: 조회 시마다 +1

### 구현 방식
```java
public void incrementViewCount() {
    this.viewCount++;
}

// 서비스에서
notepad.incrementViewCount();
```

### 대안 고려
- **별도 조회 로그**: 누가 읽었는지까지 추적 (과잣설)

### 변경 이력
- 2024-12-28: viewCount 필드 채택

---

## 4. Soft Delete

### 결정
deletedAt 필드로 Soft Delete

### 이유
1. **데이터 보존**: 삭제된 공지도 기록 유지
2. **감사 추적**: 언제 삭제되었는지 확인
3. **복구 가능**: 실수로 삭제 시 복구

### 구현 방식
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}
```

### 변경 이력
- 2024-12-28: Soft Delete 채택

---

## 5. 공지사항 API 설계

### 결정
RESTful API 설계

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | /api/v1/announcements | 공지사항 작성 |
| GET | /api/v1/announcements/{id} | 공지사항 조회 |
| GET | /api/v1/announcements | 공지사항 목록 (페이지네이션) |
| GET | /api/v1/announcements/important | 중요 공지사항 |
| PUT | /api/v1/announcements/{id} | 공지사항 수정 |
| DELETE | /api/v1/announcements/{id} | 공지사항 삭제 |

### 이유
1. **표준 준수**: RESTful API 원칙
2. **명확한 자원**: announcements 하위 리소스

### 변경 이력
- 2024-12-28: RESTful API 채택

---

## 6. 권한 제어

### 결정
원장만 공지사항 작성/수정/삭제

### 권한 규칙
- **작성/수정/삭제**: 원장 (해당 유치원)
- **조회**: 모든 역할 (해당 유치원 소속)

### 구현 방식
```java
@PreAuthorize("hasRole('PRINCIPAL')")
public Long createAnnouncement(AnnouncementRequest request) {
    // 원장이 자신의 유치원에만 공지 작성 가능
}
```

### 변경 이력
- 2024-12-28: 원장 전용 쓰기 채택

---

## 7. 페이지네이션

### 결정
Spring Data의 Pageable 활용

### 구현 방식
```java
@GetMapping
public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAnnouncements(
    @PageableDefault(size = 20) Pageable pageable
) {
    return announcementRepository.findAll(pageable);
}
```

### 이유
1. **표준 방식**: Spring Data의 표준 기능
2. **정렬 지원**: 최신순 등 정렬 용이

### 변경 이력
- 2024-12-28: Pageable 채택

---

## 8. 공지사항 UI

### 결정
HTMX로 무한 스크롤 + 중요 공지 상단 고정

### 주요 기능
1. **중요 공지**: 상단 배너로 표시
2. **무한 스크롤**: 스크롤 시 추가 로딩
3. **조회수 표시**: 아이콘과 숫자로 표시

### 이유
1. **사용자 경험**: 중요 공지를 먼저 확인
2. **성능**: 필요한 만큼만 로딩

### 변경 이력
- 2024-12-28: HTMX 무한 스크롤 채택

---

## 면접 예상 질문 답변

### Q: 왜 공지사항을 유치원별로 관리하나요?
> A: 이 시스템은 여러 유치원이 함께 사용하는 멀티테넌트 구조입니다. 공지사항이 섞이면 혼란이 발생할 수 있어서, 각 유치원별로 공지사항을 격리했습니다. Announcement 엔티티가 Kindergarten과 다대일 관계를 맺고 있어서, 원장이 자신의 유치원에만 공지를 작성할 수 있고, 학부모나 교사는 자신이 소속된 유치원의 공지만 볼 수 있습니다.

### Q: 중요 공지는 어떻게 표현하나요?
> A: isImportant라는 불리언 필드로 관리합니다. 단순한 불리언 값이라서 구현하기 쉽고, UI에서 배경색을 주거나 상단에 고정해서 표시하기 좋습니다. 우선순위 숫자를 사용할 수도 있지만, 그러면 UI 구현이 복잡해지고 사용자에게도 혼란을 줄 수 있어서 단순한 불리언으로 구현했습니다.

### Q: 조회수는 어떻게 관리하나요?
> A: viewCount 필드를 두고, 공지사항을 조회할 때마다 incrementViewCount() 메서드로 +1씩 증가시킵니다. 이렇게 하면 어떤 공지사항이 가장 많이 조회되었는지 파악할 수 있고, 인기 있는 공지사항을 분석하는 데 활용할 수 있습니다.

---

## 다음 단계
Phase 7: 지원/승인 워크플로우 구현

---

## 구현 현황 (Phase 6 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/announcement/entity/Announcement.java` | 공지사항 엔티티 |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/announcement/repository/AnnouncementRepository.java` | 공지사항 리포지토리 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/announcement/dto/request/AnnouncementRequest.java` | 공지사항 작성 요청 |
| `domain/announcement/dto/response/AnnouncementResponse.java` | 공지사항 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/announcement/service/AnnouncementService.java` | 공지사항 서비스 |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/announcement/controller/AnnouncementController.java` | 공지사항 API | POST /api/v1/announcements, GET /api/v1/announcements |

---

### 구현된 기능 상세

#### 백엔드

**1. Announcement 엔티티**
```java
// 필드: kindergarten, writer, title, content, isImportant, viewCount
// 연관관계: Kindergarten (N:1), Member (작성자, N:1)
// 정적 팩토리: create(), createImportant()
// 비즈니스 메서드: markAsImportant(), incrementViewCount(), softDelete()
```

**2. Announcement 서비스**
- `createAnnouncement()`: 공지사항 작성
- `getAnnouncement()`: 공지사항 상세 조회 (조회수 증가)
- `getAnnouncements()`: 공지사항 목록 (페이지네이션)
- `getImportantAnnouncements()`: 중요 공지사항 목록

---

**Phase 6 완료일: 2025-01-14**
