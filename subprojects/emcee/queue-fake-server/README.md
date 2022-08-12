# Fake emcee server

## How to run locally

We use [Gradle application plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
Plugin adds `:run` task which run `main` function in specified `mainClass:
```kotlin
application {
    mainClass.set("ClassFullName")
}
```

```shell
./gradlew :subprojects:emcee:queue-fake-server:run
```

## How to test

Ask me for sharing POSTMAN files

## How to build Docker image

[Ktor docker image sample](https://github.com/ktorio/ktor-samples/tree/main/docker-image)

We use Gradle plugin `com.google.cloud.tools.jib`
Plugin adds task `jibDockerBuild` - to build an image to the local Docker registry run

### Using minukube docker daemon

```shell
eval $(minikube docker-env)
./gradlew jibDockerBuild
```

### Ktor useful references

[Ktor http api tutorial](https://ktor.io/docs/creating-http-apis.html)
[Content serialization](https://ktor.io/docs/serialization.html)
