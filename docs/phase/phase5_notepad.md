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

---

# Phase 5 추가 기록: 알림장 목록 표시/상세/권한/UX 개선 (2026-01-14)

> 이 섹션은 Phase 5 구현 완료 이후, 실제 운영/테스트 중 발견된 문제를 해결하면서 확정된 구현 방식과 UX 결정을 기록합니다.

## 1. 알림장 목록 “DB엔 있는데 화면에 안 뜸” 이슈

### 증상
- 알림장 작성 성공(메시지 표시) 후에도 목록이 비어 보임
- 새로고침 후에도 목록이 비어 보임
- DB에는 알림장이 정상 생성됨(다수 확인)

### 원인
1) 프론트엔드 JS 런타임 에러
- `notepad/notepad.html`에서 존재하지 않는 DOM(`filterDate`)을 참조하여 `loadNotepads()` 실행 전에 에러로 중단됨
- 결과적으로 API 호출 자체가 발생하지 않아 목록이 비어 보였음

2) 백엔드 페이징 + 연관 로딩 전략 충돌 가능성
- Hibernate 경고(`HHH000104: firstResult/maxResults specified with collection fetch`)가 발생할 수 있는 구조
- Page + FETCH JOIN 조합은 페이지네이션/중복/카운트 쿼리 문제를 유발할 수 있음

### 해결
- 프론트엔드:
  - 필터 파라미터를 `classroomId`, `kidId`로 정리하고 DOM 참조를 실제 요소(`filterKid`)로 정정
  - 목록 카드 전체 클릭(링크)로 상세 이동하도록 UX 개선(아래 4번 참고)

- 백엔드:
  - `NotepadRepository.findByKindergartenId()` 조회를 `@EntityGraph` 기반으로 전환하여 페이징 안정성을 확보
  - 유치원 기준 목록에 “전체 알림장(classroom NULL)”도 포함되도록 조회 조건 보강

### 관련 문서
- 디버깅 가이드: `docs/retrospective/2025-01-14-notepad-debugging-guide.md`
- 상세 시도 기록: `docs/retrospective/2025-01-14-notepad-list-display-issue.md`

---

## 2. 알림장 상세 페이지 500(TemplateInputException)

### 증상
- 목록에서 상세 이동 시 `/notepad/{id}`가 500

### 원인
- `NotepadViewController`가 `notepad/detail` 템플릿을 반환하지만 실제 템플릿 파일이 없어서 Thymeleaf가 `TemplateInputException` 발생

### 해결
- `src/main/resources/templates/notepad/detail.html` 추가

---

## 3. 권한 규칙 확정: 수정/삭제는 원장/교사만

### 요구사항
- 원장과 교사는 알림장을 수정/삭제할 수 있어야 함
- 학부모는 읽기(조회/읽음 처리)만 가능

### 결정
- 공지사항과 동일한 UX/권한 체계를 채택한다.
  - **UI 노출**은 Thymeleaf Security(`sec:authorize`)로 처리하여 서버 상태/role enum 변화에 강하게 만든다.
  - **서버 권한**은 서비스 레벨에서 강제한다(뷰에서 버튼을 숨기는 것만으로는 불충분).

### 구현
- 뷰 권한:
  - 알림장 상세에서 `sec:authorize="hasAnyRole('PRINCIPAL','TEACHER')"`일 때만 수정/삭제 버튼 노출
- 서버 권한:
  - `NotepadService.updateNotepad()` / `deleteNotepad()`는 원장/교사만 허용
  - 학부모 읽음 처리는 `markAsRead()`에서 학부모 role만 허용

---

## 4. UX 개선: “자세히 보기” 대신 카드 전체 클릭

### 요구사항
- 공지사항처럼 카드 전체를 클릭하면 상세로 이동

### 결정
- 목록 렌더링 시 카드 래퍼를 `<a href="/notepad/{id}">`로 구성해 클릭 영역을 넓힌다.
- 기존 “자세히 보기 →” 텍스트는 중복이므로 제거한다.

---

## 5. 개발 환경 이슈: VSCode Run vs Gradle bootRun

### 문제
- IDE의 Run(Launch)이 `bin/main` 산출물을 참조하면서, 이전 컴파일 실패 클래스가 로딩되어 `Unresolved compilation problems`가 런타임에 발생할 수 있음

### 결정
- 로컬 실행은 Gradle 기반으로 표준화한다.
  - VSCode: `ERP: bootRun (local)` / `ERP: clean bootRun (local)` task로 실행

---

## 6. 알림장 목록/필터 HTMX 동적 로딩 정리

### 목표
- 템플릿 하드코딩을 제거하고(반/원생/목록), API 기반으로 동적 로딩한다.
- 목록 렌더링을 서버 템플릿 프래그먼트로 통일해 HTMX로 갱신한다.

### 변경 사항
- `/notepad` 목록 영역을 `/notepad/list`(HTMX) 조각 로딩으로 전환
  - `notepad/fragments/list :: notepadList`에서 카드 렌더링
  - 필터 변경 시 `filters-changed` 이벤트로 목록 영역만 갱신

- 반/원생 옵션 하드코딩 제거 → API로 동적 로드
  - 원장/교사: 반 선택 시 `/api/v1/kids?classroomId=...`로 원생 목록 로드
  - 학부모: `/api/v1/kids/my-kids`로 내 원생 목록 로드

- 학부모 조회 편의 확장
  - 학부모가 `kidId`를 주지 않으면 “내 원생 전체 기준”으로 알림장을 조회
  - 이를 위해 `NotepadRepository`에 `findNotepadsForParentKids(classroomIds, kidIds)` 쿼리를 추가

### 참고(범위)
- 이번 작업은 “알림장 목록 표시(필터/렌더링)” 정리에 집중했고, 페이지네이션/무한스크롤은 추후 단계에서 확정한다.

---

## 변경 이력
- 2026-01-14: 알림장 목록 표시 문제 해결(JS 오류 + 조회 전략 개선)
- 2026-01-14: 알림장 상세 템플릿 추가로 500 해결
- 2026-01-14: 원장/교사 수정·삭제 권한/화면 적용, 학부모 읽기 전용 확정
- 2026-01-14: 목록 카드 전체 클릭 UX 적용
- 2026-01-14: 알림장 목록/필터 HTMX 동적 로딩 전환(하드코딩 제거, `/notepad/list` 도입)
- 2026-01-18: 대시보드 통계용 기간(7/30일) 알림장 집계 기준 확정
- 2026-02-14: 알림장 작성 페이지 데이터 로딩 전략을 API 기반으로 확정(컨트롤러 TODO 제거)
