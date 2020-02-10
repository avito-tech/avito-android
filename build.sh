#!/usr/bin/env bash

set -e

TEMP_PROJECT_VERSION="temp-version"

source $(dirname $0)/_main.sh "${GIT_COMMANDS}
    ./gradlew build publishToMavenLocal ${GRADLE_ARGS} -PprojectVersion=${TEMP_PROJECT_VERSION};
    :subprojects:android-test:test-app:instrumentationUi -PinfraVersion=${TEMP_PROJECT_VERSION}"

docs/check.sh
