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
    ./publish.sh <directory> <image-name>
    Or
    ./publish.sh <directory> <path to Dockerfile> <image-name>

    Example:
    ./publish.sh image-builder android/image-builder
    ./publish.sh android-builder hermetic/Dockerfile android/builder-hermetic
    "
    exit 1
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publish \
        --dockerfilePath "${DOCKERFILE}" \
        --buildDir /build \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --imageName "${IMAGE_NAME}"

# DockerHub is disabled temporary to make builds more hermetic
#        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
#        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
