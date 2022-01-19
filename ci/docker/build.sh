#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if test "$#" -ne 1; then
    echo "ERROR: Missing arguments. You should pass a path to a directory with Dockerfile: ./build.sh <directory>"
    exit 1
fi

BUILD_DIRECTORY=$(pwd)/$1

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" build \
        --buildDir /build \
        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}"
