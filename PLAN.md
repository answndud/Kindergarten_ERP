# PLAN.md

## 작업명
- 후속 개선 배치 (문서 정합화 + API 안정성 + 테스트 보강)

## 1) 목표 / 범위
- 점검 결과로 식별된 P0/P1/P2 개선 과제를 한 배치로 처리한다.
- 문서 정합화: `README.md` API 표와 실제 컨트롤러 매핑을 재동기화한다.
- 보안/호환성: `SecurityConfig` CORS 허용 메서드에 `PATCH`를 반영한다.
- API 명확성: `ClassroomController`, `KidController`의 필수 필터 누락 요청을 명시적 400으로 처리한다.
- 데이터 품질: `KidApplication` 승인 시 ParentKid 관계를 고정값(FATHER)에서 요청 기반으로 전환한다.
- 테스트 보강: 지원 워크플로우/교실/유치원 API에 대한 통합 테스트를 추가한다.
- 문서 백로그 정리: `docs/project_plan.md`, `docs/requirements/dev-todo.md`를 현재 상태 기준으로 정리한다.

## 2) 세부 작업 단계
1. 문서/API 매핑 정합화
   - 컨트롤러 매핑 기준으로 README API 표 누락 항목 보완
   - 현재 구현과 다른/모호한 설명 제거

2. API/보안 코드 보강
   - CORS 허용 메서드에 PATCH 반영
   - kids/classrooms 목록 API의 필터 누락 요청에 대한 예외 코드 정의 및 400 응답 적용
   - KidApplication 승인 DTO/서비스를 확장해 ParentKid 관계를 요청값으로 저장

3. 테스트 보강
   - 지원 워크플로우 API 통합 테스트(성공/권한 실패) 추가
   - classroom/kindergarten API 통합 테스트 추가(핵심 시나리오)

4. 백로그/계획 문서 정리
   - `docs/requirements/dev-todo.md` 체크 상태 갱신
   - `docs/project_plan.md`를 현재 기준(완료/운영 중/후속 과제)으로 요약 정리

## 3) 검증 계획
- 코드 컴파일/테스트
  - `./gradlew compileJava compileTestJava`
  - `./gradlew test --tests "com.erp.api.ClassroomApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.KindergartenApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.KidApplicationApiIntegrationTest"`
  - `./gradlew test --tests "com.erp.api.KindergartenApplicationApiIntegrationTest"`
- 문서 정합성 점검
  - README API 표와 주요 컨트롤러 매핑 교차 확인
  - dev-todo/project_plan의 상태 표현이 현재 구현과 충돌하지 않는지 확인

## 4) 리스크 및 대응
- 목록 API의 400 전환으로 기존 프런트 요청과 충돌할 위험
  - 대응: 템플릿/스크립트 호출부를 확인하고, 필요한 경우 요청 파라미터 기본 주입
- ParentKid 관계 필드 추가로 기존 클라이언트 요청 호환성 저하 위험
  - 대응: 요청 DTO 기본값(FATHER)을 두어 하위 호환 유지
- 테스트 추가 시 시드/권한 전제 차이로 flaky 위험
  - 대응: 기존 BaseIntegrationTest 패턴 재사용, 역할별 계정 생성/인증 헬퍼 활용
