#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

docs/check.sh

# `tasks` triggers full tasks graph resolving, checking for possible misconfigurations
runInBuilder "set -e;
    ./gradlew help;
    ./gradlew help -PuseCompositeBuild=false;
    ./gradlew -p subprojects build ${GRADLE_ARGS};
    ./gradlew :samples:test-app:instrumentationUi :samples:test-app-second:instrumentationUi ${GRADLE_ARGS}"
