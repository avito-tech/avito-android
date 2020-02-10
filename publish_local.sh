#!/usr/bin/env bash

source $(dirname $0)/_main.sh "${GIT_COMMANDS} ./gradlew publishToArtifactory ${GRADLE_ARGS}"
