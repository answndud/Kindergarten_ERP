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

### Q: 왜 교사가 원장이 작성한 공지를 수정/삭제할 수 있나요?
> A: 유치원 운영의 유연성을 위해서입니다. 원장이 부재중이거나 긴급한 상황에서 교사가 중요 공지를 수정해야 할 수 있습니다. 또한 작성자가 퇴사하더라도 공지사항을 관리할 수 있어야 합니다. 역할 기반 권한 제어(RBAC)를 사용해서 원장과 교사 역할을 가진 사용자는 모두 공지사항을 관리할 수 있도록 했습니다. 학부모는 조회만 가능합니다.

### Q: HTML form 데이터 바인딩 시 Setter가 왜 필요한가요?
> A: Spring MVC는 @ModelAttribute로 받는 DTO 객체에 form 데이터를 바인딩할 때 리플렉션을 사용합니다. 이때 기본 생성자로 객체를 생성한 후, Setter 메서드를 호출해서 각 필드 값을 설정합니다. Setter가 없으면 필드에 값을 설정할 수 없어서 모든 필드가 null이나 기본값으로 남게 됩니다. 저는 Lombok의 @Setter를 사용해서 간단하게 해결했습니다.

### Q: LazyInitializationException은 왜 발생했고 어떻게 해결했나요?
> A: JPA의 지연 로딩 때문입니다. Announcement 엔티티의 kindergarten과 writer 관계가 LAZY로 설정되어 있는데, 트랜잭션이 종료된 후에 이 필드들에 접근하면 세션이 이미 닫혀있어서 예외가 발생합니다. @ModelAttribute는 트랜잭션 외부에서 실행되기 때문에 문제가 됩니다. JPQL의 JOIN FETCH를 사용해서 조회 시점에 연관 엔티티를 함께 가져오도록 해서 해결했습니다.

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

## 9. LazyInitializationException 방지

### 문제
공지사항 뷰에서 `AnnouncementResponse.from()`을 호출할 때 `kindergarten.getName()`과 `writer.getName()` 접근 시 LazyInitializationException 발생

### 원인
- `@ModelAttribute` 메서드는 트랜잭션 외부에서 실행됨
- Kindergarten과 Writer 관계가 LAZY 로딩으로 설정됨
- 트랜잭션이 종료된 후 프록시 객체 접근 시 세션 이미 닫힘

### 해결 방법
JOIN FETCH 쿼리로 연관 엔티티를 함께 로드

```java
// AnnouncementRepository.java
@Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.kindergarten k LEFT JOIN FETCH a.writer w WHERE a.id = :id AND a.deletedAt IS NULL")
Optional<Announcement> findByIdWithRelations(@Param("id") Long id);

@Query("SELECT a FROM Announcement a LEFT JOIN FETCH a.kindergarten k LEFT JOIN FETCH a.writer w WHERE a.kindergarten.id = :kindergartenId AND a.deletedAt IS NULL ORDER BY a.isImportant DESC, a.createdAt DESC")
List<Announcement> findByKindergartenIdWithRelations(@Param("kindergartenId") Long kindergartenId);
```

### 변경 이력
- 2025-01-14: JOIN FETCH 쿼리 추가로 LazyInitializationException 해결

---

## 10. 뷰 템플릿 구현

### 결정
Thymeleaf + HTMX로 동적 뷰 구현

### 템플릿 구조
| 파일 | 설명 |
|------|------|
| `announcement/announcements.html` | 공지사항 목록 페이지 (HTMX로 목록 조각 로딩) |
| `announcement/detail.html` | 공지사항 상세 페이지 |
| `announcement/write.html` | 공지사항 작성 페이지 |
| `announcement/edit.html` | 공지사항 수정 페이지 |
| `announcement/fragments/list.html` | 공지사항 목록 조각 (HTMX용) |

### HTMX 활용
```html
<!-- 목록 조각 로딩 -->
<div id="announcement-list"
     hx-get="/announcements/list?importantOnly=false"
     hx-trigger="load"
     hx-swap="outerHTML">
</div>

<!-- 필터 버튼 -->
<button hx-get="/announcements/list?importantOnly=true"
        hx-target="#announcement-list"
        hx-swap="outerHTML">
    중요 공지
</button>
```

### 이유
1. **서버 사이드 렌더링**: 보안이 중요한 데이터는 서버에서 처리
2. **부분 업데이트**: HTMX로 목록만 교체하여 전체 페이지 리로드 방지
3. **단순함**: React 같은 클라이언트 사이드 프레임워크 불필요

### 변경 이력
- 2025-01-14: Thymeleaf + HTMX 기반 뷰 템플릿 구현

---

## 11. 권한 확장 및 최종 권한 정책

### 결정
원장과 교사는 누가 작성했든 모든 공지사항을 관리 가능

### 권한 규칙 (최종)
- **작성**: 원장, 교사 (자신이 소속된 유치원)
- **수정**: 원장, 교사 (작성자 무관, 누구나 수정 가능)
- **삭제**: 원장, 교사 (작성자 무관, 누구나 삭제 가능)
- **중요 공지 토글**: 원장, 교사 (작성자 무관)
- **조회**: 모든 역할 (자신이 소속된 유치원)

### 구현 방식
```java
// 작성 - 원장 또는 교사만
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public Long createAnnouncement(AnnouncementRequest request, Long writerId) {
    validateWriterRole(writer); // 원장 또는 교사 확인
}

// 수정 - 원장 또는 교사면 누구나 가능 (작성자 무관)
@Transactional
public void updateAnnouncement(Long id, AnnouncementRequest request, Long requesterId) {
    Member requester = memberService.getMemberById(requesterId);
    validateWriterRole(requester); // 역할만 확인, 작성자 확인 제거
    // ...
}

// 삭제 - 원장 또는 교사면 누구나 가능 (작성자 무관)
@Transactional
public void deleteAnnouncement(Long id, Long requesterId) {
    Member requester = memberService.getMemberById(requesterId);
    validateWriterRole(requester); // 역할만 확인, 작성자 확인 제거
    // ...
}
```

### 변경 이유
1. **협업 용이**: 원장이 부재 시 교사가 중요 공지 수정 가능
2. **운영 유연성**: 작성자가 퇴사해도 공지사항 관리 가능
3. **권한 단순화**: 역할 기반 권한만으로 충분

### 변경 이력
- 2025-01-14: 교사도 공지사항 작성 가능하도록 권한 확장
- 2025-01-14: 원장/교사 모두 작성자 무관하게 수정/삭제 가능하도록 변경

---

## 12. 뷰 컨트롤러 추가

### 결정
API 컨트롤러와 별도로 뷰 컨트롤러 분리

### 엔드포인트
| 엔드포인트 | 설명 | 권한 |
|-----------|------|------|
| GET /announcements | 공지사항 목록 페이지 | PRINCIPAL, TEACHER, PARENT |
| GET /announcements/list | 공지사항 목록 조각 (HTMX) | PRINCIPAL, TEACHER, PARENT |
| GET /announcement/write | 공지사항 작성 페이지 | PRINCIPAL, TEACHER |
| POST /announcement | 공지사항 작성 | PRINCIPAL, TEACHER |
| GET /announcement/{id} | 공지사항 상세 페이지 | PRINCIPAL, TEACHER, PARENT |
| GET /announcement/{id}/edit | 공지사항 수정 페이지 | PRINCIPAL, TEACHER |
| POST /announcement/{id} | 공지사항 수정 | PRINCIPAL, TEACHER (작성자 무관) |
| POST /announcement/{id}/delete | 공지사항 삭제 | PRINCIPAL, TEACHER (작성자 무관) |
| POST /announcement/{id}/toggle-important | 중요 공지 토글 | PRINCIPAL, TEACHER (작성자 무관) |

### 이유
1. **관심사 분리**: API는 JSON 응답, 뷰는 HTML 렌더링
2. **HTMX 통합**: 조각 HTML 반환을 위한 별도 엔드포인트 필요

### 변경 이력
- 2025-01-14: AnnouncementViewController 분리 및 구현

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
| `domain/announcement/repository/AnnouncementRepository.java` | 공지사항 리포지토리 (JOIN FETCH 쿼리 포함) |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/announcement/dto/request/AnnouncementRequest.java` | 공지사항 작성 요청 |
| `domain/announcement/dto/response/AnnouncementResponse.java` | 공지사항 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/announcement/service/AnnouncementService.java` | 공지사항 서비스 (뷰용 메서드 포함) |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/announcement/controller/AnnouncementController.java` | 공지사항 API | POST /api/v1/announcements, GET /api/v1/announcements |
| `domain/announcement/controller/AnnouncementViewController.java` | 공지사항 뷰 | GET /announcements, GET /announcement/write, etc. |

#### 6. 템플릿
| 파일 | 설명 |
|------|------|
| `templates/announcement/announcements.html` | 공지사항 목록 페이지 (HTMX) |
| `templates/announcement/detail.html` | 공지사항 상세 페이지 |
| `templates/announcement/write.html` | 공지사항 작성 페이지 |
| `templates/announcement/edit.html` | 공지사항 수정 페이지 |
| `templates/announcement/fragments/list.html` | 공지사항 목록 조각 |

---

### 구현된 기능 상세

#### 백엔드

**1. Announcement 엔티티**
```java
// 필드: kindergarten, writer, title, content, isImportant, viewCount
// 연관관계: Kindergarten (N:1), Member (작성자, N:1)
// 정적 팩토리: create(), createImportant()
// 비즈니스 메서드: update(), setImportant(), toggleImportant(), incrementViewCount(), softDelete()
```

**2. Announcement 서비스**
- `createAnnouncement()`: 공지사항 작성 (원장/교사만)
- `getAnnouncement()`: 공지사항 상세 조회 (조회수 증가)
- `getAnnouncementWithoutIncrement()`: 공지사항 조회 (조회수 증가 없음)
- `getAnnouncementsByKindergartenForView()`: 유치원별 공지사항 목록 (뷰용, 연관 엔티티 포함)
- `getAnnouncementsByKindergarten()`: 유치원별 공지사항 목록 (페이지네이션)
- `getImportantAnnouncements()`: 중요 공지사항 목록
- `searchByTitle()`: 제목으로 검색
- `getMostViewedAnnouncements()`: 인기 공지사항 (조회수 순)
- `updateAnnouncement()`: 공지사항 수정 (원장/교사, 작성자 무관)
- `deleteAnnouncement()`: 공지사항 삭제 (원장/교사, 작성자 무관, Soft Delete)
- `toggleImportant()`: 중요 공지 토글 (원장/교사, 작성자 무관)
- `validateWriterRole()`: 작성자 역할 검증 (원장 또는 교사)

**3. Announcement 리포지토리**
- `findByIdAndDeletedAtIsNull()`: ID로 조회
- `findByIdWithRelations()`: ID로 조회 (연관 엔티티 포함) - **LazyInitializationException 방지**
- `findByKindergartenIdAndDeletedAtIsNull()`: 유치원별 목록 (페이지네이션)
- `findByKindergartenIdWithRelations()`: 유치원별 목록 (연관 엔티티 포함) - **LazyInitializationException 방지**
- `findImportantByKindergartenId()`: 중요 공지사항 목록
- `findByKindergartenIdAndTitleContaining()`: 제목으로 검색
- `findMostViewedByKindergartenId()`: 조회수 상위 공지사항

**4. Announcement 뷰 컨트롤러**
- `announcementsPage()`: 공지사항 목록 페이지
- `announcementList()`: 공지사항 목록 조각 (HTMX)
- `writeForm()`: 공지사항 작성 페이지
- `detail()`: 공지사항 상세 페이지
- `editForm()`: 공지사항 수정 페이지
- `write()`: 공지사항 작성 처리
- `update()`: 공지사항 수정 처리
- `delete()`: 공지사항 삭제 처리
- `toggleImportant()`: 중요 공지 토글 처리

#### 프론트엔드

**1. 공지사항 목록 페이지**
- HTMX로 페이지 로드 시 목록 자동 로딩
- 전체/중요 공지 필터 버튼
- 원장/교사에게만 작성 버튼 표시

**2. 공지사항 상세 페이지**
- 제목, 작성자, 작성일, 조회수 표시
- 중요 공지 배지 표시
- 수정일 표시 (수정된 경우)
- 작성자 또는 원장에게 수정/삭제 버튼 표시

**3. 공지사항 작성 페이지**
- 중요 공지 체크박스
- 제목, 내용 입력
- 미리보기 기능

**4. 공지사항 수정 페이지**
- 기존 내용 표시
- 작성자, 작성일, 조회수 정보 표시 (읽기 전용)

---

---

## 13. 폼 데이터 바인딩 문제 해결

### 문제
공지사항 작성/수정 시 데이터가 전송되지 않음

### 원인
1. **AnnouncementRequest DTO에 Setter 부재**: Spring이 HTML form 데이터를 DTO 필드에 바인딩하지 못함
2. **체크박스 미체크 시 값 누락**: `isImportant` 체크박스가 체크되지 않으면 form data에 포함되지 않음
3. **kindergartenId null**: 템플릿에서 `currentMember.kindergartenId` 접근 시 값이 제대로 전달되지 않음

### 해결 방법

**1. DTO에 @Setter 추가**
```java
@Getter
@Setter  // 추가
@NoArgsConstructor
public class AnnouncementRequest {
    private Long kindergartenId;
    private String title;
    private String content;
    private Boolean isImportant;
}
```

**2. 체크박스 hidden input 추가**
```html
<!-- 체크하지 않아도 false 값 전송 -->
<input type="hidden" name="isImportant" value="false" />
<input type="checkbox" name="isImportant" id="isImportant" value="true" ... />
```

**3. kindergartenId 명시적 전달**
```java
// 컨트롤러
@GetMapping("/announcement/write")
public String writeForm(..., Model model) {
    Member member = memberService.getMemberByIdWithKindergarten(userDetails.getMemberId());
    model.addAttribute("kindergartenId", member.getKindergarten().getId());
    return "announcement/write";
}
```

### 변경 이력
- 2025-01-14: 폼 데이터 바인딩 문제 해결 (Setter 추가, 체크박스 처리 개선)

---

## 14. 로깅 추가

### 결정
디버깅을 위한 로그 추가

### 구현 방식
```java
@Slf4j
@Controller
public class AnnouncementViewController {
    
    @PostMapping("/announcement")
    public String write(...) {
        log.debug("공지사항 작성 요청 - kindergartenId: {}, title: {}", 
                  request.getKindergartenId(), request.getTitle());
        
        try {
            Long announcementId = announcementService.createAnnouncement(...);
            log.info("공지사항 작성 성공 - id: {}", announcementId);
            // ...
        } catch (Exception e) {
            log.error("공지사항 작성 중 예외 발생", e);
            // ...
        }
    }
}
```

### 이유
1. **문제 추적**: 어떤 단계에서 실패했는지 파악
2. **데이터 검증**: 전송된 파라미터 값 확인
3. **운영 모니터링**: 성공/실패 통계 수집

### 변경 이력
- 2025-01-14: 공지사항 작성/수정/삭제에 상세 로그 추가

---

## 기능 총정리

### 핵심 기능
1. ✅ **공지사항 작성**: 원장/교사가 유치원별 공지사항 작성
2. ✅ **중요 공지**: 중요 공지사항 표시 및 토글
3. ✅ **조회수 추적**: 공지사항별 조회수 자동 증가
4. ✅ **목록 조회**: 전체/중요 공지 필터링, 최신순 정렬
5. ✅ **제목 검색**: 제목으로 공지사항 검색
6. ✅ **인기 공지**: 조회수 상위 공지사항
7. ✅ **수정/삭제**: 원장/교사 누구나 수정/삭제 가능 (작성자 무관)
8. ✅ **Soft Delete**: 삭제된 공지사항도 데이터 보존

### 권한 정책 (최종)
| 역할 | 생성 | 조회 | 수정 | 삭제 | 중요 토글 |
|------|------|------|------|------|----------|
| 원장 (PRINCIPAL) | ✅ | ✅ | ✅ | ✅ | ✅ |
| 교사 (TEACHER) | ✅ | ✅ | ✅ | ✅ | ✅ |
| 학부모 (PARENT) | ❌ | ✅ | ❌ | ❌ | ❌ |

**특이사항**: 원장과 교사는 작성자가 누구든 상관없이 모든 공지사항을 수정/삭제 가능

### 기술 스택
- **백엔드**: Spring Boot 3.x, JPA/Hibernate, QueryDSL
- **프론트엔드**: Thymeleaf, HTMX, Tailwind CSS
- **보안**: Spring Security (역할 기반 접근 제어)
- **데이터베이스**: PostgreSQL (Soft Delete 지원)

### 주요 기술 결정 사항
1. **JOIN FETCH**: LazyInitializationException 방지
2. **Soft Delete**: 데이터 보존 및 감사 추적
3. **역할 기반 권한**: 작성자 확인 대신 역할만 확인
4. **HTMX**: 부분 업데이트로 UX 개선
5. **Lombok @Setter**: Form 데이터 바인딩

### 해결한 주요 문제
1. ❌ → ✅ **LazyInitializationException**: JOIN FETCH로 해결
2. ❌ → ✅ **Form 데이터 바인딩 실패**: @Setter 추가로 해결
3. ❌ → ✅ **체크박스 미체크 시 값 누락**: hidden input 추가로 해결
4. ❌ → ✅ **작성자만 수정 가능 문제**: 권한 정책 변경으로 해결

---

**Phase 6 완료일: 2025-01-14**
**최종 업데이트: 2025-01-14**

**주요 업데이트 내역**:
- 뷰 템플릿 구현 (Thymeleaf + HTMX)
- LazyInitializationException 해결 (JOIN FETCH)
- 권한 정책 변경 (원장/교사 모두 작성자 무관 수정/삭제 가능)
- 폼 데이터 바인딩 문제 해결 (@Setter, 체크박스 처리)
- 로깅 추가 (디버깅 및 모니터링)

**다음 단계**: Phase 7 - 지원/승인 워크플로우 구현
