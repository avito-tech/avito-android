#!/usr/bin/env bash

set -exu

if [[ "$#" -eq 2 ]]
then
    readonly dockerDir=$(dirname "$0")
    source "$dockerDir"/../_environment.sh
    # build dir is a root dir of git repository
    readonly BUILD_DIRECTORY=$(cd "$dockerDir"/../../; pwd)
    readonly IMAGE_DIR=$dockerDir/emcee-worker/hermetic
    readonly RELATIVE_DOCKERFILE=ci/docker/emcee-worker/hermetic/Dockerfile

    readonly TMP_IMAGE_NAME=$(cat "$IMAGE_DIR"/image_name.txt)
    readonly DEBUG=$2
    if [[ $DEBUG = true ]]; then
      readonly IMAGE_NAME=$TMP_IMAGE_NAME-debug
    else
      readonly IMAGE_NAME=$TMP_IMAGE_NAME
    fi
else
    echo "ERROR: Wrong number of arguments
    Expected: ./publish_emcee_worker '<space separated list of APIs>' <debug>

    Example: ./publish_emcee_worker '22 29 30 31' true
    "
    exit 1
fi

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publishEmceeWorker \
        --dockerfilePath "${RELATIVE_DOCKERFILE}" \
        --buildDir /build \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}" \
        --artifactoryUrl "${ARTIFACTORY_URL}" \
        --apis "$1" \
        --imageName "${IMAGE_NAME}"


