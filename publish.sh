#!/usr/bin/env bash

source $(dirname $0)/_main.sh

runInBuilder "./gradlew publishRelease ${GRADLE_ARGS} --no-parallel"
