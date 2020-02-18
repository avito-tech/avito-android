#!/bin/bash

set -e

# https://denibertovic.com/posts/handling-permissions-with-docker-volumes/

USER_ID=${LOCAL_USER_ID:-9001}
BUILD_USER=build_user

echo "Starting with UID : $USER_ID"
groupadd --gid "${USER_ID}" ${BUILD_USER}
useradd --shell /bin/bash --uid "${USER_ID}" --gid "${USER_ID}" --comment "User for container" --create-home ${BUILD_USER}

mkdir -p "${GRADLE_USER_HOME}"

# были проблемы, когда создавал git при первом обращении
SSH_DIR=/home/${BUILD_USER}/.ssh
mkdir -p ${SSH_DIR}
chown ${BUILD_USER} ${SSH_DIR}

echo "Claiming ownership of mounted gradle dirs:"
find "${GRADLE_USER_HOME}" -maxdepth 3 -type d -not -user ${BUILD_USER} -print -execdir chown ${BUILD_USER} {} \+

# shellcheck disable=SC2145
echo "Running command: $@"

function copyDependenciesCache() {
    echo "[Gradle] Preparing read only dependencies cache for further reuse..."
    rsync --archive --info=progress2 --exclude='*.lock' --exclude='gc.properties' /gradle/caches/modules-2/ $GRADLE_RO_DEP_CACHE_DEST/modules-2/
    echo "Dependencies cache populated" >/gradle/caches/marker.avito
    echo "[Gradle] Done"
}

# Запускаем команды в докере от имении $BUILD_USER
# Дополнитьно прокидываем PATH, т.к. не прокидывается через --preserve-env, как safe value
sudo --set-home --preserve-env "PATH=$PATH" -u ${BUILD_USER} "$@"

if [ -z $GRADLE_RO_DEP_CACHE ]; then
    sudo --set-home --preserve-env "PATH=$PATH" -u ${BUILD_USER} bash -c "$(declare -f copyDependenciesCache); copyDependenciesCache"
fi
