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

IMAGE_BUILDER=${DOCKER_REGISTRY}/android/image-builder:e3ef3815ff20
DOCUMENTATION_IMAGE=${DOCKER_REGISTRY}/android/documentation:802502572f
