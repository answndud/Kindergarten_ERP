# 2025-01-14: 알림장 작성 "반을 찾을 수 없습니다" 에러 회고

## 문제 개요

**증상:** 알림장 작성 시 "작성 실패: 반을 찾을 수 없습니다" 에러 발생

**영향:** 교사/원장이 알림장을 작성할 수 없음

---

## 문제 원인

### 1. 직접적 원인
- 템플릿에서 하드코딩된 반 ID (1, 2, 3)를 사용
- 실제 DB에 해당 ID의 반이 없거나 삭제됨
- DTO에 `@Setter` 없어서 form 데이터 바인딩 실패 가능성

### 2. 근본적 원인

#### 2.1 하드코딩된 데이터
```html
<!-- notepad.html (수정 전) -->
<select id="classroomId" required>
    <option value="">반을 선택하세요</option>
    <option value="1">해바라기반</option>  <!-- 하드코딩 -->
    <option value="2">장미반</option>
    <option value="3">나무반</option>
</select>
```

#### 2.2 DTO Setter 부재
```java
// NotepadRequest.java (수정 전)
@Getter
@NoArgsConstructor
public class NotepadRequest {
    private Long classroomId;  // Setter 없음
    // ...
}
```

Spring MVC가 form 데이터를 바인딩할 때 Setter가 필요함

#### 2.3 DB 데이터 불일치
- DataLoader가 반을 생성하지만 ID가 보장되지 않음
- H2 in-memory DB는 재시작 시 데이터 초기화
- PostgreSQL 사용 시 이전 데이터와 충돌 가능

---

## 해결 과정

### 1. 문제 추적
1. 사용자: "알림장 작성 시 반을 찾을 수 없다는 에러"
2. 로그 확인: `ClassroomNotFoundException` - classroomId=1
3. 템플릿 확인: 하드코딩된 값 발견
4. DTO 확인: Setter 부재 발견

### 2. 해결 조치

| 파일 | 작업 |
|------|------|
| `NotepadRequest.java` | `@Setter` 추가 |
| `notepad.html` | API로 실제 반 목록 동적 로드 |
| `NotepadService.java` | 디버그 로그 추가 |

### 3. 구현 내용

**1. NotepadRequest.java**
```java
@Getter
@Setter  // 추가
@NoArgsConstructor
public class NotepadRequest {
    private Long classroomId;
    private Long kidId;
    private String title;
    private String content;
    private String photoUrl;
}
```

**2. notepad.html - 동적 반 목록 로드**
```javascript
// Get kindergartenId from current member
const kindergartenId = /*[[${currentMember?.kindergartenId}]]*/ null;

// Load classrooms via API
if (kindergartenId) {
    const response = await fetch(`/api/v1/classrooms?kindergartenId=${kindergartenId}`);
    const result = await response.json();
    const classrooms = result.data || [];
    
    // Populate classroom select
    const classroomSelect = document.getElementById('classroomId');
    classroomSelect.innerHTML = '<option value="">반을 선택하세요</option>';
    classrooms.forEach(classroom => {
        const option = document.createElement('option');
        option.value = classroom.id;
        option.textContent = classroom.name;
        classroomSelect.appendChild(option);
    });
}
```

**3. NotepadService.java - 디버그 로그**
```java
@Transactional
public Long createNotepad(NotepadRequest request, Long writerId) {
    log.debug("알림장 생성 요청 - classroomId: {}, kidId: {}, title: {}", 
              request.getClassroomId(), request.getKidId(), request.getTitle());
    
    // ... 기존 로직
    
    if (request.getClassroomId() != null) {
        log.debug("반별 알림장 생성 - classroomId: {}", request.getClassroomId());
        var classroom = classroomService.getClassroom(request.getClassroomId());
        // ...
    }
}
```

### 4. 검증 방법

**DB 데이터 확인**
```sql
-- H2 Console (http://localhost:8080/h2-console)
SELECT * FROM classroom WHERE deleted_at IS NULL;
SELECT * FROM kindergarten;
```

**API 테스트**
```bash
# 반 목록 조회
curl http://localhost:8080/api/v1/classrooms?kindergartenId=1

# 알림장 작성
curl -X POST http://localhost:8080/api/v1/notepads \
  -H "Content-Type: application/json" \
  -d '{
    "classroomId": 1,
    "title": "테스트",
    "content": "내용"
  }'
```

---

## 배운 점

### 1. 하드코딩은 위험하다
- 개발 시 임시로 하드코딩한 값은 반드시 제거
- 테스트 데이터와 실제 데이터가 불일치할 수 있음
- 드롭다운은 항상 API로 동적 로드

### 2. DTO는 Setter가 필요하다
- Spring MVC는 `@ModelAttribute`나 `@RequestBody`로 바인딩 시 Setter 사용
- Lombok `@Setter` 또는 `@Data` 사용
- Record는 불변이므로 form 바인딩에 부적합

### 3. 디버그 로그는 중요하다
- 요청 데이터 로깅으로 실제 전송 값 확인
- `log.debug`로 개발 시에만 출력
- Production에서는 `log.info` 이상만 출력

### 4. DB 데이터 초기화 전략이 필요하다
- H2 in-memory: 재시작 시 초기화
- PostgreSQL: 수동 초기화 필요
- DataLoader는 `count() > 0` 체크로 중복 방지

---

## 앞으로의 개선 방향

### 1. 템플릿 가이드라인

#### ✅ 하드코딩 금지
```html
<!-- BAD -->
<option value="1">해바라기반</option>

<!-- GOOD -->
<select id="classroomId"></select>
<script>
  // Load from API
  fetch('/api/v1/classrooms?kindergartenId=' + kindergartenId)
    .then(res => res.json())
    .then(data => populateSelect(data));
</script>
```

#### ✅ 초기값 제공
```html
<!-- 로딩 중 표시 -->
<select id="classroomId" disabled>
    <option>반 목록 로딩 중...</option>
</select>

<script>
  // 로딩 완료 후 활성화
  classroomSelect.disabled = false;
</script>
```

### 2. DTO 설계 원칙

#### ✅ Form 바인딩용 DTO
```java
// Form 데이터 수신
@Getter
@Setter  // 필수!
@NoArgsConstructor
public class NotepadRequest {
    private Long classroomId;
    // ...
}
```

#### ✅ API 응답용 DTO
```java
// JSON 응답 전송
public record NotepadResponse(
    Long id,
    String title,
    // ...
) {
    // Record는 불변 (Setter 없음)
}
```

### 3. API 엔드포인트 표준화

#### ✅ 관계 조회 패턴
```
GET /api/v1/kindergartens/{id}/classrooms
GET /api/v1/classrooms?kindergartenId={id}
GET /api/v1/classrooms/{id}/kids
```

#### ✅ 에러 응답 통일
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "CL001",
    "message": "반을 찾을 수 없습니다"
  }
}
```

### 4. DB 초기화 스크립트

#### ✅ 개발 환경 리셋
```bash
# H2 (자동)
./gradlew bootRun  # 재시작 시 자동 초기화

# PostgreSQL (수동)
psql -d erp_db -f scripts/reset-db.sql
./gradlew bootRun
```

#### ✅ DataLoader 조건
```java
@Override
@Transactional
public void run(String... args) {
    // 이미 데이터가 있으면 스킵
    if (memberRepository.count() > 0) {
        log.info("Dummy data already exists. Skipping.");
        return;
    }
    
    // 데이터 생성...
}
```

### 5. 프론트엔드 에러 처리

#### ✅ API 호출 에러 핸들링
```javascript
try {
    const response = await fetch('/api/v1/classrooms?kindergartenId=' + id);
    
    if (!response.ok) {
        const error = await response.json();
        alert('반 목록을 불러올 수 없습니다: ' + error.message);
        return;
    }
    
    const result = await response.json();
    populateClassrooms(result.data);
    
} catch (error) {
    console.error('API 호출 실패:', error);
    alert('서버와 통신할 수 없습니다');
}
```

#### ✅ 빈 데이터 처리
```javascript
if (classrooms.length === 0) {
    classroomSelect.innerHTML = '<option value="">등록된 반이 없습니다</option>';
    classroomSelect.disabled = true;
    alert('먼저 반을 생성해주세요');
}
```

---

## 결론

이번 문제는 **"하드코딩 + DTO Setter 부재 + DB 데이터 불일치"** 3가지가 복합적으로 발생했습니다.

앞으로는:
1. **하드코딩 금지** - 모든 선택 옵션은 API로 동적 로드
2. **DTO Setter 필수** - Form 바인딩용 DTO는 반드시 @Setter
3. **디버그 로그 활용** - 요청 데이터 로깅으로 문제 추적
4. **DB 초기화 전략** - DataLoader count 체크, 수동 리셋 스크립트

이 원칙들을 지키면 유사한 버그를 예방할 수 있습니다.

---

## 체크리스트 (앞으로 적용)

### 새 기능 개발 시
- [ ] 드롭다운/선택 옵션은 API로 동적 로드
- [ ] Form 바인딩 DTO에 @Setter 추가
- [ ] API 호출 에러 처리 (try-catch, 빈 데이터)
- [ ] 디버그 로그 추가 (요청 데이터, 중요 분기)
- [ ] DB 데이터 존재 여부 확인

### 디버깅 시
- [ ] 로그에서 실제 전송된 데이터 확인
- [ ] DB에서 해당 ID의 데이터 존재 확인
- [ ] API 엔드포인트 직접 테스트 (curl/Postman)
- [ ] 브라우저 Network 탭에서 요청/응답 확인
- [ ] DTO Setter 존재 여부 확인

이 체크리스트를 지키면 개발 속도와 품질이 모두 향상됩니다.
