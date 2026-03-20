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

check_file "src/main/resources/application-demo.yml"
check_file "src/main/java/com/erp/global/config/DataLoader.java"
check_file "docs/portfolio/demo/demo-preflight.md"
check_file "docs/portfolio/demo/demo-runbook.md"
check_file "docs/portfolio/architecture/system-architecture.md"
check_file "docs/portfolio/hiring-pack/backend-hiring-pack.md"
check_file "docs/portfolio/interview/interview_one_pager.md"
check_file "docs/portfolio/interview/interview_qa_script.md"
check_file "docs/portfolio/case-studies/auth-incident-response.md"

check_pattern "principal@test.com" "src/main/java/com/erp/global/config/DataLoader.java"
check_pattern "teacher1@test.com" "src/main/java/com/erp/global/config/DataLoader.java"
check_pattern "parent1@test.com" "src/main/java/com/erp/global/config/DataLoader.java"
check_pattern "principal@test.com / test1234!" "docs/portfolio/demo/demo-preflight.md"
check_pattern "teacher1@test.com / test1234!" "docs/portfolio/demo/demo-preflight.md"
check_pattern "parent1@test.com / test1234!" "docs/portfolio/demo/demo-preflight.md"

echo "checkpoint-26 OK"
