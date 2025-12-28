# 아이바요(AiBayo) 리빌딩 계획서

## 📋 목차
1. [리빌딩 전략 결정](#1-리빌딩-전략-결정)
2. [Phase 1: 기반 정비](#2-phase-1-기반-정비)
3. [Phase 2: 백엔드 리팩토링](#3-phase-2-백엔드-리팩토링)
4. [Phase 3: 프론트엔드 개선](#4-phase-3-프론트엔드-개선)
5. [Phase 4: 인프라 및 배포](#5-phase-4-인프라-및-배포)
6. [기술 스택 추천](#6-기술-스택-추천)
7. [작업 우선순위](#7-작업-우선순위)

---

## 1. 리빌딩 전략 결정

### 🎯 결론: 기존 프로젝트 수정 권장

#### 이유:
1. **도메인 로직 보존**: 유치원 ERP의 복잡한 비즈니스 로직(승인 시스템, 역할 기반 접근제어 등)이 이미 구현됨
2. **데이터베이스 스키마**: `data.sql`에 정의된 스키마를 재활용 가능
3. **시간 효율성**: 새로 만드는 것보다 리팩토링이 빠름
4. **학습 효과**: 레거시 코드 개선 경험은 실무에서 더 유용함

#### 전략:
```
점진적 리팩토링 (Strangler Fig Pattern)
├── 기존 코드 분석 및 이해
├── 새로운 구조로 하나씩 이전
├── 테스트 코드 작성하며 검증
└── 구버전 코드 제거
```

---

## 2. Phase 1: 기반 정비 (1-2주)

### 2.1 프로젝트 구조 재정비

#### 패키지 구조 변경
```
기존: com.aico.aibayo
변경: com.aibayo (그룹ID 정리)

com.aibayo/
├── AibayoApplication.java
├── global/                          # 전역 설정
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── QuerydslConfig.java
│   │   ├── WebConfig.java
│   │   └── S3Config.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── security/
│   │   ├── jwt/
│   │   └── oauth2/
│   └── common/
│       ├── BaseEntity.java          # 공통 엔티티 (생성일, 수정일)
│       └── BaseResponse.java        # 공통 응답 포맷
│
├── domain/                          # 도메인별 분리
│   ├── member/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── kindergarten/
│   ├── classroom/
│   ├── kid/
│   ├── attendance/
│   ├── announcement/
│   ├── notepad/
│   ├── meal/
│   ├── schedule/
│   └── medication/
│
└── infra/                           # 외부 연동
    ├── mail/
    └── storage/
```

### 2.2 build.gradle 정리

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.5'
}

group = 'com.aibayo'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Spring Boot Starters ===
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'  // 추가
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    
    // === Database ===
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    
    // === Security ===
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // === AWS (업데이트 권장) ===
    implementation platform('software.amazon.awssdk:bom:2.21.0')
    implementation 'software.amazon.awssdk:s3'
    
    // === Utilities ===
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'  // DTO 변환
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // === Development ===
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // === Test ===
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
    useJUnitPlatform()
}

// QueryDSL 설정
def querydslDir = "$buildDir/generated/querydsl"
sourceSets {
    main.java.srcDirs += [querydslDir]
}
tasks.withType(JavaCompile) {
    options.generatedSourceOutputDirectory = file(querydslDir)
}
clean.doLast {
    file(querydslDir).deleteDir()
}
```

### 2.3 환경 설정 분리

```yaml
# application.yml (공통)
spring:
  profiles:
    active: local
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

---
# application-local.yml (로컬 개발)
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:mysql://localhost:3306/aibayo?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: ${DB_PASSWORD}
  jpa:
    show-sql: true

logging:
  level:
    com.aibayo: DEBUG
    org.hibernate.SQL: DEBUG

---
# application-prod.yml (프로덕션)
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    com.aibayo: INFO
    org.hibernate.SQL: WARN
```

### 2.4 공통 클래스 작성

#### BaseEntity.java
```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

#### ErrorCode.java
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
    RESOURCE_NOT_FOUND(404, "C002", "리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다"),
    
    // Member
    MEMBER_NOT_FOUND(404, "M001", "회원을 찾을 수 없습니다"),
    DUPLICATE_EMAIL(400, "M002", "이미 사용중인 이메일입니다"),
    INVALID_PASSWORD(400, "M003", "비밀번호가 일치하지 않습니다"),
    
    // Authentication
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다"),
    ACCESS_DENIED(403, "A003", "접근 권한이 없습니다");
    
    private final int status;
    private final String code;
    private final String message;
}
```

#### GlobalExceptionHandler.java
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getErrorCode()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage());
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult()));
    }
}
```

---

## 3. Phase 2: 백엔드 리팩토링 (3-4주)

### 3.1 엔티티 개선

#### 기존 문제
```java
// 기존: 연관관계 없이 FK만 저장
@Column(name = "kinder_no")
private Long kinderNo;
```

#### 개선 방향
```java
// 개선: JPA 연관관계 매핑
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "kinder_no")
private Kindergarten kindergarten;
```

#### MemberEntity 개선 예시
```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String password;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kinder_no")
    private Kindergarten kindergarten;
    
    private String profilePicture;
    
    private LocalDateTime lastLoginAt;
    private LocalDateTime inactivatedAt;
    
    // === 생성 메서드 ===
    public static Member createMember(String email, String name, 
            String encodedPassword, String phone, MemberRole role) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.password = encodedPassword;
        member.phone = phone;
        member.role = role;
        member.status = MemberStatus.ACTIVE;
        return member;
    }
    
    // === 비즈니스 메서드 ===
    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }
    
    public void inactivate() {
        this.status = MemberStatus.INACTIVE;
        this.inactivatedAt = LocalDateTime.now();
    }
    
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
```

### 3.2 Repository 패턴 정리

#### 기존 문제
- JpaRepository + Custom 인터페이스 + CustomImpl 3개 파일
- QueryDSL 쿼리에서 직접 DTO 프로젝션

#### 개선 방향
```java
// MemberRepository.java
public interface MemberRepository extends JpaRepository<Member, Long>, 
        MemberRepositoryCustom {
    
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}

// MemberRepositoryCustom.java
public interface MemberRepositoryCustom {
    List<Member> findAllWithKindergarten(MemberSearchCondition condition);
    Page<Member> findAllWithPage(MemberSearchCondition condition, Pageable pageable);
}

// MemberRepositoryImpl.java (Impl로 통일)
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Member> findAllWithKindergarten(MemberSearchCondition condition) {
        return queryFactory
            .selectFrom(member)
            .leftJoin(member.kindergarten).fetchJoin()
            .where(
                eqStatus(condition.getStatus()),
                eqRole(condition.getRole())
            )
            .fetch();
    }
    
    private BooleanExpression eqStatus(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }
}
```

### 3.3 서비스 레이어 개선

#### 기존 문제
- Interface + Impl 구조 (단일 구현에 불필요)
- 트랜잭션 범위 불명확
- 비즈니스 로직과 인프라 로직 혼재

#### 개선 방향
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Long signUp(MemberSignUpRequest request) {
        validateDuplicateEmail(request.getEmail());
        
        Member member = Member.createMember(
            request.getEmail(),
            request.getName(),
            passwordEncoder.encode(request.getPassword()),
            request.getPhone(),
            request.getRole()
        );
        
        return memberRepository.save(member).getId();
    }
    
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
    
    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest request) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), 
                member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
    
    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
```

### 3.4 컨트롤러 분리

#### 기존 문제
- View 반환과 REST API가 혼재
- `@ResponseBody` 메서드와 일반 메서드 혼용

#### 개선 방향: API와 View 컨트롤러 분리
```java
// REST API 컨트롤러
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {
    
    private final MemberService memberService;
    
    @PostMapping
    public ResponseEntity<Long> signUp(@Valid @RequestBody MemberSignUpRequest request) {
        Long memberId = memberService.signUp(request);
        return ResponseEntity.created(URI.create("/api/v1/members/" + memberId))
            .body(memberId);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }
}

// View 컨트롤러
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberViewController {
    
    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new MemberSignUpForm());
        return "member/signup";
    }
    
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, 
            Model model) {
        model.addAttribute("member", userDetails.getMember());
        return "member/mypage";
    }
}
```

### 3.5 DTO 개선

#### 기존 문제
- 하나의 DTO에 너무 많은 생성자 (7개+)
- Entity와 DTO 변환 로직 분산

#### 개선 방향: Record 또는 MapStruct 사용
```java
// Request DTO (입력용)
public record MemberSignUpRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,
    
    @NotBlank(message = "이름은 필수입니다")
    String name,
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password,
    
    String phone,
    
    @NotNull(message = "역할은 필수입니다")
    MemberRole role
) {}

// Response DTO (출력용)
public record MemberResponse(
    Long id,
    String email,
    String name,
    String phone,
    MemberRole role,
    MemberStatus status,
    LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getEmail(),
            member.getName(),
            member.getPhone(),
            member.getRole(),
            member.getStatus(),
            member.getCreatedAt()
        );
    }
}
```

### 3.6 보안 개선

#### SecurityConfig 개선
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF: API는 비활성화, View는 활성화 고려
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            
            // 세션: Stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인가 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스
                .requestMatchers("/css/**", "/js/**", "/images/**", "/vendor/**").permitAll()
                // 공개 API
                .requestMatchers("/api/v1/auth/**", "/member/**").permitAll()
                // 관리자 API
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "PRINCIPAL")
                // 교사 API
                .requestMatchers("/api/v1/teacher/**").hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER")
                // 그 외 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            
            // OAuth2 로그인
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/member/signIn")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )
            
            // 로그아웃
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("jwt")
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 4. Phase 3: 프론트엔드 개선 (2-3주)

### 4.1 CSS 구조 개선

#### 디렉토리 구조
```
static/css/
├── base/
│   ├── reset.css           # CSS 리셋
│   ├── variables.css       # CSS 변수 (색상, 폰트 등)
│   └── typography.css      # 타이포그래피
├── components/
│   ├── button.css
│   ├── card.css
│   ├── modal.css
│   ├── form.css
│   └── table.css
├── layout/
│   ├── header.css
│   ├── footer.css
│   └── sidebar.css
├── pages/
│   ├── main.css
│   ├── attendance.css
│   └── ...
└── main.css               # 진입점 (모든 CSS import)
```

#### CSS 변수 활용
```css
/* variables.css */
:root {
    /* Colors */
    --color-primary: #FFDB21;
    --color-primary-dark: #E5C400;
    --color-secondary: #4A90A4;
    --color-success: #28A745;
    --color-warning: #FFC107;
    --color-danger: #DC3545;
    
    /* Neutrals */
    --color-white: #FFFFFF;
    --color-gray-100: #F8F9FA;
    --color-gray-200: #E9ECEF;
    --color-gray-500: #6C757D;
    --color-gray-800: #343A40;
    --color-black: #000000;
    
    /* Typography */
    --font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-size-xs: 0.75rem;
    --font-size-sm: 0.875rem;
    --font-size-md: 1rem;
    --font-size-lg: 1.125rem;
    --font-size-xl: 1.25rem;
    
    /* Spacing */
    --spacing-xs: 0.25rem;
    --spacing-sm: 0.5rem;
    --spacing-md: 1rem;
    --spacing-lg: 1.5rem;
    --spacing-xl: 2rem;
    
    /* Border Radius */
    --radius-sm: 4px;
    --radius-md: 8px;
    --radius-lg: 12px;
    --radius-full: 9999px;
    
    /* Shadow */
    --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
    --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
    --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

### 4.2 JavaScript 모듈화

#### ES 모듈 구조
```
static/js/
├── utils/
│   ├── api.js              # fetch 래퍼
│   ├── dom.js              # DOM 유틸리티
│   ├── format.js           # 날짜/숫자 포맷
│   └── validate.js         # 유효성 검사
├── components/
│   ├── Modal.js
│   ├── Toast.js
│   ├── DatePicker.js
│   └── Pagination.js
├── pages/
│   ├── main.js
│   ├── attendance.js
│   └── ...
└── app.js                  # 메인 진입점
```

#### API 래퍼 예시
```javascript
// utils/api.js
class ApiClient {
    constructor(baseUrl = '/api/v1') {
        this.baseUrl = baseUrl;
    }
    
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const error = await response.json();
                throw new ApiError(error.message, response.status, error.code);
            }
            
            return response.json();
        } catch (error) {
            if (error instanceof ApiError) throw error;
            throw new ApiError('네트워크 오류가 발생했습니다', 0, 'NETWORK_ERROR');
        }
    }
    
    get(endpoint, params = {}) {
        const query = new URLSearchParams(params).toString();
        const url = query ? `${endpoint}?${query}` : endpoint;
        return this.request(url, { method: 'GET' });
    }
    
    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

export const api = new ApiClient();
```

### 4.3 Thymeleaf 레이아웃 개선

#### layout.html 개선
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="아이바요 - 유치원 통합 관리 시스템">
    
    <title layout:title-pattern="$CONTENT_TITLE - 아이바요">아이바요</title>
    
    <!-- CSS -->
    <link rel="stylesheet" th:href="@{/css/main.css}">
    <th:block layout:fragment="css"></th:block>
</head>
<body>
    <!-- 헤더 -->
    <th:block th:replace="~{fragments/header :: header}"></th:block>
    
    <!-- 메인 컨텐츠 -->
    <main class="main-content">
        <th:block layout:fragment="content"></th:block>
    </main>
    
    <!-- 푸터 -->
    <th:block th:replace="~{fragments/footer :: footer}"></th:block>
    
    <!-- 공통 모달 -->
    <th:block th:replace="~{fragments/modal :: modal}"></th:block>
    
    <!-- JavaScript -->
    <script th:src="@{/js/app.js}" type="module"></script>
    <th:block layout:fragment="scripts"></th:block>
</body>
</html>
```

---

## 5. Phase 4: 인프라 및 배포 (1-2주)

### 5.1 Docker 설정

#### Dockerfile
```dockerfile
# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://db:3306/aibayo
      - DB_USERNAME=aibayo
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - db
    networks:
      - aibayo-network

  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=aibayo
      - MYSQL_USER=aibayo
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - aibayo-network

volumes:
  mysql-data:

networks:
  aibayo-network:
```

### 5.2 AWS 배포 구성 (업그레이드)

```
현재: spring-cloud-starter-aws (deprecated)
변경: AWS SDK v2 사용

배포 아키텍처:
┌──────────────────────────────────────────────────────┐
│                      AWS Cloud                        │
│  ┌─────────────┐      ┌─────────────┐               │
│  │   Route 53  │─────▶│ CloudFront  │               │
│  └─────────────┘      └──────┬──────┘               │
│                              │                        │
│                     ┌────────▼────────┐              │
│                     │     ALB         │              │
│                     └────────┬────────┘              │
│                              │                        │
│            ┌─────────────────┼─────────────────┐     │
│            │                 │                 │     │
│     ┌──────▼──────┐   ┌──────▼──────┐        │     │
│     │ ECS Fargate │   │ ECS Fargate │  ...   │     │
│     └──────┬──────┘   └──────┬──────┘        │     │
│            │                 │                │     │
│            └────────┬────────┘                │     │
│                     │                         │     │
│              ┌──────▼──────┐   ┌─────────┐   │     │
│              │   RDS MySQL  │   │   S3    │   │     │
│              └─────────────┘   └─────────┘   │     │
└──────────────────────────────────────────────────────┘
```

---

## 6. 기술 스택 추천

### 6.1 최종 기술 스택

| 분류 | 기술 | 버전 | 비고 |
|------|------|------|------|
| **Language** | Java | 17 LTS | 유지 |
| **Framework** | Spring Boot | 3.3.x | 유지 |
| **ORM** | Spring Data JPA + QueryDSL | 5.0.0 | 유지 |
| **Security** | Spring Security + JWT | - | 유지 |
| **Database** | MySQL | 8.0 | 유지 |
| **Template** | Thymeleaf | - | 유지 |
| **Build** | Gradle | 8.x | 업그레이드 |
| **Cloud** | AWS SDK v2 | 2.21.x | 업그레이드 |
| **Container** | Docker + Docker Compose | - | 추가 |
| **Test** | JUnit 5 + Mockito | - | 강화 |

### 6.2 추가 고려 라이브러리

| 라이브러리 | 용도 | 필수 여부 |
|-----------|------|----------|
| MapStruct | DTO 변환 자동화 | 권장 |
| Flyway/Liquibase | DB 마이그레이션 | 권장 |
| Testcontainers | 통합 테스트 | 권장 |
| Spring REST Docs | API 문서화 | 선택 |
| Resilience4j | 서킷브레이커 | 선택 |

---

## 7. 작업 우선순위

### 7.1 필수 작업 (Must Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 1 | 환경 설정 분리 (application.yml) | 2시간 | ⭐⭐⭐⭐⭐ |
| 2 | 전역 예외 처리 구현 | 4시간 | ⭐⭐⭐⭐⭐ |
| 3 | 엔티티 연관관계 매핑 | 1일 | ⭐⭐⭐⭐⭐ |
| 4 | 테스트 코드 작성 | 2일 | ⭐⭐⭐⭐⭐ |
| 5 | API/View 컨트롤러 분리 | 2일 | ⭐⭐⭐⭐ |
| 6 | 서비스 레이어 리팩토링 | 2일 | ⭐⭐⭐⭐ |
| 7 | 보안 설정 개선 | 1일 | ⭐⭐⭐⭐ |

### 7.2 권장 작업 (Should Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 8 | CSS 구조 개선 | 1일 | ⭐⭐⭐ |
| 9 | JavaScript 모듈화 | 1일 | ⭐⭐⭐ |
| 10 | DTO Record 전환 | 1일 | ⭐⭐⭐ |
| 11 | Docker 환경 구성 | 4시간 | ⭐⭐⭐ |
| 12 | AWS SDK 업그레이드 | 4시간 | ⭐⭐⭐ |

### 7.3 선택 작업 (Nice to Have)

| 순번 | 작업 | 예상 시간 | 중요도 |
|------|------|----------|--------|
| 13 | MapStruct 도입 | 4시간 | ⭐⭐ |
| 14 | API 문서화 (REST Docs) | 1일 | ⭐⭐ |
| 15 | DB 마이그레이션 도구 도입 | 4시간 | ⭐⭐ |
| 16 | 로깅 체계 개선 | 4시간 | ⭐⭐ |

---

## 체크리스트

### Phase 1 완료 조건
- [ ] 패키지 구조 재정비 완료
- [ ] build.gradle 정리 완료
- [ ] 환경 설정 분리 완료 (local/prod)
- [ ] 공통 클래스 작성 완료 (BaseEntity, ErrorCode 등)

### Phase 2 완료 조건
- [ ] 핵심 엔티티 JPA 연관관계 매핑 완료
- [ ] Repository 패턴 통일
- [ ] 서비스 레이어 리팩토링 완료
- [ ] API/View 컨트롤러 분리 완료
- [ ] 테스트 커버리지 50% 이상

### Phase 3 완료 조건
- [ ] CSS 변수 및 구조 개선
- [ ] JavaScript 모듈화
- [ ] Thymeleaf 레이아웃 정리

### Phase 4 완료 조건
- [ ] Docker 환경 구성
- [ ] CI/CD 파이프라인 구축
- [ ] AWS 배포 완료

---

## 마무리

이 계획서는 기존 코드를 점진적으로 개선하는 방식으로 작성되었습니다. 
한 번에 모든 것을 바꾸려 하지 말고, 우선순위가 높은 작업부터 차근차근 진행하세요.

**핵심 원칙:**
1. 테스트 먼저 작성 후 리팩토링
2. 작은 커밋, 자주 커밋
3. 기능 단위로 브랜치 분리
4. 코드 리뷰 습관화 (혼자라도 PR 작성)

화이팅! 🚀

