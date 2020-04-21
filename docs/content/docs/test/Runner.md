---
title: Test Runner
type: docs
---

# Avito test runner

This is the Gradle plugin to run Android instrumentation tests.

It can do the following:

- Filter tests by annotations, by packages, by previous runs.
- Run tests in parallel. It orchestrates emulators in Kubernetes or uses local emulators.
- Rerun failed tests to deal with flakiness
- Save tests result in a report.\
It uses an internal TMS (test management system). We are working on support other formats.

## Getting started

1. Apply the instrumentation-tests gradle plugin
Add to your build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("com.avito.android.instrumentation-tests")
}
```

2. Add common plugin configuration

```kotlin
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    // they are required for Avito app. We will make them optional in future.
    reportApiUrl = "http://stub"
    reportApiFallbackUrl = "http://stub"
    reportViewerUrl = "http://stub"
    registry = "registry"
    sentryDsn = "http://stub-project@stub-host/0"
    slackToken = "stub"
    fileStorageUrl = "http://stub"

    configurationsContainer.register("Local") {
        tryToReRunOnTargetBranch = false

        targetsContainer.register("api27") {
            deviceName = "API27"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = LocalEmulator.device(27)
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }
}
```

3. Run tests via gradle task

```shell script
    ./gradlew :<projectPath>:instrumentation<ConfigurationName>           
```

In our case:

```shell script
    ./gradlew :<projectPath>:instrumentationLocal 
```

## Examples

Check out a configuration to run in `GradleInstrumentationPluginConfiguration` in the [test app](https://github.com/avito-tech/avito-android/blob/develop/subprojects/android-test/test-app/build.gradle.kts#L114)

## Filtering tests for execution

0. [You must apply a plugin]({{< relref "#getting-started">}})
1. Create filter

```kotlin
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        fromSource.includeByAnnotations(annotations)
        fromSource.excludeByAnnotations(annotations)
        fromSource.includeByPrefixes(prefixes)
        fromSource.excludeByPrefixes(prefixes)
        
        // it is internal for Avito. It uses run history from our test-report system.
        fromRunHistory.excludePreviousStatuses(statuses)
        fromRunHistory.excludePreviousStatuses(statuses)
        fromRunHistory.report("reportId") { reportStatuses ->
            reportStatuses.include(statuses)
            reportStatuses.exclude(statuses)
        }
    }
}
```

2. Add filter to configuration

```kotlin
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    configurationsContainer.register("Local") {
        filter = "filterName"
        // else...   
    }    
}
```

### Filter tests by annotations

```kotlin
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        val yourFullyQualifiedAnnotationName = "package.AnnotationClassName"
        
        val annotations = setOf(youFullyQualifiedAnnotationName)
        // will include only tests with at least one annotation
        fromSource.includeByAnnotations(annotations)
        // will exclude all tests with at least one annotation
        fromSource.excludeByAnnotations(annotations)
    }
}
```

### Filter tests by prefix or name

```kotlin
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        val packageTestFilter = "testPackage"
        val classTestFilter = "testPackage.testClass"
        val fullyQualifiedTestFilter = "testPackage.testClass.testMetod"
        val prefixes = setOf(packageTestFilter, classTestFilter, fullyQualifiedTestFilter)
        // will include only tests from package, class and concrete test
        fromSource.includeByPrefixes(prefixes)
        
        // will exclude all tests from package, class and concrete test
        fromSource.excludeByPrefixes(prefixes)
    }
}
```

### Filter tests by statuses from previous run on the same commit

```kotlin
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        val statuses = setOf(RunStatus.Success)
        
        // will run only Success previously Succeed tests
        fromRunHistory.includePreviousStatuses(statuses)
        // will run all tests except previously Succeed
        fromRunHistory.excludePreviousStatuses(statuses)
    }
}
```

### Filter tests by statuses from report by id

```kotlin
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        // report-viewer report id
        val reportId = "id"
        val statuses = setOf(RunStatus.Failed)
        
        fromRunHistory.report(reportId) { reportStatuses ->
            // will run only Failed tests from report 
            reportStatuses.include(statuses)
            // will run all tests except Failed tests from report
            reportStatuses.exclude(statuses)
        }
    }
}
```

### Apply a filter without changing `build.gradle.kts`

1. Add custom gradle property for filter name to gradle.properties file

```properties
filterName="filterName"
```

2. Let plugin configuration depends on `property`

```kotlin
val filterName by project

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    configurationsContainer.register("Local") {
        filter = filterName
        // else...   
    }    
}
```

3. Add `property` to cli command if you want to override `filterName`

```shell script
./gradlew instrumentationLocal -PfilterName=<any name of defined filter>
```

### Customize filter without changing `build.gradle.kts`

You can customize everything by adding custom properties to cli command e.g.

1. You have [filter including tests by annotation]({{< relref "#filter-tests-by-annotations">}})
2. Add logic to check presence of gradle property.

```kotlin
val includedAnnotation: String? by project
 
extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {
    filters.register("filterName") {
        val annotation = if(includedAnnotation != null){
            includedAnnotation
        } else {
            "package.AnnotationClassName"
        }
        
        val annotations = setOf(annotation)
        fromSource.includeByAnnotations(annotations)
    }
}
```

3. Run gradle task with property `includedAnnotation` to override filter

```shell script
./gradlew instrumentationLocal -PincludedAnnotation="package.AnotherAnnotationClassName"
```

## Choosing target for tests execution

### Run tests on kubernetes target from a local machine

{{< avito >}}

1. Get access to kubernetes cloud: [internal doc](http://links.k.avito.ru/Kubectl)
1. [Request](http://links.k.avito.ru/androidEmulatorServiceDesk) `exec` access to `android-emulator` namespace in `beta` cluster
1. Setup a context on `beta`, `android-emulator` with your user access.\
More about kubernetes context: [Official docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/#define-clusters-users-and-contexts)
1. Add a configuration with target on kubernetes

```kotlin
TBD
```

1. Run tests with extra parameters specified. The example for `:test-app`:

```shell script
./gradlew :subprojects:android-test:test-app:instrumentationUi 
    -Pci=true 
    -PkubernetesContext=<your context>
    -PkubernetesCaCertFile=<path to ca certificate file>
    -Pavito.registry=<avito registry>
```

### Run tests on local emulator target

0. Add a configuration with target on local emulator

```kotlin
configurationsContainer.register("Local") {

        targetsContainer.register("api27") {
            deviceName = "API27"

            scheduling = SchedulingConfiguration().apply {
                quota = QuotaConfiguration().apply {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                reservation = TestsBasedDevicesReservationConfiguration().apply {
                    device = LocalEmulator.device(27)
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }
```

1. Run an emulator with 27 API
2. Run gradle cli command

```shell script
`./gradlew :<project gradle path>:instrumentation<configuration name>`, e.g.
`./gradlew :subprojects:android-test:test-app:instrumentationLocal`, e.g.
```

## Run test on APK was built before

Plugin builds APKs on his own by default.\
If for any reason you have to build APK externally, you can pass files manually:

```kotlin
// optional
applicationApk = "/path/to/app.apk"
// optional
testApplicationApk = "/path/to/test-app-debug-androidTest.apk"
```
