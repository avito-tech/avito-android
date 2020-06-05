#!/usr/bin/env bash

BUILD_DIRECTORY=$1
API=$2

if [ -n "$BUILD_DIRECTORY" ] && [ -n "$API" ]; then
    echo "building image for API=$API"
else
    echo "'BUILD_DIRECTORY' and 'API' parameters should be provided. Example ./publish_emulator android_emulator 22. See available API's in ./android_emulator/hardware/"
    exit 1
fi

source $(dirname $0)/../_environment.sh

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "$(pwd)/$BUILD_DIRECTORY":/build \
    --env "DOCKER_REGISTRY=${DOCKER_REGISTRY}" \
    --env DOCKER_LOGIN=${DOCKER_LOGIN} \
    --env DOCKER_PASSWORD=${DOCKER_PASSWORD} \
    ${IMAGE_DOCKER_IN_DOCKER} publish_docker_image publish_emulator /build "${API}"
