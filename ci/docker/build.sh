#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if test "$#" -ne 2; then
    echo "ERROR: Missing arguments.
    You should pass a path to a directory with Dockerfile and image name to publish:
    ./build.sh <directory> <image-name>

    Example:
    ./build.sh image-builder android/image-builder
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(pwd)/$1
readonly IMAGE_NAME=$2

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" build \
        --buildDir /build \
        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --imageName "${IMAGE_NAME}"
