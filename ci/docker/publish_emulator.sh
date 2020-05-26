#!/usr/bin/env bash

source $(dirname $0)/../_environment.sh

BUILD_DIRECTORY=$(pwd)/$1

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume ${BUILD_DIRECTORY}:/build \
    --env "DOCKER_REGISTRY=${DOCKER_REGISTRY}" \
    --env DOCKER_LOGIN=${DOCKER_LOGIN} \
    --env DOCKER_PASSWORD=${DOCKER_PASSWORD} \
    ${IMAGE_DOCKER_IN_DOCKER} publish_docker_image publish_emulator /build "28"
