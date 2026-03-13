# 🚀 Spring Boot 프로젝트 구현 가이드

> Spring Boot 프로젝트를 처음부터 구현하는 순서와 패턴을 정리한 튜토리얼입니다.
> 이 가이드를 따라가면 어떤 Spring Boot 프로젝트든 체계적으로 구현할 수 있습니다.

---

## 📋 목차

1. [프로젝트 생성](#1-프로젝트-생성)
2. [프로젝트 구조 설계](#2-프로젝트-구조-설계)
3. [설정 파일 구성](#3-설정-파일-구성)
4. [공통 컴포넌트 구현](#4-공통-컴포넌트-구현)
5. [도메인 구현 순서](#5-도메인-구현-순서)
6. [테스트 코드 작성](#6-테스트-코드-작성)
7. [보안 구현](#7-보안-구현-spring-security--jwt)
8. [코드 패턴 레퍼런스](#8-코드-패턴-레퍼런스)

---

## 1. 프로젝트 생성

### 1.1 Spring Initializr 사용

**URL**: https://start.spring.io/

**기본 설정**:
| 항목 | 권장값 |
|------|--------|
| Project | Gradle - Groovy |
| Language | Java |
| Spring Boot | 최신 안정 버전 (3.x) |
| Packaging | Jar |
| Java | 17 (LTS) |

### 1.2 필수 의존성

```groovy
// 웹 애플리케이션
implementation 'org.springframework.boot:spring-boot-starter-web'

// 데이터베이스 (JPA)
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

// 검증
implementation 'org.springframework.boot:spring-boot-starter-validation'

// 보안
implementation 'org.springframework.boot:spring-boot-starter-security'

// 개발 편의
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

// 데이터베이스 드라이버 (선택)
runtimeOnly 'com.mysql:mysql-connector-j'        // MySQL
runtimeOnly 'org.postgresql:postgresql'          // PostgreSQL
runtimeOnly 'com.h2database:h2'                  // H2 (테스트용)
```

### 1.3 추가 의존성 (필요시)

```groovy
// QueryDSL (동적 쿼리)
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

// Redis
implementation 'org.springframework.boot:spring-boot-starter-data-redis'

// Flyway (DB 마이그레이션)
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'  // MySQL용

// Swagger (API 문서화)
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

---

## 2. 프로젝트 구조 설계

### 2.1 패키지 구조 (도메인형)

```
src/main/java/com/{company}/{project}/
├── {Project}Application.java          # 메인 클래스
│
├── global/                              # 전역 설정
│   ├── config/                         # 설정 클래스
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   ├── QuerydslConfig.java
│   │   └── WebConfig.java
│   ├── exception/                      # 예외 처리
│   │   ├── ErrorCode.java
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── common/                         # 공통 클래스
│   │   ├── BaseEntity.java
│   │   └── ApiResponse.java
│   ├── security/                       # 보안 관련
│   │   ├── jwt/
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   └── util/                           # 유틸리티
│
└── domain/                              # 도메인별 패키지
    ├── member/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    ├── order/
    │   └── ...
    └── product/
        └── ...
```

### 2.2 패키지 구조 (계층형) - 대안

```
src/main/java/com/{company}/{project}/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── config/
└── exception/
```

> **권장**: 도메인형 구조 (규모가 커질수록 관리 용이)

### 2.3 네이밍 컨벤션

| 패키지 | 클래스 네이밍 | 예시 |
|--------|--------------|------|
| controller | `*Controller`, `*ApiController` | `MemberApiController` |
| service | `*Service` | `MemberService` |
| repository | `*Repository` | `MemberRepository` |
| entity | 단수형 | `Member`, `Order` |
| dto | `*Request`, `*Response` | `MemberCreateRequest` |
| config | `*Config` | `SecurityConfig` |
| exception | `*Exception` | `BusinessException` |

---

## 3. 설정 파일 구성

### 3.1 application.yml 구조

```yaml
# application.yml (공통 설정)
spring:
  application:
    name: my-application
  
  profiles:
    active: local  # 기본 프로파일

  jpa:
    open-in-view: false  # 성능 최적화 (권장)
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100

server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      force: true
```

### 3.2 환경별 설정 분리

```yaml
# application-local.yml (로컬 개발)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create  # 개발: create, 운영: none
    show-sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
```

```yaml
# application-prod.yml (운영)
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

logging:
  level:
    root: WARN
```

### 3.3 환경변수 사용 패턴

```yaml
# 기본값 설정
spring:
  datasource:
    username: ${DB_USERNAME:root}  # 환경변수 없으면 root 사용
```

---

## 4. 공통 컴포넌트 구현

### 4.1 구현 순서

```
1. BaseEntity          → 모든 엔티티의 공통 필드
2. ErrorCode           → 에러 코드 정의
3. BusinessException   → 비즈니스 예외
4. GlobalExceptionHandler → 전역 예외 처리
5. ApiResponse         → 공통 응답 형식
6. Config 클래스들      → JPA, QueryDSL, Security 등
```

### 4.2 BaseEntity

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**활성화**: `@EnableJpaAuditing` 설정 필요

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

### 4.3 ErrorCode

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
    ENTITY_NOT_FOUND(404, "C002", "엔티티를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다"),
    
    // Domain specific
    MEMBER_NOT_FOUND(404, "M001", "회원을 찾을 수 없습니다"),
    EMAIL_ALREADY_EXISTS(409, "M002", "이미 사용 중인 이메일입니다");
    
    private final int status;
    private final String code;
    private final String message;
}
```

### 4.4 BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### 4.5 GlobalExceptionHandler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(e.getErrorCode()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

### 4.6 ApiResponse

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    private String code;
    
    private ApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null, null);
    }
    
    public static ApiResponse<?> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getMessage(), errorCode.getCode());
    }
    
    public static ApiResponse<?> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode.getCode());
    }
}
```

---

## 5. 도메인 구현 순서

### 5.1 한 도메인 구현 순서

```
1. Entity (+ Enum)     → 데이터 모델 정의
2. Repository          → 데이터 접근 계층
3. DTO                 → 요청/응답 객체
4. Service             → 비즈니스 로직
5. Controller          → API 엔드포인트
6. Test                → 단위/통합 테스트
```

### 5.2 Entity 작성

```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자
public class Member extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)  // Enum은 STRING으로
    @Column(nullable = false, length = 20)
    private MemberRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)  // 연관관계는 LAZY
    @JoinColumn(name = "team_id")
    private Team team;
    
    // === 정적 팩토리 메서드 ===
    public static Member create(String email, String password, String name, MemberRole role) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.role = role;
        return member;
    }
    
    // === 비즈니스 메서드 (상태 변경) ===
    public void updateProfile(String name) {
        this.name = name;
    }
    
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}
```

**Entity 작성 규칙**:
- `@NoArgsConstructor(access = PROTECTED)` 사용
- Setter 사용 금지 → 비즈니스 메서드로 상태 변경
- 연관관계는 `FetchType.LAZY` 기본
- Enum은 `@Enumerated(EnumType.STRING)` 사용
- 정적 팩토리 메서드로 생성

### 5.3 Repository 작성

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 쿼리 메서드
    Optional<Member> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Member> findByRoleOrderByCreatedAtDesc(MemberRole role);
    
    // JPQL
    @Query("SELECT m FROM Member m WHERE m.team.id = :teamId")
    List<Member> findByTeamId(@Param("teamId") Long teamId);
}
```

**QueryDSL 사용 시**:

```java
// 인터페이스
public interface MemberRepositoryCustom {
    Page<Member> searchMembers(String keyword, MemberRole role, Pageable pageable);
}

// 구현체
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Member> searchMembers(String keyword, MemberRole role, Pageable pageable) {
        QMember member = QMember.member;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if (StringUtils.hasText(keyword)) {
            builder.and(member.name.contains(keyword)
                .or(member.email.contains(keyword)));
        }
        
        if (role != null) {
            builder.and(member.role.eq(role));
        }
        
        List<Member> content = queryFactory
            .selectFrom(member)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(member.createdAt.desc())
            .fetch();
        
        Long total = queryFactory
            .select(member.count())
            .from(member)
            .where(builder)
            .fetchOne();
        
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
```

### 5.4 DTO 작성

**Request DTO**:
```java
@Getter
@NoArgsConstructor
public class MemberCreateRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
}
```

**Response DTO**:
```java
@Getter
@Builder
public class MemberResponse {
    
    private Long id;
    private String email;
    private String name;
    private MemberRole role;
    private LocalDateTime createdAt;
    
    // 정적 팩토리 메서드
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .name(member.getName())
            .role(member.getRole())
            .createdAt(member.getCreatedAt())
            .build();
    }
}
```

### 5.5 Service 작성

```java
@Service
@Transactional(readOnly = true)  // 기본 읽기 전용
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 조회 (읽기 전용)
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        return MemberResponse.from(member);
    }
    
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
            .map(MemberResponse::from)
            .collect(Collectors.toList());
    }
    
    // 생성 (쓰기)
    @Transactional
    public Long create(MemberCreateRequest request) {
        // 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 엔티티 생성
        Member member = Member.create(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            MemberRole.USER
        );
        
        // 저장
        Member saved = memberRepository.save(member);
        
        return saved.getId();
    }
    
    // 수정 (쓰기)
    @Transactional
    public void update(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.updateProfile(request.getName());
    }
    
    // 삭제 (쓰기)
    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        memberRepository.delete(member);
    }
}
```

**Service 작성 규칙**:
- 클래스에 `@Transactional(readOnly = true)` 적용
- 변경 메서드만 `@Transactional` 오버라이드
- 조회 시 DTO로 변환하여 반환
- 예외는 `BusinessException` 사용

### 5.6 Controller 작성

```java
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {
    
    private final MemberService memberService;
    
    // GET /api/v1/members
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> findAll() {
        List<MemberResponse> members = memberService.findAll();
        return ResponseEntity.ok(ApiResponse.success(members));
    }
    
    // GET /api/v1/members/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        MemberResponse member = memberService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(member));
    }
    
    // POST /api/v1/members
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody MemberCreateRequest request) {
        
        Long id = memberService.create(request);
        
        return ResponseEntity
            .created(URI.create("/api/v1/members/" + id))
            .body(ApiResponse.success(id));
    }
    
    // PUT /api/v1/members/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        
        memberService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // DELETE /api/v1/members/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
```

**Controller 작성 규칙**:
- `@Valid`로 요청 검증
- `ResponseEntity`로 HTTP 상태 코드 명시
- `ApiResponse`로 일관된 응답 형식
- POST 성공 시 201 Created + Location 헤더

---

## 6. 테스트 코드 작성

### 6.1 테스트 종류

| 종류 | 어노테이션 | 용도 |
|------|-----------|------|
| 단위 테스트 | `@ExtendWith(MockitoExtension.class)` | Service 로직 |
| Repository 테스트 | `@DataJpaTest` | 쿼리 검증 |
| 통합 테스트 | `@SpringBootTest` | 전체 흐름 |

### 6.2 Service 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private MemberService memberService;
    
    @Test
    @DisplayName("회원 조회 성공")
    void findById_Success() {
        // given
        Member member = createMember(1L, "test@test.com", "테스트");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        
        // when
        MemberResponse response = memberService.findById(1L);
        
        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }
    
    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않음")
    void findById_NotFound() {
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> memberService.findById(1L))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }
    
    @Test
    @DisplayName("회원 생성 성공")
    void create_Success() {
        // given
        MemberCreateRequest request = new MemberCreateRequest("test@test.com", "password", "테스트");
        
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(createMember(1L, "test@test.com", "테스트"));
        
        // when
        Long memberId = memberService.create(request);
        
        // then
        assertThat(memberId).isEqualTo(1L);
        verify(memberRepository).save(any(Member.class));
    }
}
```

### 6.3 Repository 테스트

```java
@DataJpaTest
@Import(QuerydslConfig.class)  // QueryDSL 사용 시
class MemberRepositoryTest {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private TestEntityManager em;
    
    @Test
    @DisplayName("이메일로 회원 조회")
    void findByEmail() {
        // given
        Member member = Member.create("test@test.com", "password", "테스트", MemberRole.USER);
        em.persist(member);
        em.flush();
        em.clear();
        
        // when
        Optional<Member> found = memberRepository.findByEmail("test@test.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트");
    }
}
```

### 6.4 통합 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Test
    @DisplayName("회원 생성 API")
    void createMember() throws Exception {
        // given
        MemberCreateRequest request = new MemberCreateRequest("test@test.com", "password123", "테스트");
        
        // when & then
        mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isNumber());
        
        // DB 확인
        assertThat(memberRepository.existsByEmail("test@test.com")).isTrue();
    }
}
```

---

## 7. 보안 구현 (Spring Security + JWT)

### 7.1 구현 순서

```
1. JwtProperties       → JWT 설정값
2. JwtTokenProvider    → 토큰 생성/검증
3. JwtAuthenticationFilter → 요청 필터
4. CustomUserDetails   → 인증 정보
5. CustomUserDetailsService → 사용자 조회
6. SecurityConfig      → 보안 설정
```

### 7.2 SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())
            
            // 세션 사용 안함 (Stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 인증/인가 설정
            .authorizeHttpRequests(auth -> auth
                // 공개 API
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                
                // 정적 리소스
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // 역할별 접근 제어
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터 추가
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                UsernamePasswordAuthenticationFilter.class
            )
            
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 7.3 인증 정보 사용

```java
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {
    
    // 현재 로그인한 사용자 정보 가져오기
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long memberId = userDetails.getMember().getId();
        // ...
    }
}
```

---

## 8. 코드 패턴 레퍼런스

### 8.1 Soft Delete 패턴

```java
@Entity
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Post extends BaseEntity {
    
    private LocalDateTime deletedAt;
    
    // delete() 호출 시 실제 삭제 대신 deletedAt 업데이트
}
```

### 8.2 페이징 처리

```java
// Controller
@GetMapping
public ResponseEntity<ApiResponse<Page<MemberResponse>>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<MemberResponse> members = memberService.findAll(pageable);
    return ResponseEntity.ok(ApiResponse.success(members));
}

// Service
public Page<MemberResponse> findAll(Pageable pageable) {
    return memberRepository.findAll(pageable)
        .map(MemberResponse::from);
}
```

### 8.3 N+1 문제 해결

```java
// 1. Fetch Join (JPQL)
@Query("SELECT m FROM Member m JOIN FETCH m.team WHERE m.id = :id")
Optional<Member> findByIdWithTeam(@Param("id") Long id);

// 2. EntityGraph
@EntityGraph(attributePaths = {"team"})
Optional<Member> findById(Long id);

// 3. Batch Size (application.yml)
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

### 8.4 Auditing (생성자/수정자)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    private String modifiedBy;
}
```

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            return Optional.of(authentication.getName());
        };
    }
}
```

---

## 📌 체크리스트

### 프로젝트 시작 시
- [ ] Spring Initializr로 프로젝트 생성
- [ ] build.gradle 의존성 확인
- [ ] 패키지 구조 생성
- [ ] application.yml 환경별 분리
- [ ] BaseEntity, ApiResponse, Exception 구현

### 도메인 구현 시
- [ ] Entity 작성 (정적 팩토리, 비즈니스 메서드)
- [ ] Repository 작성 (쿼리 메서드, QueryDSL)
- [ ] DTO 작성 (Validation, from() 메서드)
- [ ] Service 작성 (트랜잭션, 예외 처리)
- [ ] Controller 작성 (API 엔드포인트)
- [ ] 테스트 코드 작성

### 배포 전
- [ ] 로그 레벨 확인 (운영: WARN)
- [ ] ddl-auto: none 확인
- [ ] 환경변수로 민감 정보 관리
- [ ] 테스트 코드 통과 확인

---

## 🔗 참고 자료

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [QueryDSL Reference](http://querydsl.com/static/querydsl/latest/reference/html/)
- [jjwt GitHub](https://github.com/jwtk/jjwt)

---

**이 가이드를 기반으로 프로젝트를 체계적으로 구현하세요! 🚀**


