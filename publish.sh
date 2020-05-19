#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

source $(dirname $0)/_main.sh

runInBuilder "./gradlew publishRelease ${GRADLE_ARGS} --no-parallel --stacktrace"
