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

check_file "src/main/resources/db/migration/V10__create_auth_audit_log.sql"
check_file "src/main/resources/db/migration/V11__denormalize_auth_audit_log_and_add_retention_archive.sql"
check_file "src/main/java/com/erp/domain/authaudit/service/AuthAuditRetentionService.java"
check_file "src/main/java/com/erp/domain/authaudit/controller/AuthAuditLogController.java"
check_file "src/main/java/com/erp/domain/domainaudit/controller/DomainAuditLogController.java"
check_file "src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java"

check_pattern "executeRetention\\(" "src/main/java/com/erp/domain/authaudit/service/AuthAuditRetentionService.java"
check_pattern "@GetMapping\\(\"/export\"\\)" "src/main/java/com/erp/domain/authaudit/controller/AuthAuditLogController.java"
check_pattern "@GetMapping\\(\"/export\"\\)" "src/main/java/com/erp/domain/domainaudit/controller/DomainAuditLogController.java"
check_pattern "class AuditConsolePerformanceSmokeTest" "src/test/java/com/erp/performance/AuditConsolePerformanceSmokeTest.java"

echo "checkpoint-24 OK"
