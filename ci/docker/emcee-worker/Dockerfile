ARG DOCKER_REGISTRY
ARG ARTIFACTORY_URL
ARG GRADLE_GITHUB_BUILD_CACHE_URL

FROM ${DOCKER_REGISTRY}/android/openjdk:11 as BUILD

RUN mkdir /app
COPY . /app
WORKDIR /app

RUN ./gradlew subprojects:emcee:worker:build \
    -PartifactoryUrl=${ARTIFACTORY_URL} \
    -Pcom.avito.android.tools.buildCache.remote.url=${GRADLE_GITHUB_BUILD_CACHE_URL}

RUN ls subprojects/emcee/worker/build/libs

FROM ${DOCKER_REGISTRY}/android/openjdk:11

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/subprojects/emcee/worker/build/libs/emcee-worker.jar emcee-worker.jar

# TODO: add parameters
ENTRYPOINT ["java", "-jar", "emcee-worker.jar"]
