# 알림장 관련 코드 스냅샷 (2025-01-14)

> 디버깅을 위한 현재 코드 상태 기록

## 1. NotepadController.java - GET /api/v1/notepads

```java
@RestController
@RequestMapping("/api/v1/notepads")
@RequiredArgsConstructor
public class NotepadController {

    private final NotepadService notepadService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotepadResponse>>> getNotepads(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long kidId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotepadResponse> responses;

        if (kidId != null) {
            // 원생별 조회
            responses = notepadService.getKidNotepads(kidId, pageable);
        } else if (classroomId != null) {
            // 반별 조회
            responses = notepadService.getClassroomNotepads(classroomId, pageable);
        } else {
            // 유치원 전체 조회
            Long kindergartenId = userDetails.getMember().getKindergarten().getId();
            responses = notepadService.getNotepadsByKindergarten(kindergartenId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
```

## 2. NotepadService.java - getNotepadsByKindergarten

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotepadService {

    private final NotepadRepository notepadRepository;

    /**
     * 유치원별 알림장 목록 조회 (페이지)
     */
    public Page<NotepadResponse> getNotepadsByKindergarten(Long kindergartenId, Pageable pageable) {
        return notepadRepository.findByKindergartenId(kindergartenId, pageable)
                .map(notepad -> NotepadResponse.from(notepad, 0));
    }
}
```

## 3. NotepadRepository.java - findByKindergartenId

```java
@Repository
public interface NotepadRepository extends JpaRepository<Notepad, Long> {

    /**
     * 유치원별 알림장 목록 조회 (최신순)
     * 
     * ⚠️ 문제 의심 지점: Page + 3개의 FETCH JOIN
     */
    @Query("SELECT n FROM Notepad n " +
           "LEFT JOIN FETCH n.classroom c " +
           "LEFT JOIN FETCH n.kid k " +
           "LEFT JOIN FETCH n.writer w " +
           "WHERE c.kindergarten.id = :kindergartenId " +
           "ORDER BY n.createdAt DESC")
    Page<Notepad> findByKindergartenId(
        @Param("kindergartenId") Long kindergartenId, 
        Pageable pageable
    );
}
```

### 문제점 분석
1. **3개의 FETCH JOIN + Page 반환**: Hibernate warning 발생 가능
2. **WHERE c.kindergarten.id**: classroom이 NULL인 notepad 제외됨
3. **OSIV 비활성화**: 이후 Lazy Loading 불가

## 4. NotepadResponse.java - from 메서드

```java
public record NotepadResponse(
        Long id,
        Long classroomId,
        String classroomName,  // classroom.getName() - LazyInit 위험
        Long kidId,
        String kidName,         // kid.getName() - LazyInit 위험
        Long writerId,
        String writerName,      // writer.getName() - LazyInit 위험
        String title,
        String content,
        String photoUrl,
        List<String> photoUrls,
        int readCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotepadResponse from(Notepad notepad, int readCount) {
        // classroom 접근
        Long classroomId = null;
        String classroomName = null;
        if (notepad.getClassroom() != null) {
            classroomId = notepad.getClassroom().getId();
            classroomName = notepad.getClassroom().getName(); // ⚠️ Lazy 접근
        }

        // kid 접근
        Long kidId = null;
        String kidName = null;
        if (notepad.getKid() != null) {
            kidId = notepad.getKid().getId();
            kidName = notepad.getKid().getName(); // ⚠️ Lazy 접근
        }

        // writer 접근 (필수)
        return new NotepadResponse(
                notepad.getId(),
                classroomId,
                classroomName,
                kidId,
                kidName,
                notepad.getWriter().getId(),
                notepad.getWriter().getName(), // ⚠️ Lazy 접근
                notepad.getTitle(),
                notepad.getContent(),
                notepad.getPhotoUrl(),
                photoUrls,
                readCount,
                notepad.getCreatedAt(),
                notepad.getUpdatedAt()
        );
    }
}
```

## 5. notepad.html - JavaScript 코드

```javascript
// 페이지 로드 시 실행 (추정)
document.addEventListener('DOMContentLoaded', function() {
    // ...
});

// 알림장 목록 로드
async function loadNotepads() {
    const filterClassroom = document.getElementById('filterClassroom').value;
    const filterDate = document.getElementById('filterDate').value;
    
    try {
        let url = '/api/v1/notepads';
        const params = new URLSearchParams();
        
        if (filterClassroom) {
            params.append('classroomId', filterClassroom);
        }
        
        if (params.toString()) {
            url += '?' + params.toString();
        }
        
        console.log('Loading notepads from:', url);
        const response = await fetch(url);
        
        if (response.ok) {
            const result = await response.json();
            console.log('Notepads loaded:', result);
            const notepads = result.data?.content || [];
            displayNotepads(notepads);
        } else {
            console.error('Failed to load notepads:', response.status);
        }
    } catch (error) {
        console.error('알림장 목록 로드 실패:', error);
    }
}

// 알림장 표시
function displayNotepads(notepads) {
    const container = document.getElementById('notepad-list');
    
    if (notepads.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <h3>알림장이 없습니다</h3>
            </div>
        `;
        return;
    }
    
    container.innerHTML = notepads.map(notepad => `
        <div class="bg-white rounded-2xl border border-gray-200 p-6">
            <h3 class="text-lg font-semibold">${notepad.title}</h3>
            <p class="text-gray-600">${notepad.content}</p>
            <div class="text-sm text-gray-500">
                ${notepad.classroomName || '전체'} | ${notepad.writerName}
            </div>
        </div>
    `).join('');
}

// 폼 제출 후 목록 새로고침
form.addEventListener('submit', async function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    const data = Object.fromEntries(formData.entries());
    
    const response = await fetch('/api/v1/notepads', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    
    if (response.ok) {
        alert('알림장이 작성되었습니다');
        modal.classList.add('hidden');
        form.reset();
        await loadNotepads(); // ⚠️ 목록 새로고침
    }
});
```

## 6. application.yml 설정

```yaml
spring:
  jpa:
    open-in-view: false  # ⚠️ OSIV 비활성화
    properties:
      hibernate:
        default_batch_fetch_size: 100
        
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
```

## 문제 발생 플로우

```
1. 사용자가 알림장 작성 완료
   ↓
2. POST /api/v1/notepads (성공) → DB INSERT ✅
   ↓
3. "알림장이 작성되었습니다" alert 표시 ✅
   ↓
4. loadNotepads() 호출
   ↓
5. GET /api/v1/notepads 요청
   ↓
6. NotepadController.getNotepads()
   - kindergartenId = 1
   ↓
7. NotepadService.getNotepadsByKindergarten(1, pageable)
   ↓
8. NotepadRepository.findByKindergartenId(1, pageable)
   - ⚠️ Hibernate 쿼리 실행
   - ⚠️ Page + FETCH JOIN 충돌?
   - ⚠️ WHERE c.kindergarten.id = 1 조건 평가
   ↓
9. Page<Notepad> → map(NotepadResponse.from)
   - ⚠️ classroom.getName() 접근
   - ⚠️ writer.getName() 접근
   - ⚠️ LazyInitializationException?
   ↓
10. JSON 응답 반환
    ↓
11. 프론트엔드 displayNotepads(notepads)
    - ⚠️ notepads 배열이 비어있음?
    - ⚠️ JavaScript 에러?
    ↓
12. ❌ 목록이 화면에 표시되지 않음
```

## 비교: 정상 작동하는 공지사항 코드

### AnnouncementController.java
```java
@GetMapping
public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAnnouncements(
        @RequestParam Long kindergartenId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<AnnouncementResponse> responses = announcementService.getAnnouncements(kindergartenId, pageable);

    return ResponseEntity.ok(ApiResponse.success(responses));
}
```

### AnnouncementRepository.java
```java
// 공지사항은 FETCH JOIN 없이 조회 (차이점!)
@Query("SELECT a FROM Announcement a WHERE a.kindergarten.id = :kindergartenId ORDER BY a.createdAt DESC")
Page<Announcement> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

### 차이점 분석
| 항목 | 알림장 (Notepad) | 공지사항 (Announcement) |
|------|------------------|------------------------|
| FETCH JOIN | 3개 (classroom, kid, writer) | 없음 |
| WHERE 조건 | `c.kindergarten.id` (조인 필요) | `a.kindergarten.id` (직접 FK) |
| LazyInit 위험 | 높음 (3개 연관) | 낮음 (1개 연관: writer) |

**결론**: 공지사항은 FETCH JOIN 없이 정상 작동. **알림장도 FETCH JOIN을 제거하거나 다른 방식으로 변경 필요**.

## 권장 해결 방법 (우선순위)

### 방법 1: @EntityGraph 사용 (가장 간단)
```java
@EntityGraph(attributePaths = {"classroom", "kid", "writer"})
@Query("SELECT n FROM Notepad n " +
       "WHERE n.classroom.kindergarten.id = :kindergartenId " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

### 방법 2: FETCH JOIN 제거 + Lazy Loading 활용
```java
@Query("SELECT n FROM Notepad n " +
       "WHERE n.classroom.kindergarten.id = :kindergartenId " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```
단, `NotepadResponse.from()`에서 LazyInitializationException 발생 가능.
해결: Service에서 `@Transactional(readOnly = true)` 내에서 DTO 변환 수행.

### 방법 3: DTO Projection (가장 안전)
```java
@Query("SELECT new com.erp.domain.notepad.dto.response.NotepadResponse(" +
       "n.id, c.id, c.name, k.id, k.name, w.id, w.name, n.title, n.content, " +
       "n.photoUrl, 0, n.createdAt, n.updatedAt) " +
       "FROM Notepad n " +
       "LEFT JOIN n.classroom c " +
       "LEFT JOIN n.kid k " +
       "LEFT JOIN n.writer w " +
       "WHERE c.kindergarten.id = :kindergartenId " +
       "ORDER BY n.createdAt DESC")
Page<NotepadResponse> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

### 방법 4: 2-Step Query (성능 최적)
```java
// Step 1: ID만 조회
@Query("SELECT n.id FROM Notepad n WHERE n.classroom.kindergarten.id = :kindergartenId ORDER BY n.createdAt DESC")
Page<Long> findIdsByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);

// Step 2: FETCH JOIN으로 조회
@Query("SELECT n FROM Notepad n " +
       "LEFT JOIN FETCH n.classroom " +
       "LEFT JOIN FETCH n.kid " +
       "LEFT JOIN FETCH n.writer " +
       "WHERE n.id IN :ids " +
       "ORDER BY n.createdAt DESC")
List<Notepad> findByIdsWithFetchJoin(@Param("ids") List<Long> ids);

// Service
public Page<NotepadResponse> getNotepadsByKindergarten(Long kindergartenId, Pageable pageable) {
    Page<Long> idPage = notepadRepository.findIdsByKindergartenId(kindergartenId, pageable);
    List<Notepad> notepads = notepadRepository.findByIdsWithFetchJoin(idPage.getContent());
    return new PageImpl<>(
        notepads.stream().map(n -> NotepadResponse.from(n, 0)).toList(),
        pageable,
        idPage.getTotalElements()
    );
}
```

---

**작성일**: 2025-01-14  
**목적**: CLI AI Agent가 문제를 빠르게 파악하고 해결할 수 있도록 현재 코드 상태 기록
