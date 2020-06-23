FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN \
  apt-get update && \
  apt-get install -y \
    unzip \
    curl \
    git \
    gcc \
    openssh-client \
    ca-certificates \
    acl \
    sudo \
    openjdk-8-jdk && \
  apt-get clean && \
  apt-get purge

ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/

# ----------------- Android SDK -----------------
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools/:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${ANDROID_HOME}/tools/bin
# https://developer.android.com/studio/index.html#command-tools
ARG ANDROID_SDK_URL=https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip
ARG ANDROID_SDK_FILE_NAME=android-sdk.zip

RUN curl $ANDROID_SDK_URL --progress-bar --location --output $ANDROID_SDK_FILE_NAME && \
  unzip $ANDROID_SDK_FILE_NAME -d $ANDROID_HOME && \
  rm -f $ANDROID_SDK_FILE_NAME

# Update sdk and install components
# --package_file is broken https://issuetracker.google.com/issues/66465833
RUN mkdir $HOME/.android && \
  # empty file to mitigate warning
  touch $HOME/.android/repositories.cfg && \
  echo "y" | sdkmanager --verbose \
    "build-tools;29.0.3" \
    "patcher;v4" \
    "platform-tools" \
    "platforms;android-29" \
    "tools" \
    "extras;google;google_play_services" \
    "extras;google;m2repository" && \
  chmod -R o+rwX ${ANDROID_HOME}

# --------------- Gradle Profiler -----------------
# https://github.com/gradle/gradle-profiler/releases
ARG PROFILER_VERSION=0.12.0

# Install gradle-profiler
RUN \
  cd /opt/ && \
  curl https://repo.gradle.org/gradle/ext-releases-local/org/gradle/profiler/gradle-profiler/${PROFILER_VERSION}/gradle-profiler-${PROFILER_VERSION}.zip --progress-bar --location --output gradle-profiler.zip && \
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
