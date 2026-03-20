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

check_file "src/main/java/com/erp/global/common/ApiResponse.java"
check_file "src/main/java/com/erp/global/common/BaseEntity.java"
check_file "src/main/java/com/erp/global/exception/BusinessException.java"
check_file "src/main/java/com/erp/global/exception/ErrorCode.java"
check_file "src/main/java/com/erp/global/exception/GlobalExceptionHandler.java"

check_pattern "ApiResponse" "src/main/java/com/erp/global/common/ApiResponse.java"
check_pattern "class BaseEntity" "src/main/java/com/erp/global/common/BaseEntity.java"
check_pattern "class BusinessException" "src/main/java/com/erp/global/exception/BusinessException.java"
check_pattern "enum ErrorCode" "src/main/java/com/erp/global/exception/ErrorCode.java"
check_pattern "class GlobalExceptionHandler" "src/main/java/com/erp/global/exception/GlobalExceptionHandler.java"

echo "checkpoint-06 OK"
