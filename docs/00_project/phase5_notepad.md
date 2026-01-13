# Phase 5: 알림장 기술 선택 결정

## 개요
- 단계: 알림장 관리 구현
- 목표: 교사가 학부모에게 자녀의 하루 활동을 공유
- 작업: Notepad 도메인

---

## 1. Notepad 엔티티 설계

### 결정
반별/원생별 알림장을 별도 엔티티로 관리

### 이유
1. **유연성**: 반 전체 또는 특정 원생 대상 알림장
2. **시계열 데이터**: 매일 작성되는 알림장 기록
3. **다중 읽음 확인**: 학부모별 읽음 상태 관리

### 관계 설계
```
Classroom (1:N) Notepad (반별 알림장)
Kid (1:N) Notepad (원생별 알림장)
Member (1:N) Notepad (작성자)
Notepad (1:N) NotepadReadConfirm (읽음 확인)
```

### 종류
1. **반별 알림장**: classroomId만 설정, kidId는 null
2. **원생별 알림장**: kidId 설정

### 변경 이력
- 2024-12-28: Notepad 엔티티 분리 결정

---

## 2. 읽음 확인 기능

### 결정
별도 엔티티(NotepadReadConfirm)로 읽음 확인 관리

### 이유
1. **다대다 관계**: 여러 학부모가 하나의 알림장을 읽음
2. **읽은 시간 기록**: 언제 읽었는지 추적
3. **통계 활용**: 읽지 않은 학부모 파악

### 구현 방식
```java
@Entity NotepadReadConfirm {
    - notepad: Notepad (ManyToOne)
    - reader: Member (학부모, ManyToOne)
    - readAt: LocalDateTime
    - UK(notepad, reader)
}
```

### 대안 고려
- **Notepad에 컬럼 추가**: 여러 학부모의 읽음 상태 관리 어려움

### 변경 이력
- 2024-12-28: 별도 읽음 확인 엔티티 채택

---

## 3. 알림장 종류

### 결정
정적 팩토리 메서드로 종류별 생성

### 구현 방식
```java
// 반별 알림장
Notepad.createClassroomNotepad(classroom, writer, title, content)

// 원생별 알림장
Notepad.createKidNotepad(kid, writer, title, content)

// 전체 알림장
Notepad.createGlobalNotepad(writer, title, content)
```

### 이유
1. **명확한 구분**: 메서드명으로 용도 파악
2. **타입 안전성**: 컴파일 타임에 검증

### 변경 이력
- 2024-12-28: 정적 팩토리 메서드 채택

---

## 4. 사진/파일 첨부

### 결정
photoUrl 필드로 콤마로 구분된 URL 저장

### 이유
1. **간단함**: 별도 파일 테이블 없이 문자열로 관리
2. **S3 활용**: AWS S3 등의 URL 저장

### 구현 방식
```java
@Column(name = "photo_url")
private String photoUrl;  // "https://s3.../photo1.jpg,https://s3.../photo2.jpg"
```

### 확장성 고려
- 추후 별도 Photo 엔티티로 분리 가능

### 변경 이력
- 2024-12-28: URL 문자열 저장 채택

---

## 5. 알림장 API 설계

### 결정
RESTful API 설계

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | /api/v1/notepads | 알림장 작성 |
| GET | /api/v1/notepads/{id} | 알림장 조회 |
| GET | /api/v1/notepads/classroom/{id} | 반별 알림장 목록 |
| GET | /api/v1/notepads/kid/{id} | 원생별 알림장 목록 |
| PUT | /api/v1/notepads/{id} | 알림장 수정 |
| DELETE | /api/v1/notepads/{id} | 알림장 삭제 |
| POST | /api/v1/notepads/{id}/read | 읽음 확인 |

### 이유
1. **표준 준수**: RESTful API 원칙
2. **명확한 자원 계층**: classroom/kid 하위 리소스

### 변경 이력
- 2024-12-28: RESTful API 채택

---

## 6. 권한 제어

### 결정
역할별 알림장 접근 제어

### 권한 규칙
- **작성**: 원장, 교사
- **조회**: 원장, 교사, 학부모 (자녀/반만)
- **읽음 확인**: 학부모만

### 구현 방식
```java
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public Long createNotepad(NotepadRequest request) { }

@PreAuthorize("hasRole('PARENT')")
public void markAsRead(Long notepadId) { }
```

### 변경 이력
- 2024-12-28: 역할별 접근 제어 채택

---

## 7. 알림 연동

### 결정
알림장 작성 시 알림 생성

### 이유
1. **실시성**: 새 알림장이 올라왔음을 즉시 알림
2. **편의성**: 알림을 통해 바로 접근

### 구현 방식
```java
// 알림장 작성 후
notificationService.notify(
    parent.getId(),
    NotificationType.NOTEPAD_CREATED,
    "새 알림장",
    title,
    "/notepad/" + notepadId
);
```

### 변경 이력
- 2024-12-28: 알림 연동 채택

---

## 8. 알림장 UI

### 결정
HTMX로 무한 스크롤 구현

### 주요 기능
1. **최신 순 목록**: 최근 알림장부터 표시
2. **사진 라이트박스**: 클릭하면 크게 보기
3. **읽음 표시**: 읽은 알림장은 스타일 변경

### 이유
1. **부분 업데이트**: 새 알림장만 가져오기
2. **간단한 구현**: 스크롤 이벤트로 추가 로딩

### 변경 이력
- 2024-12-28: HTMX 무한 스크롤 채택

---

## 면접 예상 질문 답변

### Q: 왜 읽음 확인을 별도 엔티티로 만드셨나요?
> A: 알림장은 여러 학부모가 읽을 수 있어서 다대다 관계가 됩니다. Notepad 엔티티에 읽음 상태를 넣으면 한 명만 읽었는지 여러 명이 읽었는지 파악하기 어렵습니다. 별도 NotepadReadConfirm 엔티티를 두면 (notepad, reader) 조합으로 유니크 제약을 걸어서, 각 학부모별 읽음 상태와 읽은 시간을 정확히 관리할 수 있습니다.

### Q: 사진은 어떻게 저장하나요?
> A: photoUrl 필드에 콤마로 구분된 S3 URL을 저장합니다. 예를 들어 "https://s3.../photo1.jpg,https://s3.../photo2.jpg" 같이 저장하고, 프론트엔드에서 콤마로 분리해서 보여줍니다. 이 방식은 구현이 간단하고 S3 같은 오브젝트 스토리지를 바로 활용할 수 있습니다. 추후 파일 관리가 복잡해지면 별도 Photo 엔티티로 분리할 수 있습니다.

### Q: 반별 알림장과 원생별 알림장은 어떻게 구분하나요?
> A: 정적 팩토리 메서드로 구분합니다. createClassroomNotepad()는 반만 설정하고 kid는 null로, createKidNotepad()는 kid를 설정해서 구분합니다. 조회 시에 kid가 null이면 반별 알림장, null이 아니면 원생별 알림장으로 판단할 수 있습니다.

---

## 다음 단계
Phase 6: 공지사항 관리 구현

---

## 구현 현황 (Phase 5 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/notepad/entity/Notepad.java` | 알림장 엔티티 |
| `domain/notepad/entity/NotepadReadConfirm.java` | 읽음 확인 엔티티 |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/notepad/repository/NotepadRepository.java` | 알림장 리포지토리 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/notepad/dto/request/NotepadRequest.java` | 알림장 작성 요청 |
| `domain/notepad/dto/response/NotepadResponse.java` | 알림장 응답 |
| `domain/notepad/dto/response/NotepadDetailResponse.java` | 알림장 상세 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/notepad/service/NotepadService.java` | 알림장 서비스 |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/notepad/controller/NotepadController.java` | 알림장 API | POST /api/v1/notepads, GET /api/v1/notepads/{id} |

---

### 구현된 기능 상세

#### 백엔드

**1. Notepad 엔티티**
```java
// 필드: classroom, kid, writer, title, content, photoUrl, isRead
// 연관관계: Classroom (N:1), Kid (N:1), Member (작성자, N:1)
// 정적 팩토리: createClassroomNotepad(), createKidNotepad(), createGlobalNotepad()
```

**2. Notepad 서비스**
- `createNotepad()`: 알림장 작성
- `getNotepad()`: 알림장 상세 조회
- `getClassroomNotepads()`: 반별 알림장 목록
- `getKidNotepads()`: 원생별 알림장 목록
- `markAsRead()`: 읽음 표시

---

**Phase 5 완료일: 2025-01-14**
