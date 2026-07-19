#!/usr/bin/env bash

set -Eeuo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
COMPOSE_ENV_FILE="${COMPOSE_ENV_FILE:-.env.prod}"
READINESS_URL="${READINESS_URL:-http://127.0.0.1:9091/actuator/health/readiness}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-30}"
SLEEP_SECONDS="${SLEEP_SECONDS:-5}"

compose() {
    docker compose --env-file "$COMPOSE_ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

wait_for_readiness() {
    local attempt
    for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
        if curl --fail --silent --show-error --max-time 5 "$READINESS_URL" >/dev/null; then
            return 0
        fi
        sleep "$SLEEP_SECONDS"
    done
    return 1
}

previous_image="$(docker inspect --format '{{.Config.Image}}' kindergarten-erp-app 2>/dev/null || true)"

compose pull
compose up -d

if wait_for_readiness; then
    echo "Deployment became ready with ${APP_IMAGE:?APP_IMAGE is required}."
    exit 0
fi

echo "Deployment failed readiness; starting rollback." >&2
if [[ -z "$previous_image" ]]; then
    echo "No previous app image was found; manual recovery is required." >&2
    compose ps >&2 || true
    compose logs --tail=200 app >&2 || true
    exit 1
fi

APP_IMAGE="$previous_image" compose up -d
if ! wait_for_readiness; then
    echo "Rollback also failed readiness; manual recovery is required." >&2
    compose ps >&2 || true
    compose logs --tail=200 app >&2 || true
    exit 1
fi

echo "Rollback restored ${previous_image}; the new release was not promoted." >&2
exit 1
