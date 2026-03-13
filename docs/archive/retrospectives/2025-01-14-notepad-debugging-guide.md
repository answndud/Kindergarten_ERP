# 알림장 목록 표시 버그 디버깅 가이드

> **CLI AI Agent 시작점**: 이 문서를 먼저 읽고 아래 순서대로 진행하세요.

## 문제 요약
- **증상**: 알림장 작성 시 "알림장이 작성되었습니다" 메시지 표시 후:
  - ❌ 모달이 자동으로 닫히지 않음
  - ❌ 작성된 알림장이 목록에 표시되지 않음
  - ❌ 페이지 새로고침 후에도 목록이 비어있음
- **확인 사항**: ✅ DB에는 알림장이 정상적으로 생성됨 (16개 확인)

## 디버깅 순서

### 1단계: 문서 읽기 (5분)
1. **현재 문서** (2025-01-14-notepad-debugging-guide.md) - 전체 개요 파악
2. **상세 기록** (2025-01-14-notepad-list-display-issue.md) - 시도한 해결 방법 및 의심 사항
3. **코드 스냅샷** (2025-01-14-notepad-code-snapshot.md) - 현재 코드 상태 및 권장 해결 방법

### 2단계: 로그 확인 (3분)
```bash
# 서버 로그 확인
tail -100 /Users/alex/project/kindergarten_ERP/erp/logs/erp.log | grep -E "(HHH000104|notepad|LazyInitializationException)"
```

**확인 포인트**:
- [ ] `HHH000104: firstResult/maxResults specified with collection fetch` 경고
- [ ] `LazyInitializationException` 예외
- [ ] SQL 쿼리 실행 로그 (`SELECT n FROM Notepad n ...`)

### 3단계: API 응답 확인 (2분)
브라우저 DevTools 또는 cURL로 API 직접 호출:

```bash
# 1. 로그인 (쿠키 저장)
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"password"}' \
  -c /tmp/cookies.txt

# 2. 알림장 목록 조회
curl -X GET "http://localhost:8080/api/v1/notepads" \
  -b /tmp/cookies.txt \
  -H "Accept: application/json" \
  | jq '.'
```

**확인 포인트**:
- [ ] HTTP 상태 코드 (200 OK 여부)
- [ ] 응답 구조: `{"success": true, "data": {"content": [...], "totalElements": 16}}`
- [ ] `content` 배열이 비어있는지 확인
- [ ] 에러 메시지 확인

**판단 기준**:
- `content`가 **비어있음** → **백엔드 쿼리 문제** (4단계)
- `content`에 **데이터 있음** → **프론트엔드 문제** (5단계)
- **500 에러** → 서버 예외 발생, 로그 확인

### 4단계: 백엔드 문제 해결 (30분)

#### 4-1. Hibernate Warning 확인
로그에서 `HHH000104` 발견 시:
- **원인**: Page + FETCH JOIN 충돌
- **해결**: 아래 방법 중 하나 선택

#### 4-2. 해결 방법 선택

**방법 A: @EntityGraph 사용** (권장, 가장 간단)

`NotepadRepository.java` 수정:
```java
@EntityGraph(attributePaths = {"classroom", "kid", "writer"})
@Query("SELECT n FROM Notepad n " +
       "WHERE n.classroom.kindergarten.id = :kindergartenId " +
       "OR (n.classroom IS NULL AND n.writer.kindergarten.id = :kindergartenId) " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

**방법 B: FETCH JOIN 제거** (가장 빠른 테스트)

`NotepadRepository.java` 수정:
```java
@Query("SELECT n FROM Notepad n " +
       "WHERE n.classroom.kindergarten.id = :kindergartenId " +
       "OR (n.classroom IS NULL AND n.writer.kindergarten.id = :kindergartenId) " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

이 경우 `NotepadResponse.from()`에서 LazyInitializationException 발생 가능.
→ `NotepadService.getNotepadsByKindergarten()`가 이미 `@Transactional(readOnly = true)`이므로 작동할 수 있음.

**방법 C: 2-Step Query** (가장 안전)

`NotepadRepository.java`에 추가:
```java
// Step 1: ID만 조회 (페이징)
@Query("SELECT n.id FROM Notepad n " +
       "LEFT JOIN n.classroom c " +
       "LEFT JOIN n.writer w " +
       "WHERE c.kindergarten.id = :kindergartenId " +
       "OR (n.classroom IS NULL AND w.kindergarten.id = :kindergartenId) " +
       "ORDER BY n.createdAt DESC")
Page<Long> findIdsByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);

// Step 2: FETCH JOIN으로 조회
@Query("SELECT DISTINCT n FROM Notepad n " +
       "LEFT JOIN FETCH n.classroom " +
       "LEFT JOIN FETCH n.kid " +
       "LEFT JOIN FETCH n.writer " +
       "WHERE n.id IN :ids " +
       "ORDER BY n.createdAt DESC")
List<Notepad> findByIdsWithFetchJoin(@Param("ids") List<Long> ids);
```

`NotepadService.java` 수정:
```java
public Page<NotepadResponse> getNotepadsByKindergarten(Long kindergartenId, Pageable pageable) {
    Page<Long> idPage = notepadRepository.findIdsByKindergartenId(kindergartenId, pageable);
    
    if (idPage.isEmpty()) {
        return Page.empty(pageable);
    }
    
    List<Notepad> notepads = notepadRepository.findByIdsWithFetchJoin(idPage.getContent());
    List<NotepadResponse> responses = notepads.stream()
            .map(n -> NotepadResponse.from(n, 0))
            .toList();
    
    return new PageImpl<>(responses, pageable, idPage.getTotalElements());
}
```

#### 4-3. 빌드 및 테스트
```bash
cd /Users/alex/project/kindergarten_ERP/erp
./gradlew compileJava
# 애플리케이션 재시작 (기존 프로세스 종료 후)
./gradlew bootRun --args='--spring.profiles.active=local'
```

3단계(API 응답 확인) 재수행.

### 5단계: 프론트엔드 문제 해결 (20분)

API 응답에 데이터가 있지만 화면에 표시되지 않는 경우:

#### 5-1. 브라우저 콘솔 확인
Chrome DevTools → Console 탭:
- [ ] JavaScript 에러 (TypeError, ReferenceError 등)
- [ ] `Loading notepads from: /api/v1/notepads` 로그 출력 여부
- [ ] `Notepads loaded: {...}` 로그 출력 여부

#### 5-2. 코드 검증
`notepad.html` 확인:

**1. loadNotepads() 호출 확인**
```javascript
// 페이지 로드 시 실행되는지 확인
document.addEventListener('DOMContentLoaded', function() {
    loadNotepads(); // ← 이 줄이 있는지 확인
});
```

**2. displayNotepads() 로직 확인**
```javascript
function displayNotepads(notepads) {
    const container = document.getElementById('notepad-list');
    
    console.log('Displaying notepads:', notepads.length); // 디버깅 로그 추가
    
    if (notepads.length === 0) {
        container.innerHTML = `<div>알림장이 없습니다</div>`;
        return;
    }
    
    container.innerHTML = notepads.map(notepad => `
        <div class="bg-white p-6">
            <h3>${notepad.title}</h3>
            <p>${notepad.content}</p>
        </div>
    `).join('');
}
```

**3. 폼 제출 후 새로고침 확인**
```javascript
form.addEventListener('submit', async function(e) {
    e.preventDefault();
    // ... 제출 로직 ...
    
    if (response.ok) {
        alert('알림장이 작성되었습니다');
        modal.classList.add('hidden'); // 모달 닫기
        form.reset();
        await loadNotepads(); // 목록 새로고침
    } else {
        const error = await response.json();
        alert('작성 실패: ' + (error.message || '서버 오류'));
    }
});
```

#### 5-3. HTML 요소 확인
```html
<!-- notepad-list ID가 있는지 확인 -->
<div id="notepad-list" class="space-y-4">
    <!-- 목록이 여기에 렌더링됨 -->
</div>
```

### 6단계: 최종 테스트 (5분)

1. **애플리케이션 재시작**
2. **브라우저 하드 새로고침** (Cmd+Shift+R)
3. **알림장 작성**:
   - 반 선택
   - 제목/내용 입력
   - 제출
4. **확인**:
   - [ ] "알림장이 작성되었습니다" 메시지
   - [ ] 모달 자동 닫힘
   - [ ] 목록에 새 알림장 표시

## 빠른 임시 해결 (5분)

시간이 부족한 경우, 가장 빠른 임시 해결책:

### 임시 해결 1: FETCH JOIN 완전 제거

`NotepadRepository.java`:
```java
@Query("SELECT n FROM Notepad n " +
       "WHERE (n.classroom.kindergarten.id = :kindergartenId " +
       "OR (n.classroom IS NULL AND n.writer.kindergarten.id = :kindergartenId)) " +
       "ORDER BY n.createdAt DESC")
Page<Notepad> findByKindergartenId(@Param("kindergartenId") Long kindergartenId, Pageable pageable);
```

`NotepadService.java`:
```java
@Transactional(readOnly = true) // 이미 있음 확인
public Page<NotepadResponse> getNotepadsByKindergarten(Long kindergartenId, Pageable pageable) {
    return notepadRepository.findByKindergartenId(kindergartenId, pageable)
            .map(notepad -> {
                // Lazy Loading이 @Transactional 내에서 작동
                return NotepadResponse.from(notepad, 0);
            });
}
```

### 임시 해결 2: 프론트엔드 강제 새로고침

`notepad.html`:
```javascript
if (response.ok) {
    alert('알림장이 작성되었습니다');
    window.location.reload(); // 페이지 전체 새로고침
}
```

## 추가 디버깅 도구

### SQL 로그 활성화
`application-local.yml`:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
    com.erp.domain.notepad: DEBUG
```

### DB 직접 확인
```bash
docker exec erp-mysql mysql -uroot -proot1234 --default-character-set=utf8mb4 -e "
USE erp_db;
SELECT n.id, n.title, n.classroom_id, c.name AS classroom_name, c.kindergarten_id, 
       n.writer_id, w.name AS writer_name, w.kindergarten_id AS writer_kg_id
FROM notepad n
LEFT JOIN classroom c ON n.classroom_id = c.id
LEFT JOIN member w ON n.writer_id = w.id
ORDER BY n.created_at DESC
LIMIT 10;
"
```

## 예상 소요 시간
- **백엔드 문제**: 30-60분
- **프론트엔드 문제**: 20-30분
- **둘 다 문제**: 60-90분

## 성공 기준
- [ ] GET `/api/v1/notepads` API가 200 OK 응답
- [ ] 응답 JSON의 `content` 배열에 16개의 notepad 포함
- [ ] 브라우저에서 알림장 목록 표시됨
- [ ] 새 알림장 작성 후 자동으로 목록 업데이트
- [ ] 모달 자동 닫힘

## 참고 문서
- `2025-01-14-notepad-list-display-issue.md` - 상세 시도 기록
- `2025-01-14-notepad-code-snapshot.md` - 코드 스냅샷 및 비교 분석

---

**작성일**: 2025-01-14  
**대상**: CLI AI Agent  
**목표**: 1시간 내 문제 해결
