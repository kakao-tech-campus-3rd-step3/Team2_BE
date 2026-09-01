#!/usr/bin/env bash

set -euo pipefail

readonly APP_DIR=/opt/pullit
readonly COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"
readonly ENV_FILE="$APP_DIR/pullit-production.env"
readonly DEPLOY_IMAGE="${PULLIT_BACKEND_IMAGE:-}"

if [[ ! -r "$ENV_FILE" ]]; then
  echo "운영 비밀 파일이 없습니다: $ENV_FILE" >&2
  exit 1
fi

if [[ -z "$DEPLOY_IMAGE" ]]; then
  echo "PULLIT_BACKEND_IMAGE가 없습니다." >&2
  exit 1
fi

cd "$APP_DIR"
chmod 750 "$APP_DIR/deploy-production.sh"
chmod 600 "$ENV_FILE"

set -a
source "$ENV_FILE"
set +a
export PULLIT_BACKEND_IMAGE="$DEPLOY_IMAGE"

for volume in "$PULLIT_DB_VOLUME" "$PULLIT_REDIS_VOLUME" "$PULLIT_RABBITMQ_VOLUME"; do
  docker volume inspect "$volume" >/dev/null
done

export PULLIT_BACKEND_IMAGE
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
for service in pullit-prod-db pullit-prod-redis pullit-prod-rabbitmq pullit-prod-tunnel; do
  if [[ -z "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running --services "$service")" ]]; then
    echo "필수 의존 서비스가 실행 중이 아닙니다: $service" >&2
    exit 1
  fi
done
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull pullit-prod-app pullit-prod-worker
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-deps pullit-prod-app pullit-prod-worker

for attempt in {1..30}; do
  if curl --fail --silent --show-error http://127.0.0.1:"${PULLIT_BACKEND_PORT:-18082}"/actuator/health >/dev/null; then
    echo "Pull-it backend health check passed."
    exit 0
  fi
  sleep 2
done

echo "Pull-it backend health check failed; previous data volumes were not modified." >&2
exit 1
