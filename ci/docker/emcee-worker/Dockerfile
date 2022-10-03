ARG DOCKER_REGISTRY

FROM ${DOCKER_REGISTRY}/android/openjdk:11 as BUILD

ARG ARTIFACTORY_URL

RUN mkdir /app
COPY . /app
WORKDIR /app

RUN ./gradlew subprojects:emcee:worker:build \
    -PartifactoryUrl=${ARTIFACTORY_URL} \
    -Pcom.avito.android.tools.buildCache.remote.url=unused \
    --no-build-cache

FROM ${DOCKER_REGISTRY}/android/openjdk:11

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/subprojects/emcee/worker/build/libs/emcee-worker.jar emcee-worker.jar
COPY --from=BUILD /app/ci/docker/emcee-worker/config.json config.json

EXPOSE 80

ENTRYPOINT java -jar emcee-worker.jar start -c config.json -d
