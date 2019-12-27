#!/bin/bash

set -e

# https://denibertovic.com/posts/handling-permissions-with-docker-volumes/

# Добавляем локального юзера
USER_ID=${LOCAL_USER_ID:-9001}
BUILD_USER=build_user

echo "Starting with UID : $USER_ID"
groupadd --gid "${USER_ID}" ${BUILD_USER}
useradd --shell /bin/bash --uid "${USER_ID}" --gid "${USER_ID}" --comment "User for container" --create-home ${BUILD_USER}

setfacl -m user:$BUILD_USER:rw /var/run/docker.sock

mkdir -p "${GRADLE_USER_HOME}"

# были проблемы, когда создавал git при первом обращении
SSH_DIR=/home/${BUILD_USER}/.ssh
mkdir -p ${SSH_DIR}
chown ${BUILD_USER} ${SSH_DIR}

echo "Claiming ownership of mounted gradle dirs:"
find "${GRADLE_USER_HOME}" -maxdepth 3 -type d -not -user ${BUILD_USER} -print -execdir chown ${BUILD_USER} {} \+

# shellcheck disable=SC2145
echo "Running command: $@"

# Запускаем команды в докере от имении $BUILD_USER
# Дополнитьно прокидываем PATH, т.к. не прокидывается через --preserve-env, как safe value
sudo --set-home --preserve-env "PATH=$PATH" -u ${BUILD_USER} "$@"
