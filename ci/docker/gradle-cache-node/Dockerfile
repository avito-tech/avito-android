ARG DOCKER_REGISTRY=DOCKER_REGISTRY
# TODO: Use public image
FROM ${DOCKER_REGISTRY}/avito/openjdk:1.8.0

ADD https://docs.gradle.com/build-cache-node/jar/build-cache-node-7.0.jar build-cache-node.jar

COPY config.yaml /data/bin/conf/config.yaml

COPY gradle-cache-node /bin/gradle-cache-node

ENTRYPOINT /bin/gradle-cache-node
