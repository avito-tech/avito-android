#!/usr/bin/env bash

set -exu

readonly dockerDir=$(dirname "$0")
source "$dockerDir"/../_environment.sh

if [[ "$#" -eq 3 ]]
then
  readonly BUILD_DIRECTORY=$(cd "$dockerDir"/"$1"; pwd)
  if [ ! -d "$BUILD_DIRECTORY" ]; then
        echo "ERROR dir $BUILD_DIRECTORY doesn't exist"
        exit 1
  fi
  readonly RELATIVE_BUILD_DIR=$BUILD_DIRECTORY/$2
  if [ ! -d "$RELATIVE_BUILD_DIR" ]; then
          echo "ERROR dir $RELATIVE_BUILD_DIR doesn't exist"
          exit 1
  fi
  readonly DOCKERFILE=$RELATIVE_BUILD_DIR/Dockerfile
  if [ ! -f "$DOCKERFILE" ]; then
        echo "ERROR $DOCKERFILE doesn't exist. See README.md#image_name.txt"
        exit 1
  fi
  readonly RELATIVE_DOCKERFILE=$2/Dockerfile
  readonly IMAGE_NAME_FILE=$RELATIVE_BUILD_DIR/image_name.txt
  if [ ! -f "$IMAGE_NAME_FILE" ]; then
      echo "ERROR $IMAGE_NAME_FILE doesn't exist. See README.md#image_name.txt"
      exit 1
  fi
  readonly TMP_IMAGE_NAME=$(cat "$IMAGE_NAME_FILE")

  readonly DEBUG=$3
  if [[ $DEBUG = true ]]; then
      readonly IMAGE_NAME=$TMP_IMAGE_NAME-debug
  else
      readonly IMAGE_NAME=$TMP_IMAGE_NAME
  fi
else
    echo "ERROR: Wrong number of arguments. See README.md
     Expected ones:
    ./publish.sh <directory> <relative path to directory with Dockerfile and image name> <debug>

    Example:
    ./publish.sh android-builder hermetic true
    "
    exit 1
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publish \
        --dockerfilePath "${RELATIVE_DOCKERFILE}" \
        --buildDir /build \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --imageName "${IMAGE_NAME}"
