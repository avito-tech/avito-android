#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if [[ "$#" -eq 3 ]]
then
  readonly BUILD_DIRECTORY=$(pwd)/$1
  readonly DOCKERFILE=$2
  readonly IMAGE_NAME=$3
else
    echo "ERROR: Wrong number of arguments. Expected ones:
    ./build.sh <directory> <relative path to Dockerfile in directory> <image-name>

    Example:
    ./build.sh android-builder hermetic/Dockerfile android/builder-hermetic
    "
    exit 1
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" build \
        --dockerfilePath "${DOCKERFILE}" \
        --buildDir /build \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --registry "${DOCKER_REGISTRY}" \
        --imageName "${IMAGE_NAME}"
