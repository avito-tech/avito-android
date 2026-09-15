ARG DOCKER_REGISTRY
ARG ARTIFACTORY_URL

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:13.0 AS build

ARG ARTIFACTORY_URL
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends openjdk-21-jdk-headless ca-certificates git \
 && rm -rf /var/lib/apt/lists/*
RUN mkdir /app
COPY . /app
WORKDIR /app
RUN ./gradlew :subprojects:instant-feedback:shadowJar \
    -Pavito.internalBuild=true \
    -PartifactoryUrl="${ARTIFACTORY_URL}" \
    --no-build-cache

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:13.0

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      openjdk-21-jdk-headless ca-certificates ca-certificates-java \
 && update-ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /app
WORKDIR /app
COPY --from=build /app/subprojects/instant-feedback/build/libs/instant-feedback-all.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
