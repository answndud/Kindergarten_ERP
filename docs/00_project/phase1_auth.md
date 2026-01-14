# Phase 1: 인증 시스템 기술 선택 결정

## 개요
- 단계: 인증 시스템 구현
- 목표: 회원가입, 로그인, JWT 기반 인증/인가
- 작업: Member 도메인, JWT, Spring Security

---

## 1. 인증 방식: JWT (Stateless)

### 결정
JWT(JSON Web Token)를 사용한 Stateless 인증

### 이유
1. **확장성**: 세션을 서버에 저장하지 않아 서버 다중화 용이
2. **성능**: 데이터베이스 조회 없이 토큰만으로 인증
3. **RESTful**: STATELESS 원칙 준수, 클라이언트가 토큰 저장
4. **모바일 확장**: 앱 개발 시 동일 인증 방식 사용 가능

### 대안 고려
- **Session-Based**: 세션을 서버에 저장, 확장성 문제
- **OAuth2**: 소셜 로그인 시 필요 but MVP 이후 추가

### 토큰 설계
| 토큰 | 유효기간 | 용도 | 저장소 |
|------|----------|------|--------|
| Access Token | 15분 | API 요청 | HTTP-only 쿠키 |
| Refresh Token | 7일 | Access Token 갱신 | Redis |

### 변경 이력
- 2024-12-28: 초기 채택

---

## 2. JWT 저장소: 쿠키 (HTTP-only)

### 결정
JWT를 HTTP-only 쿠키에 저장

### 이유
1. **XSS 방지**: JavaScript에서 접근 불가
2. **CSRF 방지**: SameSite 설정으로 CSRF 최소화
3. **자동 전송**: 모든 요청에 자동 포함
4. **간편함**: 프론트엔드에서 토큰 관리 불필요

### 대안 고려
- **localStorage**: XSS 취약, JavaScript 접근 가능
- **Authorization 헤더**: 매번 헤더에 추가해야 함

### 쿠키 설정
```java
Cookie cookie = new Cookie("jwt", accessToken);
cookie.setHttpOnly(true);  // JavaScript 접근 불가
cookie.setSecure(true);    // HTTPS 전용 (운영)
cookie.setPath("/");        // 모든 경로에서 전송
cookie.setMaxAge(900);      // 15분
```

### 변경 이력
- 2024-12-28: HTTP-only 쿠키로 결정

---

## 3. 비밀번호 암호화: BCrypt

### 결정
Spring Security의 BCryptPasswordEncoder 사용

### 이유
1. **강력한 암호화**: Blowfish 기반, 해시 무차별 대항 저항
2. **솔트 자동**: 매번 다른 솔트로 레인보우 테이블 공격 방지
3. **Spring Security 통합**: 별도 설정 불필요
4. **조정 가능**: cost factor로 강도 조절 (기본값 10)

### 대안 고려
- **SHA-256**: 솔트 직접 관리 필요, 구현 복잡
- **Argon2**: 더 강력하지만 Spring Security 기본 지원 아님

### 변경 이력
- 2024-12-28: BCrypt 채택

---

## 4. Refresh Token 저장소: Redis

### 결정
Refresh Token을 Redis에 저장

### 이유
1. **TTL 자동 삭제**: 만료 후 자동 제거
2. **빠른 조회**: 인메모리 DB로 ms 단위 응답
3. **로그아웃 구현**: Redis에서 토큰 삭제로 즉시 로그아웃
4. **토큰 폐지**: Refresh Token 탈취 시 폐지 가능

### 저장 형식
```
Key: "refresh:{email}:{tokenId}"
Value: "{tokenInfo}"
TTL: 7일 (604800초)
```

### 대안 고려
- **DB 저장**: 조회 성능 저하, 별도 테이블 필요
- **JWT만 사용**: Refresh Token 폐기 불가능, 보안 약함

### 변경 이력
- 2024-12-28: Redis 저장 채택

---

## 5. 사용자 역할: RBAC (Role-Based Access Control)

### 결정
3가지 역할로 권한 분리

### 역할 정의
| 역할 | 권한 | 코드 |
|------|------|------|
| 원장 (PRINCIPAL) | 전체 관리, 승인 | ROLE_PRINCIPAL |
| 교사 (TEACHER) | 반 관리, 출석, 알림장 | ROLE_TEACHER |
| 학부모 (PARENT) | 조회만 (알림장, 출석) | ROLE_PARENT |

### 이유
1. **명확한 권한**: 직무에 따른 권한 분리
2. **확장성**: 새로운 역할 추가 용이
3. **Spring Security 통합**: @PreAuthorize 어노테이션으로 쉽게 제어

### 권한 제어 예시
```java
@PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
public void createClass() { }

@PreAuthorize("hasRole('PARENT')")
public void viewNotepad() { }
```

### 변경 이력
- 2024-12-28: 3가지 역할 정의

---

## 6. 회원 상태: Status Enum

### 결정
3가지 상태로 회원 생명주기 관리

### 상태 정의
| 상태 | 설명 |
|------|------|
| ACTIVE | 활성 (로그인 가능) |
| INACTIVE | 비활성 (탈퇴 또는 정지) |
| PENDING | 승인 대기 (유치원 승인 필요 시) |

### 이유
1. **Soft Delete**: 실제 삭제 대신 상태 변경으로 데이터 보존
2. **승인 프로세스**: 교사/학부모는 승인 후 ACTIVE
3. **계정 정지**: 위반 시 INACTIVE로 변경

### 추가 결정(2026-01-14)
- **회원가입 시 상태 정책**
  - `PRINCIPAL`: 기본 `ACTIVE`
  - `TEACHER`, `PARENT`: 기본 `PENDING` (유치원 승인 전까지)
- **로그인 허용 정책**
  - `PENDING`도 로그인은 허용하고, 접근 가능한 화면을 `/applications/pending`로 제한한다.

### 변경 이력
- 2024-12-28: 3가지 상태 정의
- 2026-01-14: TEACHER/PARENT 가입 기본 PENDING + PENDING 로그인 허용(대기 페이지로 제한)

---

## 7. Spring Security 필터 체인

### 결정
JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가

### 필터 순서
```
1. LogoutFilter (로그아웃 요청)
2. JWTFilter (토큰 검증) ← 커스텀
3. UsernamePasswordAuthenticationFilter (로그인 요청)
4. FilterSecurityInterceptor (권한 체크)
```

### JWTFilter 역할
1. 쿠키에서 JWT 추출
2. 토큰 검증 (서명, 만료)
3. SecurityContext에 인증 정보 설정
4. 체인 계속 진행

### 이유
- 로그인 전/후 모두 JWT 토큰으로 인증
- 인증된 요청은 DB 조회 없이 토큰만으로 인증

### 변경 이력
- 2024-12-28: JWTFilter 위치 결정

---

## 8. 로그인 프로세스: 커스텀 필터

### 결정
LoginFilter로 UsernamePasswordAuthenticationFilter 대체

### 로그인 흐름
```
1. POST /api/v1/auth/login (이메일, 비밀번호)
2. LoginFilter가 요청 가로챔
3. AuthenticationManager로 인증 시도
4. UserDetailsService로 DB 조회
5. BCrypt로 비밀번호 검증
6. 성공: JWT 토큰 생성 후 쿠키에 저장
7. 실패: 401 에러 반환
```

### 이유
1. **JSON 요청**: 폼 데이터가 아닌 JSON 본문 처리
2. **커스텀 응답**: ApiResponse 형식으로 일관된 응답
3. **JWT 발급**: 로그인 성공 시 즉시 토큰 발급

### 대안 고려
- **기본 필터 사용**: 폼 데이터만 지원, JWT 발급 어려움

### 변경 이력
- 2024-12-28: LoginFilter 커스텀 구현

---

## 9. CORS 설정

### 결정
CorsConfigurationSource로 CORS 설정

### 설정 내용
```java
.allowedOrigins("http://localhost:8080")  // 동일 출처
.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
.allowedHeaders("*")
.allowCredentials(true)  // 쿠키 전송 허용
```

### 이유
1. **쿠키 전송**: allowCredentials(true)로 JWT 쿠키 전송
2. **프리플라이트**: OPTIONS 요청 허용
3. **안전성**: 허용하는 출처 명시적 지정

### 변경 이력
- 2024-12-28: CORS 설정 추가

---

## 10. CSRF 비활성화

### 결정
JWT 사용 시 CSRF 비활성화

### 이유
1. **JWT로 보호**: CSRF는 세션 기반 공격, JWT는 영향 없음
2. **쿠키 기반**: HTTP-only 쿠키로 XSS 방지, CSRF는 우선순위 낮음
3. **간편성**: 매번 CSRF 토큰 전송 불필요

### 설정
```java
.httpBasic(Customizer.withDefaults())
.csrf(csrf -> csrf.disable())
```

### 변경 이력
- 2024-12-28: CSRF 비활성화

---

## 11. 비밀번호 정책

### 결정
최소 길이 8자, 영문+숫자+특수문자 조합

### 검증 로직
```java
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
private String password;
```

### 이유
1. **보안**: 충분히 복잡한 비밀번호
2. **사용자 경험**: 너무 엄격하지 않게
3. **일반적**: 많은 서비스에서 사용하는 기준

### 변경 이력
- 2024-12-28: 비밀번호 정책 결정

---

## 12. 이메일 유효성 검증

### 결정
회원가입 시 이메일 중복 검증

### 검증 로직
1. 정규식으로 이메일 형식 검증
2. DB로 중복 이메일 조회
3. 중복 시 409 에러 반환

### 이유
1. **유일성**: 이메일은 사용자 식별자
2. **보안**: 가입 전 중복 확인으로 불필요한 요청 방지
3. **UX**: 빠른 피드백

### 변경 이력
- 2024-12-28: 이메일 중복 검증 채택

---

## 면접 예상 질문 답변

### Q: 왜 JWT를 선택했나요?
> A: 확장성을 고려했습니다. 세션은 서버에 저장해야 해서 서버 다중화 시 세션 공유가 필요하지만, JWT는 클라이언트에 저장해서 그런 문제가 없습니다. 또한 RESTful한 API를 만들기 위해 STATELESS 원칙을 준수하고 싶었습니다.

### Q: Refresh Token은 왜 따로 만드셨나요?
> A: Access Token의 유효기간이 짧으면 보안은 좋지만 사용자 경험이 안 좋아집니다. 15분마다 로그인하면 불편하니까, Refresh Token으로 Access Token을 갱신해주는 방식을 선택했습니다. Refresh Token은 Redis에 저장해서 탈취 시 즉시 폐기할 수 있게 했습니다.

### Q: JWT를 쿠키에 저장하면 CSRF 공격에 취약하지 않나요?
> A: 맞습니다. 그래서 HTTP-only 쿠키를 사용해서 XSS 공격은 방지하고, SameSite 설정을 Strict로 해서 CSRF를 최소화했습니다. 또한 운영 환경에서는 HTTPS만 허용하도록 Secure 플래그를 설정할 예정입니다.

### Q: 비밀번호는 왜 BCrypt로 암호화했나요?
> A: BCrypt는 솔트를 자동으로 생성하고, cost factor로 강도를 조절할 수 있어서 선택했습니다. SHA-256 같은 단방향 해시는 솔트를 직접 관리해야 하는데, BCrypt는 그런 과정이 없어서 실수를 줄일 수 있습니다.

### Q: 왜 세션 대신 JWT를 썼나요?
> A: 두 가지 이유가 있습니다. 첫째, 추후 서버 다중화를 고려해서 세션 공유 문제를 피하고 싶었습니다. 둘째, 모바일 앱 개발 시 동일한 인증 방식을 사용할 수 있어서입니다. 세션은 쿠키 기반이라 모바일에서 사용하기 어렵지만 JWT는 헤더에만 담아서 보내면 돼서 모바일과 호환性好습니다.

### Q: 로그인은 왜 커스텀 필터를 만드셨나요?
> A: 기본 UsernamePasswordAuthenticationFilter는 폼 데이터만 받는데, 저는 JSON 본문으로 받고 싶었습니다. 그리고 로그인 성공 시 즉시 JWT를 발급해서 쿠키에 저장하고 싶었는데, 기본 필터로는 처리가 어려워서 커스텀 필터를 만들었습니다.

---

## 다음 단계
Phase 2: 유치원 & 반 관리 구현

---

## 구현 현황 (Phase 1 완료)

### 생성된 파일 목록

#### 1. 도메인 엔티티
| 파일 | 설명 |
|------|------|
| `domain/member/entity/MemberRole.java` | 역할 enum (PRINCIPAL, TEACHER, PARENT) |
| `domain/member/entity/MemberStatus.java` | 상태 enum (ACTIVE, INACTIVE, PENDING) |
| `domain/member/entity/Member.java` | 회원 엔티티 |
| `domain/member/entity/Kindergarten.java` | 유치원 엔티티 |

#### 2. 리포지토리
| 파일 | 설명 |
|------|------|
| `domain/member/repository/MemberRepository.java` | 회원 리포지토리 인터페이스 |
| `domain/member/repository/MemberRepositoryCustom.java` | QueryDSL 커스텀 인터페이스 |
| `domain/member/repository/MemberRepositoryImpl.java` | QueryDSL 구현체 |

#### 3. DTO
| 파일 | 설명 |
|------|------|
| `domain/auth/dto/request/SignUpRequest.java` | 회원가입 요청 (비밀번호 검증) |
| `domain/auth/dto/request/LoginRequest.java` | 로그인 요청 |
| `domain/auth/dto/response/TokenResponse.java` | 토큰 응답 |
| `domain/member/dto/response/MemberResponse.java` | 회원 정보 응답 (record) |

#### 4. JWT & Security
| 파일 | 설명 | 주요 기능 |
|------|------|----------|
| `global/security/jwt/JwtProperties.java` | JWT 설정 | secret, validity, 쿠키 이름 |
| `global/security/jwt/JwtTokenProvider.java` | JWT 토큰 제공자 | 생성, 검증, 클레임 추출 |
| `global/security/jwt/JwtFilter.java` | JWT 인증 필터 | 쿠키에서 토큰 추출, SecurityContext 설정 |
| `global/security/user/CustomUserDetails.java` | 사용자 정보 | Spring Security UserDetails 구현 |
| `global/security/user/CustomUserDetailsService.java` | 사용자 조회 서비스 | DB에서 회원 조회 |
| `global/config/SecurityConfig.java` | Security 설정 | 필터 체인, CORS, 접근 권한 |

#### 5. 서비스
| 파일 | 설명 | 주요 메서드 |
|------|------|----------|
| `domain/member/service/MemberService.java` | 회원 서비스 | signUp, getMember, updateProfile, changePassword, withdraw |
| `domain/auth/service/AuthService.java` | 인증 서비스 | signUp, login, logout, refreshAccessToken |

#### 6. 컨트롤러
| 파일 | 설명 | 엔드포인트 |
|------|------|----------|
| `domain/auth/controller/AuthApiController.java` | 인증 API | POST /api/v1/auth/signup, /login, /logout, /refresh |
| `domain/auth/controller/AuthViewController.java` | 인증 뷰 | GET /login, /signup, / |

#### 7. 템플릿
| 파일 | 설명 | 주요 내용 |
|------|------|----------|
| `templates/auth/login.html` | 로그인 페이지 | Alpine.js로 폼 전송, 에러 처리 |
| `templates/auth/signup.html` | 회원가입 페이지 | 역할 선택, 비밀번호 확인, 검증 |
| `templates/index.html` | 메인 페이지 | 로그인 후 환영 메시지, 회원 정보 카드 |

---

### 구현된 기능 상세

#### 백엔드

**1. Member 엔티티**
```java
// 역할: PRINCIPAL(원장), TEACHER(교사), PARENT(학부모)
// 상태: ACTIVE(활성), INACTIVE(비활성), PENDING(승인 대기)
```
- 정적 팩토리 메서드: `create()`, `createSocial()`
- 비즈니스 메서드: `updateProfile()`, `changePassword()`, `activate()`, `deactivate()`, `withdraw()`
- Kindergarten과 N:1 연관관계
- Soft Delete: `deletedAt` 필드

**2. JWT 토큰 생성**
```java
createAccessToken(email, role)   // 15분
createRefreshToken(email, role)  // 7일
```
- jjwt 0.12.6 라이브러리 사용
- HMAC-SHA 알고리즘
- Claims: subject(이메일), role(역할), issuedAt, expiration

**3. JWT 인증 필터**
```
요청 → 쿠키에서 토큰 추출 → 토큰 검증 → DB 조회 → SecurityContext 설정 → 체인 계속
```
- 매 요청마다 JWT 검증
- UserDetailsService로 회원 상태 확인
- ACTIVE 상태만 인증 성공

**4. Security 설정**
```
 permitAll: /, /login, /signup, /api/v1/auth/*
 authenticated: 그 외 모든 요청
```
- 세션: STATELESS (JWT 사용)
- CSRF: 비활성화
- CORS: localhost:8080 허용

**5. 회원가입 로직**
```
1. 이메일 중복 확인
2. 비밀번호 암호화 (BCrypt)
3. Member 엔티티 생성 (status=ACTIVE)
4. DB 저장
```

**6. 로그인 로직**
```
1. AuthenticationManager 인증
2. Access Token 생성
3. Refresh Token 생성
4. Refresh Token을 Redis에 저장 (key: refresh:{email}:{tokenId})
5. 두 토큰을 쿠키에 저장
```

**7. 로그아웃 로직**
```
1. Redis에서 Refresh Token 삭제
2. 쿠키 만료 (maxAge=0)
```

**8. Access Token 갱신**
```
1. Refresh Token 검증
2. Redis에서 존재 확인
3. 새 Access Token 생성
4. 쿠키에 업데이트
```

#### 프론트엔드

**1. 로그인 페이지 (Alpine.js)**
```javascript
// x-data 속성
email, password, isLoading, error

// @submit.prevent="handleSubmit()"
fetch('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
})
```
- 로딩 중 스피너 표시
- 에러 메시지 표시
- 성공 시 `/` 리다이렉트

**2. 회원가입 페이지**
```javascript
// 역할 선택 (radio button)
PARENT(학부모), TEACHER(교사), PRINCIPAL(원장)

// 비밀번호 확인
password !== passwordConfirm → 에러 표시
```
- 실시간 비밀번호 확인
- 필드별 에러 표시
- 역할 선택 라디오 버튼

**3. 메인 페이지**
- 현재 로그인한 회원 정보 표시
- 빠른 메뉴 카드 (알림장, 출석부, 공지사항)

---

### 확인 가능한 상태

#### 실행 방법
```bash
# 1. Docker 컨테이너 실행
cd docker
docker-compose up -d

# 2. 애플리케이션 실행
cd ..
./gradlew bootRun

# 3. 접속
http://localhost:8080/login
```

#### 테스트 시나리오
1. **회원가입**: `/signup` → 정보 입력 → 가입 완료 → `/login`으로 이동
2. **로그인**: 이메일/비밀번호 입력 → 성공 → `/` 메인 페이지
3. **로그아웃**: 헤더 프로필 클릭 → 로그아웃 → `/login`
4. **토큰 갱신**: Access Token 만료 후 `/api/v1/auth/refresh`로 갱신

#### API 테스트
```bash
# 회원가입
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "parent@test.com",
    "password": "Test1234!",
    "passwordConfirm": "Test1234!",
    "name": "홍길동",
    "phone": "01012345678",
    "role": "PARENT"
  }'

# 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "parent@test.com",
    "password": "Test1234!"
  }' \
  -c cookies.txt

# 현재 회원 정보 (쿠키 필요)
curl -X GET http://localhost:8080/api/v1/auth/me \
  -b cookies.txt

# 로그아웃
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

---

### Phase 1 완료 체크리스트
- [x] MemberRole enum (PRINCIPAL, TEACHER, PARENT)
- [x] MemberStatus enum (ACTIVE, INACTIVE, PENDING)
- [x] Member 엔티티 (BaseEntity 상속, Kindergarten 연관)
- [x] JWT 토큰 생성/검증 (Access + Refresh)
- [x] JWT 필터 (쿠키에서 토큰 추출)
- [x] Security 설정 (STATELESS, CORS, 접근 권한)
- [x] 회원가입 API (이메일 중복 검증, 비밀번호 암호화)
- [x] 로그인 API (BCrypt 검증, JWT 발급)
- [x] 로그아웃 API (Redis에서 Refresh Token 삭제)
- [x] Access Token 갱신 API
- [x] 로그인 페이지 (Alpine.js)
- [x] 회원가입 페이지 (역할 선택, 검증)
- [x] 메인 페이지 (회원 정보 표시)

---

## 13. 프로필 관리

### 결정
회원 본인의 정보 조회 및 수정 기능 구현

### 기능
- **프로필 조회**: 이름, 이메일, 전화번호, 역할, 유치원, 가입일
- **프로필 수정**: 이름, 전화번호 수정 가능
- **비밀번호 변경**: 현재 비밀번호 검증 후 새 비밀번호로 변경
- **회원 탈퇴**: Soft Delete (deletedAt 설정)

### 구현
```java
// DTO
@NotBlank(message = "이름은 필수입니다")
@Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다")
private String name;

@Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호 형식이 올바르지 않습니다")
private String phone;
```

### 변경 이력
- 2025-01-14: 프로필 관리 기능 추가

---

**Phase 1 완료일: 2024-12-28**
**업데이트일: 2025-01-14 (프로필 관리 기능 추가)**
