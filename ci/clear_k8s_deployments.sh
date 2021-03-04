#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -xe

source $(dirname $0)/_main.sh

runInBuilder "./gradlew -p subprojects :subprojects:ci:k8s-deployments-cleaner:clearByNamespaces --no-daemon ${GRADLE_ARGS}"
