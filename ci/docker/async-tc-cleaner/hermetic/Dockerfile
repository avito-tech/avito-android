ARG DOCKER_REGISTRY
ARG ARTIFACTORY_URL

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:bookworm AS build

ARG ARTIFACTORY_URL
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends openjdk-17-jdk-headless ca-certificates git \
 && rm -rf /var/lib/apt/lists/*
RUN mkdir /app
COPY . /app
WORKDIR /app
RUN ./gradlew :subprojects:async-tc-cleaner:shadowJar \
    -Pavito.internalBuild=true \
    -PartifactoryUrl="${ARTIFACTORY_URL}" \
    --no-build-cache

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:bookworm

ARG ARTIFACTORY_URL
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      openjdk-17-jre-headless ca-certificates ca-certificates-java curl \
 && update-ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /app
WORKDIR /app
COPY --from=build /app/subprojects/async-tc-cleaner/build/libs/async-tc-cleaner-all.jar app.jar

ARG RMZ_VERSION=3.1.7
ARG RMZ_SHA256=cb9972f536de49788ce5ccb7b2456e0a6a6dd1ea8d992ccea85e853d21f3f048
RUN curl "${ARTIFACTORY_URL}android-build-env/rmz/${RMZ_VERSION}/x86_64-unknown-linux-musl-rmz" \
      --progress-bar --location --fail --output /usr/local/bin/rmz \
 && echo "${RMZ_SHA256}  /usr/local/bin/rmz" | sha256sum -c - \
 && chmod +x /usr/local/bin/rmz \
 && rmz --version | grep -xF "rmz ${RMZ_VERSION}"

ENV JAVA_OPTS="-Xmx128m -XX:+UseG1GC"

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
