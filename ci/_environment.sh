#!/usr/bin/env bash
set -xe

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY env is not set. Images from DockerHub are not supported temporary."
    exit 1
fi

IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder-hermetic:1d9738d9f57b
IMAGE_BUILDER=${DOCKER_REGISTRY}/android/image-builder:69bdb1e36644
DOCUMENTATION_IMAGE=${DOCKER_REGISTRY}/android/documentation:802502572f
