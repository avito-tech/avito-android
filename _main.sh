#!/usr/bin/env bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

source "$DIR"/ci/_environment.sh

# don't use quotes here, it will break command
# shellcheck disable=SC2086
USER_ID=$(id -u $USER)

GRADLE_HOME_DIR=$HOME/.gradle
GRADLE_CACHE_DIR=$GRADLE_HOME_DIR/caches
GRADLE_WRAPPER_DIR=$GRADLE_HOME_DIR/wrapper

# todo move to daemon
function clearDockerContainers() {
    local containers=$(docker container ls -aq)
    if [[ ! -z "$containers" ]]; then
        echo "Stopping and removing containers: $containers"
        docker container rm --force ${containers}
    fi
}

# https://docs.gradle.org/6.2/userguide/dependency_resolution.html#sub:ephemeral-ci-cache
MARKER_FILE="$GRADLE_CACHE_DIR"/marker.avito
MOUNT_OPTIONS=""
# in container
GRADLE_RO_DEP_CACHE_DEST=/gradle/readonlydepcache
function resolveDependenciesCacheOptions() {
    if [ ! -f "$MARKER_FILE" ]; then
        echo "[Gradle] Marker file not found. Mounting writeable dependencies cache."
        MOUNT_OPTIONS+="--volume ${GRADLE_CACHE_DIR}:/gradle/caches "
        MOUNT_OPTIONS+="--volume ${GRADLE_HOME_DIR}/readonlydepcache:$GRADLE_RO_DEP_CACHE_DEST "
        MOUNT_OPTIONS+="--env GRADLE_RO_DEP_CACHE_DEST=$GRADLE_RO_DEP_CACHE_DEST "
    else
        echo "[Gradle] Marker file found. Mounting pre-populated read-only dependencies cache."
        MOUNT_OPTIONS+="--env GRADLE_RO_DEP_CACHE=$GRADLE_RO_DEP_CACHE_DEST "
        MOUNT_OPTIONS+="--volume ${GRADLE_HOME_DIR}/readonlydepcache:$GRADLE_RO_DEP_CACHE_DEST:ro"
    fi
}

GIT_COMMANDS="git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
            git config --global user.name 'builder';
            git config --global user.email 'builder@avito.ru';"

GRADLE_ARGS="-PartifactoryUrl=$ARTIFACTORY_URL "
GRADLE_ARGS+="--stacktrace "
GRADLE_ARGS+="--console=plain "
GRADLE_ARGS+="-Pci=true "
GRADLE_ARGS+="-PteamcityUrl=$TEAMCITY_URL "
GRADLE_ARGS+="-PteamcityBuildType=$BUILD_TYPE "
GRADLE_ARGS+="-PbuildNumber=$BUILD_NUMBER "
GRADLE_ARGS+="-PgitBranch=$BUILD_BRANCH "
GRADLE_ARGS+="-PbuildCommit=$BUILD_COMMIT "
GRADLE_ARGS+="-PteamcityBuildId=$BUILD_ID "
GRADLE_ARGS+="-PslackToken=$AVITO_SLACK_TOKEN "
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
GRADLE_ARGS+="-PkubernetesToken=$KUBERNETES_TOKEN "
GRADLE_ARGS+="-PkubernetesCaCertData=$KUBERNETES_CA_CERT_DATA "
GRADLE_ARGS+="-PkubernetesUrl=$KUBERNETES_URL \\
             -Pavito.bitbucket.enabled=true"

# TODO: Use IMAGE_ANDROID_BUILDER image from public registry

function runInBuilder() {
    COMMANDS=$@

    clearDockerContainers
    resolveDependenciesCacheOptions

    docker run -t --rm \
        --volume "$(pwd)":/app \
        --volume /var/run/docker.sock:/var/run/docker.sock \
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
        $MOUNT_OPTIONS \
        avitotech/android-builder:2193a0d87515 \
        bash -c "${GIT_COMMANDS} ${COMMANDS}"
}
