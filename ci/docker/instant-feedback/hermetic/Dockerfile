ARG DOCKER_REGISTRY
ARG ARTIFACTORY_URL

FROM ${DOCKER_REGISTRY}/android/openjdk:17-bullseye AS build
ARG ARTIFACTORY_URL
RUN mkdir /app
COPY . /app
WORKDIR /app
RUN ./gradlew :subprojects:instant-feedback:shadowJar \
    -Pavito.internalBuild=true \
    -PartifactoryUrl="${ARTIFACTORY_URL}" \
    --no-build-cache

FROM ${DOCKER_REGISTRY}/android/openjdk:17-bullseye
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/subprojects/instant-feedback/build/libs/instant-feedback-all.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
