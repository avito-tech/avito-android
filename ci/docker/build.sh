#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if [[ "$#" -eq 2 ]]
then
  readonly BUILD_DIRECTORY=$(pwd)/$1
  readonly DOCKERFILE="Dockerfile"
  readonly IMAGE_NAME=$2
elif [[ "$#" -eq 3 ]]
then
  readonly BUILD_DIRECTORY=$(pwd)/$1
  readonly DOCKERFILE=$2
  readonly IMAGE_NAME=$3
else
    echo "ERROR: Wrong number of arguments. Expected ones:
    ./build.sh <directory> <image-name>
    Or
    ./build.sh <directory> <path to Dockerfile> <image-name>

    Example:
    ./build.sh image-builder android/image-builder
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

# DockerHub is disabled temporary to make builds more hermetic
#        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
#        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
