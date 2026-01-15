# 2026-01-15: 원생 생성 시 LazyInitializationException

## 문제
`POST /api/v1/kids` 호출 후 응답 생성 단계에서 LazyInitializationException 발생.

```
Could not initialize proxy [com.erp.domain.classroom.entity.Classroom#2] - no session
at com.erp.domain.kid.dto.response.KidResponse.from(KidResponse.java:30)
```

## 원인
- OSIV 비활성화 상태에서 `KidController`가 `KidService.getKid()` 반환값을 사용해 `KidResponse.from()` 호출
- `Kid.classroom`이 Lazy 로딩이어서 트랜잭션 종료 후 `classroom.getName()` 접근 시 예외 발생

## 해결
- KidRepository 조회 쿼리를 `JOIN FETCH`로 변경해 classroom을 즉시 로딩

변경 사항:
- `findByIdAndDeletedAtIsNull`
- `findByClassroomIdAndDeletedAtIsNull`
- `findByClassroomIdAndNameContaining`
- `findByParentId`

## 결과
- 원생 생성 후 응답 생성 단계에서 LazyInitializationException 해결
- 목록/검색/학부모 조회에서도 classroom 접근 안정화
- 실제 브라우저 테스트에서 생성/편집/삭제 정상 동작 확인
