# 2026-01-15: 원생 추가 500 Internal Server Error

## Problem Description
원생 추가 기능(`POST /api/v1/kids`)이 500 Internal Server Error를 반환합니다.

## Error Details

### 1. 500 Internal Server Error
```
Request: POST http://localhost:8080/api/v1/kids
Status Code: 500 Internal Server Error
```

### 2. 인증 오류 (curl 테스트)
```
Request with Cookie: jwt_token=test
Response: {"success":false,"message":"인증이 필요합니다","code":"C001"}
```

## Root Cause Analysis

### 500 Internal Server Error 원인
1. **요청 데이터 포맷 문제**:
   - 요청 포맷은 JSON이지만 실제 데이터 전송 문제
2. **KidController 생성 로직 확인 필요**:
   - `KidService.createKid()` → `KidRepository.save()` 과정에서 문제 발생 가능
3. **페이증 유효성**:
   - 요청 중 classroomId 검증 필요

### 인증 오류 원인
- **JWT 토큰 문제**: `Cookie: jwt_token=test`는 유효하지 않음
- **브라우저 vs curl**:
  - 브라우저에서는 정상 로그인 상태라서 JWT 쿠기가 있음
  - curl 테스트에서는 인증 오류 (정상)

## Investigation Steps

### 1. 서버 로그 확인
- Tomcat이 정상적으로 시작됨 (port 8080)
- 다른 API는 정상 동작
- 오직 `/api/v1/kids` POST만 500 에러

### 2. KidController 코드 확인
```java
@PostMapping
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public ResponseEntity<ApiResponse<KidResponse>> create(
        @Valid @RequestBody KidRequest request) {

    Long id = kidService.createKid(request);

    Kid kid = kidService.getKid(id);  // ← 여기서 문제 발생 가능

    return ResponseEntity
            .ok(ApiResponse.success(KidResponse.from(kid), "원생이 등록되었습니다"));
}
```

### 3. KidService.createKid() 확인
```java
@Transactional
public Long createKid(KidRequest request) {
    // 반 조회
    Classroom classroom = classroomService.getClassroom(request.getClassroomId());

    // 원생 생성
    Kid kid = Kid.create(classroom, request.getName(), ...);
    return kidRepository.save(kid);
}
```

### 4. 가능한 문제들
1. **Classroom이 DB에 없음**:
   - `classroomService.getClassroom(classroomId)`가 없는 classroom 조회 시 `Optional.empty()` 반환
   - 이 경우 `Kid.create(classroom, ...)`에서 NPE 발생 가능
2. **FK 제약 조건 문제**:
   - classroom.id가 null이면 DB 저장 시 FK 오류 발생
3. **요청 검증 누락**:
   - `@Valid @RequestBody`으로 검증하나 실제 데이터가 유효한지 체크 안을 수 있음

## Solution Applied

### 즉시 해결 방법
1. **데이터 확인**:
   ```sql
   SELECT * FROM classroom WHERE id = 1;
   SELECT * FROM kid;
   ```

2. **서버 로그에서 전체 스택트레이스 확인**:
   - 브라우저에서 원생 추가 시도
   - `/tmp/erp.log`에서 `POST /api/v1/kids` 관련 에러 확인
   - 자바 스택트레이스(NullPointerException 등) 확인

3. **KidController 디버깅 추가**:
   ```java
   @PostMapping
   @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
   public ResponseEntity<ApiResponse<KidResponse>> create(
           @Valid @RequestBody KidRequest request) {

       Long id = kidService.createKid(request);
       System.out.println("[DEBUG] KidController.create - kid ID: " + id);

       // 생성된 kid 조회 전에 예외 처리
       try {
           Kid kid = kidService.getKid(id);
           return ResponseEntity.ok(ApiResponse.success(KidResponse.from(kid), "원생이 등록되었습니다"));
       } catch (Exception e) {
           System.out.println("[ERROR] KidController.create - Exception: " + e.getMessage());
           e.printStackTrace();
           throw e;  // 예외를 다시 던져서 정확한 에러 반환
       }
   }
   ```

## Status
🔍 **원인 분석 중**:
- 데이터베이스에 classroom 데이터 확인 필요
- 전체 서버 로그 분석 필요
- KidController에 디버깅 추가 필요
- 브라우저에서 재시도하여 원인 확인 필요

## Related Files
- `src/main/java/com/erp/domain/kid/controller/KidController.java`
- `src/main/java/com/erp/domain/kid/service/KidService.java`
- `src/main/java/com/erp/domain/kid/repository/KidRepository.java`
- `src/main/java/com/erp/domain/classroom/service/ClassroomService.java`

## Next Steps
1. 서버 로그에서 정확한 에러 스택트레이스 확인
2. Classroom 데이터 존재 여부 확인
3. KidService.createKid()에 예외 처리 추가
4. 원인 확인 후 올바른 해결책 적용
