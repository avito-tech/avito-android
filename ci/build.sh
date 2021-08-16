#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

runInBuilder "set -e; ./gradlew -p subprojects build ${GRADLE_ARGS} --stacktrace"

# TODO MBS-11769 make single ./gradlew build
runInBuilder "set -e; ./gradlew :check ${GRADLE_ARGS} --stacktrace"

bash $(dirname $0)/documentation/lint.sh
