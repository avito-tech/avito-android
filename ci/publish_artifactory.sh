#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path (name)

source $(dirname $0)/_main.sh

# force Envs to be set up
set -uex

function publish() {
    local readonly user="-Pavito.artifactory.user=${ARTIFACTORY_PUBLISH_USER}"
    local readonly password="-Pavito.artifactory.password=${ARTIFACTORY_PUBLISH_PASSWORD}"

    local readonly CREDENTIALS="${user} ${password}"

    runInBuilder "./gradlew publishToArtifactory ${GRADLE_ARGS} ${CREDENTIALS} --stacktrace"
}

readonly CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ "$CURRENT_BRANCH" == "develop" ]; then
    echo "ERROR: Releasing from develop branch. It can clash with a release from a release branch. Develop contains the next version. See a release process in docs."
    exit 1
fi

publish
