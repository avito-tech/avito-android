#!/usr/bin/env bash

set -ex

readonly ANDROID_SDK_BASE_URL=$1
readonly ANDROID_HOME=$2
readonly SDK_VERSIONS=($3)

function architectureOf() {
    if [ "$1" -lt 28 ]; then
        echo "x86"
    else
        echo "x86_64"
    fi
}

./unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/cmdline-tools/cmdline-tools-linux_6_0.zip $ANDROID_HOME
./unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platforms/platforms_31_0_1.zip $ANDROID_HOME
./unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/emulator/emulator_linux_31_2_8.zip $ANDROID_HOME
./unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platform-tools/platform-tools_linux_33_0_0.zip $ANDROID_HOME
./unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/tools/tools_linux_26_1_1.zip $ANDROID_HOME

for api in "${SDK_VERSIONS[@]}"
do
    architecture=$(architectureOf $api)
    ./unzip_from_url.sh "${ANDROID_SDK_BASE_URL}/system-images/system-images-android-${api}-${architecture}.zip" $ANDROID_HOME
    echo no | avdmanager create avd \
        --name emulator_${api} \
        --package "system-images;android-${api};google_apis;${architecture}" \
        --abi google_apis/${architecture} && \
        mksdcard -l e 512M /sdcard.img
    mv ./hardware/config_${api}.ini /root/.android/avd/emulator_${api}.avd/config.ini
done

rm -rf ./hardware
rm unzip_from_url.sh
