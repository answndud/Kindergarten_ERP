# Phase 8: 알림 시스템 기술 선택 결정

## 개요
- 단계: 알림 시스템 구현
- 목표: 지원/승인, 알림장, 공지사항 등의 알림 발송, 안 읽은 알림 표시
- 작업: Notification 도메인

---

## 1. Notification 엔티티 설계

### 결정
수신자별 알림을 별도 엔티티로 관리

### 이유
1. **다양한 알림**: 지원, 승인, 거절, 알림장, 공지사항 등
2. **시계열 데이터**: 알림이 시간 순으로 누적
3. **읽음 관리**: isRead 필드로 읽음 상태 추적

### 관계 설계
```
Member (수신자) (1:N) Notification
```

### 주요 필드
- `type`: 알림 타입 (enum)
- `title`: 제목
- `content`: 내용
- `linkUrl`: 관련 페이지 링크
- `isRead`: 읽음 여부
- `readAt`: 읽은 시간
- `relatedEntityType`, `relatedEntityId`: 연관 엔티티 (선택)

### 변경 이력
- 2025-01-14: Notification 엔티티 분리 결정
- 2026-01-18: 읽음 보관/삭제 숨김 정책과 기본 미읽음 필터 UX 확정

---

## 2. 알림 타입

### 결정
NotificationType enum으로 알림 종류 관리

### 타입 목록
```java
public enum NotificationType {
    // 교사 지원 관련
    KINDERGARTEN_APPLICATION_SUBMITTED,
    KINDERGARTEN_APPLICATION_APPROVED,
    KINDERGARTEN_APPLICATION_REJECTED,
    KINDERGARTEN_APPLICATION_CANCELLED,

    // 입학 신청 관련
    KID_APPLICATION_SUBMITTED,
    KID_APPLICATION_APPROVED,
    KID_APPLICATION_REJECTED,
    KID_APPLICATION_CANCELLED,

    // 알림장 관련
    NOTEPAD_CREATED,
    NOTEPAD_READ_CONFIRM,

    // 공지사항 관련
    ANNOUNCEMENT_CREATED,

    // 시스템
    SYSTEM
}
```

### 이유
1. **타입 안전성**: 컴파일 타임에 검증
2. **확장성**: 새로운 알림 타입 추가 용이
3. **그룹핑**: 타입별로 알림 필터링 가능

### 변경 이력
- 2025-01-14: enum 타입 채택

---

## 3. 읽음 관리

### 결정
isRead 불리언 필드 + readAt 시간 필드

### 구현 방식
```java
@Column(name = "is_read")
private Boolean isRead = false;

@Column(name = "read_at")
private LocalDateTime readAt;

public void markAsRead() {
    if (!this.isRead) {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
```

### 이유
1. **간단함**: 불리언으로 읽음 상태 표시
2. **시간 추적**: 언제 읽었는지 기록

### 대안 고려
- **읽음 횟수**: 여러 번 읽어도 1로 카운트

### 변경 이력
- 2025-01-14: isRead + readAt 채택

---

## 4. 안 읽은 알림 개수

### 결정
데이터베이스 카운트 쿼리 + 캐싱

### 구현 방식
```java
@Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false AND n.deletedAt IS NULL")
long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
```

### 성능 최적화
```java
// Redis 캐싱 (선택사항)
@Cacheable(value = "unreadCount", key = "#receiverId")
public long getUnreadCount(Long receiverId) {
    return repository.countUnreadByReceiverId(receiverId);
}
```

### 이유
1. **정확성**: DB에서 직접 카운트
2. **확장성**: 캐시로 성능 개선 가능

### 변경 이력
- 2025-01-14: DB 카운트 채택 (캐시는 선택)

---

## 5. 알림 API 설계

### 결정
RESTful API 설계

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | /api/v1/notifications | 알림 목록 |
| GET | /api/v1/notifications/{id} | 알림 상세 |
| GET | /api/v1/notifications/unread-count | 안 읽은 알림 수 |
| GET | /api/v1/notifications/unread | 안 읽은 알림 목록 |
| PUT | /api/v1/notifications/{id}/read | 읽음 표시 |
| PUT | /api/v1/notifications/read-all | 전체 읽음 표시 |
| DELETE | /api/v1/notifications/{id} | 삭제 |

### 이유
1. **표준 준수**: RESTful API 원칙
2. **명확한 자원**: notifications 하위 리소스

### 변경 이력
- 2025-01-14: RESTful API 채택

---

## 6. 알림 생성 패턴

### 결정
Service 내부 편의 메서드 + 공개 API

### 편의 메서드
```java
// 단순 알림
notify(receiverId, type, title, content)

// 링크 포함
notifyWithLink(receiverId, type, title, content, linkUrl)

// 연관 엔티티 포함
notifyWithRelatedEntity(receiverId, type, title, content, entityType, entityId)
```

### 공개 API
```java
// 다양한 알림 생성
public Long create(NotificationCreateRequest request)
```

### 이유
1. **사용 편의성**: 상황에 맞는 메서드 선택
2. **유연성**: 외부에서도 직접 알림 생성 가능

### 변경 이력
- 2025-01-14: 편의 메서드 + 공개 API 채택

---

## 7. 알림 UI

### 결정
HTMX + Alpine.js로 알림 배지 구현

### 추가 결정(2026-01-14)
- 화면 내 주요 액션(승인/거절/취소 등) 이후에는 **이벤트 기반(HTMX trigger)** 으로 알림 배지/목록을 즉시 갱신한다.
- 주기적 폴링(`every 30s`)은 보조 수단으로 두되, UX는 이벤트 기반 즉시 갱신을 우선한다.

### 주요 기능
1. **빨간 점**: 안 읽은 알림 수 표시
2. **드롭다운**: 벨 클릭 시 알림 목록 표시
3. **읽음 처리**: 클릭 시 읽음 표시 + 배지 제거
4. **전체 읽음**: "모두 읽음" 버튼

### 구현 방식
```html
<!-- HTMX로 즉시 + 주기적 미읽은 알림 수 갱신 -->
<div hx-get="/notifications/fragments/badge"
     hx-trigger="load, notifications-changed from:body, every 30s"
     hx-swap="outerHTML">
    <!-- 안 읽은 알림 수 -->
</div>
```

### 이유
1. **실시성**: 30초마다 자동 갱신
2. **간단한 구현**: HTMX로 부분 렌더링

### 변경 이력
- 2025-01-14: HTMX 주기적 갱신 채택
- 2026-01-14: 이벤트 기반 즉시 갱신(`notifications-changed`) 추가
- 2026-01-17: 드롭다운에서 미읽음 필터/개별 읽음/삭제 액션 추가

---

## 8. 알림 정책

### 결정
알림 생성 시점 정의

### 지원/승인 알림
- 지원 제출 시: 원장/교사에게 알림
- 승인 완료 시: 지원자에게 알림
- 거절 시: 지원자에게 거절 사유 포함 알림

### 알림장/공지사항
- 작성 시: 대상에게 알림
- 링크 제공: 바로 이동 가능
- 삭제는 소프트 삭제로 처리, 읽음은 보관

### 이유
1. **즉시성**: 이벤트 발생 시 즉시 알림
2. **편의성**: 알림을 통해 바로 접근

### 변경 이력
- 2025-01-14: 이벤트 기반 알림 채택

---

## 9. Soft Delete

### 결정
deletedAt 필드로 Soft Delete

### 구현 방식
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}

// 조회 시 deletedAt 필터링
```

### 이유
1. **데이터 보존**: 삭제된 알림도 기록 유지
2. **복구 가능**: 실수로 삭제 시 복구
3. **감사 추적**: 알림 삭제 이력 확인

### 변경 이력
- 2025-01-14: Soft Delete 채택

---

## 10. 인덱스 전략

### 결정
조회 성능을 위한 인덱스 구성

### 인덱스
```sql
-- 수신자 + 읽음 여부 (미읽은 알림 조회)
INDEX idx_receiver_read (receiver_id, is_read)

-- 수신자 + 생성일시간 (최신 알림 조회)
INDEX idx_receiver_created (receiver_id, created_at DESC)
```

### 이유
1. **조회 성능**: 자주 조회하는 조합으로 인덱스
2. **정렬 성능**: created_at DESC로 최신순 정렬

### 변경 이력
- 2025-01-14: 복합 인덱스 채택

---

## 면접 예상 질문 답변

### Q: 왜 알림을 별도 엔티티로 만드셨나요?
> A: 알림은 다양한 종류가 있고 시간 순으로 누적되는 시계열 데이터입니다. Member에 필드로 넣으면 한 명의 알림이 많아질수록 엔티티가 커지고, 조회 성능도 저하됩니다. 별도 Notification 엔티티를 두면 알림 타입별로 필터링하기 쉽고, 인덱스를 활용해서 효율적으로 조회할 수 있습니다.

### Q: 안 읽은 알림 수는 어떻게 관리하나요?
> A: DB에서 isRead = false인 알림을 직접 카운트합니다. 정확성이 가장 중요하기 때문에 DB 카운트를 사용하고, 성능이 문제가 되면 Redis 캐싱을 도입할 계획입니다. 쿼리에 인덱스를 걸어서 (receiver_id, is_read) 조합으로 빠르게 카운트할 수 있습니다.

### Q: 알림을 읽으면 어떻게 처리하나요?
> A: markAsRead() 메서드로 isRead를 true로 바꾸고 readAt에 현재 시간을 기록합니다. 이미 읽은 알림을 다시 읽어도 상태가 변하지 않아서, 한 번만 읽은 것으로 카운트됩니다. 프론트엔드에서는 읽음 표시 후 빨간 배지를 제거합니다.

### Q: 알림 타입은 어떻게 관리하나요?
> A: enum으로 관리합니다. KINDERGARTEN_APPLICATION_SUBMITTED, KID_APPLICATION_APPROVED 등으로 명확하게 구분하고, 새로운 알림 타입이 필요하면 enum에 추가하기만 하면 됩니다. 타입별로 알림을 필터링하거나 그룹핑할 때도 enum 값을 활용해서 쉽게 구현할 수 있습니다.

---

## 구현 현황 (Phase 8 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/notification/entity/Notification.java` | 알림 엔티티 |
| `domain/notification/entity/NotificationType.java` | 알림 타입 enum |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/notification/repository/NotificationRepository.java` | 알림 리포지토리 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/notification/dto/request/NotificationCreateRequest.java` | 알림 생성 요청 |
| `domain/notification/dto/response/NotificationResponse.java` | 알림 응답 |
| `domain/notification/dto/response/UnreadCountResponse.java` | 미읽은 알림 수 응답 |

#### 4. 서비스
| 파일 | 설명 |
|------|------|
| `domain/notification/service/NotificationService.java` | 알림 서비스 |

#### 5. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/notification/controller/NotificationController.java` | 알림 API | GET /api/v1/notifications, /unread-count, PUT /{id}/read |

---

### 구현된 기능 상세

#### 백엔드

**1. Notification 엔티티**
```java
// 필드: receiver, type, title, content, linkUrl, isRead, readAt, relatedEntityType, relatedEntityId
// 연관관계: Member (수신자, N:1)
// 정적 팩토리: create(), createWithLink(), createWithRelatedEntity()
// 비즈니스 메서드: markAsRead(), softDelete()
```

**2. 알림 생성 편의 메서드**
```java
// 단순 알림
notify(receiverId, type, title, content)

// 링크 포함
notifyWithLink(receiverId, type, title, content, linkUrl)

// 연관 엔티티 포함
notifyWithRelatedEntity(receiverId, type, title, content, entityType, entityId)
```

**3. Notification 서비스**
- `create()`: 알림 생성 (공개 API)
- `getNotifications()`: 알림 목록
- `getUnreadCount()`: 안 읽은 알림 수
- `markAsRead()`: 읽음 표시
- `markAllAsRead()`: 전체 읽음 표시
- `delete()`: 삭제

**4. 알림 타입**
- 교사 지원: KINDERGARTEN_APPLICATION_SUBMITTED, APPROVED, REJECTED, CANCELLED
- 입학 신청: KID_APPLICATION_SUBMITTED, APPROVED, REJECTED, CANCELLED
- 알림장: NOTEPAD_CREATED, NOTEPAD_READ_CONFIRM
- 공지사항: ANNOUNCEMENT_CREATED
- 시스템: SYSTEM

---

**Phase 8 완료일: 2025-01-14**
