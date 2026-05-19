#!/usr/bin/env bash

set -e

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY env must be set"
    exit 1
fi

if [[ -z "${ARTIFACTORY_URL+x}" ]]; then
    echo "WARN: ARTIFACTORY_URL env is not set. It's required for hermetic builds."
fi

IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder-hermetic:2194ce9c4656
IMAGE_BUILDER=${DOCKER_REGISTRY}/android/image-builder:6258016ea33d
