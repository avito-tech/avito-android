#!/usr/bin/env sh

set -e

USER_ID=${LOCAL_USER_ID:-1000}
DOCKER_USER=user

echo "Creating user: $USER_ID"
adduser --shell /bin/sh --disabled-password --uid "${USER_ID}" ${DOCKER_USER}

# where were errors when created by git itself
SSH_DIR=/home/${DOCKER_USER}/.ssh
mkdir -p ${SSH_DIR}
chown ${DOCKER_USER} ${SSH_DIR}

# shellcheck disable=SC2145
echo "Running command: $@"

sudo --set-home --preserve-env "PATH=$PATH" -u ${DOCKER_USER} "$@"
