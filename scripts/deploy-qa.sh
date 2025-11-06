#!/bin/bash
set -euo pipefail

# ===================================================================
# EC2 호스트 Nginx와 연동하는 블루/그린 무중단 배포 스크립트
# 단일 Nginx 설정 파일의 'upstream api_backend' 블록을 수정한다.
# ===================================================================

# 0. 필수 변수 및 경로 설정
NGINX_CONF="/etc/nginx/conf.d/qa.api.pull.it.kr.conf"

cd /home/ubuntu/app

# 워커 서비스에서 사용할 이미지 변수 설정
export DOCKER_IMAGE_WORKER=$DOCKER_IMAGE_NAME:$IMAGE_TAG

# 1. 현재 활성 포트 확인 및 신규 배포 대상 결정
ACTIVE_LINE=$(sudo grep -A 2 'upstream api_backend {' "$NGINX_CONF" | grep 'server 127.0.0.1:' || echo "server 127.0.0.1:18080;")

if echo "$ACTIVE_LINE" | grep -q "18080"; then
  INACTIVE_PORT="18081"
  INACTIVE_COLOR="green"
else
  INACTIVE_PORT="18080"
  INACTIVE_COLOR="blue"
fi

echo "현재 활성 포트는 $(echo $ACTIVE_LINE | grep -o '[0-9]\+') 입니다. 비활성 포트 $INACTIVE_PORT ($INACTIVE_COLOR)에 배포를 시작합니다..."

# 2. 비활성 그룹에 새 버전의 애플리케이션 배포
if [ "$INACTIVE_COLOR" = "blue" ]; then
  export DOCKER_IMAGE_BLUE=$DOCKER_IMAGE_NAME:$IMAGE_TAG
  export DOCKER_IMAGE_GREEN=$(docker inspect --format='{{.Config.Image}}' pullit-qa-green 2>/dev/null || echo "$DOCKER_IMAGE_NAME:latest")
else
  export DOCKER_IMAGE_GREEN=$DOCKER_IMAGE_NAME:$IMAGE_TAG
  export DOCKER_IMAGE_BLUE=$(docker inspect --format='{{.Config.Image}}' pullit-qa-blue 2>/dev/null || echo "$DOCKER_IMAGE_NAME:latest")
fi

docker compose -f docker-compose.qa.yml pull "pullit-qa-$INACTIVE_COLOR" pullit-qa-worker
docker compose -f docker-compose.qa.yml up -d --no-deps "pullit-qa-$INACTIVE_COLOR" pullit-qa-worker

# 3. 새 버전 헬스 체크
echo "API 서버($INACTIVE_COLOR)가 정상 상태가 되기를 기다리는 중..."
HEALTH_STATUS="unhealthy"
for i in {1..30}; do
    if docker inspect --format="{{.State.Health.Status}}" "pullit-qa-$INACTIVE_COLOR" 2>/dev/null | grep -q "healthy"; then
        echo "API 서버($INACTIVE_COLOR)가 성공적으로 시작되었습니다!"
        HEALTH_STATUS="healthy"
        break
    fi
    echo "API 서버 헬스 체크 실패 (시도: $i). 10초 후 재시도합니다..."
    sleep 10
done

echo "워커 서버가 정상 상태가 되기를 기다리는 중..."
WORKER_HEALTH_STATUS="unhealthy"
for i in {1..30}; do
    if docker inspect --format="{{.State.Health.Status}}" "pullit-qa-worker" 2>/dev/null | grep -q "healthy"; then
        echo "워커 서버가 성공적으로 시작되었습니다!"
        WORKER_HEALTH_STATUS="healthy"
        break
    fi
    echo "워커 서버 헬스 체크 실패 (시도: $i). 10초 후 재시도합니다..."
    sleep 10
done

if [ "$HEALTH_STATUS" != "healthy" ] || [ "$WORKER_HEALTH_STATUS" != "healthy" ]; then
  echo "배포에 실패했습니다."
  if [ "$HEALTH_STATUS" != "healthy" ]; then
      echo "API 서버($INACTIVE_COLOR)가 정상적으로 시작되지 않았습니다."
      docker compose -f docker-compose.qa.yml logs "pullit-qa-$INACTIVE_COLOR"
  fi
  if [ "$WORKER_HEALTH_STATUS" != "healthy" ]; then
      echo "워커 서버가 정상적으로 시작되지 않았습니다."
      docker compose -f docker-compose.qa.yml logs "pullit-qa-worker"
  fi
  exit 1
fi

# 4. Nginx Upstream 설정 변경
echo "Nginx Upstream 설정을 $INACTIVE_PORT 포트로 변경합니다..."

# upstream api_backend { ... } 블록 안에서만 18080/18081을 교체
sudo sed -i \
  -e '/upstream api_backend {/,/}/ s/server 127\.0\.0\.1:1808[0-1];/server 127.0.0.1:'"$INACTIVE_PORT"';/' \
  "$NGINX_CONF"

# 변경된 설정 검사 후 reload
sudo nginx -t
sudo nginx -s reload

echo "배포 성공. Nginx가 $INACTIVE_COLOR($INACTIVE_PORT 포트)를 바라보도록 설정되었고, 워커 서버도 업데이트되었습니다."
