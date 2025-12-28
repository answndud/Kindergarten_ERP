# 📝 유치원 ERP 개발 일지

> 프로젝트 개발 과정을 기록합니다.  
> 면접 대비 및 학습 내용 정리 목적입니다.

---

## 2025-12-28 (Day 1)

### ✅ 완료 작업

#### 1. 프로젝트 초기 생성
- **도구**: Spring Initializr (https://start.spring.io/)
- **설정**:
  - Spring Boot 3.5.9
  - Java 17
  - Gradle (Groovy)
  - Group: `com.erp`
  - Artifact: `erp`
- **기본 의존성**: Spring Web, Data JPA, Security, Validation, Thymeleaf, Lombok, MySQL, Redis

---

#### 2. build.gradle 의존성 추가

##### 2-1. QueryDSL 추가

```groovy
// QueryDSL
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

**왜 QueryDSL인가?**
- JPA의 JPQL은 문자열 기반 → 컴파일 시점에 오류를 잡을 수 없음
- QueryDSL은 Q클래스(메타모델)를 생성하여 **타입 안전한 쿼리** 작성 가능
- IDE 자동완성 지원으로 생산성 향상
- 동적 쿼리(조건에 따라 WHERE절 변경 등) 작성이 매우 편리

**면접 예상 질문**:
> Q: "QueryDSL을 왜 선택했나요?"  
> A: "JPQL은 문자열 기반이라 런타임에야 오류를 발견할 수 있지만, QueryDSL은 컴파일 타임에 쿼리 오류를 잡을 수 있습니다. 또한 동적 쿼리 작성 시 BooleanBuilder나 BooleanExpression을 활용하면 조건별 쿼리를 깔끔하게 조합할 수 있어 선택했습니다."

> Q: "jakarta classifier는 왜 붙였나요?"  
> A: "Spring Boot 3.x부터 Jakarta EE 9+를 사용하므로, javax 패키지가 jakarta로 변경되었습니다. 따라서 querydsl-jpa:5.0.0:jakarta를 사용해야 호환됩니다."

---

##### 2-2. JWT 라이브러리 추가

```groovy
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

**왜 jjwt인가?**
- Java에서 가장 널리 사용되는 JWT 라이브러리
- API와 구현체 분리 설계 (인터페이스 기반)
- 서명 알고리즘(HS256, RS256 등) 다양하게 지원

**왜 3개로 분리되어 있나?**
| 의존성 | 역할 | 스코프 |
|--------|------|--------|
| jjwt-api | JWT 생성/파싱 인터페이스 | implementation |
| jjwt-impl | 실제 구현체 | runtimeOnly |
| jjwt-jackson | JSON 직렬화 (Jackson 사용) | runtimeOnly |

- 컴파일 시점에는 API만 필요하고, 런타임에 구현체가 주입됨
- **의존성 역전 원칙(DIP)** 적용된 설계

**면접 예상 질문**:
> Q: "세션 대신 JWT를 선택한 이유는?"  
> A: "세션은 서버에 상태를 저장하므로 수평 확장(Scale-out) 시 세션 동기화 문제가 발생합니다. JWT는 토큰 자체에 정보를 담아 Stateless하게 운영할 수 있어, 마이크로서비스나 로드밸런싱 환경에서 유리합니다."

> Q: "JWT의 단점은?"  
> A: "토큰 탈취 시 만료 전까지 무효화가 어렵습니다. 이를 보완하기 위해 Refresh Token을 Redis에 저장하고, Access Token은 짧은 만료시간(15분)을 설정할 예정입니다."

---

##### 2-3. Flyway 추가

```groovy
// Flyway (DB Migration)
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'
```

**왜 Flyway인가?**
- DB 스키마 변경을 **코드로 버전 관리**
- 팀원 간 DB 동기화 자동화
- 배포 시 자동 마이그레이션 실행
- 롤백 가능한 마이그레이션 이력 관리

**Flyway vs Liquibase**:
| 항목 | Flyway | Liquibase |
|------|--------|-----------|
| 설정 | 간단 (SQL 파일만) | 복잡 (XML/YAML/JSON) |
| 학습곡선 | 낮음 | 높음 |
| 롤백 | 유료 버전 | 무료 지원 |
| 추천 | 단순 프로젝트 | 복잡한 엔터프라이즈 |

**마이그레이션 파일 네이밍 규칙**:
```
V1__init_schema.sql        # 버전 1: 초기 스키마
V2__add_classroom.sql      # 버전 2: classroom 테이블 추가
V3__add_index_member.sql   # 버전 3: member 인덱스 추가
```
- `V{버전}__{설명}.sql` 형식
- 언더스코어 2개(`__`)로 버전과 설명 구분

**면접 예상 질문**:
> Q: "JPA의 ddl-auto 대신 Flyway를 쓰는 이유는?"  
> A: "ddl-auto는 개발 환경에서는 편리하지만, 운영 환경에서는 위험합니다. 테이블이 예기치 않게 변경되거나 데이터가 손실될 수 있습니다. Flyway는 명시적인 SQL 스크립트로 변경 이력을 관리하여 안전하고 추적 가능합니다."

---

#### 3. 디렉토리 구조 생성 (1-3)

프로젝트 패키지 구조를 생성했습니다.

```
src/main/java/com/erp/
├── ErpApplication.java
├── global/                    # 전역 설정
│   ├── config/               # 설정 클래스 (Security, JPA, Redis 등)
│   ├── exception/            # 예외 처리
│   ├── security/             # 보안 관련
│   │   └── jwt/              # JWT 토큰 처리
│   ├── common/               # 공통 클래스 (BaseEntity, ApiResponse)
│   └── util/                 # 유틸리티
└── domain/                    # 도메인별 패키지
    ├── member/               # 회원
    ├── auth/                 # 인증
    ├── kindergarten/         # 유치원
    ├── classroom/            # 반
    ├── kid/                  # 원생
    ├── attendance/           # 출석
    ├── notepad/              # 알림장
    └── announcement/         # 공지사항
```

**패키지 구조 설계 원칙**:
- `global`: 전체 애플리케이션에서 공통으로 사용하는 설정, 예외, 유틸
- `domain`: 비즈니스 도메인별로 분리 (DDD 영향)
- 각 도메인 내부: `controller`, `service`, `repository`, `entity`, `dto`로 계층 분리

**면접 예상 질문**:
> Q: "왜 이런 패키지 구조를 선택했나요?"  
> A: "도메인 주도 설계(DDD)의 영향을 받아, 기능별이 아닌 도메인별로 패키지를 구성했습니다. 이렇게 하면 특정 도메인의 코드가 한 곳에 모여 있어 응집도가 높아지고, 팀원이 담당 도메인만 집중해서 개발할 수 있습니다."

---

#### 4. application.yml 환경별 분리 설정 (1-4)

`.properties` → `.yml`로 변경하고, 환경별 설정 파일을 분리했습니다.

**생성된 파일**:
| 파일 | 용도 |
|------|------|
| `application.yml` | 공통 설정 (모든 환경) |
| `application-local.yml` | 로컬 개발 환경 |
| `application-prod.yml` | 운영 환경 |

##### 주요 설정 내용

**application.yml (공통)**:
```yaml
spring:
  profiles:
    active: local  # 기본 활성 프로파일
  jpa:
    open-in-view: false  # OSIV 비활성화 (성능 최적화)
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: ${JWT_SECRET:default-secret-key}
  access-token-validity: 900000      # 15분
  refresh-token-validity: 604800000  # 7일
```

**application-local.yml (개발)**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/erp_db
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway 사용하므로 validate
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379
```

**application-prod.yml (운영)**:
```yaml
spring:
  datasource:
    url: ${DB_URL}  # 환경변수로 주입
  jpa:
    hibernate:
      ddl-auto: none  # 운영에서는 절대 auto 금지
    show-sql: false
  flyway:
    clean-disabled: true  # 운영에서는 clean 절대 금지
```

**주요 설정 설명**:

| 설정 | 값 | 이유 |
|------|-----|------|
| `open-in-view` | false | LazyLoading이 View 렌더링까지 연장되는 것 방지 (성능 이슈) |
| `ddl-auto` | validate/none | Flyway로 스키마 관리하므로 자동 생성 금지 |
| `clean-disabled` | true (prod) | 운영 DB가 실수로 초기화되는 것 방지 |
| `${ENV_VAR}` | 환경변수 | 민감 정보를 코드에 노출하지 않음 |

**면접 예상 질문**:
> Q: "open-in-view를 왜 false로 설정했나요?"  
> A: "OSIV(Open Session In View)가 true면 HTTP 요청 전체에 걸쳐 영속성 컨텍스트가 유지됩니다. View 렌더링 중에도 DB 커넥션을 점유하게 되어 커넥션 풀 고갈 위험이 있습니다. 또한 Controller에서 LazyLoading이 발생하면 N+1 문제를 파악하기 어려워집니다."

> Q: "환경별 설정을 어떻게 분리했나요?"  
> A: "Spring Profile을 활용했습니다. 공통 설정은 application.yml에, 환경별 설정은 application-{profile}.yml에 분리했습니다. 운영 환경에서는 민감 정보를 환경변수로 주입받도록 했습니다."

---

#### 5. Docker 개발 환경 구축

로컬 개발을 위한 MySQL, Redis를 Docker Compose로 구성했습니다.

##### docker-compose.yml

```yaml
services:
  # MySQL 8.0
  mysql:
    image: mysql:8.0
    container_name: erp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root1234
      MYSQL_DATABASE: erp_db
      MYSQL_USER: erp_user
      MYSQL_PASSWORD: erp1234
      TZ: Asia/Seoul
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci

  # Redis 7.x
  redis:
    image: redis:7-alpine
    container_name: erp-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
```

**Docker Compose 사용 명령어**:
```bash
# 시작
cd docker && docker-compose up -d

# 상태 확인
docker ps

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down

# 완전 초기화 (볼륨 삭제)
docker-compose down -v
```

**DBeaver 연결 정보**:
| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3306 |
| Database | erp_db |
| Username | erp_user |
| Password | erp1234 |

**왜 Docker를 사용하나?**
- 로컬 환경에 MySQL/Redis 직접 설치 불필요
- 팀원 간 동일한 개발 환경 보장
- 컨테이너 삭제/재생성으로 깔끔한 초기화
- 운영 환경(AWS RDS 등)과 유사한 환경 구성 가능

**면접 예상 질문**:
> Q: "왜 로컬에 직접 MySQL을 설치하지 않고 Docker를 사용했나요?"  
> A: "Docker를 사용하면 팀원 모두가 동일한 버전의 MySQL(8.0)과 동일한 설정으로 개발할 수 있습니다. 또한 `docker-compose down -v`로 데이터를 완전히 초기화할 수 있어 테스트 환경을 관리하기 편합니다."

> Q: "Docker 볼륨은 왜 사용하나요?"  
> A: "컨테이너가 삭제되어도 데이터는 유지하기 위함입니다. mysql_data 볼륨에 DB 데이터가 저장되어, 컨테이너를 재시작해도 데이터가 사라지지 않습니다. 완전 초기화가 필요하면 `-v` 옵션으로 볼륨까지 삭제합니다."

---

### 📁 현재 프로젝트 구조

```
erp/
├── build.gradle                    ✅ 의존성 추가 완료
├── settings.gradle
├── docker/                          ✅ Docker 환경
│   ├── docker-compose.yml          # MySQL + Redis
│   ├── .env                        # 환경변수 (git 제외)
│   └── init/
│       └── 01_init.sql             # DB 초기화 스크립트
├── src/
│   ├── main/
│   │   ├── java/com/erp/
│   │   │   ├── ErpApplication.java
│   │   │   ├── global/             ✅ 전역 설정 패키지
│   │   │   │   ├── config/
│   │   │   │   ├── exception/
│   │   │   │   ├── security/jwt/
│   │   │   │   ├── common/
│   │   │   │   └── util/
│   │   │   └── domain/             ✅ 도메인 패키지
│   │   │       ├── member/
│   │   │       ├── auth/
│   │   │       ├── kindergarten/
│   │   │       ├── classroom/
│   │   │       ├── kid/
│   │   │       ├── attendance/
│   │   │       ├── notepad/
│   │   │       └── announcement/
│   │   └── resources/
│   │       ├── application.yml        ✅ 공통 설정
│   │       ├── application-local.yml  ✅ 로컬 환경
│   │       ├── application-prod.yml   ✅ 운영 환경
│   │       └── db/migration/          ✅ Flyway 마이그레이션
│   └── test/
└── docs/
    ├── project_idea.md
    ├── project_summary.md
    └── project_diary.md
```

### 🐳 Docker 컨테이너 상태

```
NAMES       STATUS              PORTS
erp-mysql   Up (healthy)        0.0.0.0:3306->3306/tcp
erp-redis   Up (healthy)        0.0.0.0:6379->6379/tcp
```

---

#### 6. 구현 가이드 상세화

`docs/project_idea.md`의 Step 2~9 구현 가이드를 상세하게 수정했습니다.

**변경 내용**:
| Step | 기존 | 변경 후 |
|------|------|---------|
| 2~9 전체 | 클래스명만 나열 | 필드, 메서드, 어노테이션까지 상세 작성 |
| UI/UX 부분 | 직접 구현 | "Cursor 담당"으로 표시 |

**각 도메인별 구현 패턴**:
```
Entity
├── 필드 정의 (어노테이션 포함)
├── 정적 팩토리 메서드 create()
└── 비즈니스 메서드들

Repository
├── JpaRepository 기본 메서드
├── 쿼리 메서드 (findByXxx)
└── RepositoryCustom (QueryDSL)

DTO
├── Request: Validation 어노테이션
└── Response: 정적 팩토리 메서드 from()

Service
├── @Transactional(readOnly = true) 기본
├── 변경 메서드만 @Transactional
└── CRUD + 비즈니스 메서드

Controller
├── @RestController + @RequestMapping
├── @Valid + @RequestBody
└── @AuthenticationPrincipal 인증 정보
```

**워크플로우 확정**:
```
1️⃣ IntelliJ에서 project_idea.md 보면서 직접 코딩
   ↓
2️⃣ 완성 후 Cursor에 "2-1 완료!" 요청
   ↓
3️⃣ Cursor가 코드 리뷰 + 일지 업데이트
   ↓
4️⃣ 프론트엔드는 Cursor가 담당
```

---

#### 7. Spring Boot 튜토리얼 문서 작성

범용 Spring Boot 프로젝트 구현 가이드를 작성했습니다.

**파일**: `docs/springboot_tutorial.md`

**포함 내용**:
| 섹션 | 내용 |
|------|------|
| 1. 프로젝트 생성 | Spring Initializr, 의존성 |
| 2. 프로젝트 구조 | 도메인형 패키지, 네이밍 컨벤션 |
| 3. 설정 파일 | application.yml 환경별 분리 |
| 4. 공통 컴포넌트 | BaseEntity, ApiResponse, Exception |
| 5. 도메인 구현 | Entity → Repository → DTO → Service → Controller |
| 6. 테스트 코드 | 단위/통합 테스트 패턴 |
| 7. 보안 구현 | Spring Security + JWT |
| 8. 코드 패턴 | Soft Delete, 페이징, N+1 해결 |

**활용 방법**:
- 이 프로젝트뿐만 아니라 모든 Spring Boot 프로젝트에 적용 가능
- 면접 대비 자료로도 활용 가능

---

