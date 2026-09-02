#!/usr/bin/env bash

# Restore a separately verified Pull-it SQL dump into the dedicated production
# database. This is deliberately not part of normal CD: it is a one-time,
# operator-confirmed recovery action.

set -euo pipefail

readonly APP_DIR=/opt/pullit
readonly COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"
readonly ENV_FILE="$APP_DIR/pullit-production.env"
readonly RESTORE_ACKNOWLEDGEMENT=I_HAVE_VERIFIED_LEGACY_PULLIT_BACKUP
readonly EXPECTED_DB_VOLUME=pullit-prod-db-data

backup_file="${1:-}"
expected_sha256="${2:-}"

usage() {
  echo "Usage: PULLIT_RESTORE_ACK=$RESTORE_ACKNOWLEDGEMENT $0 /absolute/path/to/pullit.sql[.gz] <sha256>" >&2
}

if [[ "${PULLIT_RESTORE_ACK:-}" != "$RESTORE_ACKNOWLEDGEMENT" ]]; then
  echo '레거시 DB 복원은 검증된 백업을 확인한 경우에만 허용됩니다.' >&2
  usage
  exit 1
fi

if [[ -z "$backup_file" || -z "$expected_sha256" ]]; then
  usage
  exit 1
fi

if [[ ! "$backup_file" = /* || ! -f "$backup_file" || -L "$backup_file" ]]; then
  echo '백업 파일은 심볼릭 링크가 아닌 읽을 수 있는 절대 경로의 일반 파일이어야 합니다.' >&2
  exit 1
fi

if [[ ! "$expected_sha256" =~ ^[a-fA-F0-9]{64}$ ]]; then
  echo '백업 SHA-256 값의 형식이 올바르지 않습니다.' >&2
  exit 1
fi

case "$backup_file" in
  *.sql|*.sql.gz) ;;
  *)
    echo 'MariaDB SQL 덤프(.sql 또는 .sql.gz)만 복원할 수 있습니다.' >&2
    exit 1
    ;;
esac

if [[ ! -r "$ENV_FILE" || ! -r "$COMPOSE_FILE" ]]; then
  echo '검증된 Pull-it runtime contract가 없습니다.' >&2
  exit 1
fi

actual_sha256="$(sha256sum "$backup_file" | awk '{print $1}')"
if [[ "${actual_sha256,,}" != "${expected_sha256,,}" ]]; then
  echo '백업 SHA-256이 기대값과 다릅니다. 복원을 중단합니다.' >&2
  exit 1
fi

cd "$APP_DIR"
set -a
source "$ENV_FILE"
set +a

if [[ "${PULLIT_DB_VOLUME:-}" != "$EXPECTED_DB_VOLUME" ]]; then
  echo '전용 Pull-it DB volume 이름이 예상값과 다릅니다. 복원을 중단합니다.' >&2
  exit 1
fi

if ! docker volume inspect "$PULLIT_DB_VOLUME" >/dev/null 2>&1; then
  echo '전용 Pull-it DB volume이 없습니다. 먼저 복원용 bootstrap을 완료하세요.' >&2
  exit 1
fi

if [[ -n "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running --services pullit-prod-app pullit-prod-worker)" ]]; then
  echo '앱 또는 worker가 실행 중입니다. 데이터 쓰기를 막기 위해 복원을 중단합니다.' >&2
  exit 1
fi

if [[ -z "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps --status running --services pullit-prod-db)" ]]; then
  echo 'MariaDB가 실행 중이 아닙니다. 복원을 중단합니다.' >&2
  exit 1
fi

table_count="$({
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T \
    -e MYSQL_PWD="$DB_ROOT_PASSWORD" pullit-prod-db \
    mariadb --batch --skip-column-names -uroot \
      -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$PULLIT_DATABASE_NAME';"
} | tr -d '[:space:]')"

if [[ ! "$table_count" =~ ^[0-9]+$ || "$table_count" != 0 ]]; then
  echo '대상 DB가 비어 있지 않습니다. 기존 데이터를 덮어쓰지 않기 위해 복원을 중단합니다.' >&2
  exit 1
fi

if [[ "$backup_file" == *.gz ]]; then
  restore_command=(gzip --decompress --stdout -- "$backup_file")
else
  restore_command=(cat -- "$backup_file")
fi

echo '검증된 레거시 SQL 덤프를 비어 있는 Pull-it 전용 DB에 복원합니다.'
"${restore_command[@]}" | docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T \
  -e MYSQL_PWD="$DB_ROOT_PASSWORD" pullit-prod-db \
  mariadb -uroot "$PULLIT_DATABASE_NAME"

restored_table_count="$({
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T \
    -e MYSQL_PWD="$DB_ROOT_PASSWORD" pullit-prod-db \
    mariadb --batch --skip-column-names -uroot \
      -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$PULLIT_DATABASE_NAME';"
} | tr -d '[:space:]')"

if [[ ! "$restored_table_count" =~ ^[1-9][0-9]*$ ]]; then
  echo '복원 뒤 Pull-it 테이블을 확인하지 못했습니다. 앱 배포를 진행하지 마세요.' >&2
  exit 1
fi

echo "복원 검증 완료: Pull-it 테이블 ${restored_table_count}개를 확인했습니다."
