#!/usr/bin/env bash

set -e

source $(dirname $0)/_main.sh
TEMP_PROJECT_VERSION="temp-version"

# `tasks` triggers full tasks graph resolving, checking for possible misconfigurations
runInBuilder "./gradlew help;
    ./gradlew tasks build publishToMavenLocal ${GRADLE_ARGS} -PprojectVersion=${TEMP_PROJECT_VERSION};
    ./gradlew :subprojects:android-test:test-app:instrumentationUi ${GRADLE_ARGS} -PinfraVersion=${TEMP_PROJECT_VERSION}"

docs/check.sh
