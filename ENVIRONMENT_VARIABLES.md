# Pullit 프로젝트 환경 변수 가이드

이 문서는 `pullit` 애플리케이션을 실행하는 데 필요한 환경 변수를 설명합니다. 로컬 개발 환경이나 프로덕션 환경에 애플리케이션을 배포하기 전에, 아래 목록의 변수들을 `.env` 파일이나 해당 환경의 설정에 추가해야 합니다.

---

## 환경 변수 목록

| 환경 변수명                  | 설명                                                              | 예시 값                                |
| -------------------------- | ----------------------------------------------------------------- | -------------------------------------- |
| `APP_GEMINI_APIKEY`        | Google Gemini AI 모델 API 사용을 위한 인증 키입니다.                  | `your_gemini_api_key`                  |
| `AWS_REGION`               | AWS 서비스(S3 등)를 사용할 리전(Region)을 지정합니다.             | `ap-northeast-2`                       |
| `GOOGLE_API_KEY`           | Google 관련 API(예: 지도, 인증 등) 사용을 위한 인증 키입니다.       | `your_google_api_key`                  |
| `GRAFANA_ADMIN_PASSWORD`   | Grafana 대시보드의 관리자 계정 비밀번호입니다.                    | `pullit5!`                             |
| `KAKAO_CLIENT_SECRET`      | Kakao 소셜 로그인 API의 Client Secret 값입니다.                   | `your_kakao_client_secret`             |
| `KAKAO_REDIRECT_URI`       | Kakao 소셜 로그인 성공 후 리디렉션될 URI 경로입니다.              | `/login/oauth2/code`                   |
| `KAKAO_REST_API_KEY`       | Kakao 소셜 로그인 API의 REST API 키입니다.                        | `your_kakao_rest_api_key`              |
| `S3_ACCESS_KEY`            | AWS S3 버킷에 접근하기 위한 Access Key ID입니다.                  | `your_s3_access_key`                   |
| `S3_SECRET_KEY`            | AWS S3 버킷에 접근하기 위한 Secret Access Key입니다.              | `your_s3_secret_key`                   |

---

## 로컬 환경에서 HTTPS 설정하기

로컬 환경에서 `https://localhost`로 접속하여 개발을 진행하려면 SSL 인증서가 필요합니다. `mkcert`를 사용하면 신뢰할 수 있는 로컬 인증서를 간단하게 생성할 수 있습니다.

### 1. mkcert 설치

먼저 사용 중인 운영체제에 맞게 `mkcert`를 설치합니다.

- **macOS (Homebrew 사용 시):**
  ```bash
  brew install mkcert
  brew install nss # Firefox 지원이 필요한 경우
  ```

- **Linux (certutil 필요):**
  ```bash
  sudo apt install libnss3-tools
  # 또는
  sudo yum install nss-tools
  ```
  [Linux용 mkcert 릴리스 페이지](https://github.com/FiloSottile/mkcert/releases)에서 바이너리를 직접 다운로드하여 설치할 수도 있습니다.

- **Windows (Chocolatey 또는 Scoop 사용 시):**
  ```bash
  choco install mkcert
  # 또는
  scoop bucket add extras
  scoop install mkcert
  ```

### 2. 로컬 인증 기관(CA) 생성

다음 명령어를 실행하여 로컬 환경에 신뢰할 수 있는 인증 기관(CA)을 생성하고 설치합니다. 이 과정은 한 번만 수행하면 됩니다.

```bash
mkcert -install
```

### 3. 로컬 인증서 생성

프로젝트 루트 디렉토리 아래의 `nginx/certs` 경로에 `localhost`용 인증서를 생성합니다.

```bash
# 프로젝트 루트 디렉토리로 이동
cd /path/to/your/pullit/project

# nginx/certs 디렉토리가 없다면 생성
mkdir -p nginx/certs

# 인증서 생성
mkcert -key-file ./nginx/certs/localhost.key -cert-file ./nginx/certs/localhost.crt localhost 127.0.0.1 ::1
```

### 4. Docker Compose 실행

이제 모든 준비가 완료되었습니다. 아래 명령어로 Docker 컨테이너를 실행합니다.

```bash
docker-compose up --build
```

컨테이너가 성공적으로 실행되면, 웹 브라우저에서 **[https://localhost](https://localhost)** 로 접속하여 애플리케이션을 확인할 수 있습니다.

또한, API 문서는 **[https://localhost/swagger-ui/index.html](https://localhost/swagger-ui/index.html)** 에서 확인 가능합니다.

