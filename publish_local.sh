#!/usr/bin/env bash

source $(dirname $0)/_main.sh

runInBuilder "./gradlew publishToArtifactory ${GRADLE_ARGS} -Dorg.gradle.internal.publish.checksums.insecure=true"
