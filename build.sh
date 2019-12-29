#!/usr/bin/env bash

set -ex

USER_ID=`id -u ${USER}`

GRADLE_CACHE_DIR=$HOME/.gradle/caches
GRADLE_WRAPPER_DIR=$HOME/.gradle/wrapper

docker run --rm \
    --volume "$(pwd)":/app \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${GRADLE_CACHE_DIR}":/gradle/caches \
    --volume "${GRADLE_WRAPPER_DIR}":/gradle/wrapper \
    --workdir /app \
    --env GRADLE_USER_HOME=/gradle \
    --env LOCAL_USER_ID="$USER_ID" \
    dsvoronin/android-builder \
    bash -c "git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no';
             git config --global user.name 'builder';
             git config --global user.email 'builder@avito.ru';
             ./gradlew build"
