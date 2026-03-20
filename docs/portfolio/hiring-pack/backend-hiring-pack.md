# Backend Hiring Pack

이 문서는 채용 담당자와 면접관을 위한 1차 진입 문서입니다.

## 1. 이 프로젝트를 왜 봐야 하나

이 프로젝트는 유치원 ERP 기능 자체보다, 기능이 늘어날수록 중요해지는 **권한 경계, 인증 세션, 상태 전이 워크플로우, 감사 추적, 운영 관측성, 실환경 테스트**를 실제 코드와 문서로 닫아 둔 백엔드 포트폴리오입니다.

## 2. 5분 읽기 순서

1. [시스템 아키텍처](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/architecture/system-architecture.md)
2. [데모 Preflight](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-preflight.md)
3. [데모 Runbook](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/demo/demo-runbook.md)
4. [Auth Incident Response Case Study](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/case-studies/auth-incident-response.md)
5. [Interview One Pager](/Users/alex/project/kindergarten_ERP/erp/docs/portfolio/interview/interview_one_pager.md)

## 3. 대표 스토리 4개

- 멀티테넌시 권한 경계 하드닝: IDOR 성격 접근 문제를 requester 기반 서비스 검증으로 정리
- 인증 lifecycle: JWT session rotation, active session registry, rate limit, trusted proxy, audit trail
- 운영형 워크플로우: 반 정원, waitlist/offer, 출결 변경 요청 승인, 업무 감사 로그
- 운영성: Actuator readiness, Prometheus/Grafana, outbox retry/dead-letter, Testcontainers CI

## 4. 바로 보여줄 수 있는 증거

- 실행 중 API 계약: `/swagger-ui.html`, `/v3/api-docs`
- 운영 probe: `/actuator/health`, `/actuator/health/readiness`, `/actuator/prometheus`
- 운영 콘솔: `/audit-logs`, `/domain-audit-logs`
- CI: GitHub Actions `Backend CI`

## 5. 면접에서 먼저 꺼낼 문장

`기능을 많이 만드는 대신, 권한 경계와 상태 전이, 감사 로그, 운영 관측성, 실환경 테스트를 중심으로 백엔드를 고도화했습니다.`
