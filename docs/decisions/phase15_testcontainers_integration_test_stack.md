# Phase 15: MySQL/Redis Testcontainers 기반 통합 테스트 전환

## 배경

- 기존 테스트 스택은 H2 메모리 DB, `ddl-auto=create-drop`, Flyway 비활성화, Redis mock 기반이었다.
- 이 구성은 테스트 속도는 빠르지만, 실제 운영 환경(MySQL 8 + Redis + Flyway)과 차이가 커서 포트폴리오 신뢰도를 깎는다.
- 특히 이 프로젝트는 JWT refresh token을 Redis에 저장하고, Flyway 마이그레이션으로 스키마를 관리하기 때문에 운영 스택 검증이 빠진 상태로는 면접에서 설계 완성도를 설명하기 어렵다.

## 문제점

1. H2와 MySQL의 SQL/DDL 차이를 테스트가 잡지 못한다.
2. Flyway를 끄면 실제 마이그레이션이 엔티티와 맞는지 검증할 수 없다.
3. Redis를 mock 처리하면 로그인/토큰 갱신 흐름이 진짜 저장소를 통하지 않는다.
4. 테스트는 통과해도 운영 환경에서만 깨질 수 있는 구간을 놓친다.

## 결정

1. 테스트 프로필을 H2 대신 MySQL Testcontainer에 연결한다.
2. Redis mock을 제거하고 Redis Testcontainer를 실제로 연결한다.
3. `application-test.yml`에서도 Flyway를 활성화하고 JPA는 `validate`로 둔다.
4. 컨테이너 속성은 공통 지원 클래스에서 동적으로 주입해 모든 통합 테스트가 같은 인프라를 공유하도록 한다.

## 구현 요약

### 1) 의존성 추가

- `build.gradle`
  - `org.testcontainers:testcontainers`
  - `org.testcontainers:junit-jupiter`
  - `org.testcontainers:mysql`

### 2) 공통 컨테이너 지원 클래스 도입

- `TestcontainersSupport`
  - MySQL 8.0.36, Redis 7-alpine 컨테이너를 static으로 기동
  - `@DynamicPropertySource`로 datasource, flyway, redis 속성을 주입
- `BaseIntegrationTest`, `ErpApplicationTests`, `ViewEndpointTest`가 이 베이스를 사용하도록 정리

### 3) 테스트 프로필 현실화

- `application-test.yml`
  - H2 설정 제거
  - `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver`
  - Flyway 활성화
  - JPA `ddl-auto=validate`
- 의미:
  - 테스트가 애플리케이션 엔티티가 아니라 실제 마이그레이션 결과를 기준으로 실행된다.

### 4) Redis mock 제거

- `BaseIntegrationTest`의 `@MockitoBean RedisTemplate`, `RedisConnectionFactory` 제거
- 실제 `RedisTemplate`을 주입받도록 변경
- 테스트 시작 시 Redis DB flush로 상태를 초기화
- `AuthApiIntegrationTest`는 Mockito stub 대신 실제 Redis 값을 읽고 덮어쓰는 방식으로 수정

### 5) MySQL 기준 cleanup/reset 로직 보정

- H2 전용 `ALTER COLUMN ID RESTART` 구문 제거
- MySQL용 `ALTER TABLE ... AUTO_INCREMENT = 1`로 교체
- 테스트 데이터 cleanup은 `SET FOREIGN_KEY_CHECKS = 0/1` + lowercase table name 기준으로 정리
- `calendar_event`, `kid_application`, `kindergarten_application`까지 포함해 누락 없이 초기화

## 검증

실행 명령:

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests "com.erp.ErpApplicationTests" \
  --tests "com.erp.integration.ViewEndpointTest" \
  --tests "com.erp.api.AuthApiIntegrationTest" \
  --tests "com.erp.api.KidApiIntegrationTest" \
  --tests "com.erp.api.AttendanceApiIntegrationTest" \
  --tests "com.erp.api.NotepadApiIntegrationTest" \
  --tests "com.erp.api.AnnouncementApiIntegrationTest" \
  --tests "com.erp.api.NotificationApiIntegrationTest"
./gradlew test
```

결과:

- 컴파일 성공
- 대표 통합 테스트 성공
- 전체 테스트 스위트 성공

## 인터뷰 답변 포인트

### 1) 왜 H2를 버렸는가

- 빠르다는 장점은 있지만, 운영 DB와 달라서 "테스트가 통과해도 운영에서 깨지는" 위험을 줄이지 못했다.
- 포트폴리오에서는 테스트 개수보다 "운영과 얼마나 닮은 환경에서 검증했는가"가 더 중요하다고 판단했다.

### 2) 왜 Flyway + validate 조합을 선택했는가

- 테스트에서도 실제 마이그레이션을 적용해야 엔티티와 스키마가 drift 없이 맞는지 검증할 수 있다.
- `ddl-auto=create-drop`는 편하지만, 실제 운영 스키마 기준 검증이라는 목적에는 맞지 않았다.

### 3) Redis mock 제거의 의미는 무엇인가

- JWT refresh token이 실제로 Redis에 저장되고 조회되는지 검증해야 인증 설계가 완성된다.
- mock 기반 테스트는 "메서드 호출"만 검증하지 "저장소 연동"은 검증하지 못한다.

### 4) 이 작업으로 무엇이 좋아졌는가

- 보안/인증/조회 권한 테스트가 운영 인프라와 더 가까운 조건에서 실행된다.
- 면접에서 "왜 이 테스트를 믿을 수 있나요?"라는 질문에 더 설득력 있게 답할 수 있다.
- 성능 최적화나 보안 하드닝 작업도 이제 더 신뢰도 높은 기반 위에서 검증할 수 있다.

## 트레이드오프

- 장점
  - 테스트 신뢰도 상승
  - Flyway/Redis 연동 실제 검증
  - 운영 환경과의 차이 축소
- 단점
  - Docker 의존성이 생긴다
  - 테스트 실행 시간이 H2 대비 길어진다
  - 테스트 cleanup/초기화 코드가 더 엄격해져야 한다

## 후속 과제

1. CI에서도 Docker 기반 테스트를 안정적으로 돌릴 수 있게 workflow를 정리
2. 테스트를 “빠른 레이어(unit/slice)”와 “실환경형 통합 테스트”로 분리해 실행 전략 최적화
3. README/포트폴리오 소개 문서에 "왜 Testcontainers를 도입했는가"를 짧게 연결
