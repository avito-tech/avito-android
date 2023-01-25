ARG DOCKER_REGISTRY

FROM ${DOCKER_REGISTRY}/android/openjdk:11 as BUILD

ARG ARTIFACTORY_URL

RUN mkdir /app
COPY . /app
WORKDIR /app

RUN ./gradlew subprojects:emcee:apk-discoverer:build \
    -PartifactoryUrl=${ARTIFACTORY_URL} \
    -Pcom.avito.android.tools.buildCache.remote.url=unused \
    --no-build-cache

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:9.13

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/subprojects/emcee/apk-discoverer/build/libs/emcee-apk-discoverer.jar emcee-apk-discoverer.jar

RUN apt-get update && \
	apt-get install --no-install-recommends -y \
	        curl \
    	    unzip \
    	    openjdk-11-jdk && \
    apt-get clean && apt-get purge

ARG ARTIFACTORY_URL
ARG BUILD_TOOLS_URL=${ARTIFACTORY_URL}android-build-env/android_sdk/build-tools/build-tools-linux-32_0_0.zip
ARG ANDROID_HOME=/opt/android-sdk
ARG BUILD_TOOLS_FILE_NAME=buildtools.zip

ENV LANG=C.UTF-8 \
    SHELL=/bin/bash \
    JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 \
    DEBIAN_FRONTEND=noninteractive

RUN echo $BUILD_TOOLS_URL && curl $BUILD_TOOLS_URL --progress-bar --location --output $BUILD_TOOLS_FILE_NAME && \
    unzip $BUILD_TOOLS_FILE_NAME -d $ANDROID_HOME && \
    rm -f $BUILD_TOOLS_FILE_NAME

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "emcee-apk-discoverer.jar"]
