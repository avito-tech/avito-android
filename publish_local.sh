#!/usr/bin/env bash

source $(dirname $0)/_main.sh

runInBuilder "${GIT_COMMANDS} ./gradlew publishToArtifactory ${GRADLE_ARGS}"
