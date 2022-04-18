#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if [[ -z "${DOCKER_REGISTRY_USERNAME+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY_USERNAME env must be set"
    exit 1
fi

if [[ -z "${DOCKER_REGISTRY_PASSWORD+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY_PASSWORD env must be set"
    exit 1
fi

if [[ "$#" -ne 3 ]]; then
    echo "ERROR: Wrong number of arguments. Expected ones:
    You should pass a path to a directory with Dockerfile and image name to publish:
    ./publish_emulator.sh <relative path to Dockerfile> <image-name> <API level>

    Example:
    ./publish_emulator.sh hermetic/Dockerfile android/emulator-hermetic 30
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(pwd)/android-emulator
readonly DOCKERFILE=$1
readonly IMAGE_NAME=$2
readonly API=$3

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publishEmulator \
        --dockerfilePath "${DOCKERFILE}" \
        --buildDir /build \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --imageName "${IMAGE_NAME}" \
        --api "${API}"

# DockerHub is disabled temporary to make builds more hermetic
#        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
#        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
