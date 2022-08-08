#!/usr/bin/env bash

# Local run in container for debugging purposes

set -e

source $(dirname $0)/_local.sh

runInBuilder "set -e; ./gradlew "$@" ${GRADLE_ARGS} --stacktrace"
