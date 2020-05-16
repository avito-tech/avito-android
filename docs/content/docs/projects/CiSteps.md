---
title: CI steps plugin
type: docs
---

# CI steps plugin

{{< hint warning>}}
Plugin wasn't tested outside Avito yet, so expect difficulties, or even blockers.
However, if you interested, please [contact us]({{< ref "/docs/Contacts.md" >}})!
{{< /hint >}}

Plugin creates chains of tasks for CI, encapsulating it under single gradle task.

## Getting started

Apply the plugin in the app's `build.gradle` file:

```groovy
plugins {
    id("com.avito.android.cd")
}
```

The plugin can be applied to a root project or any module.

{{%plugins-setup%}}

## Builds

First of all, name your chain:

{{< tabs "chain" >}}
{{< tab "Kotlin" >}}

```kotlin
builds {
    register("myChain") {
 
       //optional description for generated task
       taskDescription.set("This chain does something useful")
    }
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
builds {
    myChain {

       //optional description for generated task
       taskDescription.set("This chain does something useful")
    }
}
```

{{< /tab >}}
{{< /tabs >}}

### Avito example chains

- `localCheck` - compilation checks for local run
- `prCheck` - as fast as possible checks for Pull Request. It must conform [CI agreement]({{< ref "/docs/ci/CIValues.md" >}})
- `fullCheck` - as full as possible checks to be run after merges, non-blocking, could be slow
- `release` - chain to release our app

## Steps

Step is a declaration to run some logic. It works inside a chain:

{{< tabs "steps" >}}
{{< tab "Kotlin" >}}

```kotlin
build {
    register("prCheck") { // <--- chain

        unitTests {} // <--- step
        uiTests {}
        lint {}

        //optional description for generated task
        taskDescription.set("This chain does something useful")
    }
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
build {
    fastCheck { // <--- chain

        unitTests {} // <--- step
        uiTests {}
        lint {}

        //optional description for generated task
        taskDescription.set("This chain does something useful")
    }
}
```

{{< /tab >}}
{{< /tabs >}}

Now when you invoke `./gradlew fastCheck` gradle will run unitTests, uiTests and lint of corresponding project

### Built-in steps

#### UI tests

Runs instrumentation tests.

{{< tabs "ui tests" >}}
{{< tab "Kotlin" >}}

```kotlin
uiTests {
  configurations("configurationName") // list of instrumentation configuration to depends on
  sendStatistics = false // by default
  suppressFailures = false // by default
  useImpactAnalysis = false // by default
  suppressFlaky = false // by default. [игнорирование падений FlakyTest]({{< ref "/docs/test/FlakyTests.md" >}}).
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
uiTests {
  configurations = ["configurationName"] // list of instrumentation configuration to depends on
  sendStatistics = false // by default
  suppressFailures = false // by default
  useImpactAnalysis = false // by default
  suppressFlaky = false // by default. [игнорирование падений FlakyTest]({{< ref "/docs/test/FlakyTests.md" >}}).
}
```

{{< /tab >}}
{{< /tabs >}}

#### Performance tests

Runs performance tests.

{{< tabs "performance tests" >}}
{{< tab "Kotlin" >}}

```kotlin
performanceTests {
  configuration("configuration name") // performance configuration from Instrumentation plugin
  enabled = true // true by default
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
performanceTests {
  configuration = "configuration name" // performance configuration from Instrumentation plugin
  enabled = true // true by default
}
```

{{< /tab >}}
{{< /tabs >}}

#### Android lint

Run [Android lint]({{< ref "/docs/checks/AndroidLint.md" >}}) tasks.

{{< tabs "lint" >}}
{{< tab "Kotlin" >}}

```kotlin
lint {}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
lint {}
```

{{< /tab >}}
{{< /tabs >}}

#### Compile UI tests

Compile instrumentation tests. It is helpful in local development.

{{< tabs "compile ui tests" >}}
{{< tab "Kotlin" >}}

```kotlin
compileUiTests {}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
compileUiTests {}
```

{{< /tab >}}
{{< /tabs >}}

#### Unit tests

Run unit tests.

{{< tabs "unit tests" >}}
{{< tab "Kotlin" >}}

```kotlin
unitTests {}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
unitTests {}
```

{{< /tab >}}
{{< /tabs >}}

#### Upload to QApps

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to QApps (internal system)

{{< tabs "upload to qapps" >}}
{{< tab "Kotlin" >}}

```kotlin
artifacts {
    apk("debugApk", ...)
}
uploadToQapps {
    artifacts = setOf("debugApk")
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToQapps {
    artifacts = ["debugApk"]
}
```

{{< /tab >}}
{{< /tabs >}}

#### Upload to Artifactory

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to Artifactory.

{{< tabs "upload to artifactory" >}}
{{< tab "Kotlin" >}}

```kotlin
artifacts {
    file("myReport", "${project.buildDir}/reports/my_report.json")
}
uploadToArtifactory {
    artifacts = setOf("myReport")
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
artifacts {
    file("myReport", "${project.buildDir}/reports/my_report.json")
}
uploadToArtifactory {
    artifacts = ["myReport"]
}
```

{{< /tab >}}
{{< /tabs >}}

#### Upload to Prosector

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to [Prosector (internal)](http://links.k.avito.ru/cfxrREPBQ).

{{< tabs "upload to prosector" >}}
{{< tab "Kotlin" >}}

```kotlin
artifacts {
    apk("debugApk", ...)
}
uploadToProsector {
    artifacts = setOf("debugApk")
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToProsector {
    artifacts = ["debugApk"]
}
```

{{< /tab >}}
{{< /tabs >}}

#### Upload build results

{{<avito step>}}

Upload all build results to a deploy service.

{{< tabs "upload build result" >}}
{{< tab "Kotlin" >}}

```kotlin
uploadBuildResult {
    uiTestConfiguration = "regression" // instrumentation configuration
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
uploadBuildResult {
    uiTestConfiguration = "regression" // instrumentation configuration
}
```

{{< /tab >}}
{{< /tabs >}}

#### Deploy to Google Play

{{<avito step>}}

Deploy to Google play.

{{< tabs "deploy to google play" >}}
{{< tab "Kotlin" >}}

```kotlin
deploy {}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
deploy {}
```

{{< /tab >}}
{{< /tabs >}}

#### Configuration checks

{{<avito check>}}

Checks a repository configuration. See `:build-script-test` for details.

{{< tabs "config checks" >}}
{{< tab "Kotlin" >}}

```kotlin
configuration {}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
configuration {}
```

{{< /tab >}}
{{< /tabs >}}

### Using impact analysis in step

Step could use [Impact analysis]({{< ref "/docs/ci/ImpactAnalysis.md" >}}). It is disabled by default.

{{< tabs "use impact analysis" >}}
{{< tab "Kotlin" >}}

```kotlin
fastCheck {
    uiTests {
        useImpactAnalysis = true
    }
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
fastCheck {
    uiTests {
        useImpactAnalysis = true
    }
}
```

{{< /tab >}}
{{< /tabs >}}

### Suppressing errors in step

In different scenarios steps could fail whole build, some can be configured not to.

{{< tabs "suppressing errors" >}}
{{< tab "Kotlin" >}}

```kotlin
fastCheck {
    uiTests { 
        suppressFailures = false 
    }
}

release {
    uiTests { 
        suppressFailures = true 
    }
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
fastCheck {
    uiTests { 
        suppressFailures = false 
    }
}

release {
    uiTests { 
        suppressFailures = true 
    }
}
```

{{< /tab >}}
{{< /tabs >}}

### Collecting artifacts

Artifacts that planned to be used(uploaded somewhere) must be registered:

{{< tabs "collecting artifacts" >}}
{{< tab "Kotlin" >}}

```kotlin
artifacts {
   file("lintReport", "${project.buildDir}/reports/lint-results-release.html")
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
artifacts {
   file("lintReport", "${project.buildDir}/reports/lint-results-release.html")
}
```

{{< /tab >}}
{{< /tabs >}}

There are different types of artifacts:

- apk - gets apk by buildType and checks packageName and signature
- bundle - gets bundle by buildType and checks packageName and signature
- mapping - gets r8 mapping by buildType and checks availability
- file - gets any file by path and checks availability

{{< tabs "different types of artifacts" >}}
{{< tab "Kotlin" >}}

```kotlin
import com.avito.cd.BuildVariant.RELEASE

val releaseSha1 = "my sha" // it's public info, so safe to share

artifacts {
   apk("releaseApk", RELEASE, "com.avito.android", "${project.buildDir}/outputs/apk/release/avito.apk") { signature = releaseSha1 }
   bundle("releaseBundle", RELEASE, "com.avito.android", "${project.buildDir}/outputs/bundle/release/avito.aab") { signature = releaseSha1 }
   mapping("releaseMapping", RELEASE, "${project.buildDir}/outputs/mapping/release/mapping.txt")
   file("featureTogglesJson", "${project.buildDir}/reports/feature_toggles.json")
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

```groovy
def releaseSha1 = "my sha" // it's public info, so safe to share

artifacts {
   apk("releaseApk", RELEASE, "com.avito.android", "${project.buildDir}/outputs/apk/release/avito.apk") { signature = releaseSha1 }
   bundle("releaseBundle", RELEASE, "com.avito.android", "${project.buildDir}/outputs/bundle/release/avito.aab") { signature = releaseSha1 }
   mapping("releaseMapping", RELEASE, "${project.buildDir}/outputs/mapping/release/mapping.txt")
   file("featureTogglesJson", "${project.buildDir}/reports/feature_toggles.json")
}
```

{{< /tab >}}
{{< /tabs >}}

The first argument is a key for upload steps.

### Writing a custom step

Inherit from `BuildStep`, check available implementations as examples
