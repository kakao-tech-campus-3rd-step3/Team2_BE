#!/bin/bash
set -euo pipefail

# ===================================================================
# EC2 호스트 Nginx와 연동하는 블루/그린 무중단 배포 스크립트
# 단일 Nginx 설정 파일의 'upstream api_backend' 블록을 수정한다.
# ===================================================================

# 0. 필수 변수 및 경로 설정
NGINX_CONF="/etc/nginx/conf.d/qa.api.pull.it.kr.conf"

cd /home/ubuntu/app

# 1. 현재 활성 포트 확인 및 신규 배포 대상 결정
ACTIVE_LINE=$(sudo grep -A 2 'upstream api_backend {' "$NGINX_CONF" | grep 'server 127.0.0.1:' || echo "server 127.0.0.1:18080;")

if echo "$ACTIVE_LINE" | grep -q "18080"; then
  INACTIVE_PORT="18081"
  INACTIVE_COLOR="green"
else
  INACTIVE_PORT="18080"
  INACTIVE_COLOR="blue"
fi

echo "Current Active Port is $(echo $ACTIVE_LINE | grep -o '[0-9]\+'). Deploying to Port $INACTIVE_PORT ($INACTIVE_COLOR)..."

# 2. 비활성 그룹에 새 버전의 애플리케이션 배포
if [ "$INACTIVE_COLOR" = "blue" ]; then
  export DOCKER_IMAGE_BLUE=$DOCKER_IMAGE_NAME:$IMAGE_TAG
  export DOCKER_IMAGE_GREEN=$(docker inspect --format='{{.Config.Image}}' pullit-qa-green 2>/dev/null || echo "$DOCKER_IMAGE_NAME:latest")
else
  export DOCKER_IMAGE_GREEN=$DOCKER_IMAGE_NAME:$IMAGE_TAG
  export DOCKER_IMAGE_BLUE=$(docker inspect --format='{{.Config.Image}}' pullit-qa-blue 2>/dev/null || echo "$DOCKER_IMAGE_NAME:latest")
fi

docker compose -f docker-compose.qa.yml pull "pullit-qa-$INACTIVE_COLOR"
docker compose -f docker-compose.qa.yml up -d --no-deps "pullit-qa-$INACTIVE_COLOR"

# 3. 새 버전 헬스 체크
echo "Waiting for pullit-qa-$INACTIVE_COLOR to be healthy..."
HEALTH_STATUS="unhealthy"
for i in {1..30}; do
    if docker inspect --format="{{.State.Health.Status}}" "pullit-qa-$INACTIVE_COLOR" 2>/dev/null | grep -q "healthy"; then
        echo "Service is healthy!"
        HEALTH_STATUS="healthy"
        break
    fi
    echo "Health check attempt $i failed. Retrying in 10 seconds..."
    sleep 10
done

if [ "$HEALTH_STATUS" != "healthy" ]; then
  echo "Deployment failed: pullit-qa-$INACTIVE_COLOR did not become healthy."
  docker compose -f docker-compose.qa.yml logs "pullit-qa-$INACTIVE_COLOR"
  exit 1
fi

# 4. Nginx Upstream 설정 변경
echo "Switching Nginx upstream to port $INACTIVE_PORT..."

# upstream api_backend { ... } 블록 안에서만 18080/18081을 교체
sudo sed -i \
  -e '/upstream api_backend {/,/}/ s/server 127\.0\.0\.1:1808[0-1];/server 127.0.0.1:'"$INACTIVE_PORT"';/' \
  "$NGINX_CONF"

# 변경된 설정 검사 후 reload
sudo nginx -t
sudo nginx -s reload

echo "Deployment successful. Switched to $INACTIVE_COLOR on port $INACTIVE_PORT."
