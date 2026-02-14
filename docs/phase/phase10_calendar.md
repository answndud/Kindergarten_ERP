# Phase 10: 일정/캘린더 (Calendar/Schedule)

## 개요
- 단계: 일정/캘린더 시스템 구현
- 목표: 유치원/반/개인 일정 관리, 공휴일/행사 등록 및 조회
- 작업: CalendarEvent, Schedule 도메인

---

## 1. CalendarEvent 엔티티 설계

### 결정
일정을 별도 엔티티로 관리 (유치원/반/개인 범위 지원)

### 이유
1. **범위 구분**: 유치원 전체, 반 단위, 개인 일정 구분 필요
2. **반복 일정**: 주간/월간 반복 일정 지원
3. **알림 연동**: 일정 알림 발송 가능

### 관계 설계
```
Kindergarten (1:N) CalendarEvent (전체 일정)
Classroom (1:N) CalendarEvent (반 일정)
Member (1:N) CalendarEvent (개인 일정)
```

### 주요 필드
- `title`: 일정 제목
- `description`: 상세 설명
- `startDateTime`: 시작 일시
- `endDateTime`: 종료 일시
- `eventType`: 일정 유형 (LESSON, EVENT, HOLIDAY, MEETING, ETC)
- `scopeType`: 범위 (KINDERGARTEN, CLASSROOM, PERSONAL)
- `isAllDay`: 종일 일정 여부
- `location`: 장소
- `repeatType`: 반복 유형 (NONE, DAILY, WEEKLY, MONTHLY)
- `repeatEndDate`: 반복 종료일

### 변경 이력
- 2026-02-13: CalendarEvent 엔티티 설계 결정

---

## 2. 일정 범위 (Scope) 설계

### 결정
3가지 범위 타입으로 일정 구분

### 범위 타입
```java
public enum CalendarScopeType {
    KINDERGARTEN,  // 유치원 전체 (원장/교사 모두 조회)
    CLASSROOM,     // 반 단위 (해당 반 교사/학부모 조회)
    PERSONAL       // 개인 (본인만 조회)
}
```

### 권한 규칙
- **KINDERGARTEN**: 원장, 교사 모두 조회 가능
- **CLASSROOM**: 해당 반 소속 교사/학부모 조회 가능
- **PERSONAL**: 생성자만 조회/수정 가능

### 변경 이력
- 2026-02-13: 3단계 범위 구분 결정

---

## 3. 일정 유형 (EventType) 설계

### 결정
업무 특성에 따른 일정 유형 분류

### 유형 목록
```java
public enum CalendarEventType {
    LESSON,      // 수업/교육
    EVENT,       // 행사 (울림축제, 체육대회 등)
    HOLIDAY,     // 공휴일/휴원일
    MEETING,     // 회의 (학부모회, 교사회의 등)
    EXAM,        // 평가/시험
    FIELD_TRIP,  // 현장학습
    ETC          // 기타
}
```

### 변경 이력
- 2026-02-13: 7가지 일정 유형 결정

---

## 4. 반복 일정 설계

### 결정
반복 일정을 단일 엔티티로 관리 (repeat 필드 사용)

### 반복 유형
```java
public enum RepeatType {
    NONE,     // 반복 없음
    DAILY,    // 매일
    WEEKLY,   // 매주
    MONTHLY   // 매월
}
```

### 구현 방식
- 반복 일정은 하나의 엔티티로 저장
- 조회 시 repeatType에 따라 동적 생성
- 수정: 반복 일정 중 특정 일정만 수정 vs 전체 수정 선택

### 변경 이력
- 2026-02-13: 단일 엔티티 + repeat 필드 방식 채택

---

## 5. API 설계

### 결정
RESTful API 설계

### 일정 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | /api/v1/calendar/events | 일정 목록 조회 (월간/주간/일간) |
| GET | /api/v1/calendar/events/{id} | 일정 상세 조회 |
| POST | /api/v1/calendar/events | 일정 생성 |
| PUT | /api/v1/calendar/events/{id} | 일정 수정 |
| DELETE | /api/v1/calendar/events/{id} | 일정 삭제 |
| GET | /api/v1/calendar/events/today | 오늘의 일정 |
| GET | /api/v1/calendar/events/upcoming | 다가오는 일정 |

### 조회 파라미터
```
GET /api/v1/calendar/events?startDate=2026-02-01&endDate=2026-02-28&scopeType=CLASSROOM&classroomId=1
```

### 변경 이력
- 2026-02-13: RESTful API 설계

---

## 6. 캘린더 UI 설계

### 결정
FullCalendar 라이브러리 또는 HTMX + Alpine.js 커스텀 구현

### 화면 구성
1. **월간 뷰**: 전체 월 달력 표시
2. **주간 뷰**: 주 단위 상세 표시
3. **일간 뷰**: 하루 상세 일정
4. **일정 목록**: 리스트 형태 조회

### 기능
- 날짜 클릭으로 일정 생성
- 드래그로 일정 이동
- 일정 클릭으로 상세/수정
- 범위 필터 (전체/반/개인)

### 변경 이력
- 2026-02-13: FullCalendar 검토, HTMX 구현 결정

---

## 7. 알림 연동

### 결정
일정 알림 자동 생성

### 알림 시점
- 일정 당일 아침 (오전 7시)
- 일정 1일 전 (오후 6시)
- 주간 일정 미리보기 (월요일 아침)

### 알림 타입
```java
SCHEDULE_REMINDER_TODAY    // 당일 알림
SCHEDULE_REMINDER_TOMORROW // 1일 전 알림
SCHEDULE_WEEKLY_PREVIEW    // 주간 미리보기
```

### 변경 이력
- 2026-02-13: Phase 8 알림 시스템과 연동 결정

---

## 8. 권한 제어

### 결정
일정 범위별 생성/수정 권한 분리

### 권한 규칙
| 범위 | 생성 | 수정 | 삭제 |
|------|------|------|------|
| KINDERGARTEN | PRINCIPAL | PRINCIPAL | PRINCIPAL |
| CLASSROOM | TEACHER (담당) | TEACHER (담당) | TEACHER (담당) |
| PERSONAL | 본인 | 본인 | 본인 |

### 변경 이력
- 2026-02-13: 범위별 권한 규칙 결정

---

## 면접 예상 질문 답변

### Q: 일정을 어떻게 구분하나요?
> A: scopeType으로 유치원 전체, 반 단위, 개인 3가지로 구분합니다. 유치원 전체 일정은 원장이 등록하고 모두가 볼 수 있고, 반 일정은 담당 교사가 등록하여 해당 반 학부모와 교사가 조회합니다. 개인 일정은 각자만 볼 수 있습니다.

### Q: 반복 일정은 어떻게 구현했나요?
> A: 반복 유형과 종료일을 필드로 저장하고, 조회 시점에 동적으로 생성합니다. DB에 매일/매주 데이터를 쌓지 않고 하나의 엔티티로 관리해서 저장 공간을 절약하고, 수정 시에도 전체 수정/특정 일정만 수정을 선택할 수 있게 했습니다.

### Q: 알림은 어떻게 연동하나요?
> A: Phase 8에서 구현한 알림 시스템을 활용합니다. 일정 당일 아침과 1일 전, 주간 미리보기 알림을 자동으로 생성하도록 연동했습니다. 알림에 일정 상세 페이지 링크를 포함해서 바로 확인할 수 있게 했습니다.

---

## 구현 체크리스트

### 백엔드
- [ ] CalendarEvent 엔티티 생성
- [ ] CalendarEventRepository 생성
- [ ] DTO (Request/Response) 생성
- [ ] CalendarEventService 구현
- [ ] CalendarEventController 구현
- [ ] 권한 체크 로직 구현

### 프론트엔드
- [ ] 캘린더 페이지 (/calendar) 생성
- [ ] 월간/주간/일간 뷰 구현
- [ ] 일정 생성/수정 모달 구현
- [ ] HTMX 부분 갱신 구현
- [ ] 범위 필터 UI 구현

### DB 마이그레이션
- [ ] calendar_events 테이블 생성 (Flyway)

---

## 다음 단계
Phase 11: 식단 관리 구현

---

**Phase 10 설계일: 2026-02-13**
