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
check_file "src/main/java/com/erp/domain/classroom/service/ClassroomCapacityService.java"
check_file "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_file "src/main/java/com/erp/domain/kidapplication/entity/KidApplication.java"

check_pattern "summarize\\(" "src/main/java/com/erp/domain/classroom/service/ClassroomCapacityService.java"
check_pattern "placeOnWaitlist\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "offer\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "acceptOffer\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "markOfferExpired\\(" "src/main/java/com/erp/domain/kidapplication/entity/KidApplication.java"

echo "checkpoint-22 OK"
