FROM nvidia/opengl:1.0-glvnd-runtime-ubuntu18.04

ARG SDK_VERSION
ARG EMULATOR_ARCH

ENV VERSION=${SDK_VERSION}

# -------------------- Common -------------------
# socat         For redirecting adb and VNC from emulator to host
# libglu1       For emulators software rendering
# lib32stdc++6  For mksdcard android sdk tool
# net-tools     For basic network primitives
#
# libglvnd0     For initialization OpenGl with gpu rendering
RUN apt-get update && \
	apt-get install -y \
	        wget \
    	    unzip \
    	    openjdk-8-jdk \
    	    git && \
    apt-get install -y \
            net-tools \
            socat \
            libglu1 \
            lib32stdc++6 && \
    apt-get clean && apt-get purge

ENV SHELL /bin/bash
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
# -----------------------------------------------

# ------------------ Nvidia GPU -----------------
ENV NVIDIA_VISIBLE_DEVICES \
    ${NVIDIA_VISIBLE_DEVICES:-all}
ENV NVIDIA_DRIVER_CAPABILITIES \
    ${NVIDIA_DRIVER_CAPABILITIES:+$NVIDIA_DRIVER_CAPABILITIES,}graphics
# -----------------------------------------------

# ----------------- Android SDK -----------------
ARG ANDROID_SDK_VERSION=6858069

# TODO: reuse android-sdk image MBS-7071
ENV ANDROID_HOME /opt/android-sdk

ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/cmdline-tools/tools/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator
ENV LD_LIBRARY_PATH $LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64/libstdc++:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_VERSION}_latest.zip && \
    unzip *tools*linux*.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/tools && \
    rm *tools*linux*.zip

# platform-tools For adb and libc
# tools          For mksdcard and avdmanager
# emulator       For emulator command and libraries
RUN mkdir $HOME/.android && touch $HOME/.android/repositories.cfg && \
    yes | sdkmanager --verbose --channel=3 \
    "platforms;android-${SDK_VERSION}" \
    "tools" \
    "system-images;android-${SDK_VERSION};google_apis;${EMULATOR_ARCH}" && \
    chmod -R o+rwX ${ANDROID_HOME}

RUN mkdir --parents $ANDROID_HOME/tools/keymaps && \
    touch $ANDROID_HOME/tools/keymaps/en-us && \
    mkdir --parents $ANDROID_HOME/tools/lib/pc-bios/keymaps && \
    touch $ANDROID_HOME/tools/lib/pc-bios/keymaps/en-us
# -----------------------------------------------

# ------------------ Emulators ------------------
# Create emulator and increase internal storage
RUN echo "no" | avdmanager create avd --name emulator_${SDK_VERSION} --package "system-images;android-${SDK_VERSION};google_apis;${EMULATOR_ARCH}" --abi google_apis/${EMULATOR_ARCH}

RUN mksdcard -l e 512M /sdcard.img

COPY hardware/config_${SDK_VERSION}.ini /root/.android/avd/emulator_${SDK_VERSION}.avd/config.ini
# -----------------------------------------------

# ----------------- Entrypoint ------------------
COPY prepare_snapshot.sh /

COPY adb_redirect.sh /
COPY run_emulator.sh /
COPY entrypoint.sh /

# https://developer.android.com/studio/command-line/adb#howadbworks
# ADB server port
EXPOSE 5037
# Console port
EXPOSE 5554
# ADB
EXPOSE 5555
# VNC
EXPOSE 5900

CMD ["/entrypoint.sh"]
# -----------------------------------------------