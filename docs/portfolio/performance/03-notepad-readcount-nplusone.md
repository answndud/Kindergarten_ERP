# Notepad 목록 N+1 개선 (1차)

## 문제 재현 시나리오

- 사용자: 학부모
- 화면: `/notepad` 목록 또는 `/api/v1/notepads/classroom/{classroomId}`
- 조건: 페이지 크기 20, 알림장 데이터 누적

기존 구현에서는 목록을 DTO로 변환하는 과정에서 알림장마다 읽음 수 조회가 반복될 수 있어,
데이터가 커질수록 요청당 쿼리 수가 선형으로 증가하는 구조였습니다.

## 개선 전 병목 포인트

### 1) 읽음 수 계산

- 위치: `NotepadService#getNotepadsForParent(classroomId, kidId, pageable)`
- 패턴: 페이지 결과 N건에 대해 `findReadConfirmsByNotepadId()`를 N번 호출
- 결과: `1 + N` 형태의 쿼리 증가 가능성

### 2) 반별 알림장 알림 발송 대상 계산

- 위치: `NotepadService#notifyParentsAboutNotepad`
- 패턴: 반 원생 K명에 대해 `findParentsByKidId()`를 K번 호출
- 결과: 원생 수에 비례한 반복 조회

## 개선 내용 (1차 적용)

### 1) 읽음 수 일괄 집계로 전환

- `NotepadRepository`에 다건 집계 쿼리 추가
  - `countReadConfirmsByNotepadIds(List<Long> notepadIds)`
- `NotepadService`에 공통 매핑 유틸 추가
  - `mapWithReadCounts(Page<Notepad>)`
  - `loadReadCountMap(List<Long>)`
- 효과
  - 기존: 목록 N건 -> 읽음 조회 N회
  - 변경: 목록 N건 -> 읽음 집계 1회

### 2) 반별 알림 수신자 조회 배치화

- `ParentKidRepository`에 다건 조회 추가
  - `findDistinctParentIdsByKidIds(List<Long> kidIds)`
- `NotepadService#notifyParentsAboutNotepad`에서 Kid별 반복 조회 제거

## 코드 변경 포인트

- `src/main/java/com/erp/domain/notepad/service/NotepadService.java`
- `src/main/java/com/erp/domain/notepad/repository/NotepadRepository.java`
- `src/main/java/com/erp/domain/kid/repository/ParentKidRepository.java`
- `src/test/java/com/erp/api/NotepadApiIntegrationTest.java`

## 검증

- 읽음 처리 후 목록에서 `readCount`가 반영되는 통합 테스트 추가
  - `NotepadApiIntegrationTest.MarkAsReadTest#getClassroomNotepads_ReadCountReflected`

## 측정 계획 (2차)

이 문서의 목표는 "문제 재현 -> 수치 개선"까지 완성하는 것입니다.
현재는 1차로 코드 경로를 개선했고, 다음 단계에서 동일 시나리오로 전/후 수치를 채웁니다.

### 측정 항목

- avg / p95 응답 시간
- 요청당 SQL 쿼리 수

### 측정 환경

- 실행 일시: 2026-02-14
- 테스트: `com.erp.performance.NotepadPerformanceStoryTest`
- 데이터: 기본 테스트 데이터 + 추가 알림장 80건 (페이지 조회 20건)
- DB: H2(test profile)

### 측정 표

| Scenario | Before avg | Before p95 | Before queries | After avg | After p95 | After queries |
|---|---:|---:|---:|---:|---:|---:|
| GET `/api/v1/notepads/classroom/{id}` (size=20) | 15ms | 15ms | 22 | 4ms | 4ms | 4 |

> 측정 로그: `build/test-results/test/TEST-com.erp.performance.NotepadPerformanceStoryTest.xml`

## 포트폴리오 스토리텔링 포인트

1. "기능은 먼저 완성했지만, 데이터가 늘면서 목록 조회가 느려졌다"
2. "코드 전체를 재작성하지 않고 조회 경로를 배치화해서 병목을 제거했다"
3. "읽음 수는 UX 요구사항이었고, 성능까지 유지하도록 집계 쿼리로 바꿨다"
4. "동일 시나리오 기준으로 쿼리 수를 22 -> 4로 줄여 병목이 구조적으로 해결됨을 증명했다"
