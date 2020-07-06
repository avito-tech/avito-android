#!/usr/bin/env bash

set -e

source $(dirname $0)/../_environment.sh

BUILD_DIRECTORY=$(pwd)/$1

ARGS=""
if [ -n "${DOCKER_LOGIN}" ]; then
    ARGS+="--env DOCKER_LOGIN=${DOCKER_LOGIN} "
fi
if [ -n "${DOCKER_PASSWORD}" ]; then
    ARGS+="--env DOCKER_PASSWORD=${DOCKER_PASSWORD} "
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume ${BUILD_DIRECTORY}:/build \
    --env "DOCKER_REGISTRY=${DOCKER_REGISTRY}" \
    ${ARGS} \
    ${IMAGE_DOCKER_IN_DOCKER} publish_docker_image publish /build
