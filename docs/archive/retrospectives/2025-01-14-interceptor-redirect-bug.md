# 2025-01-14: 인터셉터 리다이렉트 버그 회고

## 문제 개요

**증상:** 원장 계정으로 로그인 후 모든 페이지 접근 시 `NoResourceFoundException: No static resource kindergarten/create` 에러 발생

**영향:** 로그인 후 애플리케이션 사용 불가

---

## 문제 원인

### 1. 직접적 원인
`RoleRedirectInterceptor`가 원장 계정이 유치원이 없을 때 `/kindergarten/create`로 강제 리다이렉트했으나, 이를 처리하는 컨트롤러가 존재하지 않았음.

```java
// RoleRedirectInterceptor.java (수정 전)
if (member.getRole() == MemberRole.PRINCIPAL &&
        member.getKindergarten() == null &&
        !uri.startsWith("/kindergarten/create")) {
    return "/kindergarten/create";  // 컨트롤러 없음!
}
```

### 2. 근본적 원인

#### 2.1 순차적 개발 부재
- 뷰 컨트롤러와 템플릿을 API 컨트롤러보다 나중에 개발
- 인터셉터 리다이렉트 로직을 먼저 구현하고, 대상 페이지를 나중에 만들려다 누락

#### 2.2 통합 테스트 부족
- 단위 테스트만으로는 발견 불가
- 로그인 흐름에 대한 통합 테스트가 없었음
- 인터셉터 → 컨트롤러 → 뷰 전체 흐름 검증 부재

#### 2.3 인터셉터 로직 순서 문제
```java
// 수정 전: 문제 있는 순서
if (isPublicPath(uri)) {  // "/"가 true 반환
    return true;  // 바로 종료 -> 리다이렉트 로직 실행 안 됨
}
// ... 인증 확인 및 리다이렉트 로직 도달 불가
```

```java
// 수정 후: 올바른 순서
// 1. 정적 리소스 먼저 체크
// 2. API 경로 체크
// 3. 공개 경로 체크 (단, 로그인된 사용자 처리 추가)
// 4. 인증 확인
// 5. 강제 리다이렉트 로직
```

#### 2.4 보안 설정 누락
- `/kindergarten/create`가 `SecurityConfig.permitAll()`에 누락
- 인터셉터에서도 제외 경로에 누락

---

## 해결 과정

### 1. 문제 추적
1. 사용자: "모든 메뉴가 에러"
2. 에러 로그 확인: `/kindergarten/create` 요청 실패
3. `RoleRedirectInterceptor` 발견
4. 누락된 컨트롤러/템플릿 확인

### 2. 해결 조치
| 파일 | 작업 |
|------|------|
| `KindergartenViewController.java` | 신규 생성 |
| `kindergarten/create.html` | 신규 생성 |
| `kindergarten/select.html` | 신규 생성 |
| `SecurityConfig.java` | 경로 허용 추가 |
| `WebMvcConfig.java` | 인터셉터 제외 경로 추가 |
| `RoleRedirectInterceptor.java` | 로직 순서 수정 |

### 3. 검증
- `ViewEndpointTest` 통합 테스트 작성
- 수동 테스트: 로그인 → 유치원 생성 → 홈 접근

---

## 배운 점

### 1. 뷰 레이어는 API보다 먼저 또는 함께 개발해야 한다
- 인터셉터, 필터 등 하위 레이어에서 참조하는 것은 먼저 만들어야 함
- 또는 레퍼런스가 없을 때 리다이렉트하지 않도록 안전하게 구현

### 2. 통합 테스트의 중요성
- 단위 테스트로는 인터셉터 → 컨트롤러 연결 문제를 찾을 수 없음
- 로그인 흐름 전체를 테스트하는 통합 테스트 필수

### 3. 인터셉터 로직 순서가 중요하다
- 공개 경로 먼저 체크하면 강제 리다이렉트가 동작하지 않음
- 체크 순서를 신중하게 설계해야 함

### 4. 에러 메시지를 정확히 읽자
- `NoResourceFoundException`은 컨트롤러가 없다는 뜻
- 인터셉터가 리다이렉트하는 곳이 컨트롤러가 있는지 확인해야 함

---

## 앞으로의 개선 방향

### 1. 기능 개발 프로세스 개선

#### ✅ 뷰 → 컨트롤러 → 서비스 순서 준수
```
1. 뷰 템플릿 (HTML)
2. 뷰 컨트롤러 (@Controller)
3. API 컨트롤러 (@RestController)
4. 서비스
5. 리포지토리
```

#### ✅ 하위 레이어에서 참조하는 것 먼저 개발
- 인터셉터가 리다이렉트하는 페이지는 필수적으로 먼저 구현
- 또는 리다이렉트 대상이 없을 때 안전하게 처리

#### ✅ 통합 테스트 작성
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationFlowTest {
    // 로그인 → 인터셉터 → 리다이렉트 → 뷰 렌더링 전체 흐름 테스트
}
```

### 2. 안전한 인터셉터 구현

#### ✅ 리다이렉트 대상 존재 확인
```java
private String shouldForceRedirect(Member member, String uri) {
    // 리다이렉트할 URL이 컨트롤러에 있는지 확인
    String redirectUrl = determineRedirectUrl(member, uri);
    if (redirectUrl != null && !isHandlerExists(redirectUrl)) {
        log.error("Redirect target not found: {}", redirectUrl);
        return null;  // 또는 기본 페이지로
    }
    return redirectUrl;
}
```

#### ✅ 순환 리다이렉트 방지
```java
// 리다이렉트 횟수 추적
private static final String REDIRECT_COUNT_ATTR = "redirectCount";
private static final int MAX_REDIRECTS = 3;

if (request.getSession().getAttribute(REDIRECT_COUNT_ATTR) != null) {
    int count = (int) request.getSession().getAttribute(REDIRECT_COUNT_ATTR);
    if (count > MAX_REDIRECTS) {
        log.error("Too many redirects detected");
        response.sendRedirect("/error?code=too_many_redirects");
        return false;
    }
    request.getSession().setAttribute(REDIRECT_COUNT_ATTR, count + 1);
}
```

### 3. 더 나은 에러 처리

#### ✅ GlobalExceptionHandler 추가
```java
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
    log.error("Resource not found: {}", e.getResourcePath(), e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
}
```

#### ✅ 개발 모드에서 상세 에러 표시
```java
if (isDevelopmentProfile()) {
    return "error/debug";  // 상세 에러 페이지
}
```

### 4. 영향 범위 최소화 원칙

#### ✅ 기능 추가 시 체크리스트
- [ ] 이 변경이 기존 인터셉터/필터에 영향을 주는가?
- [ ] 리다이렉트 대상 URL이 존재하는가?
- [ ] Security 설정에 허용이 필요한가?
- [ ] 통합 테스트가 통과하는가?
- [ ] 기존 기능 회귀가 없는가?

#### ✅ 브랜치 전략
- 뷰/컨트롤러 변경은 `feature/view-xxx` 브랜치
- 인터셉터/필터 변경은 `feature/security-xxx` 브랜치
- 각각 독립적으로 PR 및 리뷰

---

## 참고: 수정 전후 코드 비교

### RoleRedirectInterceptor.java

```diff
  @Override
  public boolean preHandle(...) {
      String uri = request.getRequestURI();

-     // 공개 경로는 통과
-     if (isPublicPath(uri)) {
-         return true;  // 문제: "/"가 여기서 바로 종료됨
-     }
-
-     // 정적 리소스는 통과
+     // 정적 리소스는 통과
      if (isStaticResource(uri)) {
          return true;
      }

      // API 엔드포인트는 통과
      if (isApiPath(uri)) {
          return true;
      }

+     // 인증 확인
+     boolean isAuthenticated = ...;
+
+     // 로그인/회원가입 페이지는 인증 없이 통과
+     if (isPublicPath(uri)) {
+         if (isAuthenticated && (uri.startsWith("/login") || uri.startsWith("/signup"))) {
+             response.sendRedirect("/");
+             return false;
+         }
+         return true;
+     }
+
+     // 인증되지 않은 사용자는 로그인 페이지로
+     if (!isAuthenticated) {
+         response.sendRedirect("/login");
+         return false;
+     }
+
      // 강제 리다이렉트 로직 (원장 유치원 없음 등)
      String redirectUrl = shouldForceRedirect(member, uri);
      if (redirectUrl != null) {
          response.sendRedirect(redirectUrl);
          return false;
      }

      return true;
  }
```

---

## 결론

이번 버그는 **"순차적 개발 부족 + 통합 테스트 부재 + 인터셉터 로직 순서 오류"** 3가지가 복합적으로 발생했습니다.

앞으로는:
1. **뷰 → 컨트롤러 → 서비스** 순서 준수
2. **통합 테스트** 필수 작성
3. **인터셉터 로직** 순서 신중 설계
4. **영향 범위 최소화** 위한 체크리스트 활용

이 원칙들을 지키면 유사한 버그를 예방할 수 있습니다.
