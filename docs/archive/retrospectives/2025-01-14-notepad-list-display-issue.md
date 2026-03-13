# 알림장 목록 표시 문제 해결 시도 기록

> **TL;DR**: 알림장이 DB에는 정상 생성되지만 화면에 표시되지 않음. DB 데이터는 정상. **가장 유력한 원인은 Hibernate의 Page + FETCH JOIN 충돌** 또는 프론트엔드 JavaScript 에러.

## 핵심 의심 사항
1. **Hibernate Page + FETCH JOIN 충돌** (가장 유력)
   - `HHH000104` warning 발생 가능성
   - 페이징이 메모리에서 처리되거나 실패
2. **프론트엔드 JavaScript 에러**
   - `loadNotepads()` 함수 실행 안됨
   - 브라우저 콘솔 에러 확인 필요
3. **WHERE 조건 문제**
   - `c.kindergarten.id` 조인 조건이 NULL을 배제
   - classroom이 NULL인 전체 알림장 미조회

## 문제 상황
- **증상**: 알림장 작성 시 "알림장이 작성되었습니다" 메시지는 표시되지만:
  1. 모달이 자동으로 닫히지 않음
  2. 작성된 알림장이 목록에 표시되지 않음
  3. 페이지 새로고침 후에도 목록이 비어있음
- **확인 사항**: DB에는 알림장이 정상적으로 생성되고 있음 (16개 확인됨)

## 환경 정보
- Java 17 + Spring Boot 3.5.9
- MySQL 8.0 (Docker)
- Thymeleaf + HTMX + Alpine.js + Tailwind CSS (CDN)
- JPA + QueryDSL 5.0.0
- OSIV 비활성화 (`spring.jpa.open-in-view=false`)

## DB 상태 확인
```sql
SELECT id, title, classroom_id, kid_id, writer_id, created_at 
FROM notepad 
ORDER BY created_at DESC 
LIMIT 5;

-- 결과: 16개의 notepad 레코드 확인됨
-- id: 16, 15, 14, 13, 12 (classroom_id=1, writer_id=1)
```

## 시도한 해결 방법

### 1. NotepadRequest DTO에 @Setter 추가 ✅
**문제**: 폼 데이터가 DTO에 바인딩되지 않음
**해결**: `@Setter` 어노테이션 추가
```java
@Getter
@Setter // 추가
@NoArgsConstructor
public class NotepadRequest { ... }
```

### 2. 동적 Classroom/Kid 데이터 로딩 구현 ✅
**문제**: 하드코딩된 classroom_id=1로 인한 "반을 찾을 수 없습니다" 에러
**해결**: API로 동적 데이터 로딩
```javascript
// /api/v1/classrooms?kindergartenId={id} 호출
// 반 목록과 원생 목록 동적 로딩
```

### 3. NotepadService 로깅 추가 ✅
**목적**: 디버깅을 위한 로그 출력
```java
@Slf4j
public class NotepadService {
    public NotepadResponse createNotepad(...) {
        log.debug("Creating notepad - classroomId: {}, kidId: {}, title: {}", ...);
        // ...
    }
}
```

### 4. LazyInitializationException 해결 (findById) ✅
**문제**: `NotepadResponse.from()`에서 `classroom.getName()` 호출 시 LazyInitializationException
**해결**: JOIN FETCH 추가
```java
@Query("SELECT n FROM Notepad n " +
       "LEFT JOIN FETCH n.classroom " +
       "LEFT JOIN FETCH n.kid " +
       "LEFT JOIN FETCH n.writer " +
       "WHERE n.id = :id")
Optional<Notepad> findById(@Param("id") Long id);
```

### 5. GET /api/v1/notepads 엔드포인트 구현 ✅
**문제**: 알림장 목록 조회 API 없음
**해결**: NotepadController에 GET 메서드 추가
```java
@GetMapping
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
public ResponseEntity<ApiResponse<Page<NotepadResponse>>> getNotepads(
        @RequestParam(required = false) Long kindergartenId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    // ...
    responses = notepadService.getNotepadsByKindergarten(kindergartenId, pageable);
    return ResponseEntity.ok(ApiResponse.success(responses));
}
```

### 6. NotepadService.getNotepadsByKindergarten() 구현 ✅
**구현 내용**:
```java
public Page<NotepadResponse> getNotepadsByKindergarten(Long kindergartenId, Pageable pageable) {
    kindergartenService.getKindergarten(kindergartenId);
    return notepadRepository.findByKindergartenId(kindergartenId, pageable)
            .map(notepad -> NotepadResponse.from(notepad, 0));
}
```

### 7. NotepadRepository.findByKindergartenId() 구현 ✅
**문제**: Kindergarten ID로 알림장 조회 쿼리 없음
**해결**: JOIN FETCH를 포함한 쿼리 추가
```java
@Query("SELECT n FROM Notepad n " +
       "LEFT JOIN FETCH n.classroom c " +
       "LEFT JOIN FETCH n.kid k " +
       "LEFT JOIN FETCH n.writer w " +
       "WHERE c.kindergarten.id = :kindergartenId " +
       "OR (n.classroom IS NULL AND n.kid IS NULL AND w.kindergarten.id = :kindergartenId) " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

### 8. 기존 Repository 쿼리에 JOIN FETCH 추가 ✅
**목적**: 모든 조회 쿼리에서 LazyInitializationException 방지
- `findClassroomNotepads`: classroom, writer JOIN FETCH
- `findKidNotepads`: kid, classroom, writer JOIN FETCH
- `findNotepadsForParent`: classroom, kid, writer JOIN FETCH

### 9. notepad.html - 목록 로딩 JavaScript 구현 ✅
**구현 내용**:
```javascript
// loadNotepads() 함수
async function loadNotepads() {
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
    }
}

// displayNotepads() 함수 - HTML 렌더링
```

### 10. notepad.html - 폼 제출 후 목록 새로고침 구현 ✅
**구현 내용**:
```javascript
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
        await loadNotepads(); // 목록 새로고침
    }
});
```

## 현재 상태
- ✅ DB에 알림장 생성 성공
- ✅ POST /api/v1/notepads 정상 작동
- ❌ 알림장 목록이 화면에 표시되지 않음
- ❌ 모달이 자동으로 닫히지 않음
- ❌ 목록 새로고침이 작동하지 않음

## 데이터베이스 상태 확인 완료

```sql
-- Classroom 확인
SELECT c.id, c.name, c.kindergarten_id FROM classroom c WHERE c.id = 1;
-- 결과: id=1, name=해바라기반, kindergarten_id=1 ✅

-- Member (Writer) 확인
SELECT m.id, m.name, m.kindergarten_id FROM member m WHERE m.id = 1;
-- 결과: id=1, name=김원장, kindergarten_id=1 ✅

-- Notepad 확인
SELECT id, title, classroom_id, kid_id, writer_id, created_at 
FROM notepad 
ORDER BY created_at DESC LIMIT 5;
-- 결과: 16개의 notepad (모두 classroom_id=1, writer_id=1) ✅
```

**결론**: DB 데이터는 정상이며, 연관 관계도 올바름. **백엔드 쿼리 또는 프론트엔드 렌더링 이슈**로 추정.

## 추가 확인 필요 사항

### 1. 브라우저 콘솔 에러 확인
- JavaScript 에러 발생 여부
- Network 탭에서 `/api/v1/notepads` GET 요청 응답 확인
- Console 탭의 디버깅 로그 확인:
  ```
  Loading notepads from: /api/v1/notepads
  Notepads loaded: {...}
  ```

### 2. 서버 로그 확인
- `/api/v1/notepads` GET 요청 처리 로그
- LazyInitializationException 또는 다른 예외 발생 여부
- Hibernate 쿼리 실행 로그

### 3. **페이징 쿼리 + JOIN FETCH 이슈 (HIGH PRIORITY)**
**의심 사항**: Hibernate에서 **Page<T> + JOIN FETCH 조합** 시 문제 발생 가능:
1. **MultipleBagFetchException**: 여러 컬렉션을 FETCH JOIN 할 때 발생
2. **HHH000104 Warning**: "firstResult/maxResults specified with collection fetch; applying in memory!"
   - Hibernate가 모든 데이터를 메모리에 로드한 후 페이징 적용 (성능 문제)
   - **페이징이 작동하지 않을 수 있음**
3. **중복 결과**: FETCH JOIN으로 인한 카르테시안 곱

**현재 쿼리**:
```java
@Query("SELECT n FROM Notepad n " +
       "LEFT JOIN FETCH n.classroom c " +
       "LEFT JOIN FETCH n.kid k " +
       "LEFT JOIN FETCH n.writer w " +
       "WHERE c.kindergarten.id = :kindergartenId " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

**예상 문제점**:
- 3개의 FETCH JOIN (classroom, kid, writer)과 Page 반환 타입 충돌
- Hibernate가 warning 로그를 출력하거나 쿼리 실패 가능
- 빈 결과 또는 예외 발생 가능

**해결 방안**:
1. **@EntityGraph 사용** (권장):
   ```java
   @EntityGraph(attributePaths = {"classroom", "kid", "writer"})
   @Query("SELECT n FROM Notepad n WHERE n.classroom.kindergarten.id = :kindergartenId ORDER BY n.createdAt DESC")
   Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
   ```

2. **DTO Projection 사용**:
   ```java
   @Query("SELECT new com.erp.domain.notepad.dto.NotepadListDto(n.id, n.title, c.name, k.name, w.name, n.createdAt) " +
          "FROM Notepad n " +
          "LEFT JOIN n.classroom c " +
          "LEFT JOIN n.kid k " +
          "LEFT JOIN n.writer w " +
          "WHERE c.kindergarten.id = :kindergartenId")
   Page<NotepadListDto> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
   ```

3. **2-Step Query** (가장 안전):
   ```java
   // Step 1: ID만 조회 (페이징)
   @Query("SELECT n.id FROM Notepad n WHERE n.classroom.kindergarten.id = :kindergartenId ORDER BY n.createdAt DESC")
   Page<Long> findIdsByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
   
   // Step 2: ID로 FETCH JOIN 조회
   @Query("SELECT n FROM Notepad n " +
          "LEFT JOIN FETCH n.classroom " +
          "LEFT JOIN FETCH n.kid " +
          "LEFT JOIN FETCH n.writer " +
          "WHERE n.id IN :ids")
   List<Notepad> findByIdWithFetchJoin(@Param("ids") List<Long> ids);
   ```

4. **DISTINCT + Set 사용**:
   ```java
   @Query("SELECT DISTINCT n FROM Notepad n " +
          "LEFT JOIN FETCH n.classroom c " +
          "LEFT JOIN FETCH n.kid k " +
          "LEFT JOIN FETCH n.writer w " +
          "WHERE c.kindergarten.id = :kindergartenId")
   List<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId);
   // Service에서 수동 페이징 처리
   ```

### 4. OSIV 비활성화 영향
**설정**: `spring.jpa.open-in-view=false`
**영향**: Controller에서 응답 변환 시 Lazy Loading 불가
**확인**: `NotepadResponse.from()` 호출 시점에 모든 연관 엔티티가 로드되었는지

### 5. notepad.html 페이지 로드 시 초기화
**확인 필요**:
- `kindergartenId` 변수가 올바르게 설정되었는지
- `loadNotepads()` 함수가 페이지 로드 시 실행되는지
- `hx-trigger="load"` 또는 `DOMContentLoaded` 이벤트 리스너

## 관련 파일 목록
```
src/main/java/com/erp/domain/notepad/
├── controller/
│   └── NotepadController.java (GET /api/v1/notepads 추가)
├── service/
│   └── NotepadService.java (getNotepadsByKindergarten 추가)
├── repository/
│   └── NotepadRepository.java (findByKindergartenId + JOIN FETCH 추가)
├── dto/
│   ├── request/
│   │   └── NotepadRequest.java (@Setter 추가)
│   └── response/
│       └── NotepadResponse.java (from 메서드 확인)
└── entity/
    └── Notepad.java

src/main/resources/templates/notepad/
└── notepad.html (loadNotepads, displayNotepads 구현)
```

## 즉시 확인해야 할 사항 (우선순위)

### Priority 1: Hibernate Warning 확인
서버 로그에서 다음 warning 검색:
```
HHH000104: firstResult/maxResults specified with collection fetch
```
이 경고가 있다면 **페이징 + FETCH JOIN 충돌**이 확실함.

### Priority 2: 실제 API 응답 확인
브라우저에서 직접 확인:
1. Network 탭 → `/api/v1/notepads` GET 요청
2. Response 본문 확인:
   ```json
   {
     "success": true,
     "data": {
       "content": [...],  // 빈 배열인지 확인
       "totalElements": 16,  // 실제 총 개수
       "totalPages": 2,
       "size": 10
     }
   }
   ```
3. `content` 배열이 비어있다면 → **백엔드 쿼리 문제**
4. `content` 배열에 데이터가 있다면 → **프론트엔드 렌더링 문제**

### Priority 3: Classroom 조인 조건 검증
현재 쿼리의 WHERE 조건:
```sql
WHERE c.kindergarten.id = :kindergartenId
```
이 조건은 **classroom이 NULL인 notepad를 제외**합니다.
DB에 `classroom_id IS NULL`인 notepad가 있는지 확인:
```sql
SELECT COUNT(*) FROM notepad WHERE classroom_id IS NULL;
```

만약 NULL이 있다면 쿼리를 수정해야 함:
```java
WHERE (c.kindergarten.id = :kindergartenId OR (n.classroom IS NULL AND n.writer.kindergarten.id = :kindergartenId))
```

## 다음 디버깅 단계 제안

1. **서버 로그 실시간 모니터링**:
   ```bash
   tail -f logs/erp.log
   ```

2. **브라우저 Network 탭에서 API 응답 확인**:
   - GET `/api/v1/notepads` 응답 구조
   - HTTP 상태 코드
   - 응답 본문 (JSON)

3. **브라우저 Console 탭에서 JavaScript 에러 확인**:
   - TypeError, ReferenceError 등
   - 디버깅 로그 출력 여부

4. **수동 API 테스트**:
   ```bash
   # cURL로 직접 API 호출
   curl -X GET "http://localhost:8080/api/v1/notepads" \
     -H "Cookie: ACCESS_TOKEN=..." \
     -H "Accept: application/json"
   ```

5. **Hibernate 쿼리 로그 확인**:
   - `application.yml`에서 `logging.level.org.hibernate.SQL=DEBUG` 설정 확인
   - JOIN FETCH가 실제 SQL에 반영되는지 확인

## 참고: 유사한 작동하는 코드 (공지사항)
알림장과 동일한 구조인 공지사항 기능은 정상 작동 중:
- `AnnouncementController.java`: GET `/api/v1/announcements`
- `announcement/announcements.html`: 목록 표시 및 필터링 작동

비교 분석을 통해 차이점 확인 권장.

## CLI Agent용 빠른 테스트 명령어

### 1. DB 데이터 확인
```bash
docker exec erp-mysql mysql -uroot -proot1234 --default-character-set=utf8mb4 -e "
USE erp_db;
SELECT n.id, n.title, n.classroom_id, c.kindergarten_id, n.writer_id, w.kindergarten_id as writer_kg_id
FROM notepad n
LEFT JOIN classroom c ON n.classroom_id = c.id
LEFT JOIN member w ON n.writer_id = w.id
ORDER BY n.created_at DESC LIMIT 5;
"
```

### 2. API 직접 호출 (인증 필요)
```bash
# 먼저 로그인하여 쿠키 저장
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"password"}' \
  -c cookies.txt

# 알림장 목록 조회
curl -X GET "http://localhost:8080/api/v1/notepads" \
  -b cookies.txt \
  -H "Accept: application/json" \
  -v
```

### 3. Hibernate 로그 레벨 임시 변경
`application.yml` 또는 `application-local.yml`에 추가:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
    org.hibernate.query: DEBUG
    org.springframework.data.jpa: DEBUG
```

### 4. 빌드 및 재시작
```bash
cd /Users/alex/project/kindergarten_ERP/erp
./gradlew compileJava
# 기존 프로세스 종료 후 재시작
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 추가 정보
- 작성일: 2025-01-14
- Spring Boot 버전: 3.5.9
- JPA: Hibernate 6.x
- 데이터베이스: MySQL 8.0 (Docker)
