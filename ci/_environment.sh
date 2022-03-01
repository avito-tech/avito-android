#!/usr/bin/env bash
set -xe

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    echo "ERROR: DOCKER_REGISTRY env is not set. Images from DockerHub are not supported temporary."
    exit 1
fi

ANDROID_BUILDER_TAG=253bbf0f7aa3

IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder:$ANDROID_BUILDER_TAG
IMAGE_BUILDER=${DOCKER_REGISTRY}/android/image-builder:6726783be105
DOCUMENTATION_IMAGE=${DOCKER_REGISTRY}/android/documentation:802502572f
