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

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      openjdk-17-jre-headless ca-certificates ca-certificates-java \
 && update-ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /app
WORKDIR /app
COPY --from=build /app/subprojects/async-tc-cleaner/build/libs/async-tc-cleaner-all.jar app.jar

ENV JAVA_OPTS="-Xmx128m -XX:+UseG1GC"

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
