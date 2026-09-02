#!/usr/bin/env bash

# One-time bootstrap for an already verified legacy SQL dump. It creates only
# the dedicated target volumes and starts only stateful dependencies; it never
# starts the application, worker, or public tunnel.

set -euo pipefail

readonly APP_DIR=/opt/pullit
readonly COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"
readonly ENV_FILE="$APP_DIR/pullit-production.env"
readonly RESTORE_SCRIPT="$APP_DIR/restore-legacy-database.sh"
readonly RESTORE_ACKNOWLEDGEMENT=I_HAVE_VERIFIED_LEGACY_PULLIT_BACKUP

backup_file="${1:-}"
expected_sha256="${2:-}"

if [[ "${PULLIT_RESTORE_ACK:-}" != "$RESTORE_ACKNOWLEDGEMENT" ]]; then
  echo '레거시 DB 복원용 volume 생성은 검증된 백업을 확인한 경우에만 허용됩니다.' >&2
  exit 1
fi

if [[ ! -r "$ENV_FILE" || ! -r "$COMPOSE_FILE" || ! -x "$RESTORE_SCRIPT" ]]; then
  echo '검증된 Pull-it 복원 도구 또는 runtime contract가 없습니다.' >&2
  exit 1
fi

cd "$APP_DIR"
set -a
source "$ENV_FILE"
set +a
export PULLIT_BACKEND_IMAGE="${PULLIT_BACKEND_IMAGE:-registry.example.invalid/pullit-backend:restore-validation}"

for volume in "$PULLIT_DB_VOLUME" "$PULLIT_REDIS_VOLUME" "$PULLIT_RABBITMQ_VOLUME"; do
  if docker volume inspect "$volume" >/dev/null 2>&1; then
    echo "기존 volume이 있어 레거시 복원 bootstrap을 중단합니다: $volume" >&2
    exit 1
  fi
done

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
for volume in "$PULLIT_DB_VOLUME" "$PULLIT_REDIS_VOLUME" "$PULLIT_RABBITMQ_VOLUME"; do
  docker volume create "$volume" >/dev/null
done

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d \
  pullit-prod-db pullit-prod-redis pullit-prod-rabbitmq

for attempt in {1..30}; do
  if [[ -n "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running --services pullit-prod-db)" ]]; then
    break
  fi
  sleep 2
done

if [[ -z "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running --services pullit-prod-db)" ]]; then
  echo 'MariaDB가 시작되지 않았습니다. volume을 유지한 채 복원을 중단합니다.' >&2
  exit 1
fi

PULLIT_RESTORE_ACK="$RESTORE_ACKNOWLEDGEMENT" "$RESTORE_SCRIPT" "$backup_file" "$expected_sha256"
