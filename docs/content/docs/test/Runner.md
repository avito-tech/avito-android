---
title: Test Runner
type: docs
---

# Avito test runner

## How to run tests in kubernetes from a local machine (internal)

1. Get access to kubernetes cloud: [internal doc](http://links.k.avito.ru/Kubectl)
1. [Request](http://links.k.avito.ru/androidEmulatorServiceDesk) `exec` access to `android-emulator` namespace in `beta` cluster
1. Setup a context on `beta/android-emulator` with your user access (also described in first doc)
1. Run tests with extra parameters specified, example for `:test-app`:

```shell script
./gradlew :subprojects:android-test:test-app:instrumentationUi 
    -Pci=true 
    -PkubernetesContext=<your context>
    -PkubernetesCaCertFile=<kube cert>
    -Pavito.registry=<avito registry>
```
