FROM nvidia/opengl:1.0-glvnd-runtime-ubuntu18.04

ARG SDK_VERSION

ENV VERSION=${SDK_VERSION}

# -------------------- Common -------------------
# socat         For redirecting adb and vnc from emulator to host
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
# TODO: reuse android-sdk image MBS-7071
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools/:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${ANDROID_HOME}/tools/bin
ENV LD_LIBRARY_PATH $LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64/libstdc++:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader

RUN wget https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip -O android-sdk.zip && \
    unzip android-sdk.zip -d $ANDROID_HOME && \
    rm -f android-sdk.zip

# platform-tools For adb and libc
# tools          For mksdcard and avdmanager
# emulator       For emulator command and libraries
RUN mkdir $HOME/.android && \
    touch $HOME/.android/repositories.cfg && \
    yes | sdkmanager --verbose --channel=3 \
        "platform-tools" \
        "platforms;android-28" \
        "emulator" \
        "tools" \
        "system-images;android-${SDK_VERSION};google_apis;x86" && \
    chmod -R o+rwX ${ANDROID_HOME}

RUN mkdir --parents $ANDROID_HOME/tools/keymaps && \
    touch $ANDROID_HOME/tools/keymaps/en-us && \
    mkdir --parents $ANDROID_HOME/tools/lib/pc-bios/keymaps && \
    touch $ANDROID_HOME/tools/lib/pc-bios/keymaps/en-us
# -----------------------------------------------

# ------------------ Emulators ------------------
# Create emulator and increase internal storage
RUN echo "no" | avdmanager create avd --name emulator_${SDK_VERSION} --package "system-images;android-${SDK_VERSION};google_apis;x86" --abi google_apis/x86

RUN mksdcard -l e 512M /sdcard.img

COPY hardware/config_${SDK_VERSION}.ini /root/.android/avd/emulator_${SDK_VERSION}.avd/config.ini
# -----------------------------------------------

# ----------------- Entrypoint ------------------
COPY prepare_snapshot.sh /

COPY adb_redirect.sh /
COPY run_emulator.sh /
COPY entrypoint.sh /

EXPOSE 5037
EXPOSE 5554
EXPOSE 5555
EXPOSE 5900

CMD ["/entrypoint.sh"]
# -----------------------------------------------
