# 로컬 개발 환경을 위한 SSL 인증서

이 디렉토리는 `https://localhost` 접속에 필요한 SSL 인증서(`localhost.crt`)와 개인 키(`localhost.key`)를 저장하는 곳입니다.

**주의**: 개인 키 파일(`*.key`)은 보안상의 이유로 Git 버전 관리에서 제외됩니다. 프로젝트를 처음 설정하는 경우, 아래의 방법으로 직접 인증서를 생성해야 합니다.

## 인증서 생성 방법 (mkcert 사용)

`mkcert`는 로컬 환경에서 신뢰할 수 있는 SSL 인증서를 손쉽게 만들어주는 도구입니다.

### 1. mkcert 설치

사용 중인 운영체제에 맞게 `mkcert`를 설치합니다.

- **Linux (Ubuntu/Debian)**:
  ```bash
  sudo apt update
  sudo apt install libnss3-tools mkcert
  ```
- **macOS (Homebrew)**:
  ```bash
  brew install mkcert
  brew install nss # Firefox 지원을 위해 필요
  ```
- **Windows (Chocolatey)**:
  ```powershell
  choco install mkcert
  ```

### 2. 로컬 인증 기관(CA) 설치

다음 명령어를 실행하여 내 컴퓨터에 로컬 CA를 생성하고 신뢰하도록 설정합니다. 이 작업은 컴퓨터 당 한 번만 수행하면 됩니다.

```bash
mkcert -install
```

### 3. 인증서 생성

프로젝트의 `nginx/certs` 디렉토리로 이동한 후, 다음 명령어를 실행하여 `localhost`에 대한 인증서와 개인 키를 생성합니다.

```bash
# 터미널에서 `pullit/nginx/certs` 디렉토리로 이동
mkcert localhost
```

위 명령어를 실행하면 `localhost.pem` (인증서)과 `localhost-key.pem` (개인 키) 파일이 생성됩니다. Nginx 설정 파일(`default.conf`)이 `.crt`와 `.key` 확장자를 사용하므로, 생성된 파일들의 이름을 아래와 같이 변경해주세요.

- `localhost.pem` -> `localhost.crt`
- `localhost-key.pem` -> `localhost.key`

이제 `docker-compose up -d --build`을 실행하면 Nginx가 인증서를 정상적으로 로드하여 `https://localhost` 접속이 가능해집니다.
