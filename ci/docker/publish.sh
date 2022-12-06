#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if [[ "$#" -eq 4 ]]
then
  readonly BUILD_DIRECTORY=$(pwd)/$1
  readonly DOCKERFILE=$2
  readonly DEBUG=$4
  if [[ $DEBUG = true ]]; then
      readonly IMAGE_NAME=$3-debug
  else
      readonly IMAGE_NAME=$3
  fi
else
    echo "ERROR: Wrong number of arguments. Expected ones:
    ./publish.sh <directory> <relative path to Dockerfile in directory> <image-name> <debug>

    Example:
    ./publish.sh android-builder hermetic/Dockerfile android/builder-hermetic true/false
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
