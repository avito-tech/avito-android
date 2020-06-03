#!/usr/bin/env bash

if [[ -z "${DOCKER_REGISTRY+x}" ]]; then
    echo "ERROR: env DOCKER_REGISTRY is not specified"
    exit 1
fi

IMAGE_ANDROID_BUILDER=${DOCKER_REGISTRY}/android/builder:fadc2f33db
IMAGE_DOCKER_IN_DOCKER=${DOCKER_REGISTRY}/android/docker-in-docker-image:c2ecce3a3e
