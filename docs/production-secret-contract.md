# Pull-it 포트폴리오 환경·비밀값 계약

이 문서는 `https://portfolio.yeon.world/pull-it` 복구 환경의 설정 계약이다. 실제 비밀값은 저장소, PR, Actions 로그, 셸 히스토리에 넣지 않는다.

## 공개 라우팅 계약

| 항목 | 고정값 | 소유자 |
| --- | --- | --- |
| 서비스 origin | `https://portfolio.yeon.world` | Yeon edge proxy |
| Pull-it base path | `/pull-it` | FE Vite base, edge proxy |
| API 외부 경로 | `/pull-it/api` → backend `/api` | Yeon edge proxy |
| 문서 경로 | `/pull-it/docs` → docs `/` | Yeon edge proxy |
| Kakao callback | `https://portfolio.yeon.world/pull-it/login/oauth2/code/kakao` | Kakao Console, backend |
| 로그인 완료 | `https://portfolio.yeon.world/pull-it/login-success` | backend JWT redirect |
| refresh cookie path | `/pull-it/auth/refresh` | backend |
| OAuth 세션 cookie | `PULLIT_OAUTH_SESSION`, path `/pull-it` | backend |

`pull.it.kr`, `api.pull.it.kr`, `qa.api.pull.it.kr`은 새 런타임 값·OAuth callback·CORS에 넣지 않는다. 과거 운영 기록에 보존된 URL은 역사 자료이며 런타임 계약이 아니다.

## 분리 불변조건

- 기존 Yeon 컨테이너, volume, database, Redis, RabbitMQ, Cloudflare Tunnel 설정은 변경하지 않는다.
- Pull-it Compose project, named volume, internal network, DB 사용자와 비밀값은 모두 `pullit-` 접두사 전용이다.
- 공유가 허용되는 것은 read-only edge network `yeon-edge`와 Yeon edge proxy의 `pullit-*` upstream alias뿐이다.
- Pull-it Kakao 앱, JWT signing key, Gemini key, Sentry project와 Pi의 Pull-it 전용 MinIO credentials는 Yeon 것과 별도로 발급한다.
- Pull-it 쿠키는 host-only이며 Yeon cookie name과 충돌하지 않는다. `yeon.world`와 `portfolio.yeon.world` 로그인은 동일 OAuth/session을 공유하지 않는다.

## 기준본과 배포 복사본

1. 기준본은 이 Mac Keychain의 서비스 `pullit-portfolio-production`이다. 새 비밀은 생성 직후 이곳에만 기록한다.
2. GitHub은 아래의 각 **repository Environment**에 배포 복사본만 둔다. Yeon repository Environment와 공유하지 않는다.
3. Pi의 `/opt/pullit/<component>/*.env`는 root 소유 `0600`이고 dispatcher가 전용 release에서만 설치한다. Docker Compose 출력과 CI 로그에 비밀값을 출력하지 않는다.

| 저장소 | Environment | Secret | Variable |
| --- | --- | --- | --- |
| `Team2_BE` | `pullit-backend-production` | DB/Rabbit/JWT/Kakao/Gemini/MinIO app credential/MinIO root credential/Sentry, deploy SSH key, Cloudflare Access id/secret | DB username/name, Rabbit user, S3 bucket/region, deploy host/known-hosts |
| `Team2_FE` | `pullit-frontend-production` | deploy SSH key, Cloudflare Access id/secret | `VITE_SENTRY_DSN`, deploy host/known-hosts |
| `pullit-docs-server` | `pullit-docs-production` | docs DB password, deploy SSH key, Cloudflare Access id/secret | deploy host/known-hosts |

`VITE_*`는 browser public configuration이므로 비밀값을 넣지 않는다. `SENTRY_DSN`은 값이 비어도 backend deploy에 영향이 없지만, 다른 실행 비밀은 모두 empty fail-fast 대상이다.

## Pi 배포 권한·롤백 계약

- GitHub Actions는 Cloudflare Access 전용 hostname `pullit-deploy-ssh.yeon.world`만 사용한다. 기존 `ssh.yeon.world` Access 정책과 credentials는 수정하지 않는다.
- Pi 계정 `pullit-deploy`는 `/opt/pullit/incoming`에 revision 업로드와 `sudo /usr/local/sbin/pullit-deploy <component> <sha>`만 허용된다. 임의 Docker·root 명령은 금지된다.
- dispatcher는 완전한 SHA와 검증된 data-only 산출물만 받는다. Compose 파일과 deploy driver는 Pi에서 운영자가 root 소유로 설치하며 CI 업로드본을 root로 실행하지 않는다. health check가 성공할 때만 `current` release를 확정한다.
- 배포 실패 시 이전 runtime 계약·release link를 복원하고, 기존 컴포넌트가 있었다면 이전 runtime을 재기동한다. named volume은 생성·삭제·초기화하지 않는다.
- docs schema 변경은 `prisma migrate deploy`만 사용한다. `prisma db push`, reset, drop은 production deploy에 금지한다.

## 실행 전 검사와 순서

1. 각 배포 workflow의 `operation=preflight`를 한 번 실행한다. 이 job은 Cloudflare HTTP 200, SSH host key, 제한 계정, 허용 sudo command만 검사하며 Pi에 쓰지 않는다.
2. 세 preflight가 모두 성공한 뒤 backend → frontend → docs 순으로 `operation=deploy`를 각 한 번 실행한다.
3. Pi에서 Pull-it 컨테이너 health, `yeon-edge` alias, 기존 Yeon 컨테이너 목록이 그대로인지를 확인한다.
4. Pull-it 서비스가 모두 healthy인 경우에만 Yeon edge proxy의 `/pull-it` route를 merge·배포한다. 이 단계 전에는 외부 공개 라우팅을 켜지 않는다.

## 발급·회전 경계

- Kakao Developers: Pull-it 전용 앱 생성, 플랫폼 Web origin `https://portfolio.yeon.world`, callback은 위의 정확한 값 하나만 등록한다.
- Google AI Studio, Sentry, S3는 포트폴리오 Pull-it 전용 프로젝트·key·bucket을 생성한다. 기존 Yeon key/bucket 재사용은 금지한다.
- MinIO root credential은 storage 초기화·관리만 담당한다. application/worker에는 bucket policy를 가진 별도 `S3_ACCESS_KEY`/`S3_SECRET_KEY`만 전달한다.
- Cloudflare Access 서비스 토큰은 Pull-it deployment application에만 연결한다. 값 유출이 의심되면 Keychain → GitHub Environments → 연결 policy 순서로 교체하고, 새 preflight 성공 뒤 이전 token을 revoke한다.
