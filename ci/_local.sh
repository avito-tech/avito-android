#!/usr/bin/env bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

source "$DIR"/_environment.sh

# shellcheck disable=SC2086
USER_ID=$(id -u ${USER})
GRADLE_HOME_DIR=$HOME/.gradle

# only need dependencies: https://docs.gradle.org/current/userguide/dependency_resolution.html#sub:ephemeral-ci-cache
GRADLE_CACHE_DIR=$GRADLE_HOME_DIR/caches/modules-2
GRADLE_WRAPPER_DIR=$GRADLE_HOME_DIR/wrapper

GIT_COMMANDS="git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
            git config --global user.name 'builder';
            git config --global user.email 'builder@avito.ru';"

function resolveGradleArg() {
    local arg="$2"
    if [ -z "${!arg}" ]; then
        echo "$arg must be set"
        exit 1
    else GRADLE_ARGS+="-P$1=${!arg} "; fi
}

GRADLE_ARGS="-Pavito.internalBuild=true "
GRADLE_ARGS="-Pci=false "
GRADLE_ARGS+="-Pavito.build=local "

# todo if internal build
resolveGradleArg "artifactoryUrl" "ARTIFACTORY_URL"

# todo optional, under some new reportViewerNeeded flag
resolveGradleArg "avito.report.url" "AVITO_REPORT_URL"
resolveGradleArg "avito.report.viewerUrl" "AVITO_REPORT_VIEWER_URL"
resolveGradleArg "avito.fileStorage.url" "AVITO_FILESTORAGE_URL"

# Instrumentation testing
# todo should be optional
resolveGradleArg "avito.registry" "AVITO_REGISTRY"
resolveGradleArg "kubernetesUrl" "KUBERNETES_URL"
resolveGradleArg "kubernetesToken" "KUBERNETES_TOKEN"
resolveGradleArg "kubernetesCaCertData" "KUBERNETES_CA_CERT_DATA"
GRADLE_ARGS+="-PkubernetesNamespace=android-emulator "

function runInBuilder() {
    COMMANDS=$@

    docker run --rm \
        --volume "$(pwd)":/app \
        --volume /var/run/docker.sock:/var/run/docker.sock \
        --volume "${GRADLE_CACHE_DIR}":/gradle/caches/modules-2 \
        --volume "${GRADLE_WRAPPER_DIR}":/gradle/wrapper \
        --workdir /app \
        --env TZ="Europe/Moscow" \
        --env LOCAL_USER_ID="$USER_ID" \
        --env GRADLE_USER_HOME=/gradle \
        "${IMAGE_ANDROID_BUILDER}" \
        bash -c "${GIT_COMMANDS} ${COMMANDS}"
}

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
    if [[ -n "$containers" ]]; then
        echo "Stopping and removing containers: $containers"
        docker container rm --force "${containers}"
    fi
}

clearDockerContainers
clearGradleLockFiles
