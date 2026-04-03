ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/avito/debian-minbase:12.7

ARG ARTIFACTORY_URL
ENV DEBIAN_FRONTEND=noninteractive

RUN \
  apt-get update && \
  apt-get install -y \
    libjemalloc2 \
    cloc \
    zip \
    unzip \
    curl \
    git \
    git-lfs \
    gcc \
    openssh-client \
    ca-certificates ca-certificates-java \
    build-essential \
    acl \
    sudo \
    openjdk-17-jdk \
    shellcheck bats && \
  apt-get clean && \
  apt-get purge

# https://github.com/facebook/jemalloc/wiki/getting-started
ENV JEMALLOC_PATH=/usr/lib/x86_64-linux-gnu/libjemalloc.so.2
RUN test -f "$JEMALLOC_PATH" || (echo "jemalloc not found at $JEMALLOC_PATH" && exit 1)

ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/

# ------------------- Kotlin --------------------

ENV KOTLIN_VERSION=2.1.20
ENV KOTLIN_SHA256=a118197b0de55ffab2bc8d5cd03a5e39033cfb53383d6931bc761dec0784891a
RUN \
   curl ${ARTIFACTORY_URL}android-build-env/kotlin/kotlin-compiler-${KOTLIN_VERSION}.zip \
    --progress-bar --location --output kotlin.zip \
    && echo "${KOTLIN_SHA256}  kotlin.zip" | sha256sum -c - \
    && unzip kotlin.zip -d /opt \
    && rm -f kotlin.zip \
    && ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc \
    && ln -s /opt/kotlinc/bin/kotlin /usr/local/bin/kotlin

# ----------------- Android SDK -----------------
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools/:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools

ARG ANDROID_SDK_BASE_URL=${ARTIFACTORY_URL}android-build-env/android_sdk

COPY unzip_from_url.sh /usr/local/bin/unzip_from_url.sh

# See an explanation for zip files in README in parent dir
# Use different image layers to cache better more stable parts
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/cmdline-tools/cmdline-tools-linux-19_0.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/build-tools/build_tools_linux_36_0_0.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platforms/platforms_36_0_2.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/platform-tools/platform-tools-linux-36_0_0.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/ndk/ndk-linux-25_2_9519653.zip $ANDROID_HOME
RUN unzip_from_url.sh ${ANDROID_SDK_BASE_URL}/cmake/cmake_3_22_1.zip $ANDROID_HOME

# --------------- Gradle Profiler -----------------
# https://github.com/gradle/gradle-profiler/releases
ARG PROFILER_VERSION=0.19.0

# Install gradle-profiler
RUN \
  cd /opt/ && \
  curl ${ARTIFACTORY_URL}android-build-env/gradle-profiler/gradle-profiler-${PROFILER_VERSION}.zip \
    --progress-bar --location --output gradle-profiler.zip && \
  unzip gradle-profiler.zip && \
  mv gradle-profiler-${PROFILER_VERSION} gradle-profiler && \
  cp -a gradle-profiler/. /usr/local/ && \
  rm -rf /opt/gradle-profiler && \
  rm -f /opt/gradle-profiler.zip

ENV PATH $PATH:/usr/local/gradle-profiler/bin

# --------------- Perfetto -----------------
# https://github.com/google/perfetto/releases/
ARG PERFETTO_VERSION=v49.0

# Install Perfetto tracebox
RUN \
  mkdir -p /opt/perfetto && \
  unzip_from_url.sh ${ARTIFACTORY_URL}android-build-env/perfetto/${PERFETTO_VERSION}/linux-amd64.zip /opt/perfetto && \
  install -m 755 /opt/perfetto/linux-amd64/* /usr/local/bin && \
  rm -rf /opt/perfetto

# --------------- Skill Validator -----------------
ARG SKILL_VALIDATOR_VERSION=1.5.2

RUN \
  curl ${ARTIFACTORY_URL}android-build-env/skill-validator/${SKILL_VALIDATOR_VERSION}/skill-validator-linux-amd64 \
    --progress-bar --location --fail --output /usr/local/bin/skill-validator && \
  chmod +x /usr/local/bin/skill-validator && \
  skill-validator --version

# ----------------------------------------------------------------------------

RUN rm /usr/local/bin/unzip_from_url.sh

# Entrypoint script will allow us run as non-root in the container.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
