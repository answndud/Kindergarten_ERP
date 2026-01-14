# 2025-01-14: Chrome DevTools 자동 요청 에러 로그 회고

## 문제 개요

**증상:** 로그인 후 홈 페이지 접근 시 `.well-known/appspecific/com.chrome.devtools.json` 리소스 404 에러가 로그에 ERROR 레벨로 반복 출력

**영향:** 
- 애플리케이션 기능은 정상 작동
- 로그가 지저분해져 실제 에러 추적 어려움
- 운영 모니터링 시 false positive 발생

---

## 문제 원인

### 1. 직접적 원인

Chrome 브라우저가 자동으로 요청하는 메타데이터 파일이 서버에 없어서 `NoResourceFoundException` 발생

```
Request URL: /.well-known/appspecific/com.chrome.devtools.json
Status: 404 Not Found
```

### 2. 근본적 원인

#### 2.1 GlobalExceptionHandler의 일괄 처리
모든 예외를 동일한 레벨(ERROR)로 로깅하여 실제 문제와 브라우저 자동 요청을 구분하지 못함

```java
// 수정 전
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
    log.error("Exception: {}", e.getMessage(), e);  // 모든 에러가 ERROR
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
}
```

#### 2.2 예외 타입별 핸들러 부재
- `NoResourceFoundException`에 대한 전용 핸들러가 없었음
- 정적 리소스 누락과 브라우저 자동 요청을 구분하지 못함

#### 2.3 로그 레벨 설계 부족
- 브라우저 자동 요청: DEBUG 레벨이 적합 (개발 시에만 확인)
- 실제 리소스 누락: WARN 레벨이 적합 (운영팀에 알림)
- 심각한 서버 에러: ERROR 레벨 (즉시 대응 필요)

---

## 해결 과정

### 1. 문제 추적
1. 사용자: "페이지는 정상인데 에러 메시지가 떠요"
2. 터미널 로그 확인: `.well-known/appspecific/com.chrome.devtools.json` 404
3. Chrome DevTools 자동 요청임을 확인
4. GlobalExceptionHandler의 로그 레벨 문제 발견

### 2. 해결 조치

| 파일 | 작업 |
|------|------|
| `GlobalExceptionHandler.java` | `NoResourceFoundException` 핸들러 추가 |
| `GlobalExceptionHandler.java` | 브라우저 자동 요청 DEBUG 레벨 처리 |
| `GlobalExceptionHandler.java` | URI 기반 로그 레벨 분기 |

### 3. 구현 내용

```java
/**
 * 정적 리소스를 찾을 수 없는 경우 (Chrome DevTools 등)
 */
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<ApiResponse<?>> handleNoResourceFound(
        NoResourceFoundException e, 
        HttpServletRequest request) {
    
    String uri = request.getRequestURI();
    
    // .well-known 등 브라우저 자동 요청은 디버그 레벨로만 기록
    if (uri.contains(".well-known") || uri.contains("devtools")) {
        log.debug("Browser auto-request ignored: {}", uri);
    } else {
        log.warn("Resource not found: {}", uri);
    }
    
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND));
}

/**
 * 그 외 모든 예외 처리
 */
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<?>> handleException(
        Exception e, 
        HttpServletRequest request) {
    
    String uri = request.getRequestURI();
    
    // 특정 경로의 에러는 로그 레벨 조정
    if (uri.contains(".well-known") || uri.contains("devtools")) {
        log.debug("Exception in browser auto-request: {}", e.getMessage());
    } else {
        log.error("Exception: {}", e.getMessage(), e);
    }
    
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
}
```

### 4. 검증
- 애플리케이션 재시작
- 브라우저로 홈 페이지 접근
- 로그 확인: ERROR 레벨 로그 사라짐
- DEBUG 레벨로 변경하면 여전히 확인 가능

---

## 배운 점

### 1. 로그 레벨은 중요도를 반영해야 한다

| 레벨 | 용도 | 예시 |
|------|------|------|
| ERROR | 즉시 대응 필요한 심각한 에러 | DB 연결 실패, 트랜잭션 롤백 |
| WARN | 주의가 필요하지만 서비스는 가능 | 실제 리소스 누락, 느린 쿼리 |
| INFO | 중요한 비즈니스 로직 실행 | 회원가입, 주문 생성 |
| DEBUG | 개발/디버깅용 상세 정보 | 브라우저 자동 요청, 파라미터 값 |

### 2. 예외 타입별 핸들러를 세분화하라
- `Exception` 하나로 모든 예외를 처리하면 문맥을 잃음
- `NoResourceFoundException`, `AccessDeniedException` 등 타입별로 분리
- 각 타입에 적합한 로그 레벨과 응답 코드 사용

### 3. 브라우저 자동 요청은 흔한 일이다
- `.well-known/`: ACME, Apple App Site Association 등
- `favicon.ico`: 파비콘
- `robots.txt`: 크롤러 제어
- `sitemap.xml`: SEO

이런 요청들은 ERROR가 아니라 DEBUG나 무시해도 됨

### 4. 운영 환경에서는 로그 노이즈가 치명적이다
- 실제 에러가 묻힐 수 있음
- 모니터링 알람이 무의미해짐
- 디스크 공간 낭비

---

## 앞으로의 개선 방향

### 1. 로그 레벨 가이드라인 수립

#### ✅ ERROR: 즉시 대응 필요
```java
// DB 연결 실패
log.error("Database connection failed", e);

// 트랜잭션 롤백
log.error("Transaction rollback: {}", e.getMessage(), e);

// 외부 API 호출 실패 (결제, 알림 등)
log.error("Payment API call failed", e);
```

#### ✅ WARN: 주의 필요
```java
// 실제 리소스 누락
log.warn("Resource not found: {}", uri);

// 느린 쿼리
log.warn("Slow query detected: {}ms", duration);

// 비정상적인 요청
log.warn("Invalid request parameter: {}", param);
```

#### ✅ INFO: 비즈니스 로직
```java
// 중요 작업 시작/완료
log.info("회원가입 성공 - id: {}", memberId);
log.info("공지사항 작성 성공 - id: {}", announcementId);
```

#### ✅ DEBUG: 개발/디버깅
```java
// 브라우저 자동 요청
log.debug("Browser auto-request ignored: {}", uri);

// 파라미터 값 확인
log.debug("Request params: kindergartenId={}, title={}", id, title);
```

### 2. 예외 핸들러 체계화

#### ✅ 우선순위 순서
```java
// 1. 구체적인 예외 먼저 (NoResourceFoundException)
// 2. 도메인 예외 (BusinessException)
// 3. Spring 기본 예외 (MethodArgumentNotValidException)
// 4. 일반 예외 (Exception)
```

#### ✅ URI 기반 분기
```java
private boolean isBrowserAutoRequest(String uri) {
    return uri.contains(".well-known") || 
           uri.contains("devtools") ||
           uri.equals("/favicon.ico") ||
           uri.equals("/robots.txt");
}
```

### 3. 개발/운영 환경 구분

#### ✅ 개발 환경: 상세 로그
```yaml
# application-local.yml
logging:
  level:
    com.erp: DEBUG
    org.springframework.web: DEBUG
```

#### ✅ 운영 환경: 필수 로그만
```yaml
# application-prod.yml
logging:
  level:
    com.erp: INFO
    org.springframework.web: WARN
```

### 4. 로그 모니터링 알람 설정

#### ✅ Sentry, CloudWatch 등 연동
```java
// ERROR 레벨만 알람 전송
if (log.isErrorEnabled()) {
    sentryService.captureException(e);
}
```

#### ✅ 특정 에러 무시
```yaml
# Sentry ignore patterns
ignore_errors:
  - NoResourceFoundException
  - *.well-known*
  - *devtools*
```

---

## 참고: Chrome 브라우저 자동 요청 목록

### 1. `.well-known/` 경로
| 파일 | 용도 |
|------|------|
| `.well-known/appspecific/com.chrome.devtools.json` | Chrome DevTools 메타데이터 |
| `.well-known/apple-app-site-association` | iOS Universal Links |
| `.well-known/assetlinks.json` | Android App Links |
| `.well-known/acme-challenge/` | Let's Encrypt SSL 인증 |

### 2. 기타 자동 요청
| 파일 | 용도 |
|------|------|
| `/favicon.ico` | 파비콘 |
| `/robots.txt` | 크롤러 제어 |
| `/sitemap.xml` | 검색 엔진 최적화 |
| `/manifest.json` | PWA 매니페스트 |

### 3. 대응 방법
```java
// SecurityConfig.java
.requestMatchers(
    "/.well-known/**",
    "/favicon.ico",
    "/robots.txt",
    "/sitemap.xml",
    "/manifest.json"
).permitAll()
```

또는 존재하지 않는 파일은 조용히 404 반환

---

## 결론

이번 문제는 **"로그 레벨 설계 부족 + 예외 타입별 핸들러 부재"**로 발생했습니다.

브라우저가 자동으로 요청하는 파일들은:
1. **서비스에 영향 없음** → ERROR 아님
2. **흔한 일** → DEBUG나 무시
3. **운영 모니터링 방해** → 반드시 분리

앞으로는:
1. **로그 레벨 가이드라인** 수립 및 준수
2. **예외 타입별 핸들러** 세분화
3. **브라우저 자동 요청** DEBUG 처리
4. **운영 환경 로그** 최소화

이 원칙들을 지키면 깨끗한 로그와 효과적인 모니터링이 가능합니다.

---

## 추가 개선 사항 (향후)

### 1. 정적 파일 실제 제공
```
src/main/resources/static/
  ├── .well-known/
  │   └── (필요한 메타데이터)
  ├── favicon.ico
  ├── robots.txt
  └── sitemap.xml
```

### 2. 로그 집계 도구 도입
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Grafana Loki
- AWS CloudWatch Logs Insights

### 3. 알람 규칙 설정
```
ERROR 로그 발생 → 즉시 Slack 알람
WARN 로그 10회 이상/분 → Slack 알람
DEBUG 로그 → 로컬 파일만 저장
```

이렇게 하면 실제 문제만 즉시 파악하고 대응할 수 있습니다.
