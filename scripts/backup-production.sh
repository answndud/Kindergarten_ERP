#!/usr/bin/env bash

set -Eeuo pipefail

if [[ "${1:-}" == "--dry-run" ]]; then
    cat <<'EOF'
Required runtime inputs:
  BACKUP_DIR, MYSQL_HOST, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD
  MYSQLDUMP_BIN, COMPOSE_ENV_FILE, COMPOSE_FILE
The real run creates a MySQL logical dump, a Redis RDB snapshot, and SHA-256 checksums.
EOF
    exit 0
fi

: "${BACKUP_DIR:?BACKUP_DIR is required}"
: "${MYSQL_HOST:?MYSQL_HOST is required}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE is required}"
: "${MYSQL_USER:?MYSQL_USER is required}"
: "${MYSQL_PASSWORD:?MYSQL_PASSWORD is required}"

MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQLDUMP_BIN="${MYSQLDUMP_BIN:-mysqldump}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
COMPOSE_ENV_FILE="${COMPOSE_ENV_FILE:-.env.prod}"
REDIS_SERVICE="${REDIS_SERVICE:-redis}"

command -v "$MYSQLDUMP_BIN" >/dev/null || { echo "mysqldump is required" >&2; exit 1; }
command -v docker >/dev/null || { echo "docker is required" >&2; exit 1; }
command -v sha256sum >/dev/null || { echo "sha256sum is required" >&2; exit 1; }

umask 077
stamp="$(date -u +%Y%m%dT%H%M%SZ)"
backup_dir="$BACKUP_DIR/$stamp"
mkdir -p "$backup_dir"

MYSQL_PWD="$MYSQL_PASSWORD" "$MYSQLDUMP_BIN" \
    --host="$MYSQL_HOST" \
    --port="$MYSQL_PORT" \
    --user="$MYSQL_USER" \
    --single-transaction \
    --quick \
    --routines \
    --events \
    --set-gtid-purged=OFF \
    "$MYSQL_DATABASE" > "$backup_dir/mysql.sql"

compose() {
    docker compose --env-file "$COMPOSE_ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

redis_container="$(compose ps -q "$REDIS_SERVICE")"
[[ -n "$redis_container" ]] || { echo "Redis container is not running" >&2; exit 1; }
redis_path="/tmp/erp-backup-$stamp.rdb"
compose exec -T "$REDIS_SERVICE" redis-cli --rdb "$redis_path" >/dev/null
docker cp "$redis_container:$redis_path" "$backup_dir/redis.rdb"
compose exec -T "$REDIS_SERVICE" rm -f "$redis_path" >/dev/null

(cd "$backup_dir" && sha256sum mysql.sql redis.rdb > SHA256SUMS)
printf 'Backup created: %s\n' "$backup_dir"
