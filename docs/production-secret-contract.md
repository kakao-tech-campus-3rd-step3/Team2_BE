# Pull-it 프로덕션 환경·비밀값 계약

이 문서는 `portfolio.yeon.world/pull-it` 복구 환경의 유일한 비밀값 계약이다. 실제 값은 이 저장소, 이슈, PR 본문, GitHub Actions 로그, 셸 히스토리에 넣지 않는다.

## 고정 공개값

| 의미 | 현재 값 | 소비 위치 |
| --- | --- | --- |
| 서비스 원본 | `https://portfolio.yeon.world` | CORS, Cookie domain |
| 앱 기본 경로 | `/pull-it` | FE Vite base, edge proxy |
| API 외부 경로 | `/pull-it/api` | edge proxy → backend `/api` |
| 문서 외부 경로 | `/pull-it/docs` | docs proxy |
| Kakao callback | `https://portfolio.yeon.world/pull-it/login/oauth2/code/kakao` | Kakao Console, `KAKAO_REDIRECT_URI` |
| 로그인 완료 경로 | `https://portfolio.yeon.world/pull-it/login-success` | `JWT_REDIRECT_URL_1`, OAuth allow-list |
| refresh cookie path | `/pull-it/auth/refresh` | `JWT_REFRESH_COOKIE_PATH` |
| OAuth 세션 cookie name/path | `PULLIT_OAUTH_SESSION` / `/pull-it` | `SERVER_SERVLET_SESSION_COOKIE_NAME/PATH` |
| S3 리전 | `ap-northeast-2` | `S3_REGION` |
| 앱 DB/Rabbit 사용자 | `pullit_app` | `DB_USERNAME`, `RABBITMQ_DEFAULT_USER` |

`PULLIT_DATABASE_NAME=pullit_prod`와 세 `PULLIT_*_VOLUME` 값은 서버의 읽기 전용 인벤토리와 백업 복구 검증이 끝난 뒤에만 확정한다. Compose는 이 볼륨을 생성하지 않는다.

`yeon.world`의 로그인·세션·OAuth 앱과 Pull-it은 공유하지 않는다. Pull-it의 Kakao 앱, JWT 서명 키, `refresh_token` cookie의 `/pull-it/auth/refresh` 경로, `PULLIT_OAUTH_SESSION` cookie, DB 사용자와 데이터는 모두 전용으로 유지한다.

## 실제 비밀값의 기준본과 복사본

1. **기준본:** 이 Mac 로그인 키체인. 서비스 이름은 `pullit-production`, 계정명은 아래 키 이름과 정확히 같다.
2. **배포 제어값:** GitHub `pullit-production` Environment secret. 실행 비밀의 배포 복사본만 둔다. 컨테이너 이미지는 GitHub Actions의 단기 `GITHUB_TOKEN`으로 GHCR에 접근하므로 별도 레지스트리 비밀값을 두지 않는다. 이 Environment는 Pull-it backend 저장소에만 만들며 Yeon의 Environment와 공유하지 않는다.
3. **실행 복사본:** Pull-it 전용 EC2의 전용 runner 계정만 읽을 수 있는 비밀 파일(`0600`). workflow는 임시 파일의 형식 검증을 통과한 경우에만 이 파일을 원자적으로 교체한다.

키를 새로 만들거나 재발급하면 기준본 → 서버 실행 복사본 순서로 같은 작업 안에서 갱신하고, 배포 후 해당 기능을 검증한다. GitHub은 기준본이 아니다.

## Backend 실행 비밀값

| 키 | 값의 출처·형식 | GitHub/서버 사용처 | 발급 또는 회전 |
| --- | --- | --- | --- |
| `DB_PASSWORD` | 32자 이상 무작위 비밀번호 | MariaDB 앱 계정, Spring datasource | 최초 생성, 유출·권한변경 시 |
| `DB_ROOT_PASSWORD` | `DB_PASSWORD`와 다른 32자 이상 무작위 비밀번호 | MariaDB 초기화·복구 전용 | 최초 생성, 유출 시 |
| `RABBITMQ_DEFAULT_PASS` | 32자 이상 무작위 비밀번호 | RabbitMQ 전용 앱 계정 | 최초 생성, 유출 시 |
| `JWT_SECRET_KEY` | 64바이트 무작위값을 Base64로 인코딩 | Spring JWT 서명 | 최초 생성, 유출 시. 변경하면 모든 Pull-it 세션 무효화 |
| `KAKAO_REST_API_KEY` | Pull-it 전용 Kakao 앱의 REST API key | OAuth client id | Kakao Console 앱 기준 |
| `KAKAO_CLIENT_SECRET` | 같은 Kakao 앱의 client secret | OAuth client secret | Kakao Console 재발급 시 |
| `GEMINI_API_KEY` | Pull-it 전용 Google AI Studio key | Gemini 호출 | Google AI Studio 재발급·사용량 경보 시 |
| `SENTRY_DSN` | Pull-it 전용 Sentry 프로젝트 DSN | backend 오류 보고 | Sentry 프로젝트 교체·유출 대응 시 |
| `PULLIT_CLOUDFLARE_TUNNEL_TOKEN` | Pull-it 전용 Cloudflare Tunnel token | `pullit-prod-tunnel` 컨테이너 | Tunnel 재발급·connector 교체 시 |

`DB_USERNAME=pullit_app`, `RABBITMQ_DEFAULT_USER=pullit_app`, `S3_REGION=ap-northeast-2`와 경로/도메인 값은 secret이 아닌 versioned configuration이다. `S3_BUCKET_NAME`도 별도 Pull-it 버킷 이름으로 확정한 뒤 공개 설정으로 둔다. S3 접근은 이 인스턴스에만 연결된 최소권한 IAM 역할의 임시 자격 증명을 사용하므로 장기 access key를 만들거나 저장하지 않는다.

백엔드는 EC2의 loopback 포트와 Pull-it 전용 Cloudflare Tunnel에만 연결한다. Yeon Docker network, Yeon runner, 기존 Cloudflare Tunnel에는 연결하지 않는다. Tunnel upstream은 edge만 사용하는 별도 origin으로 만들고, `PULLIT_BACKEND_ORIGIN`에만 저장한다. 이 origin은 브라우저 링크ㆍOAuth redirect URIㆍ문서에 공개하는 주소가 아니며, 사용자가 보는 모든 Pull-it 주소는 `https://portfolio.yeon.world/pull-it` 아래만 사용한다. 따라서 production profile은 `X-Forwarded-Host`/`Proto`를 신뢰하도록 설정한다. EC2 security group은 HTTP/HTTPS 인바운드를 열지 않는다.

## Frontend와 Docs의 Environment 경계

| 레포 | Environment | 필요한 값 | 금지 값 |
| --- | --- | --- | --- |
| Team2_FE | Vercel Production 프로젝트 설정 | `VITE_PUBLIC_BASE_PATH=/pull-it/`, `VITE_API_BASE_URL=/pull-it`, `VITE_SENTRY_DSN` | DB, JWT, Kakao client secret, AWS secret, Gemini key |
| pullit-docs-server | Vercel `Production` Environment | `DATABASE_URL`, `POSTGRES_URL_NON_POOLING` | Pull-it DB, JWT, Kakao, AWS, Gemini 값 |

Vite의 `VITE_*` 값은 브라우저에 공개된다. `VITE_SENTRY_DSN`은 식별자일 뿐 인증 비밀값으로 취급하지 않지만, 다른 secret을 이 접두사로 선언해서는 안 된다.

## 생성 및 등록 순서

1. 전용 S3 버킷과 최소권한 EC2 IAM 역할을 만들고, 그 역할을 Pull-it EC2에만 연결한다. 기존 Yeon IAM key, 버킷, 정책은 재사용하지 않는다.
2. 전용 Gemini API key, 전용 Kakao 앱/Client Secret, 전용 Sentry 프로젝트를 만든다.
3. 로컬에서 DB·Rabbit·Grafana 비밀번호와 JWT 키를 생성해 즉시 키체인에 기록한다. 채팅이나 파일에 출력하지 않는다.
4. `Team2_BE`의 `pullit-production` Environment에는 위 표의 Pull-it 전용 secret과 runtime secret만 등록한다. 전용 EC2에는 `pullit-production` label의 self-hosted runner를 설치하고, runner root guard는 이 저장소와 `/opt/pullit`만 허용한다. PR CI에는 어떤 production secret도 주입하지 않는다.
5. FE Vercel Production 설정과 Docs Vercel `Production` Environment에는 각 표에 적힌 값만 등록한다. Docs DB는 문서 전용 PostgreSQL이어야 한다.
6. 서버의 `/opt/pullit/pullit-production.env`를 배포 전용 runner 계정 소유 `0600`으로 만든다. workflow는 `umask 077` 임시 파일을 형식 검증한 뒤 `install`로 교체하며, 로그·Compose 명령행·원격 셸에 비밀값을 출력하지 않는지 검토한다.

## 배포 전 불변 검사

- `pull.it.kr`, `api.pull.it.kr`, `qa.api.pull.it.kr`은 production 설정, OAuth callback, CORS, 문서 링크에 남기지 않는다.
- Pull-it의 servlet session cookie는 기본 `JSESSIONID`가 아니라 `PULLIT_OAUTH_SESSION`, path `/pull-it`로 설정한다.
- DB URL에는 `createDatabaseIfNotExist=true`를 넣지 않는다.
- 비밀값은 `docker compose config`, `ps`, 로그, CI 출력에 나타나지 않아야 한다.
- API worker와 web app 모두 같은 읽기 전용 비밀 파일을 사용하고, 각 서비스의 health check가 통과해야 한다.
- prod 데이터베이스 volume은 새 compose 실행 전에 이름·마운트·백업 복구 절차를 별도로 확인한다. 새 데이터베이스를 자동 생성하지 않는다.
