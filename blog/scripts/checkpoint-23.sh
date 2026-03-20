#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

check_file() {
  [[ -f "$1" ]] || { echo "Missing file: $1" >&2; exit 1; }
}

check_pattern() {
  local pattern="$1"
  local file="$2"
  rg -n "$pattern" "$file" >/dev/null || {
    echo "Missing pattern '$pattern' in $file" >&2
    exit 1
  }
}

check_file "src/main/resources/db/migration/V13__add_admission_workflow_attendance_requests_and_domain_audit.sql"
check_file "src/main/java/com/erp/domain/attendance/entity/AttendanceChangeRequest.java"
check_file "src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java"
check_file "src/main/java/com/erp/domain/attendance/controller/AttendanceChangeRequestController.java"
check_file "src/main/java/com/erp/domain/domainaudit/service/DomainAuditLogService.java"
check_file "src/main/java/com/erp/domain/domainaudit/controller/DomainAuditLogController.java"

check_pattern "create\\(" "src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java"
check_pattern "approve\\(" "src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java"
check_pattern "reject\\(" "src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java"
check_pattern "cancel\\(" "src/main/java/com/erp/domain/attendance/service/AttendanceChangeRequestService.java"
check_pattern "record\\(" "src/main/java/com/erp/domain/domainaudit/service/DomainAuditLogService.java"

echo "checkpoint-23 OK"
