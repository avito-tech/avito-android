#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

bash $(dirname $0)/documentation/lint.sh

runInBuilder "set -e; ./gradlew -p subprojects build ${GRADLE_ARGS} --stacktrace"
