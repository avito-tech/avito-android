---
title: Test Runner
type: docs
---

# Avito test runner

This is the Gradle to run Android instrumentation tests.

It can do the following:

- Filter tests by annotations, by packages, by previous runs.
- Run tests in parallel. It orchestrates emulators in Kubernetes or uses local emulators.
- Rerun failed tests to deal with flakiness
- Save tests result in a report.\
It uses an internal TMS (test management system). We are working on support other formats.

## Example

Check out a configuration to run in `GradleInstrumentationPluginConfiguration` in the [test app](https://github.com/avito-tech/avito-android/blob/develop/subprojects/android-test/test-app/build.gradle.kts#L114)

### How to run tests on local emulator

1. Run an emulator with 27 API
1. Run `./gradlew :subprojects:android-test:test-app:instrumentationLocal`

### How to run tests in kubernetes from a local machine

{{< avito >}}

1. Get access to kubernetes cloud: [internal doc](http://links.k.avito.ru/Kubectl)
1. [Request](http://links.k.avito.ru/androidEmulatorServiceDesk) `exec` access to `android-emulator` namespace in `beta` cluster
1. Setup a context on `beta`, `android-emulator` with your user access.\
More about kubernetes context: [Official docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/#define-clusters-users-and-contexts)
1. Run tests with extra parameters specified. The example for `:test-app`:

```shell script
./gradlew :subprojects:android-test:test-app:instrumentationUi 
    -Pci=true 
    -PkubernetesContext=<your context>
    -PkubernetesCaCertFile=<path to ca certificate file>
    -Pavito.registry=<avito registry>
```
