#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

docs/check.sh

TEMP_PROJECT_VERSION="temp-version"
# `tasks` triggers full tasks graph resolving, checking for possible misconfigurations
runInBuilder "set -e;
    ./gradlew help;
    ./gradlew tasks build publishToMavenLocal ${GRADLE_ARGS} -PprojectVersion=${TEMP_PROJECT_VERSION};
    ./gradlew :subprojects:android-test:test-app:instrumentationUi ${GRADLE_ARGS} -PinfraVersion=${TEMP_PROJECT_VERSION}"
