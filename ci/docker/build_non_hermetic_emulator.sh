#!/usr/bin/env bash

if [[ "$#" -ne 2 ]]; then
    echo "ERROR: Wrong number of arguments. Expected ones:
    You should pass image name and api level to publish:
    ./build_non_hermetic_emulator.sh <image-name> <API level>

    Example:
    ./build_non_hermetic_emulator.sh android/emulator 30
    "
    exit 1
fi

readonly BUILD_DIRECTORY=$(pwd)/android-emulator
readonly IMAGE_NAME=$1
readonly API=$2

# todo should run from ./ci/docker now, remove this requirement
# todo image-builder should be docker image as well
pushd image-builder
./gradlew run --args="buildEmulator \
        --dockerfilePath 'non-hermetic/Dockerfile' \
        --buildDir '$(pwd)/../android-emulator' \
        --imageName '${IMAGE_NAME}' \
        --api '${API}'"
popd
