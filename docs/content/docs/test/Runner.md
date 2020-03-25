---
title: Test Runner
type: docs
---

# Avito test runner

## How to run tests in kubernetes from a local machine

{{< avito >}}

1. Get access to kubernetes cloud: [internal doc](http://links.k.avito.ru/Kubectl)
1. [Request](http://links.k.avito.ru/androidEmulatorServiceDesk) `exec` access to `android-emulator` namespace in `beta` cluster
1. Setup a context on `beta`, `android-emulator` with your user access. More about kubernetes context: [Official docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/#define-clusters-users-and-contexts)
1. Run tests with extra parameters specified, example for `:test-app`:

```shell script
./gradlew :subprojects:android-test:test-app:instrumentationUi 
    -Pci=true 
    -PkubernetesContext=<your context>
    -PkubernetesCaCertFile=<path to ca certificate file>
    -Pavito.registry=<avito registry>
```
