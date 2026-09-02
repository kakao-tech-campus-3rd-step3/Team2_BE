# Pullit 환경 변수 가이드

프로덕션의 유일한 환경변수 계약은 [production-secret-contract.md](docs/production-secret-contract.md)입니다.

- 커밋 가능한 키 이름과 공개값은 `.env.example`만 사용합니다.
- 실제 키·비밀번호·토큰은 Git, Actions 로그, SSH 명령문, Compose 파일에 절대 넣지 않습니다.
- `APP_GEMINI_APIKEY`, `GOOGLE_API_KEY` 등 이 문서의 이전 키 이름은 더 이상 사용하지 않습니다.

아래의 로컬 HTTPS 및 SSE 예시는 개발 참고용입니다. 운영 배포에는 위 계약을 우선합니다.

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

---

## API 사용 가이드

### 실시간 알림 (SSE) 연결 방법

실시간 알림을 받기 위한 SSE(Server-Sent Events) 연결은 인증이 필요합니다. 웹 표준 `EventSource` API는 커스텀 HTTP 헤더(예: `Authorization`)를 설정하기 어렵기 때문에, 아래와 같이 URL **쿼리 파라미터**를 통해 Access Token을 전달해야 합니다.

-   **엔드포인트**: `GET /api/notifications/subscribe`
-   **쿼리 파라미터**: `token`

#### JavaScript 예시 코드

```javascript
// 1. 사용자의 Access Token을 가져옵니다.
const accessToken = "여기에_사용자의_JWT_Access_Token을_넣으세요";

// 2. EventSource 객체를 생성할 때 'token' 쿼리 파라미터를 URL에 추가합니다.
const eventSource = new EventSource(`/api/notifications/subscribe?token=${accessToken}`);

// 3. 이벤트 리스너를 등록합니다.
eventSource.addEventListener('notification', (event) => {
    const notificationData = JSON.parse(event.data);
    console.log('새 알림 도착:', notificationData);
    // TODO: 알림을 UI에 표시하는 로직 구현
});

eventSource.onerror = (error) => {
    console.error('SSE 연결 오류:', error);
    // 연결이 끊겼을 때 재연결 로직 등을 구현할 수 있습니다.
    eventSource.close();
};
```
