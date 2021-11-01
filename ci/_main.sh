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

clearDockerContainers
clearGradleLockFiles

GIT_COMMANDS="git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
            git config --global user.name 'builder';
            git config --global user.email 'builder@avito.ru';"

GRADLE_ARGS="-Pci=true "
GRADLE_ARGS+="-PartifactoryUrl=$ARTIFACTORY_URL "
GRADLE_ARGS+="-PteamcityUrl=${TEAMCITY_URL} "
GRADLE_ARGS+="-PteamcityApiUser=${TEAMCITY_API_USER} "
GRADLE_ARGS+="-PteamcityApiPassword=${TEAMCITY_API_PASSWORD} "
GRADLE_ARGS+="-PteamcityBuildType=${BUILD_TYPE} "
GRADLE_ARGS+="-PbuildNumber "
GRADLE_ARGS+="-PgitBranch=$BUILD_BRANCH "
GRADLE_ARGS+="-PbuildCommit=$BUILD_COMMIT "
GRADLE_ARGS+="-PteamcityBuildId=$BUILD_ID "
GRADLE_ARGS+="-Pavito.slack.token=$AVITO_SLACK_TOKEN "
GRADLE_ARGS+="-Pavito.instrumentaion.sentry.dsn=$AVITO_SENTRY_URL "
GRADLE_ARGS+="-Pavito.repo.ssh.url "
GRADLE_ARGS+="-Pavito.report.url=$AVITO_REPORT_URL "
GRADLE_ARGS+="-Pavito.report.fallbackUrl=$AVITO_REPORT_FALLBACK_URL "
GRADLE_ARGS+="-Pavito.report.viewerUrl=$AVITO_REPORT_VIEWER_URL "
GRADLE_ARGS+="-Pavito.registry=$AVITO_REGISTRY "
GRADLE_ARGS+="-Pavito.fileStorage.url=$AVITO_FILESTORAGE_URL "
GRADLE_ARGS+="-Pavito.bitbucket.url=$AVITO_BITBUCKET_URL "
GRADLE_ARGS+="-Pavito.bitbucket.projectKey=AG "
GRADLE_ARGS+="-Pavito.bitbucket.repositorySlug=avito-github "
GRADLE_ARGS+="-PatlassianUser=test "
GRADLE_ARGS+="-PatlassianPassword=test "
GRADLE_ARGS+="-Pavito.stats.host=$AVITO_STATS_HOST "
GRADLE_ARGS+="-Pavito.stats.fallbackHost=$AVITO_STATS_FALLBACK_HOST "
GRADLE_ARGS+="-Pavito.stats.port=$AVITO_STATS_PORT "
GRADLE_ARGS+="-Pavito.stats.namespace=$AVITO_STATS_NAMESPACE "
GRADLE_ARGS+="-Pavito.build=teamcity "

if [ -n "$ELASTIC_ENDPOINTS" ]; then
    GRADLE_ARGS+="-Pavito.elastic.enabled=true "
    GRADLE_ARGS+="-Pavito.elastic.endpoints=$ELASTIC_ENDPOINTS "
    GRADLE_ARGS+="-Pavito.elastic.indexpattern=speed-android "
fi

GRADLE_ARGS+="-Pavito.slack.test.channelId=$SLACK_TEST_CHANNEL_ID "
GRADLE_ARGS+="-Pavito.slack.test.channel=$SLACK_TEST_CHANNEL "
GRADLE_ARGS+="-Pavito.slack.test.token=$SLACK_TEST_TOKEN "
GRADLE_ARGS+="-Pavito.slack.test.workspace=$SLACK_TEST_WORKSPACE "
GRADLE_ARGS+="-PkubernetesUrl=$KUBERNETES_URL "
GRADLE_ARGS+="-PkubernetesNamespace=android-emulator "
GRADLE_ARGS+="-PkubernetesToken=$KUBERNETES_TOKEN "
GRADLE_ARGS+="-PkubernetesCaCertData=$KUBERNETES_CA_CERT_DATA "
GRADLE_ARGS+="-Pavito.build-verdict.enabled=true "
GRADLE_ARGS+="-Pavito.bitbucket.enabled=true "

GRADLE_ARGS+="-Pavito.gradle.enterprise.url=$GRADLE_ENTERPRISE_URL "

function runInBuilder() {
    COMMANDS=$@

    docker run --rm \
        --volume "$(pwd)":/app \
        --volume /var/run/docker.sock:/var/run/docker.sock \
        --volume "${GRADLE_CACHE_DIR}":/gradle/caches/modules-2 \
        --volume "${GRADLE_WRAPPER_DIR}":/gradle/wrapper \
        --volume "$DIR/gradle.properties":/gradle/gradle.properties \
        --workdir /app \
        --env TZ="Europe/Moscow" \
        --env LOCAL_USER_ID="$USER_ID" \
        --env GRADLE_USER_HOME=/gradle \
        "${IMAGE_ANDROID_BUILDER}" \
        bash -c "${GIT_COMMANDS} ${COMMANDS}"
}
