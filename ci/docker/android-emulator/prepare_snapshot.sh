#!/usr/bin/env bash

# Script that runs emulator and apply environments for saving snapshot

set -e

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

if [[ -z "${VERSION}" ]]; then
    echo_error "You must specify VERSION environment variable"
    exit 1
fi

echo "Starting emulator..."
SNAPSHOT_ENABLED="false" ./run_emulator.sh &

echo "Waiting for emulator booting..."
sleep 30

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

SNAPSHOT_ENABLED="false" ./run_emulator.sh &

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

adb emu avd snapshot list
adb emu kill

sleep 5

adb kill-server

echo "Emulator preparation finished"
