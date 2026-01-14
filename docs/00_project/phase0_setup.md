# Phase 0: 프로젝트 설정 기술 선택 결정

## 개요
- 단계: 프로젝트 기초 설정
- 목표: 개발에 필요한 인프라와 공통 컴포넌트 구축
- 작업: Docker 환경, DB 마이그레이션, 공통 클래스, 설정

---

## 1. 데이터베이스: MySQL 8.0

### 결정
MySQL 8.0을 주요 저장소로 사용

### 이유
1. **안정性与 호환성**: 가장 널리 사용되는 오픈소스 RDBMS로 운영 노하우 풍부
2. **Spring Boot와의 통합**: Spring Data JPA가 MySQL을 완벽히 지원
3. **트랜잭션 처리 필요**: 유치원 ERP는 출석, 결제 등 데이터 정합성이 중요한 도메인

### 대안 고려
- **PostgreSQL**: 더 많은 기능 but 초반 학습 비용 상승, MySQL로도 충분
- **H2**: 개발용으로 좋지만 실제 환경과 차이 존재

### 변경 이력
- 2024-12-28: 초기 채택

---

## 2. 캐시: Redis 7

### 결정
Redis를 세션/토큰 저장소로 사용

### 이유
1. **빠른 읽기/쓰기**: 인메모리 DB로 ms 단위 응답
2. **JWT 저장소**: Refresh token 저장에 적합 (TTL 자동 삭제)
3. **세션 공유**: 추후 서버 다중화 시 세션 공유 용이

### 활용처
- JWT Refresh Token 저장 (7일 TTL)
- 출석부 캐시 (일별 조회 빈도 높음)
- 공지사항 캐시 (전체 공지 자주 조회)

### 대안 고려
- **Memcached**: 단순 K-V but Redis가 더 많은 자료구조 지원
- **DB 저장**: 조회 성능 저하, DB 부하

### 변경 이력
- 2024-12-28: 초기 채택

---

## 3. DB 마이그레이션: Flyway

### 결정
Flyway로 스키마 버전 관리

### 이유
1. **버전 관리**: V1, V2... 순서대로 마이그레이션 보장
2. **팀 협업**: SQL 파일로 변경사항 공유 명확
3. **롤백 지원**: 마이그레이션 실패 시 자동 롤백

### 대안 고려
- **Liquibase**: 더 많은 기능 but 설정 복잡, Flyway로 충분
- **ddl-auto**: 개발용만 적합, 운영 부적합

### 변경 이력
- 2024-12-28: 초기 채택

---

## 4. 컨테이너화: Docker Compose

### 결정
Docker Compose로 로컬 개발 환경 구성

### 이유
1. **환경 일치성**: 개발-스테이징-운영 환경 동일
2. **빠른 세팅**: `docker-compose up` 한 번으로 DB+Redis 실행
3. **의존성 관리**: MySQL, Redis 의존성을 코드로 관리

### 대안 고려
- **로컬 설치**: OS별로 설정 다르고, 의존성 충돌 가능
- **Kubernetes**: 소규모 프로젝트에는 과도함

### 변경 이력
- 2024-12-28: 초기 채택

---

## 5. 공통 응답: ApiResponse<T>

### 결정
제네릭을 사용한 공통 응답 클래스

### 이유
1. **일관된 API 응답**: 프론트엔드에서 예측 가능한 응답 구조
2. **타입 안전성**: 제네릭으로 컴파일 타임 타입 체크
3. **중복 제거**: 모든 API가 동일한 응답 포맷 사용

### 응답 구조
```java
{
  "success": boolean,
  "data": T,
  "message": String,
  "code": String
}
```

### 변경 이력
- 2024-12-28: 초기 채택

---

## 6. 예외 처리: GlobalExceptionHandler

### 결정
@ControllerAdvice로 전역 예외 처리

### 이유
1. **중앙화된 처리**: try-catch 중복 제거
2. **일관된 에러 응답**: 모든 예외를 ApiResponse로 변환
3. **분리된 관심사**: 비즈니스 로직과 예외 처리 분리

### 계층 구조
```
BusinessException (커스텀)
  └─ ErrorCode (enum)
      └─ GlobalExceptionHandler
```

### 대안 고려
- **각 Controller별 try-catch**: 코드 중복, 일관성 없음

### 변경 이력
- 2024-12-28: 초기 채택

---

## 7. JPA Auditing: BaseEntity

### 결정
@MappedSuperclass로 생성일/수정일 자동 관리

### 이유
1. **중복 제거**: 모든 엔티티가 createdAt, updatedAt 가짐
2. **자동화**: JPA Auditing이 자동으로 시간 기록
3. **감사 추적**: 데이터 생성/수정 시점 추적

### 주의사항
- OSIV 비활성화: 트랜잭션 밖에서 lazy loading 방지
- updatedAt은 updatable=true로 수정 시 자동 갱신

### 대안 고려
- **각 엔티티에 필드 추가**: 중복 코드, 누락 가능

### 변경 이력
- 2024-12-28: 초기 채택

---

## 8. OSIV(Open Session In View) 비활성화

### 결정
`open-in-view: false` 설정

### 이유
1. **성능**: 트랜잭션 범위를 명확히 하여 불필요한 DB 연결 최소화
2. **N+1 문제 조기 발견**: Lazy Loading을 트랜잭션 내에서만 사용
3. **명시적 설계**: 필요한 데이터를 Service에서 명시적으로 조회

### 대안 고려
- **OSIV 활성화**: 편하지만 트랜잭션 범위가 불명확해지고 성능 저하

### 변경 이력
- 2024-12-28: false로 설정

---

## 9. QueryDSL 도입

### 결정
QueryDSL 5.0.0으로 동적 쿼리 처리

### 이유
1. **타입 안전성**: 컴파일 타임에 쿼리 오류 검출
2. **동적 쿼리**: BooleanBuilder로 조건부 쿼리 작성 용이
3. **가독성**: JPQL보다 코드 형태로 자연스러운 쿼리 작성

### 활용처
- 검색 조건이 동적인 화면 (날짜, 상태, 반별 필터 등)
- 복잡한 JOIN 쿼리

### 대안 고려
- **JPQL 문자열**: 타입 안전하지 않음, 리팩토링 어려움
- **Specification**: QueryDSL이 더 직관적

### 변경 이력
- 2024-12-28: 초기 채택

---

## 10. Batch Fetch Size 설정

### 결정
`default_batch_fetch_size: 100`

### 이유
1. **N+1 문제 완화**: 연관관계 조회 시 IN 쿼리로 일괄 조회
2. **메모리 효율**: 너무 크면 메모리 부담, 100은 적절한 타협
3. **자동 최적화**: @ManyToOne, @OneToOne 등에서 자동 적용

### 예시
```java
// 설정 전: SELECT 100번 실행
// 설정 후: SELECT ... WHERE id IN (1,2,3,...,100) 1번 실행
```

### 변경 이력
- 2024-12-28: 100으로 설정

---

## 11. 프론트엔드 스택: HTMX + Alpine + Tailwind

### HTMX 선택 이유
1. **서버 중심**: 백엔드 개발자로서 HTML 조각만 반환하면 됨
2. **JavaScript 최소화**: 복잡한 상태 관리 불필요
3. **Thymeleaf와 조합**: 서버 렌더링하면서 동적 업데이트 가능

### Alpine.js 선택 이유
1. **가벼움**: 15KB 미만의 작은 라이브러리
2. **간단한 상태**: 모달, 토글 등 간단한 인터랙션에 적합
3. **HTMX와 보완**: HTMX가 서버 통신, Alpine이 클라이언트 상태

### Tailwind CSS 선택 이유
1. **빠른 개발**: 클래스로 바로 스타일링
2. **일관성**: 팀 프로젝트 시 디자인 시스템 유지 용이
3. **CDN 사용**: 빌드 과정 없이 바로 사용 가능

### 대안 고려
- **React/Vue**: SPA는 백엔드 포트폴리오에 과도함
- **Bootstrap/jQuery**: 레거시, 현대적이지 않음
- **순수 CSS**: 개발 속도 느림

### 변경 이력
- 2024-12-28: Bootstrap + jQuery → HTMX + Alpine + Tailwind

---

## 면접 예상 질문 답변

### Q: 왜 MySQL을 쓰셨나요?
> A: 가장 널리 쓰이는 오픈소스 RDBMS로 운영 노하우가 풍부하고, Spring Boot와의 호환성이 좋습니다. 또한 유치원 ERP는 출석, 결제 등 트랜잭션이 중요한 도메인이라 ACID를 보장하는 RDBMS가 적절하다고 판단했습니다.

### Q: Redis는 왜 도입하셨나요?
> A: JWT Refresh Token을 저장하기 위해 도입했습니다. Redis는 TTL 기반 자동 삭제와 빠른 읽기/쓰기 속도를 제공해 토큰 저장소로 적합합니다. 또한 추후 서버 다중화 시 세션 공이도 용이합니다.

### Q: 왜 HTMX를 선택했나요?
> A: 이 프로젝트는 백엔드 중심 포트폴리오라 프론트엔드 복잡도를 낮추고 싶었습니다. HTMX를 사용하면 서버에서 HTML 조각만 반환하면 돼, JavaScript 없이도 동적인 UI를 만들 수 있습니다. 또한 Thymeleaf와 조합해 SSR의 장점을 유지하면서 인터랙티브한 경험을 제공할 수 있습니다.

### Q: OSIV를 왜 비활성화했나요?
> A: OSIV를 활성화하면 트랜잭션 범위가 불명확해지고, lazy loading으로 인한 N+1 문제가 숨어있을 수 있습니다. 비활성화하면 Service 계층에서 필요한 데이터를 명시적으로 조회하게 돼, 코드의 의도가 명확해지고 성능 이슈를 조기에 발견할 수 있습니다.

### Q: QueryDSL을 쓴 이유는요?
> A: 검색 조건이 동적인 화면이 많아서입니다. 예를 들어 출석부에서 날짜, 반, 상태 등 다양한 조건으로 조회해야 하는데, JPQL 문자열로 처리하면 타입 안전하지 않고 복잡합니다. QueryDSL의 BooleanBuilder를 사용하면 타입 안전하게 동적 쿼리를 작성할 수 있습니다.

---

## 다음 단계
Phase 1: 인증 시스템 (JWT, Spring Security) 구현

---

## 구현 현황 (Phase 0 완료)

### 생성된 파일 목록

#### 1. 인프라
| 파일 | 설명 |
|------|------|
| `docker/docker-compose.yml` | MySQL 8.0 + Redis 7 컨테이너 정의 |
| `docker/.env` | DB 환경변수 (루트 비밀번호, DB명, 유저) |

#### 2. 데이터베이스 마이그레이션
| 파일 | 설명 |
|------|------|
| `src/main/resources/db/migration/V1__init_schema.sql` | 전체 테이블 스키마 (9개 테이블) |

**생성된 테이블:**
- `member` - 회원 (이메일, 비밀번호, 역할, 상태)
- `kindergarten` - 유치원
- `classroom` - 반 (유치원, 교사 연관)
- `kid` - 원생 (반 연관)
- `parent_kid` - 학부모-원생 연결 (N:M 중간테이블)
- `attendance` - 출석 (원생 연관)
- `notepad` - 알림장 (반, 원생, 작성자 연관)
- `notepad_read_confirm` - 알림장 읽음 확인
- `announcement` - 공지사항

#### 3. 백엔드 공통 컴포넌트
| 파일 | 설명 | 주요 기능 |
|------|------|----------|
| `global/common/BaseEntity.java` | 엔티티 기본 클래스 | createdAt, updatedAt 자동 관리 |
| `global/common/ApiResponse.java` | 공통 응답 클래스 | success(T), error(ErrorCode) 정적 팩토리 |
| `global/exception/ErrorCode.java` | 에러 코드 enum | 30개 에러 코드 정의 |
| `global/exception/BusinessException.java` | 커스텀 예외 | ErrorCode 포함 |
| `global/exception/GlobalExceptionHandler.java` | 전역 예외 처리 | @RestControllerAdvice로 예외 처리 |

#### 4. 설정 클래스
| 파일 | 설명 | 설정 내용 |
|------|------|----------|
| `global/config/JpaConfig.java` | JPA 설정 | @EnableJpaAuditing |
| `global/config/QuerydslConfig.java` | QueryDSL 설정 | JPAQueryFactory 빈 등록 |
| `global/config/RedisConfig.java` | Redis 설정 | Lettuce 연결, String/JSON 직렬화 |

#### 5. 프론트엔드 기초
| 파일 | 설명 | 주요 내용 |
|------|------|----------|
| `templates/layout/default.html` | 기본 레이아웃 | HTMX/Alpine/Tailwind CDN, Primary 색상 설정 |
| `templates/fragments/header.html` | 헤더 | 인증 상태에 따른 UI (로그인/회원가입 또는 프로필/로그아웃) |
| `templates/fragments/footer.html` | 푸터 | 저작권 표시 |
| `static/js/app.js` | 공통 JS | HTMX 이벤트 로깅, Alpine utils 스토어 |
| `static/css/custom.css` | 커스텀 CSS | 로딩 인디케이터, 토스트 애니메이션, 스피너 |

---

### 구현된 기능 상세

#### 백엔드

**1. BaseEntity - JPA Auditing**
```java
@CreatedDate // 생성일 자동 기록 (수정 불가)
@LastModifiedDate // 수정일 자동 갱신
```
- 모든 엔티티가 상속받아 createdAt, updatedAt 자동 관리

**2. ApiResponse<T> - 제네릭 응답**
```java
ApiResponse.success(data)      // 성공 (데이터 있음)
ApiResponse.success()           // 성공 (데이터 없음)
ApiResponse.error(errorCode)    // 실패
```
- 모든 API가 일관된 형식 반환

**3. ErrorCode - 에러 코드 체계**
```
C: 공통 (Common)
A: 인증 (Auth)
M: 회원 (Member)
K: 유치원 (Kindergarten)
CL: 반 (Classroom)
KD: 원생 (Kid)
AT: 출석 (Attendance)
N: 알림장 (Notepad)
AN: 공지사항 (Announcement)
```

**4. GlobalExceptionHandler - 4가지 예외 처리**
| 예외 타입 | 처리 내용 |
|----------|----------|
| BusinessException | 비즈니스 로직 예외 → ErrorCode 변환 |
| MethodArgumentNotValidException | @Valid 검증 실패 → 필드별 에러 |
| BindException | ModelAttribute 검증 실패 → 필드별 에러 |
| Exception | 그 외 예외 → 500 에러 |

**5. QueryDSL 설정**
- JPAQueryFactory 빈 등록으로 컴파일 타임 쿼리 검증 가능
- BooleanBuilder로 동적 쿼리 작성 예정

**6. Redis 설정**
- Lettuce 연결풀 사용 (Netty 기반, 성능 우수)
- Key: String 직렬화
- Value: JSON 직렬화 (GenericJackson2JsonRedisSerializer)

#### 프론트엔드

**1. 레이아웃 구조**
```
default.html (기본 템플릿)
├── fragments/header (헤더)
├── th:replace="${content}" (메인 컨텐츠)
└── fragments/footer (푸터)
```

**2. 헤더 기능**
- 인증 전: 로그인, 회원가입 버튼
- 인증 후: 사용자 이메일, 드롭다운 메뉴 (Alpine.js)
- 드롭다운: 프로필, 설정, 로그아웃

**3. JS 기능**
- HTMX 요청/응답 로깅 (개발용)
- Alpine.js utils 스토어 (formatDate, formatDateTime)

**4. CSS 기능**
- HTMX 로딩 인디케이터
- fade-in 애니메이션
- 스피너 (로딩 중 표시)
- 프린트 시 .no-print 요소 숨김

---

### 확인 가능한 상태

#### 실행 방법
```bash
# 1. Docker 컨테이너 시작
cd docker
docker-compose up -d

# 2. 애플리케이션 실행
cd ..
./gradlew bootRun

# 3. 접속
http://localhost:8080
```

#### 확인 가능한 기능
- [x] MySQL 컨테이너 실행 (localhost:3306)
- [x] Redis 컨테이너 실행 (localhost:6379)
- [x] Flyway 마이그레이션 자동 실행 (9개 테이블 생성)
- [x] 기본 레이아웃 표시
- [x] 헤더/푸터 렌더링
- [x] HTMX 로드 (브라우저 개발자 도구 확인)
- [x] Alpine.js 로드
- [x] Tailwind CSS 적용 (Primary 색상: #FFEB3B)

---

### Phase 0 완료 체크리스트
- [x] Docker Compose로 MySQL + Redis 실행 가능
- [x] Flyway 마이그레이션으로 9개 테이블 생성
- [x] BaseEntity로 생성일/수정일 자동 관리
- [x] ApiResponse로 일관된 API 응답
- [x] GlobalExceptionHandler로 전역 예외 처리
- [x] QueryDSL 설정 완료
- [x] Redis 설정 완료
- [x] 기본 레이아웃 표시
- [x] HTMX + Alpine + Tailwind 로드

---

## 12. LazyInitializationException 방지 전략

### 문제
OSIV를 비활성화한 상태에서 뷰 계층에서 Lazy Loading된 연관관계 접근 시 `LazyInitializationException` 발생

### 원인
- `@ModelAttribute` 메서드와 뷰 컨트롤러는 트랜잭션 외부에서 실행됨
- 연관관계가 LAZY 로딩으로 설정된 경우 프록시 객체 반환
- 트랜잭션 종료 후 Hibernate Session 닫혀 프록시 접근 불가

### 해결 방법
1. **JOIN FETCH 쿼리**: 연관 엔티티를 함께 로드
2. **뷰용 서비스 메서드**: 뷰에서 필요한 데이터를 명시적으로 조회

### 구현 예시
```java
// Repository
@Query("SELECT m FROM Member m LEFT JOIN FETCH m.kindergarten WHERE m.id = :id AND m.deletedAt IS NULL")
Optional<Member> findByIdWithKindergarten(@Param("id") Long id);

// Service
public Member getMemberByIdWithKindergarten(Long id) {
    return memberRepository.findByIdWithKindergarten(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
}

// Controller
@ModelAttribute("currentMember")
public MemberResponse currentMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Member member = memberService.getMemberByIdWithKindergarten(userDetails.getMemberId());
    return MemberResponse.from(member); // kindergarten 접근 가능
}
```

### 변경 이력
- 2025-01-14: JOIN FETCH 쿼리 패턴 추가로 LazyInitializationException 해결

---

**Phase 0 완료일: 2024-12-28**
**업데이트일: 2025-01-14 (LazyInitializationException 방지 전략 추가)**
