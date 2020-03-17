#!/usr/bin/env bash

set -xe

source $(dirname $0)/_main.sh

runInBuilder "./gradlew :subprojects:ci:clear-k8s-deployments:run ${GRADLE_ARGS}"