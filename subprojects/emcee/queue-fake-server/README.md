# Fake emcee server

## How to run locally

We use [Gradle application plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
Plugin adds a `:run` task which invokes `main` function in specified `mainClass`:

```kotlin
application {
    mainClass.set("ClassFullName")
}
```

```shell
./gradlew :subprojects:emcee:queue-fake-server:run
```

## How to test

- Using POSTMAN files (ask us if you need these files);
- Run a real worker instance with the `./gradlew :subprojects:emcee:worker:run` command.

## How to build Docker image

We use Gradle plugin `com.bmuschko.docker-java-application`. See [official documentation](https://bmuschko.github.io/gradle-docker-plugin/current/user-guide) for more details.
The plugins adds `buildDockerImage` tasks, which builds a Docker image

Run container using the following command:
`docker run -it --rm -p 41000:41000 emcee-queue:latest`

Then check if the queue responds correctly using `curl`:
`curl -X POST <URL>/<request>`

### Using minukube docker daemon

```shell
eval $(minikube docker-env)
./gradlew :subprojects:emcee:queue-fake-server:dockerBuildImage  

minikube kubectl -- create -f subprojects/emcee/k8s/emcee.yaml
```

Find the URL of the `emcee` service using: 
```shell
minikube service list
```

Then check if the queue responds correctly using `curl`:
`curl -X POST https:127.0.0.1/<request>`

### Useful references

[Ktor http api tutorial](https://ktor.io/docs/creating-http-apis.html)
[Ktor content serialization](https://ktor.io/docs/serialization.html)
