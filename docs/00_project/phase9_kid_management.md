# Phase 9: 원생(Kid) 관리

## Overview
원생 정보 CRUD 및 학부모 연결 관리 기능 구현

## API (이미 구현 완료)
```
GET    /api/v1/kids               - 원생 목록 조회 (classroomId, name 필터)
GET    /api/v1/kids/{id}          - 원생 상세 조회
POST   /api/v1/kids               - 원생 생성 (PRINCIPAL, TEACHER)
PUT    /api/v1/kids/{id}          - 원생 정보 수정 (PRINCIPAL, TEACHER)
DELETE /api/v1/kids/{id}          - 원생 삭제 (PRINCIPAL)
PUT    /api/v1/kids/{id}/classroom - 반 변경 (PRINCIPAL)
POST   /api/v1/kids/{id}/parents  - 학부모 연결 (PRINCIPAL)
DELETE /api/v1/kids/{id}/parents/{parentId} - 학부모 연결 해제 (PRINCIPAL)
```

## Implementation Log

### 2026-01-15: Phase 1 - 기본 구조 생성
- **결정**: `ClassroomViewController` 패턴을 따라 `KidViewController` 생성
- **결정**: `/kids` 경로로 접근 시 `kid/kids.html` 렌더링
- **추가 파일**:
  - `KidViewController.java` - ViewController 클래스
  - `kid/kids.html` - 메인 페이지 템플릿
- **권한**: `PRINCIPAL`, `TEACHER`만 접근 가능

### 2026-01-15: Phase 2 - 원생 목록 조회 (반별 필터)
- **구현**: `loadKids()` 함수에서 반별/이름 필터 지원
- **API**: `GET /api/v1/kids?classroomId=X&name=Y`
- **완료**: Phase 1 이미 구현 내역에 포함됨

### 2026-01-15: Phase 3 - 원생 생성 폼 (SweetAlert2)
- **구현**: `showCreateKidModal()` 함수로 SweetAlert2 다이얼로그 구현
- **API**: `POST /api/v1/kids`
- **요청 포맷**:
  ```json
  {
    "name": "이름",
    "birthDate": "2020-01-01",
    "gender": "MALE" | "FEMALE",
    "admissionDate": "2024-01-01",
    "classroomId": 1
  }
  ```
- **UI**: 이름/생년월일/성별/입소일/반 선택 폼

### 2026-01-15: Phase 4 - 원생 편집/삭제 기능
- **구현**: `showEditKidModal()` 편집 다이얼로그 + `deleteKid()` 삭제 기능
- **API**:
  - `PUT /api/v1/kids/{id}` (편집)
  - `DELETE /api/v1/kids/{id}` (삭제)
- **편집 요청 포맷**:
  ```json
  {
    "name": "이름",
    "birthDate": "2020-01-01",
    "gender": "MALE" | "FEMALE"
  }
  ```
- **권한**: 편집은 PRINCIPAL, TEACHER; 삭제는 PRINCIPAL만

### 2026-01-15: Phase 5 - 학부모 관리 UI (추가/제거)
- **구현**: `showParentManageModal()` 학부모 관리 다이얼로그
- **API**:
  - `POST /api/v1/kids/{id}/parents` (학부모 연결)
  - `DELETE /api/v1/kids/{id}/parents/{parentId}` (학부모 연결 해제)
- **학부모 추가 요청 포맷**:
  ```json
  {
    "parentId": 1,
    "relationship": "FATHER" | "MOTHER" | "GRANDFATHER" | "GRANDMOTHER" | "GUARDIAN"
  }
  ```
- **UI**: 원생의 학부모 목록 표시 + 학부모 선택/관계 선택 폼
- **권한**: PRINCIPAL만

### 2026-01-15: 안정화/버그 수정
- **문제**: 원생 생성 시 `LazyInitializationException` 발생
- **원인**: `KidResponse.from()`에서 `classroom.getName()` 접근 시 세션 종료
- **해결**: `KidRepository` 조회 쿼리 `JOIN FETCH`로 classroom 즉시 로딩
- **적용 쿼리**:
  - `findByIdAndDeletedAtIsNull`
  - `findByClassroomIdAndDeletedAtIsNull`
  - `findByClassroomIdAndNameContaining`
  - `findByParentId`

### 2026-01-15: UI 동작 개선
- **문제**: `UI is not defined` 및 SweetAlert2 결과 처리 오류
- **해결**:
  - `kid/kids.html`에 UI fallback 추가 (app.js 로딩 실패 대비)
  - SweetAlert2 `Swal.fire()` 결과를 `result.isConfirmed` + `result.value`로 처리
  - `app.js` 내 중복 코드 제거 및 문법 오류 정리

### 2026-01-15: 전체 완료
- **구현 완료된 기능**:
  - 원생 목록 조회 (반별/이름 필터)
  - 원생 생성 (SweetAlert2 다이얼로그)
  - 원생 정보 수정 (이름/생년월일/성별)
  - 원생 삭제 (소프트 삭제)
  - 학부모 연결/해제 관리
  - 원생 생성/편집/삭제 실제 동작 확인

### 2026-01-17: UI 보완
- **추가**: 원생 목록에서 반 변경 모달 (원장 전용)
- **추가**: 반 목록에 원생 수 표시, 정렬 옵션(이름/나이/최근)
- **개선**: 유치원 ID 기준 전체 조회 API 지원
- **추가**: 원생 목록 클라이언트 페이지네이션/총원 표시
- **개선**: 학부모 연결 모달에서 추가 가능한 학부모만 표시

## Files Created/Modified

### Created:
- `src/main/java/com/erp/domain/kid/controller/KidViewController.java` - ViewController
- `src/main/resources/templates/kid/kids.html` - 메인 페이지 (218줄)
- `docs/00_project/phase9_kid_management.md` - 설계/결정 로그

## Known Limitations
- 원생 상세 페이지 없음 (목록에서 바로 편집/삭제)

## Testing Status (2026-01-15)

### 테스트 코드 작성 시도
- **KidApiIntegrationTest.java**: API 통합 테스트 작성 완료 (17개 테스트)
- **KidApiSimpleTest.java**: 간단 테스트 작성 중
- **문제 발생**: 테스트 데이터 설정 복잡도 (Member-Kindergarten-Classroom-Kid 연결)
- **데이터 제약**: FK 제약 조건으로 인증된 사용자가 유치원에 소속되어 있어야 함

### 수동 테스트 수행
- **애플리케이션 실행 성공**: `./gradlew bootRun --args='--spring.profiles.active=local'`
- **API 인증 확인**: `/api/v1/kids/1` 접근 시 인증 필요 (정상 동작)
- **권한 확인**: 인증 없이 접근 시 `{"success":false,"message":"인증이 필요합니다","code":"C001"}` 반환
- **빌드 성공**: `./gradlew build -x test` 통과
- **HTTP 접근 테스트**: `/kids` 접근 시 302 → `/login` redirect (정상)
- **템플릿 파싱 오류 해결**: `fragments/header.html` line 64 수정 (`charAt(0)?.toUpperCase()` → `toUpperCase()?.charAt(0)`)
- **UI 동작 확인**: 원생 추가/편집/삭제 정상 동작 확인

### 원생 추가 500 Internal Server Error (해결됨)
- **발생 시점**: 2026-01-15 12:30
- **오류 내용**: POST `/api/v1/kids` 요청 시 500 Internal Server Error
- **원인**: `KidResponse.from()` 호출 시 `classroom` Lazy 로딩 실패
- **해결**: `KidRepository`에 `JOIN FETCH` 추가
- **결과**: 생성/편집/삭제 정상 동작
