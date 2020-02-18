#!/usr/bin/env bash

source $(dirname $0)/../_environment.sh

BUILD_DIRECTORY=$(pwd)/$1

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    echo "ERROR: env DOCKER_REGISTRY is not specified"
    exit 1
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume ${BUILD_DIRECTORY}:/build \
    --env "DOCKER_REGISTRY=${DOCKER_REGISTRY}" \
    --env DOCKER_LOGIN=${DOCKER_LOGIN} \
    --env DOCKER_PASSWORD=${DOCKER_PASSWORD} \
    ${IMAGE_DOCKER_IN_DOCKER} publish_docker_image test
