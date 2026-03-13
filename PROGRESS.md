# PROGRESS.md

## 작업명
- 후속 고도화 (CI 실행 확인 + JWT 세션 설계 + 대시보드 지표 보정 + CI 최적화 + 인터뷰용 문서화)

## 진행 로그

| 시간 (KST) | 상태 | 수행 내용 | 다음 액션 |
|---|---|---|---|
| 2026-03-13 18:42 | IN_PROGRESS | 사용자 요청에 따라 남은 과제를 한 배치로 전환, `PLAN.md`를 CI 확인/JWT 세션 설계/대시보드 지표/CI 최적화 기준으로 갱신 | 원격 workflow run 상태 확인 후 Auth/Dashboard 구조 점검 |
| 2026-03-13 18:43 | DONE | `gh run`으로 첫 GitHub Actions 실패 원인 확인. `gradle-wrapper.jar`가 `.gitignore` 후행 `*.jar` 규칙에 막혀 runner에 누락되는 문제로 진단 | wrapper 추적 복구 + CI 분리 반영 |
| 2026-03-13 18:45 | DONE | JWT claim 확장(`memberId/sessionId/tokenType/jti`), Redis 세션 키 구조 도입, refresh rotation, 현재 세션 로그아웃/전체 세션 탈퇴 revoke 반영 | 인증 회귀 테스트 및 문서화 |
| 2026-03-13 18:47 | DONE | `announcement_view` 마이그레이션 추가, 공지 고유 열람률과 주말/입소일 반영 출석률로 대시보드 산식 보정, 집계 projection/쿼리 통합으로 쿼리 수 `13 -> 5` 확인 | Dashboard/Auth 테스트 및 README 반영 |
| 2026-03-13 18:52 | DONE | `fastTest`/`integrationTest` 태스크 추가, GitHub Actions 2-job 분리, README 및 `docs/phase/phase17~19` 문서화 완료. 검증: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.MemberApiIntegrationTest" --tests "com.erp.api.DashboardApiIntegrationTest"`, `./gradlew test --tests "com.erp.performance.DashboardPerformanceStoryTest"`, `./gradlew fastTest integrationTest`, `./gradlew test` 모두 통과 | 사용자 승인 시 add/commit/push |
| 2026-03-13 18:29 | DONE | `.github/workflows/ci.yml` 추가. PR/push/manual 트리거, Java 17, Gradle 캐시, `./gradlew test`, test report artifact 업로드 구성 | README/phase 문서와 교차 검토 |
| 2026-03-13 18:28 | DONE | `docs/phase/phase16_github_actions_ci.md`, `README.md`에 CI/Testcontainers 전략과 인터뷰용 설명 포인트 반영 | YAML/저장소 상태 검증 후 로그 마감 |
| 2026-03-13 18:27 | DONE | `ruby`로 `.github/workflows/ci.yml` YAML 파싱 검증, `git diff --check` 통과 | PROGRESS/PLAN 마감 |
| 2026-03-13 18:25 | IN_PROGRESS | 사용자 요청으로 후속 개선을 CI 자동화 작업으로 전환, `PLAN.md`를 GitHub Actions + Testcontainers 기준으로 갱신 | 워크플로우 부재 상태 확인 후 CI 파일/문서 반영 |
| 2026-03-13 18:19 | DONE | `docs/phase/phase15_testcontainers_integration_test_stack.md` 작성 완료. H2/Mock Redis 한계, Testcontainers 전환 이유, 구현 포인트, 면접 답변 포인트를 정리 | 최종 결과 공유 |
| 2026-03-13 18:18 | DONE | 검증 완료: `./gradlew test` 전체 통과. MySQL/Redis Testcontainers 기반 전체 테스트 스위트 정상 동작 확인 | 문서화 및 작업 로그 마무리 |
| 2026-03-13 18:05 | DONE | 검증 완료: `./gradlew test --tests "com.erp.ErpApplicationTests" --tests "com.erp.integration.ViewEndpointTest" --tests "com.erp.api.AuthApiIntegrationTest" --tests "com.erp.api.KidApiIntegrationTest" --tests "com.erp.api.AttendanceApiIntegrationTest" --tests "com.erp.api.NotepadApiIntegrationTest" --tests "com.erp.api.AnnouncementApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest"` 통과 | 전체 테스트 스위트 재검증 |
| 2026-03-13 18:03 | DONE | Testcontainers 지원 클래스, `application-test.yml`, Redis 설정, MySQL cleanup/reset 로직, 실제 Redis 기반 인증 테스트 반영 후 `./gradlew compileJava compileTestJava` 성공 | 대표 통합 테스트 실행 |
| 2026-03-13 18:06 | IN_PROGRESS | 사용자 요청에 따라 후속 개선을 Testcontainers 전환 작업으로 전환, `PLAN.md`를 테스트 스택 현실화 기준으로 갱신 | 테스트 설정/의존성/베이스 클래스 점검 후 코드 반영 |
| 2026-03-13 17:58 | DONE | `docs/phase/phase14_multitenant_access_hardening.md` 작성 완료. 문제 재현, 설계 결정, 테스트 증빙, 인터뷰 답변 포인트를 문서화 | 최종 결과 정리 및 사용자 공유 |
| 2026-03-13 17:56 | DONE | 검증 완료: `./gradlew test --tests "com.erp.api.KidApiIntegrationTest" --tests "com.erp.api.AttendanceApiIntegrationTest" --tests "com.erp.api.NotepadApiIntegrationTest" --tests "com.erp.api.AnnouncementApiIntegrationTest" --tests "com.erp.api.NotificationApiIntegrationTest"` 통과 | 문서화 및 작업 기록 마무리 |
| 2026-03-13 17:50 | DONE | 권한 하드닝 패치 후 `./gradlew compileJava compileTestJava` 성공. 컨트롤러 시그니처 누락 및 재조회 경로 일관성 정리 | 교차-유치원/soft delete 회귀 테스트 추가 |
| 2026-03-13 17:45 | DONE | `AccessPolicyService` 추가 및 `Kid`, `Classroom`, `Attendance`, `Notepad`, `Announcement`, `Notification` 서비스/컨트롤러에 요청자 기반 권한 검증 반영 | 컴파일 검증 및 테스트 추가 |
| 2026-03-13 17:34 | IN_PROGRESS | 사용자 요청에 따라 리뷰에서 구현 단계로 전환, `PLAN.md`를 권한 경계 하드닝 작업 기준으로 갱신 | 접근 정책 설계 후 코드/테스트/문서 순으로 반영 |
| 2026-03-13 17:30 | IN_PROGRESS | 사용자 요청으로 전면 코드 리뷰 시작, `PLAN.md`를 리뷰 작업 기준으로 갱신 | 설정/보안/도메인/테스트/문서 순으로 근거 수집 |
| 2026-03-13 17:34 | DONE | 보안 경계, 알림장/알림 권한, 테스트 현실성, 대시보드 지표 산식 중심으로 저장소 전면 리뷰 완료 | 사용자에게 severity 기반 리뷰 결과 전달 |
| 2026-02-21 10:05 | IN_PROGRESS | 사용자 요청으로 후속 개선 배치 시작, PLAN.md를 P0/P1/P2 통합 범위로 갱신 | 코드/문서/테스트 변경 순차 적용 |
| 2026-02-21 12:40 | DONE | `SecurityConfig` CORS 허용 메서드에 `PATCH` 추가, 목록 API(`/api/v1/kids`, `/api/v1/classrooms`) 필수 필터 누락 시 400 응답으로 명시화 | 입학 승인 ParentKid 관계값 개선 |
| 2026-02-21 12:50 | DONE | `ApproveKidApplicationRequest`에 `relationship` 확장 및 기본값(FATHER) 처리, 승인 시 요청 관계값으로 ParentKid 생성 반영 | API/백로그 문서 정합화 |
| 2026-02-21 12:58 | DONE | `README.md` API 표를 컨트롤러 매핑 기준으로 확장(알림/캘린더/지원/원생/출석 등), `docs/requirements/dev-todo.md`/`docs/project_plan.md` 최신 상태 반영 | 통합 테스트 보강 및 실행 |
| 2026-02-21 13:04 | DONE | 신규 통합 테스트 추가: `ClassroomApiIntegrationTest`, `KindergartenApiIntegrationTest`, `KidApplicationApiIntegrationTest`, `KindergartenApplicationApiIntegrationTest` + `KidApiIntegrationTest` 필터 누락 케이스 보강 | 최종 검증 및 작업 종료 |
| 2026-02-21 13:05 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.ClassroomApiIntegrationTest" --tests "com.erp.api.KindergartenApiIntegrationTest" --tests "com.erp.api.KidApplicationApiIntegrationTest" --tests "com.erp.api.KindergartenApplicationApiIntegrationTest" --tests "com.erp.api.KidApiIntegrationTest"` 통과 | 세션 종료 |
| 2026-02-20 22:05 | IN_PROGRESS | 사용자 요청에 따라 개선 작업 시작, PLAN.md를 이번 범위(코드 우선/문서 분리)로 갱신 | P0 코드 수정(공지사항/승인 처리) 적용 |
| 2026-02-20 22:10 | DONE | 공지사항 하드코딩 사용자 ID 제거 및 인증 사용자 기반 처리 반영, 서비스 유치원 경계 검증 추가 | 승인 처리 서비스 안정성/로직 보정 |
| 2026-02-20 22:12 | DONE | `KidApplicationService`/`KindergartenApplicationService` null 안전성 보강, 교사 지원 승인 후 후속 거절 대상을 같은 교사의 pending 지원서로 수정 | API 에러 계약 정합화 및 테스트 보강 |
| 2026-02-20 22:14 | DONE | 출석 daily 필수 파라미터 누락 시 400 응답으로 변경, 인증 진입점 응답을 `ApiResponse.error` 포맷으로 통일 | 컴파일/테스트 실행 |
| 2026-02-20 22:16 | DONE | 검증 완료: `./gradlew compileJava compileTestJava`, `./gradlew test --tests "com.erp.api.AnnouncementApiIntegrationTest"`, `./gradlew test --tests "com.erp.api.AttendanceApiIntegrationTest"`, `./gradlew test --tests "com.erp.api.AuthApiIntegrationTest"`, `./gradlew test --tests "com.erp.domain.kindergartenapplication.service.KindergartenApplicationServiceTest"` 통과 | 문서 분리 배치는 후속 진행 |
| 2026-02-20 22:22 | IN_PROGRESS | 문서 분리 배치 착수: PLAN 전환(문서 동기화), 컨트롤러/마이그레이션 기준 현행 기능 재확인 | README/CURRENT_FEATURES 업데이트 |
| 2026-02-20 22:28 | DONE | `README.md` 주요 기능/API/구조/실행 명령을 현재 구현 상태로 갱신(OAuth2, 캘린더, 지원/알림/대시보드 반영) | `CURRENT_FEATURES.md` 재작성 |
| 2026-02-20 22:31 | DONE | `CURRENT_FEATURES.md`를 실행/권한/도메인/검증 중심으로 전면 업데이트, 구식 Phase/예정 기능 제거 | 최종 교차 검토 및 작업 종료 |

## 현재 상태 요약
- 현재 단계: `DONE`
- 활성 작업: 없음
- 블로커: 없음
