#!/usr/bin/env bash

# Script for running emulator using default `emulator` tool

set -ex

if [[ -z "${VERSION}" ]]; then
    echo_error "You must specify VERSION environment variable"
    exit 1
fi

emulator_name="emulator_${VERSION}"
sd_card_name="/sdcard.img"

emulator_arguments=( -avd ${emulator_name} -sdcard ${sd_card_name} -verbose )

if [[ ${WINDOW} == "true" ]]; then
    binary_name="qemu-system-x86_64"

    if [[ -z "${DISPLAY}" ]]; then
        export DISPLAY=":0"
    fi

    if [[ ${GPU_ENABLED} == "true" ]]; then
        echo "Rendering: Window host mode is enabled on ${DISPLAY} display (make sure, that you have configured Nvidia on host and pass X11 socket)"
        emulator_arguments+=( -gpu host )
    else
        echo "Rendering: Window swiftshader (software) rendering mode is enabled on ${DISPLAY} display (make sure, that you pass X11 socket)"
        emulator_arguments+=( -gpu swiftshader_indirect )
    fi
else
    binary_name="qemu-system-x86_64-headless"

    echo "Rendering: Headless swiftshader (software) rendering mode is enabled"
    emulator_arguments+=( -no-window -gpu swiftshader_indirect )
fi

if [[ "${SNAPSHOT_DISABLED}" == "true" ]]; then
    echo "Snapshots: Emulator will be ran without snapshot feature"
    emulator_arguments+=( -no-snapshot )
else
    echo "Snapshots: Emulator will be ran with loading snapshot (for using emulator with snapshot on CI)"
    emulator_arguments+=( -snapshot ci -no-snapshot-save )
fi

emulator_arguments+=( -no-boot-anim -no-audio -partition-size 2048 )

# emulator uses adb so we make sure that server is running
adb start-server

cd /opt/android-sdk/emulator
echo "Run ${binary_name} binary for emulator ${emulator_name} with abi: x86 (Version: ${VERSION})"
echo "no" | ./qemu/linux-x86_64/${binary_name} "${emulator_arguments[@]}"
