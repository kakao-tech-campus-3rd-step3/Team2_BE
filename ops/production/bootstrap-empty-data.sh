#!/usr/bin/env bash

set -euo pipefail

readonly APP_DIR=/opt/pullit
readonly COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"
readonly ENV_FILE="$APP_DIR/pullit-production.env"
readonly EMPTY_DATA_ACKNOWLEDGEMENT=I_HAVE_NO_LEGACY_PULLIT_BACKUP

if [[ "${PULLIT_INITIALIZE_EMPTY_DATA:-}" != "$EMPTY_DATA_ACKNOWLEDGEMENT" ]]; then
  echo "빈 Pull-it 데이터 초기화는 legacy DB 복구 불가를 확인한 뒤에만 허용됩니다." >&2
  echo "PULLIT_INITIALIZE_EMPTY_DATA=$EMPTY_DATA_ACKNOWLEDGEMENT 를 명시해 다시 실행하세요." >&2
  exit 1
fi

if [[ ! -r "$ENV_FILE" || ! -r "$COMPOSE_FILE" ]]; then
  echo "검증된 Pull-it runtime contract가 없습니다." >&2
  exit 1
fi

cd "$APP_DIR"
set -a
source "$ENV_FILE"
set +a

# 이 명령은 의존 서비스만 기동한다. compose 정적 검증 과정에서 앱 image
# interpolation도 필요하므로, 실제 이미지를 pull 하지 않는 안전한 placeholder를 둔다.
export PULLIT_BACKEND_IMAGE="${PULLIT_BACKEND_IMAGE:-registry.example.invalid/pullit-backend:bootstrap-validation}"

for volume in "$PULLIT_DB_VOLUME" "$PULLIT_REDIS_VOLUME" "$PULLIT_RABBITMQ_VOLUME"; do
  if docker volume inspect "$volume" >/dev/null 2>&1; then
    echo "기존 volume이 있어 빈 데이터 초기화를 중단합니다: $volume" >&2
    exit 1
  fi
done

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
for volume in "$PULLIT_DB_VOLUME" "$PULLIT_REDIS_VOLUME" "$PULLIT_RABBITMQ_VOLUME"; do
  docker volume create "$volume" >/dev/null
done

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d \
  pullit-prod-db pullit-prod-redis pullit-prod-rabbitmq pullit-prod-tunnel

echo "빈 Pull-it data volume을 초기화했습니다. 앱/worker 배포는 별도 CD workflow가 담당합니다."
