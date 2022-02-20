# See also details in README.md

FROM openjdk:11 as BUILD

RUN mkdir /app
COPY . /app
WORKDIR /app

# Skip integration tests because they use Docker.
# This is impossible while building the image.
# We can't mount /var/run/docker.sock.
RUN ./gradlew build -x integrationTest

FROM openjdk:11

RUN mkdir /app
WORKDIR /app
COPY --from=BUILD /app/build/libs/image-builder.jar image-builder.jar

RUN apt-get update && \
	apt-get install --no-install-recommends --yes \
	                   apt-transport-https \
                       ca-certificates \
                       software-properties-common && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
    add-apt-repository \
       "deb [arch=amd64] https://download.docker.com/linux/debian \
       $(lsb_release -cs) \
       stable"

RUN apt-get update && \
    apt-get install --no-install-recommends --yes \
     docker-ce && \
    apt-get clean && apt-get purge

ENTRYPOINT ["java", "-jar", "image-builder.jar"]
