#!/usr/bin/env bash

set -uex

readonly dockerDir=$(dirname "$0")
source "$dockerDir"/../_environment.sh

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
    ./publish_emulator.sh <relative path to Dockerfile and image name> <API level> <Emulator type: google_apis or google_atd> <debug>
    See documentation about dir conventions \`./ci/docker/README.md##Docker dir structure conventions\`

    Example:
    ./publish_emulator.sh hermetic 30 google_apis true
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(cd "$dockerDir"/android-emulator; pwd)
if [ ! -d "$BUILD_DIRECTORY" ]; then
    echo "ERROR dir $BUILD_DIRECTORY doesn't exist"
    exit 1
fi
readonly RELATIVE_BUILD_DIR=$BUILD_DIRECTORY/$1
if [ ! -d "$RELATIVE_BUILD_DIR" ]; then
      echo "ERROR dir $RELATIVE_BUILD_DIR doesn't exist"
      exit 1
fi
readonly DOCKERFILE=$RELATIVE_BUILD_DIR/Dockerfile
if [ ! -f "$DOCKERFILE" ]; then
    echo "ERROR $DOCKERFILE doesn't exist. See README.md#image_name.txt"
    exit 1
fi
readonly RELATIVE_DOCKERFILE=$1/Dockerfile
readonly IMAGE_NAME_FILE=$RELATIVE_BUILD_DIR/image_name.txt
if [ ! -f "$IMAGE_NAME_FILE" ]; then
  echo "ERROR $IMAGE_NAME_FILE doesn't exist. See README.md#image_name.txt"
  exit 1
fi
readonly TMP_IMAGE_NAME=$(cat "$IMAGE_NAME_FILE")

readonly DEBUG=$4
if [[ $DEBUG = true ]]; then
  readonly IMAGE_NAME=$TMP_IMAGE_NAME-debug
else
  readonly IMAGE_NAME=$TMP_IMAGE_NAME
fi

readonly API=$2
readonly TYPE=$3

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publishEmulator \
        --dockerfilePath "${RELATIVE_DOCKERFILE}" \
        --buildDir /build \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --imageName "${IMAGE_NAME}" \
        --api "${API}" \
        --type "${TYPE}"
