# 🧪 현재 테스트 가능 기능

> IntelliJ IDEA 또는 IDE로 프로그램 실행 후 사용해볼 수 있는 기능 목록입니다.

---

## 실행 방법

### 1. 사전 요구사항
```bash
# Docker로 MySQL, Redis 실행
cd docker
docker-compose up -d

# 상태 확인
docker ps
```

### 2. IDE에서 실행
1. `ErpApplication.java` 우클릭 → Run 'ErpApplication'
2. 또는 터미널에서:
   ```bash
   ./gradlew bootRun
   ```

### 3. 접속 URL
- 애플리케이션: http://localhost:8080
- 로그인 페이지: http://localhost:8080/login
- 회원가입: http://localhost:8080/signup

---

## 현재 구현된 기능 (Phase 0~2)

### ✅ 인증 시스템 (Phase 1)

#### 1. 회원가입
**URL**: http://localhost:8080/signup

**테스트 방법**:
1. 회원가입 페이지 접속
2. 다음 정보 입력:
   - 이메일: `test@example.com`
   - 비밀번호: `Test1234!` (8자 이상, 영문+숫자+특수문자)
   - 비밀번호 확인: `Test1234!`
   - 이름: `테스트`
   - 전화번호: `01012345678` (선택)
   - 역할: 학부모 / 교사 / 원장 중 선택
3. "회원가입" 버튼 클릭
4. 성공 시 로그인 페이지로 이동

**API 테스트**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@example.com",
    "password": "Test1234!",
    "passwordConfirm": "Test1234!",
    "name": "김교사",
    "phone": "01011112222",
    "role": "TEACHER"
  }'
```

#### 2. 로그인
**URL**: http://localhost:8080/login

**테스트 방법**:
1. 로그인 페이지 접속
2. 가입한 이메일/비밀번호 입력
3. "로그인" 버튼 클릭
4. 성공 시 메인 페이지로 이동

**API 테스트**:
```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"parent@test.com","password":"Test1234!","passwordConfirm":"Test1234!","name":"홍길동","role":"PARENT"}' \
  -c cookies.txt

# 2. 로그인 (쿠키 저장)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"parent@test.com","password":"Test1234!"}' \
  -c cookies.txt

# 3. 현재 회원 정보 조회
curl -X GET http://localhost:8080/api/v1/auth/me \
  -b cookies.txt
```

#### 3. 로그아웃
**테스트 방법**:
1. 로그인 상태에서 헤더의 프로필 클릭
2. "로그아웃" 클릭
3. 로그인 페이지로 이동

**API 테스트**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -b cookies.txt \
  -c cookies.txt
```

#### 4. Access Token 갱신
**API 테스트**:
```bash
# Refresh Token으로 Access Token 갱신
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -b cookies.txt
```

---

### ✅ 데이터베이스

#### MySQL 접속 정보
```properties
Host: localhost:3306
Database: erp_db
Username: root (또는 erp_user)
Password: root1234 (또는 erp1234)
```

#### 생성된 테이블 (9개)
- `member` - 회원 정보
- `kindergarten` - 유치원 정보
- `classroom` - 반 정보
- `kid` - 원생 정보
- `parent_kid` - 학부모-원생 연결
- `attendance` - 출석 정보
- `notepad` - 알림장
- `notepad_read_confirm` - 알림장 읽음 확인
- `announcement` - 공지사항

#### DBeaver 또는 MySQL Workbench로 확인
```sql
-- 회원 목록 조회
SELECT id, email, name, role, status, created_at
FROM member;

-- 활성 회원만 조회
SELECT * FROM member WHERE status = 'ACTIVE';
```

---

### ✅ Redis 확인

#### Redis CLI 접속
```bash
redis-cli -h localhost -p 6379
```

#### Refresh Token 확인
```bash
# 모든 키 조회
KEYS *

# Refresh Token 확인
KEYS refresh:*

# 특정 토큰 조회
GET "refresh:user@example.com:{tokenId}"

# TTL 확인
TTL "refresh:user@example.com:{tokenId}"
```

---

## 🎯 추천 테스트 시나리오

### 시나리오 1: 학부모로 가입하기
```bash
1. http://localhost:8080/signup 접속
2. 정보 입력:
   - 이메일: parent@test.com
   - 비밀번호: Parent123!
   - 이름: 홍부모
   - 역할: 학부모
3. 가입 완료 후 로그인
```

### 시나리오 2: 교사로 가입하기
```bash
1. http://localhost:8080/signup 접속
2. 정보 입력:
   - 이메일: teacher@test.com
   - 비밀번호: Teacher123!
   - 이름: 김교사
   - 역할: 교사
3. 가입 완료 후 로그인
```

### 시나리오 3: 원장으로 가입하기
```bash
1. http://localhost:8080/signup 접속
2. 정보 입력:
   - 이메일: principal@test.com
   - 비밀번호: Principal123!
   - 이름: 박원장
   - 역할: 원장
3. 가입 완료 후 로그인
```

### 시나리오 4: 비밀번호 검증 테스트
```bash
# 너무 짧은 비밀번호 (8자 미만) → 실패
# 영문만 있는 비밀번호 → 실패
# 올바른 비밀번호 (Test1234!) → 성공
```

### 시나리오 5: 이메일 중복 테스트
```bash
1. parent@test.com으로 가입
2. 같은 이메일로 다시 가입 시도
3. "이미 사용 중인 이메일입니다" 에러 확인
```

---

## 🔍 브라우저 개발자 도구로 확인

### 1. 쿠키 확인
```
F12 → Application → Cookies → http://localhost:8080
```
로그인 후 다음 쿠키가 생성되어야 함:
- `access_token` (15분 유효)
- `refresh_token` (7일 유효)

### 2. 로컬 스토리지
현재 사용 안 함 (JWT는 쿠키에 저장)

### 3. 네트워크 탭
```
F12 → Network
```
다음 요청을 확인:
- POST /api/v1/auth/login (200 OK)
- GET /api/v1/auth/me (200 OK)

---

## 📝 로그 확인

### 애플리케이션 로그
```
# IntelliJ IDEA 콘솔에서 확인 가능
- JWT 인증 성공/실패 로그
- SQL 쿼리 로그 (Hibernate)
- 요청/응답 로그
```

### Docker 컨테이너 로그
```bash
# MySQL 로그
docker logs -f erp-mysql

# Redis 로그
docker logs -f erp-redis

# 전체 컨테이너 상태
docker-compose ps
```

---

## 🚨 에러 상황별 대처

### "이메일 또는 비밀번호가 잘못되었습니다"
- 이메일 또는 비밀번호 확인
- 회원가입이 되어 있는지 확인

### "회원을 찾을 수 없습니다"
- 가입되지 않은 이메일
- STATUS가 INACTIVE인 계정

### "토큰이 만료되었습니다"
- Access Token은 15분 유효
- `/api/v1/auth/refresh`로 갱신
- 또는 다시 로그인

### "접근 권한이 없습니다"
- 로그인이 필요한 페이지
- 권한이 없는 기능 (역할 확인)

---

### ✅ 유치원 & 반 관리 (Phase 2)

#### 유치원 관리
**API 테스트** (원장 권한 필요):

```bash
# 1. 원장으로 로그인 후 쿠키 저장
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"principal@test.com","password":"Principal123!"}' \
  -c cookies.txt

# 2. 유치원 등록
curl -X POST http://localhost:8080/api/v1/kindergartens \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "해바라기유치원",
    "address": "서울시 강남구 테헤란로 123",
    "phone": "0212345678",
    "openTime": "09:00",
    "closeTime": "18:00"
  }'

# 3. 전체 유치원 조회
curl -X GET http://localhost:8080/api/v1/kindergartens \
  -b cookies.txt

# 4. 특정 유치원 조회
curl -X GET http://localhost:8080/api/v1/kindergartens/1 \
  -b cookies.txt

# 5. 유치원 수정
curl -X PUT http://localhost:8080/api/v1/kindergartens/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "해바라기유치원",
    "address": "서울시 강남구 테헤란로 456",
    "phone": "0212349999",
    "openTime": "08:30",
    "closeTime": "18:30"
  }'

# 6. 유치원 삭제
curl -X DELETE http://localhost:8080/api/v1/kindergartens/1 \
  -b cookies.txt
```

#### 반(Classroom) 관리
**API 테스트** (원장, 교사 권한):

```bash
# 1. 반 생성
curl -X POST http://localhost:8080/api/v1/classrooms \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "kindergartenId": 1,
    "name": "해바라기반",
    "ageGroup": "5세반"
  }'

# 2. 유치원별 반 목록 조회
curl -X GET "http://localhost:8080/api/v1/classrooms?kindergartenId=1" \
  -b cookies.txt

# 3. 특정 반 조회
curl -X GET http://localhost:8080/api/v1/classrooms/1 \
  -b cookies.txt

# 4. 반 수정
curl -X PUT http://localhost:8080/api/v1/classrooms/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "kindergartenId": 1,
    "name": "장미반",
    "ageGroup": "4세반"
  }'

# 5. 담임 교사 배정 (원장만 가능)
curl -X PUT "http://localhost:8080/api/v1/classrooms/1/teacher?teacherId=2" \
  -b cookies.txt

# 6. 담임 교사 해제
curl -X DELETE http://localhost:8080/api/v1/classrooms/1/teacher \
  -b cookies.txt

# 7. 반 삭제 (Soft Delete)
curl -X DELETE http://localhost:8080/api/v1/classrooms/1 \
  -b cookies.txt
```

#### 권한별 접근 제어 테스트
```bash
# 원장(PRINCIPAL): 모든 기능 가능
# 교사(TEACHER): 반 생성/수정/삭제 가능, 유치원 관리 불가
# 학부모(PARENT): 조회만 가능

# 예: 교사가 유치원 생성 시도 → 403 Forbidden
curl -X POST http://localhost:8080/api/v1/kindergartens \
  -H "Content-Type: application/json" \
  -b teacher_cookies.txt \
  -d '{"name": "테스트유치원", ...}'
```

---

## 다음 Phase에서 추가될 기능

### Phase 3: 원생 관리 (다음)
- 원생 등록
- 학부모 연결
- 반 배정

### Phase 4: 출석 관리
- 일별 출석 체크
- 월별 통계

### Phase 5: 알림장
- 알림장 작성 (교사)
- 알림장 확인 (학부모)

### Phase 6: 공지사항
- 공지 작성/수정/삭제
- 중요 공지 설정

---

**마지막 업데이트: Phase 2 완료 (2024-12-28)**
