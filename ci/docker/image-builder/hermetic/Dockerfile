# See also details in README.md
ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/avito/debian-minbase:bookworm as BUILD

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install --no-install-recommends --yes openjdk-17-jdk-headless && \
    apt-get clean && apt-get purge

RUN mkdir /app
COPY . /app
WORKDIR /app

# Skip integration tests because they use Docker.
# This is impossible while building the image.
# We can't mount /var/run/docker.sock.
RUN ./gradlew build -x integrationTest

FROM ${DOCKER_REGISTRY}/avito/debian-minbase:bookworm

ENV DEBIAN_FRONTEND=noninteractive

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/build/libs/image-builder.jar image-builder.jar

RUN apt-get update && \
    apt-get install --no-install-recommends --yes \
                       openjdk-17-jdk-headless \
                       ca-certificates \
                       docker-buildx-plugin \
                       docker-ce && \
    apt-get clean && apt-get purge

ENTRYPOINT ["java", "-jar", "image-builder.jar"]
