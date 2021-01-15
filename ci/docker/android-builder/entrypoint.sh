#!/bin/bash

set -e

# https://denibertovic.com/posts/handling-permissions-with-docker-volumes/

USER_ID=${LOCAL_USER_ID:-9001}
BUILD_USER=build_user
GRADLE_USER_HOME=/gradle

echo "Starting with UID : $USER_ID"
groupadd --gid "${USER_ID}" ${BUILD_USER}
useradd --shell /bin/bash --uid "${USER_ID}" --gid "${USER_ID}" --comment "User for container" --create-home ${BUILD_USER}

mkdir -p "${GRADLE_USER_HOME}"

SSH_DIR=/home/${BUILD_USER}/.ssh
mkdir -p ${SSH_DIR}
chown ${BUILD_USER} ${SSH_DIR}

echo "Claiming ownership of mounted gradle dirs:"
find "${GRADLE_USER_HOME}" -maxdepth 3 -type d -not -user ${BUILD_USER} -print -execdir chown ${BUILD_USER} {} \+

# shellcheck disable=SC2145
echo "Running command: $@"

sudo --set-home --preserve-env "PATH=$PATH" -u ${BUILD_USER} "$@"
