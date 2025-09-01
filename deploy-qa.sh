#!/bin/bash

# Pullit QA 서버 배포 스크립트
# 사용법: ./deploy-qa.sh

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수 확인
if [ -z "$DOCKER_REGISTRY" ]; then
    echo -e "${RED}Error: DOCKER_REGISTRY 환경 변수가 설정되지 않았습니다.${NC}"
    echo "사용법: export DOCKER_REGISTRY=your-registry.com/pullit"
    exit 1
fi

if [ -z "$DB_PASSWORD" ] || [ -z "$DB_ROOT_PASSWORD" ]; then
    echo -e "${RED}Error: DB_PASSWORD 또는 DB_ROOT_PASSWORD 환경 변수가 설정되지 않았습니다.${NC}"
    exit 1
fi

echo -e "${GREEN}🚀 Pullit QA 서버 배포 시작${NC}"

# 1. 이미지 빌드 및 푸시
echo -e "${YELLOW}📦 Docker 이미지 빌드 중...${NC}"
docker build -t ${DOCKER_REGISTRY}:latest .

echo -e "${YELLOW}⬆️ Docker 레지스트리에 이미지 푸시 중...${NC}"
docker push ${DOCKER_REGISTRY}:latest

echo -e "${GREEN}✅ 이미지 빌드 및 푸시 완료${NC}"

# 2. EC2에서 실행할 명령어 출력
echo -e "${GREEN}📋 EC2에서 실행할 명령어:${NC}"
echo "=========================================="
echo "# 1. EC2에 접속 후 다음 명령어들을 실행하세요:"
echo ""
echo "# 환경 변수 설정"
echo "export DOCKER_REGISTRY=${DOCKER_REGISTRY}"
echo "export DB_PASSWORD=${DB_PASSWORD}"
echo "export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD}"
echo ""
echo "# 2. Docker Compose 파일 다운로드 (또는 직접 생성)"
echo "curl -O https://raw.githubusercontent.com/your-repo/pullit/main/docker-compose.qa.yml"
echo ""
echo "# 3. 컨테이너 실행"
echo "docker-compose -f docker-compose.qa.yml up -d"
echo ""
echo "# 4. 로그 확인"
echo "docker-compose -f docker-compose.qa.yml logs -f pullit-qa-app"
echo ""
echo "# 5. 상태 확인"
echo "curl http://localhost:8080/actuator/health"
echo "=========================================="

echo -e "${GREEN}🎉 로컬 빌드 완료! 위 명령어들을 EC2에서 실행하세요.${NC}"
