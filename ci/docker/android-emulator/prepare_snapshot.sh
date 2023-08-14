#!/usr/bin/env bash

# Script that runs emulator and apply environments for saving snapshot

set -exu

function echo_error() { echo "$@" 1>&2; }

function require() {
    local expected=$1
    shift
    local actual=`$@`

    if [[ "$actual" != *"$expected"* ]]; then
        echo "Required output for command: $@ is $expected, actual: $actual"
        exit 1
    fi
}

function requireAtLeast() {
    local expected=$1
    shift
    local actual=`$@`

    if [[ ${actual} -lt ${expected} ]]; then
        echo "Required minimum value for command: $@ is $expected, actual: $actual"
        exit 1
    fi
}

if [[ "$#" -ne 4 ]]; then
    echo "ERROR: Wrong number of arguments $#. Expected ones:
    SDK version, emulator type, emulator architecture.

    For example:
    ./prepare_snapshot.sh 24 google_apis x86
    "
    exit 1
fi

if ! [[ $1 =~ ^[0-9]+$ ]]; then
    echo_error "ERROR: Incorrect SDK version passed. An integer value expected, see https://developer.android.com/studio/releases/platforms"
    exit 1
fi

if ! [[ $3 =~ ^x86(_64)?$ ]]; then
    echo_error "ERROR: Incorrect emulator architecture passed. x86 and x86_64 are supported."
    exit 1
fi

readonly SDK_VERSION=$1
readonly EMULATOR_TYPE=$2
readonly EMULATOR_ARCH=$3

readonly AVD_IMAGE_DIR=/opt/android-sdk/system-images/android-${SDK_VERSION}/${EMULATOR_TYPE}/${EMULATOR_ARCH}
if [ ! -d "$AVD_IMAGE_DIR" ]; then
  echo "Error: avd image dir ${AVD_IMAGE_DIR} not found."
  exit 1
fi

echo "Starting emulator..."
SNAPSHOT_ENABLED="false" ./run_emulator.sh "$SDK_VERSION" "$EMULATOR_ARCH" &

echo "Waiting for emulator booting..."
sleep 60

echo "Checking boot_completed status"
require 1 adb shell "getprop sys.boot_completed"

echo "Applying settings..."

adb shell "settings put global window_animation_scale 0.0"
adb shell "settings put global transition_animation_scale 0.0"
adb shell "settings put global animator_duration_scale 0.0"
adb shell "settings put secure spell_checker_enabled 0"
adb shell "settings put secure show_ime_with_hard_keyboard 1"

# дублируем то что делают в эмуляторах по дефолту с 26, т.к.
# This is not applied to system images with API level < 26
# as there is not a reliable boot complete signal communicated back to the host for those system images.
adb shell "settings put system screen_off_timeout 2147483647"
adb shell "settings put secure long_press_timeout 1500"

# Hidden APIs
# https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces#how_can_i_enable_access_to_non-sdk_interfaces
# Android 9
adb shell "settings put global hidden_api_policy_pre_p_apps 1"
adb shell "settings put global hidden_api_policy_p_apps 1"
# Android 10+
adb shell "settings put global hidden_api_policy 1"

sleep 5

echo "Rebooting emulator..."
adb emu kill

sleep 5

SNAPSHOT_ENABLED="false"; ./run_emulator.sh "$SDK_VERSION" "$EMULATOR_ARCH" &

echo "Waiting for emulator booting..."
sleep 30

echo "Checking boot_completed status"
require 1 adb shell "getprop sys.boot_completed"

echo "Checking settings applying result..."
require 0.0 adb shell "settings get global window_animation_scale"
require 0.0 adb shell "settings get global transition_animation_scale"
require 0.0 adb shell "settings get global animator_duration_scale"
require 1 adb shell "settings get global hidden_api_policy_pre_p_apps"
require 1 adb shell "settings get global hidden_api_policy_p_apps"
require 1 adb shell "settings get global hidden_api_policy"
require 0 adb shell "settings get secure spell_checker_enabled"
require 1 adb shell "settings get secure show_ime_with_hard_keyboard"
require 1500 adb shell "settings get secure long_press_timeout"

# https://androidstudio.googleblog.com/2019/05/emulator-2906-stable.html
require 2147483647 adb shell "settings get system screen_off_timeout"

adb emu avd snapshot save ci

sleep 5

# Find `model` parameter for Gradle configuration
echo "Print devices"
adb devices -l

adb emu avd snapshot list
adb emu kill

sleep 5

adb kill-server

echo "Emulator preparation finished"
