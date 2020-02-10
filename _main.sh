#!/usr/bin/env bash

set -e

source $(dirname $0)/ci/_environment.sh

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

GIT_COMMANDS="git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
            git config --global user.name 'builder';
            git config --global user.email 'builder@avito.ru';"

GRADLE_ARGS="--info --stacktrace \\
             -PartifactoryUrl=$ARTIFACTORY_URL \\
             -Pci=true \\
             -PteamcityUrl \\
             -PteamcityBuildType \\
             -PbuildNumber \\
             -PgitBranch=$BUILD_BRANCH \\
             -PbuildCommit=$BUILD_COMMIT \\
             -PteamcityBuildId=$BUILD_ID \\
             -PslackToken=$AVITO_SLACK_TOKEN \\
             -Pavito.slack.token=$AVITO_SLACK_TOKEN \\
             -Pavito.instrumentaion.sentry.dsn=$AVITO_SENTRY_URL \\
             -Pavito.repo.ssh.url \\
             -Pavito.report.url=$AVITO_REPORT_URL \\
             -Pavito.report.fallbackUrl=$AVITO_REPORT_FALLBACK_URL \\
             -Pavito.report.viewerUrl=$AVITO_REPORT_VIEWER_URL \\
             -Pavito.registry=$AVITO_REGISTRY \\
             -Pavito.fileStorage.url=$AVITO_FILESTORAGE_URL \\
             -Pavito.bitbucket.url=$AVITO_BITBUCKET_URL \\
             -Pavito.bitbucket.projectKey=AG \\
             -Pavito.bitbucket.repositorySlug=avito-github \\
             -PatlassianUser=test \\
             -PatlassianPassword=test \\
             -Pavito.stats.host=$AVITO_STATS_HOST \\
             -Pavito.stats.fallbackHost=$AVITO_STATS_FALLBACK_HOST \\
             -Pavito.stats.port=$AVITO_STATS_PORT \\
             -Pavito.stats.namespace=$AVITO_STATS_NAMESPACE \\
             -PkubernetesToken=$KUBERNETES_TOKEN \\
             -PkubernetesCaCertData=$KUBERNETES_CA_CERT_DATA \\
             -PkubernetesUrl=$KUBERNETES_URL"

# TODO: Use IMAGE_ANDROID_BUILDER image from public registry

function runInBuilder() {
    COMMANDS=$*

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
    --env PROJECT_VERSION="$PROJECT_VERSION" \
    --env ARTIFACTORY_USER="$ARTIFACTORY_USER" \
    --env ARTIFACTORY_PASSWORD="$ARTIFACTORY_PASSWORD" \
    --env SLACK_TEST_WORKSPACE="$SLACK_TEST_WORKSPACE" \
    --env SLACK_TEST_CHANNEL="$SLACK_TEST_CHANNEL" \
    --env SLACK_TEST_TOKEN="$SLACK_TEST_TOKEN" \
    dsvoronin/android-builder \
    bash -c "${GIT_COMMANDS} ${COMMANDS}"
}
