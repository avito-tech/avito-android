#!/usr/bin/env bash

set -e

# shellcheck disable=SC2086
USER_ID=$(id -u ${USER})

docker run -t --rm \
    --volume "$(pwd)":/app \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --workdir /app \
    --env LOCAL_USER_ID="$USER_ID" \
    --env GRADLE_USER_HOME=/gradle \
    dsvoronin/android-builder \
    ./gradlew help
