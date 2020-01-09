#!/usr/bin/env bash

set -e

# shellcheck disable=SC2086
USER_ID=$(id -u ${USER})
GRADLE_HOME_DIR=$HOME/.gradle
GRADLE_CACHE_DIR=$GRADLE_HOME_DIR/caches
GRADLE_WRAPPER_DIR=$GRADLE_HOME_DIR/wrapper

# Warning. Hack!
# Мы можем удалять эти локи, т.к. гарантированно никакие другие процессы не используют этот шаренный кеш на начало новой сборки
# см. clearDockerContainers
# То что лок файлы остаются от предыдущих сборок, означает что мы где-то неправильно останавливаем процесс
# '|| true' необходим для свеже-поднятых агентов, где еще не создана папка с кешами
function clearGradleLockFiles() {
    echo "Removing Gradle lock files"
    find "${GRADLE_HOME_DIR}" \( -name "*.lock" -o -name "*.lck" \) -delete || true
}

# По-разным причинам работа контейнера при прошлой сборке может не завершиться
# Здесь мы перестраховываемся и останавливаем все работающие контейнеры
# Перед сборкой не должно быть других контейнеров в любом случае
function clearDockerContainers() {
    local containers=$(docker container ls -aq)
    if [[ ! -z "$containers" ]]; then
        echo "Stopping and removing containers: $containers"
        docker container rm --force ${containers}
    fi
}

GRADLE_CACHE_DIR=$HOME/.gradle/caches
GRADLE_WRAPPER_DIR=$HOME/.gradle/wrapper

clearDockerContainers
clearGradleLockFiles

docker run --rm \
    --volume "$(pwd)":/app \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${GRADLE_CACHE_DIR}":/gradle/caches \
    --volume "${GRADLE_WRAPPER_DIR}":/gradle/wrapper \
    --workdir /app \
    --env GRADLE_USER_HOME=/gradle \
    --env LOCAL_USER_ID="$USER_ID" \
    --env BINTRAY_USER="$BINTRAY_USER" \
    --env BINTRAY_API_KEY="$BINTRAY_API_KEY" \
    --env BINTRAY_GPG_PASSPHRASE="$BINTRAY_GPG_PASSPHRASE" \
    --env ARTIFACTORY_URL="$ARTIFACTORY_URL" \
    --env ARTIFACTORY_USER="$ARTIFACTORY_USER" \
    --env ARTIFACTORY_PASSWORD="$ARTIFACTORY_PASSWORD" \
    dsvoronin/android-builder \
    bash -c "git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
             git config --global user.name 'builder';
             git config --global user.email 'builder@avito.ru';
             ./gradlew $@"
