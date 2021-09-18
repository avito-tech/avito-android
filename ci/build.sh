#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

bash $(dirname $0)/documentation/lint.sh

# TODO remove no-build-cache, added while investigating MBS-11914
runInBuilder "set -e; ./gradlew build ${GRADLE_ARGS} --stacktrace --no-build-cache"
