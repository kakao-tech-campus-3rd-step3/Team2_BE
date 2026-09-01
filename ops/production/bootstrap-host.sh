#!/usr/bin/env bash

# 새 Pull-it 전용 EC2에서 한 번만 실행한다.
# 기존 호스트나 기존 서비스의 계정·경로·컨테이너에는 절대 사용하지 않는다.

set -euo pipefail

if [[ ${EUID} -ne 0 ]]; then
  echo "이 스크립트는 새 EC2에서 root로 실행해야 합니다." >&2
  exit 1
fi

readonly RUNNER_USER="pullit-runner"
readonly RUNNER_HOME="/opt/actions-runner"
readonly RUNTIME_HOME="/opt/pullit"

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install --yes --no-install-recommends ca-certificates curl docker.io docker-compose-v2 git
systemctl enable --now docker

if ! id --user "${RUNNER_USER}" >/dev/null 2>&1; then
  useradd \
    --create-home \
    --home-dir "/home/${RUNNER_USER}" \
    --shell /bin/bash \
    "${RUNNER_USER}"
fi

usermod --append --groups docker "${RUNNER_USER}"
install --directory --owner="${RUNNER_USER}" --group="${RUNNER_USER}" --mode=750 \
  "${RUNNER_HOME}" \
  "${RUNTIME_HOME}"

echo "Pull-it 전용 호스트 초기화가 완료되었습니다. 다음 단계는 pullit-runner 계정으로 GitHub Actions 러너를 등록하는 것입니다."
