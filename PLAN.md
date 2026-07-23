# PLAN.md

## Goal

실제 운영에 투입하기 전에 권한 상승, 로그 유실, 배포 실패, 복구 불가, proxy IP 오인, 공급망 위험을 제거하고 검증 가능한 운영 기준을 만든다.

## Active

No active work

## External prerequisites

- 실제 provider sandbox는 외부 provider credential 및 endpoint가 필요하다.
- 운영 DB/Redis의 실제 restore drill과 Alertmanager 수신 채널 연결은 운영 인프라 승인 후 실행한다.
- CSP enforcement와 외부 asset 제거는 inline script/template 자산 전환 작업으로 별도 범위가 필요하다.
