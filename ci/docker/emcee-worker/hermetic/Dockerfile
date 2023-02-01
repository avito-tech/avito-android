ARG DOCKER_REGISTRY

FROM ${DOCKER_REGISTRY}/android/openjdk:11 as BUILD

ARG ARTIFACTORY_URL

RUN mkdir /app
COPY . /app
WORKDIR /app

RUN ./gradlew subprojects:emcee:worker:build -x test \
    -PartifactoryUrl=${ARTIFACTORY_URL} \
    -Pcom.avito.android.tools.buildCache.remote.url=unused \
    --no-build-cache \
    --stacktrace

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:9.13

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/subprojects/emcee/worker/build/libs/emcee-worker.jar emcee-worker.jar
COPY --from=BUILD /app/ci/docker/emcee-worker/config.json config.json
COPY --from=BUILD /app/ci/docker/emcee-worker/cloud-config.json cloud-config.json

# -------------------- Common -------------------
# net-tools             basic network primitives
# socat                 redirecting adb and VNC from emulator to host
# libglu1               emulators software rendering
# libpulse0, libx...    qemu x64 startup (API 30)
# lib32stdc++6          mksdcard android sdk tool
RUN apt-get update && \
	apt-get install --no-install-recommends -y \
	        curl \
    	    unzip \
    	    openjdk-11-jdk && \
    apt-get install -y \
            net-tools \
            socat \
            libglu1 \
            libpulse0 \
            libx11-6 libxcb1 libxdamage1 libnss3 libxcomposite1 libxcursor1 libxi6 libxext6 libxfixes3 \
            lib32stdc++6 && \
    apt-get clean && apt-get purge

ARG ANDROID_HOME=/opt/android-sdk
ARG ARTIFACTORY_URL
ARG SDK_VERSIONS

# DEBIAN_FRONTEND - to prevent timezone questions
ENV LANG=C.UTF-8 \
    SHELL=/bin/bash \
    JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 \
    DEBIAN_FRONTEND=noninteractive \
    ANDROID_SDK_ROOT=$ANDROID_HOME \
    PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools/bin \
    LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader
# -----------------------------------------------

# ----------------- Android SDK -----------------
# https://developer.android.com/studio/index.html#command-tools
# Additional info about directory structure: https://stackoverflow.com/a/61176718/981330
ARG ANDROID_SDK_BASE_URL=${ARTIFACTORY_URL}android-build-env/android_sdk

COPY ci/docker/android-emulator/hardware ./hardware
COPY ci/docker/android-emulator/unzip_from_url.sh ci/docker/android-emulator/prepare_snapshot.sh ci/docker/android-emulator/run_emulator.sh  ci/docker/emcee-worker/setup_environment.sh ci/docker/emcee-worker/entrypoint.sh ./
RUN ./setup_environment.sh $ANDROID_SDK_BASE_URL $ANDROID_HOME "${SDK_VERSIONS}"

# 80   - Worker HTTP server
EXPOSE 80

CMD ["./entrypoint.sh"]
