#!/usr/bin/env bash

# Local run of build.sh for debugging purposes

set -e

source $(dirname $0)/_local.sh

runInBuilder "set -e; ./gradlew build ${GRADLE_ARGS} --stacktrace"
