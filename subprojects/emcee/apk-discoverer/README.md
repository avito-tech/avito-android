# APK Discoverer

APK Discoverer is a service which allows to extract tests and package name from APK. 
It is currently used in Emcee cloud to filter tests and run them from web dashboard. 

## Requirements

- Android SDK installed at `/opt/android-sdk` with `build-tools:32.0.0`

## Run in Docker locally

Build image using

```bash
docker build -t emcee-apk-discoverer:local -f ci/docker/emcee-apk-discoverer/hermetic/Dockerfile --build-arg DOCKER_REGISTRY=<...> --build-arg ARTIFACTORY_URL=<...> .
```
and run it with
```bash
docker run --rm -it  --network="host" emcee-apk-discoverer:local
```
## API

#### /tests

Request:
```bash
curl -X POST -H "Content-Type: application/json" -d '{"apkUrl":"path/to/apk"}' http://localhost:8080/tests
```
Response:
```json
{
  "tests": [
    "com.avito.emcee.ExampleInstrumentedTest#packageNameIsCorrect",
    "com.avito.emcee.ExampleInstrumentedTest#thisIsExampleInstrumentedTestClass"
  ]
}
```

#### /packagename

Request: 

```bash
curl -X POST -H "Content-Type: application/json" -d '{"apkUrl":"path/to/apk"}' http://localhost:8080/packagename
```

Response:
```json
{
  "packageName": "com.avito.emcee.test"
}
```
