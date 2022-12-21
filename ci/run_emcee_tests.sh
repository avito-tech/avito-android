#!/usr/bin/env bash

set -e

source $(dirname $0)/_main.sh

runInBuilder "set -e; ./gradlew ${GRADLE_ARGS} publishToMavenLocal -PprojectVersion=local --no-configuration-cache && cd ./samples/emcee/ && ./gradlew emceeTest ${GRADLE_ARGS} --stacktrace"
