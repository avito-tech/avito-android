FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN \
  apt-get update && \
  apt-get install -y \
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

ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/

# ------------------- Kotlin --------------------

ENV KOTLIN_VERSION=2.1.20
ENV KOTLIN_SHA256=a118197b0de55ffab2bc8d5cd03a5e39033cfb53383d6931bc761dec0784891a
RUN \
   curl https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip \
    --progress-bar --location --output kotlin.zip \
    && echo "${KOTLIN_SHA256}  kotlin.zip" | sha256sum -c - \
    && unzip kotlin.zip -d /opt \
    && rm -f kotlin.zip \
    && ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc \
    && ln -s /opt/kotlinc/bin/kotlin /usr/local/bin/kotlin

# ----------------- Android SDK -----------------
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools/:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/cmdline-tools/bin
# https://developer.android.com/studio/index.html#command-tools
ARG ANDROID_SDK_URL=https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip

COPY unzip_from_url.sh /usr/local/bin/unzip_from_url.sh

RUN unzip_from_url.sh $ANDROID_SDK_URL $ANDROID_HOME

COPY non-hermetic/packages.txt $ANDROID_HOME/packages.txt

# Update sdk and install components
RUN mkdir $HOME/.android && \
  echo "y" | sdkmanager --verbose \
    --sdk_root=$ANDROID_HOME \
    --package_file=$ANDROID_HOME/packages.txt && \
  chmod -R o+rwX $ANDROID_HOME && \
  rm $ANDROID_HOME/packages.txt

# --------------- Gradle Profiler -----------------
# https://github.com/gradle/gradle-profiler/releases
ARG PROFILER_VERSION=0.19.0

# Install gradle-profiler
RUN \
  cd /opt/ && \
  curl https://repo1.maven.org/maven2/org/gradle/profiler/gradle-profiler/${PROFILER_VERSION}/gradle-profiler-${PROFILER_VERSION}.zip \
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
  unzip_from_url.sh https://github.com/google/perfetto/releases/download/${PERFETTO_VERSION}/linux-amd64.zip /opt/perfetto && \
  install -m 755 /opt/perfetto/linux-amd64/* /usr/local/bin && \
  rm -rf /opt/perfetto

# --------------- stringsext -----------------
# https://github.com/getreu/stringsext/releases
ARG STRINGSEXT_VERSION=2.3.5
ARG STRINGSEXT_SHA256=d807efb639ef09b8589caed8e0221dcd0c5ad2701cf0a06b27b464e6c29db14f

RUN \
  curl https://github.com/getreu/stringsext/releases/download/v${STRINGSEXT_VERSION}/stringsext-v${STRINGSEXT_VERSION}-x86_64-unknown-linux-gnu.tar.gz \
    --progress-bar --location --output /tmp/stringsext.tar.gz && \
  echo "${STRINGSEXT_SHA256}  /tmp/stringsext.tar.gz" | sha256sum -c - && \
  tar -xzf /tmp/stringsext.tar.gz -C /usr/local/bin && \
  chmod +x /usr/local/bin/stringsext && \
  rm /tmp/stringsext.tar.gz && \
  stringsext --version

# ----------------------------------------------------------------------------

# Entrypoint script will allow us run as non-root in the container.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
