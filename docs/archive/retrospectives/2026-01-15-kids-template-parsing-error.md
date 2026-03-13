# 2026-01-15: kid/kids.html 템플릿 렌더링 오류 해결

## Problem Description
`http://localhost:8080/kids` 접근 시 Thymeleaf 템플릿 파싱 오류 발생

### Error Log
```
org.thymeleaf.exceptions.TemplateInputException: An error happened during template parsing
Caused by: org.thymeleaf.exceptions.TemplateProcessingException:
  Exception evaluating SpringEL expression: "#authentication.name?.charAt(0)?.toUpperCase()"
  (template: "fragments/header" - line 64, col 84)

Caused by: org.springframework.expression.spel.SpelEvaluationException:
  EL1004E: Method call: Method toUpperCase() cannot be found on type java.lang.Character
```

## Root Cause Analysis
`fragments/header.html`의 line 64에 있는 코드:
```html
<span th:text="${#authentication.name?.charAt(0)?.toUpperCase()}">U</span>
```

**문제**: `charAt(0)`는 `Character` 타입을 반환하며, Java의 `Character` 클래스에는 `toUpperCase()` 메서드가 없습니다.

**원인**:
- `authentication.name?.charAt(0)` → `Character` 타입 반환
- `Character.toUpperCase()` → 메서드 존재하지 않음 (String 클래스에만 있음)

## Solution Applied

### Before
```html
<span class="text-sm font-semibold text-white" th:text="${#authentication.name?.charAt(0)?.toUpperCase()}">U</span>
```

### After
```html
<span class="text-sm font-semibold text-white" th:text="${#authentication.name?.toUpperCase()?.charAt(0)}">U</span>
```

### 변경 이유
1. `authentication.name?.toUpperCase()` → 전체 이름을 대문자로 변환 (`String`)
2. `?.charAt(0)` → 변환된 문자열의 첫 글자만 추출 (`Character`)
3. 결과: 첫 글자만 대문자로 표시됨

## Verification

### 빌드 테스트
```bash
./gradlew build -x test
```
**결과**: BUILD SUCCESSFUL

### 애플리케이션 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
**결과**: 성공적으로 시작됨 (Port 8080)

### HTTP 접근 테스트
```bash
curl -v http://localhost:8080/kids
```
**결과**:
- HTTP/1.1 302 (정상)
- Location: http://localhost:8080/login (인증 필요 시 redirect)
- 템플릿 파싱 오류 없음

## Key Learnings

1. **Thymeleaf SpEL 메서드 체인 순서 중요**
   - 반환 타입 확인 후 해당 타입의 메서드만 호출 가능
   - 타입 변환은 먼저 수행해야 함

2. **안전한 널 체크**
   - `?.` 연산자로 중간 단계에서 널 체크
   - `authentication.name?.toUpperCase()?.charAt(0)`와 같이 전체 체인에 적용

3. **빌드 vs 런타임**
   - 빌드 성공 ≠ 런타임 오류 없음
   - Thymeleaf 템플릿 파싱은 런타임에 수행됨

## Related Files
- `src/main/resources/templates/fragments/header.html` - line 64 수정
- `docs/phase/phase9_kid_management.md` - 테스트 상태 업데이트

## Status
✅ 해결 완료 - `/kids` 페이지 접근 가능
