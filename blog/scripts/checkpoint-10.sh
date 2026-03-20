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

check_file "src/main/resources/db/migration/V2__add_application_workflow.sql"
check_file "src/main/resources/db/migration/V4__create_calendar_events.sql"
check_file "src/main/resources/db/migration/V5__add_performance_indexes_for_dashboard_and_notepad.sql"
check_file "src/main/java/com/erp/domain/calendar/service/CalendarEventService.java"
check_file "src/main/java/com/erp/domain/dashboard/service/DashboardService.java"
check_file "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_file "src/main/java/com/erp/domain/kindergartenapplication/service/KindergartenApplicationService.java"

check_pattern "getEvents\\(" "src/main/java/com/erp/domain/calendar/service/CalendarEventService.java"
check_pattern "getDashboardStatistics\\(" "src/main/java/com/erp/domain/dashboard/service/DashboardService.java"
check_pattern "apply\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "approve\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "reject\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "cancel\\(" "src/main/java/com/erp/domain/kidapplication/service/KidApplicationService.java"
check_pattern "apply\\(" "src/main/java/com/erp/domain/kindergartenapplication/service/KindergartenApplicationService.java"

echo "checkpoint-10 OK"
