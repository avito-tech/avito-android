#!/usr/bin/env bash
set -xe

ANDROID_BUILDER_TAG=253bbf0f7aa3

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    # using dockerhub for public availability
    IMAGE_ANDROID_BUILDER=avitotech/android-builder:$ANDROID_BUILDER_TAG
else
    # using in-house proxy for performance
    IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder:$ANDROID_BUILDER_TAG
fi

IMAGE_BUILDER=${DOCKER_REGISTRY}/android/image-builder:d5300935e5c9
# This image is deprecated and will be replaced by IMAGE_BUILDER in MBS-12582
IMAGE_DOCKER_IN_DOCKER=${DOCKER_REGISTRY}/android/docker-in-docker-image:c2ecce3a3e
DOCUMENTATION_IMAGE=${DOCKER_REGISTRY}/android/documentation:802502572f
