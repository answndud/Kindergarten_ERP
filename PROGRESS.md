# PROGRESS.md

## 작업명
- 후속 개선 배치 (문서 정합화 + API 안정성 + 테스트 보강)

## 진행 로그

| 시간 (KST) | 상태 | 수행 내용 | 다음 액션 |
|---|---|---|---|
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
