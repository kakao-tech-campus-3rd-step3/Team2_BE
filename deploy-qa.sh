#!/bin/bash

# Pullit QA 서버 배포 스크립트
# 사용법: . .env && ./deploy-qa.sh (env 파일 로드 후 실행)

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수 확인
if [ -z "$DOCKER_IMAGE_NAME" ]; then
    echo -e "${RED}Error: DOCKER_IMAGE_NAME 환경 변수가 설정되지 않았습니다.${NC}"
    echo "사용법: . .env 파일에 DOCKER_IMAGE_NAME=your-registry/pullit-qa 형식으로 정의하세요."
    exit 1
fi

if [ -z "$DB_PASSWORD" ] || [ -z "$DB_ROOT_PASSWORD" ] || [ -z "$GOOGLE_API_KEY" ] || [ -z "$S3_ACCESS_KEY" ] || [ -z "$S3_SECRET_KEY" ] || [ -z "$KAKAO_REST_API_KEY" ] || [ -z "$DB_USERNAME" ] || [ -z "$KAKAO_CLIENT_SECRET" ] || [ -z "$BASE_URI" ] || [ -z "$SCHEME" ] || [ -z "$ACCESS_CONTROL_ALLOWED_ORIGINS" ]; then
    echo -e "${RED}Error: 필수 배포 환경 변수(DB_*, GOOGLE_*, etc.)가 모두 설정되지 않았습니다.${NC}"
    exit 1
fi

# 현재 Git 커밋 해시 가져오기 (전체 SHA)
GIT_SHA=$(git rev-parse HEAD)
if [ -z "$GIT_SHA" ]; then
    echo -e "${RED}Error: Git commit SHA를 가져올 수 없습니다. Git 레포지토리 안에서 실행해주세요.${NC}"
    exit 1
fi

# Docker 이미지 태그 생성
SHA_IMAGE_TAG="${DOCKER_IMAGE_NAME}:${GIT_SHA}"
LATEST_IMAGE_TAG="${DOCKER_IMAGE_NAME}:latest"


echo -e "${GREEN} Pullit QA 서버 배포 시작${NC}"
echo -e "사용할 이미지 태그: ${YELLOW}${SHA_IMAGE_TAG}${NC}"

# 1. 이미지 빌드 및 푸시
echo -e "${YELLOW} Docker 이미지 빌드 중...${NC}"
docker build -t ${SHA_IMAGE_TAG} -t ${LATEST_IMAGE_TAG} .

echo -e "${YELLOW}⬆ Docker 레지스트리에 이미지 푸시 중...${NC}"
docker push ${SHA_IMAGE_TAG}
docker push ${LATEST_IMAGE_TAG}

echo -e "${GREEN} 이미지 빌드 및 푸시 완료${NC}"

# 2. EC2에서 실행할 명령어 출력
echo -e "${GREEN}📋 EC2에서 실행할 명령어:${NC}"
echo "# 1. EC2에 접속 후 다음 명령어들을 실행하세요:"
echo ""

echo "# 1. 도커 로그인"
echo "docker login -u <your-docker-username>"

echo "# 환경 변수 설정 (배포할 버전의 이미지 태그 사용)"
echo "export DOCKER_IMAGE=${SHA_IMAGE_TAG}"
echo "export DB_USERNAME=${DB_USERNAME}"
echo "export DB_PASSWORD=${DB_PASSWORD}"
echo "export DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD}"
echo "export GOOGLE_API_KEY=${GOOGLE_API_KEY}"
echo "export S3_ACCESS_KEY=${S3_ACCESS_KEY}"
echo "export S3_SECRET_KEY=${S3_SECRET_KEY}"
echo "export KAKAO_REST_API_KEY=${KAKAO_REST_API_KEY}"
echo "export KAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET}"
echo "export SCHEME=${SCHEME}"
echo "export BASE_URI=${BASE_URI}"
echo "export ACCESS_CONTROL_ALLOWED_ORIGINS=${ACCESS_CONTROL_ALLOWED_ORIGINS}"
echo "export AWS_REGION=ap-northeast-2"
echo ""
echo "# 2. Docker Compose 파일 다운로드 (또는 직접 생성)"
echo "curl -O https://raw.githubusercontent.com/kakao-tech-campus-3rd-step3/Team2_BE/develop/docker-compose.qa.yml"

echo "# 3. 컨테이너 실행 (지정한 SHA 태그 이미지 Pull 포함)"
echo "docker compose -f docker-compose.qa.yml pull && docker compose -f docker-compose.qa.yml up -d"
echo ""
echo "# 4. 로그 확인"
echo "docker logs -f --tail=500 pullit-qa-app"
echo ""
echo "# 5. 상태 확인"
echo "curl http://localhost:8080/actuator/health"

echo "# 6. 컨테이너 상태 확인"
echo "docker compose -f docker-compose.qa.yml ps"

echo -e "${GREEN} 로컬 빌드 완료. 위 명령어들을 EC2에서 실행하세요.${NC}"
