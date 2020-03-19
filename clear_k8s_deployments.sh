#!/usr/bin/env bash

set -xe

source $(dirname $0)/_main.sh

runInBuilder "./gradlew :subprojects:ci:k8s-deployments-cleaner:cleanByNamespaces ${GRADLE_ARGS}"