#!/usr/bin/env bash
set -euo pipefail
readonly APP_DIR=/opt/pullit/backend
readonly COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"
readonly ENV_FILE="$APP_DIR/pullit-production.env"
if [[ "${PULLIT_INITIALIZE_EMPTY_DATA:-}" != I_ACCEPT_NEW_PORTFOLIO_DATA ]]; then
  echo "PULLIT_INITIALIZE_EMPTY_DATA=I_ACCEPT_NEW_PORTFOLIO_DATA 를 명시하세요." >&2
  exit 1
fi
for volume in pullit-portfolio-db-data pullit-portfolio-redis-data pullit-portfolio-rabbitmq-data pullit-portfolio-storage-data; do
  if docker volume inspect "$volume" >/dev/null 2>&1; then
    echo "기존 Pull-it 전용 volume이 있어 초기화를 중단합니다: $volume" >&2
    exit 1
  fi
done
cd "$APP_DIR"
docker network inspect yeon-edge >/dev/null
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d pullit-prod-db pullit-prod-redis pullit-prod-rabbitmq pullit-prod-storage
echo "새 Pull-it 전용 named volume을 생성했습니다. 기존 Yeon 데이터는 건드리지 않았습니다."
