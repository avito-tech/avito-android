#!/usr/bin/env bash

set -ex

source $(dirname "$0")/../_environment.sh

if [[ -z "${DOCKER_REGISTRY_USERNAME+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY_USERNAME env must be set"
    exit 1
fi

if [[ -z "${DOCKER_REGISTRY_PASSWORD+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY_PASSWORD env must be set"
    exit 1
fi

if [[ "$#" -ne 4 ]]; then
    echo "ERROR: Wrong number of arguments $#. Expected ones:
    You should pass a path to a directory with Dockerfile and image name to publish:
    ./publish_emulator.sh <relative path to Dockerfile> <image-name> <API level> <debug>

    Example:
    ./publish_emulator.sh hermetic/Dockerfile android/emulator-hermetic 30 true/false
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(pwd)/android-emulator
readonly DOCKERFILE=$1
readonly API=$3
readonly DEBUG=$4

if [[ $DEBUG = true ]]; then
    readonly IMAGE_NAME=$2-debug
else
    readonly IMAGE_NAME=$2
fi

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
