#!/usr/bin/env bash

set -Eeuo pipefail

backup_dir="${1:?usage: verify-production-backup.sh BACKUP_DIRECTORY}"
[[ -f "$backup_dir/mysql.sql" ]] || { echo "mysql.sql is missing" >&2; exit 1; }
[[ -s "$backup_dir/mysql.sql" ]] || { echo "mysql.sql is empty" >&2; exit 1; }
[[ -f "$backup_dir/redis.rdb" ]] || { echo "redis.rdb is missing" >&2; exit 1; }
[[ -s "$backup_dir/redis.rdb" ]] || { echo "redis.rdb is empty" >&2; exit 1; }
[[ -f "$backup_dir/SHA256SUMS" ]] || { echo "SHA256SUMS is missing" >&2; exit 1; }

(cd "$backup_dir" && sha256sum --check SHA256SUMS)
echo "Backup artifacts are present and checksums match: $backup_dir"
