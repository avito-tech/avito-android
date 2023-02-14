ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/avito/debian-minbase:11.3

ARG ARTIFACTORY_URL
ENV DEBIAN_FRONTEND=noninteractive

# TODO: remove java 11 after full migration in MBSA-234
RUN \
  apt-get update && \
  apt-get install -y \
    unzip \
    curl \
    git \
    gcc \
    openssh-client \
    ca-certificates \
    build-essential \
    acl \
    sudo \
    openjdk-11-jdk \
    openjdk-17-jdk \
    ca-certificates-java && \
  apt-get clean && \
  apt-get purge

ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/

# ----------------- Android SDK -----------------
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools/:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools

ARG ANDROID_SDK_BASE_URL=${ARTIFACTORY_URL}android-build-env/android_sdk

COPY unzip_from_url.sh /usr/local/bin/unzip_from_url.sh

# See an explanation for zip files in README in parent dir
# Use different image layers to cache better more stable parts
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/cmdline-tools/cmdline-tools-linux-7_0.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/tools/tools_linux_26_1_1.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/build-tools/build-tools-linux-32_0_0.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platforms/platforms-32_0_1.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platform-tools/platform-tools-linux-33_0_2.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/ndk/android-ndk-r25c-linux.zip $ANDROID_HOME/ndk

RUN rm /usr/local/bin/unzip_from_url.sh
# Move all the NDK files (which are inside ndk-version-dependent dir) to stable ${ANDROID_HOME}/ndk dir
RUN \
    cd $ANDROID_HOME/ndk && \
    NDK_DIR=$(ls) && \
    mv $NDK_DIR/* . && \
    rm -d $NDK_DIR
ENV AVITO_ANDROID_NDK ${ANDROID_HOME}/ndk

# --------------- Gradle Profiler -----------------
# https://github.com/gradle/gradle-profiler/releases
ARG PROFILER_VERSION=0.15.0

# Install gradle-profiler
RUN \
  cd /opt/ && \
  curl ${ARTIFACTORY_URL}gradle-ext-releases/org/gradle/profiler/gradle-profiler/${PROFILER_VERSION}/gradle-profiler-${PROFILER_VERSION}.zip \
    --progress-bar --location --output gradle-profiler.zip && \
  unzip gradle-profiler.zip && \
  mv gradle-profiler-${PROFILER_VERSION} gradle-profiler && \
  cp -a gradle-profiler/. /usr/local/ && \
  rm -rf /opt/gradle-profiler && \
  rm -f /opt/gradle-profiler.zip

ENV PATH $PATH:/usr/local/gradle-profiler/bin

# ----------------------------------------------------------------------------

# Entrypoint script will allow us run as non-root in the container.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
