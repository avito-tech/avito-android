#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if test "$#" -ne 2; then
    echo "ERROR: Missing arguments.
    You should pass a path to a directory with Dockerfile and image name to publish:
    ./publish_emulator.sh <image-name> <api level>

    Example:
    ./publish_emulator.sh android/emulator 30
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(pwd)/android-emulator
readonly IMAGE_NAME=$1
readonly API=$2

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publishEmulator \
        --buildDir /build \
        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --imageName "${IMAGE_NAME}" \
        --api "${API}"
