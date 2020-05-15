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

For example, scenario "Pull Request checks".\
Typically, you need to create multiple CI builds and wire them into a chain.\
Chain configuration(and possibly parameters) leaks to CI server settings, and you have less control over its changes inside gradle.\
It makes sense if resources of single machine limited, but if all chain could run effectively
(e.g. tasks runs in parallel and server has extra resources) on single machine, having all configuration in Gradle has benefits.

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

```groovy
builds {
    myChain {
       ...
    }
}
```

### Avito example chains

- `localCheck` - compilation checks for local run
- `fastCheck` - as fast as possible checks for Pull Request. It must conform [CI agreement]({{< ref "/docs/ci/CIValues.md" >}})
- fullCheck - full as possible checks to be run after merges, non-blocking, could be slow
- `release` - chain to release our app

## Steps

Step is a declaration to run some logic. It works inside a chain:

```groovy
build {
    fastCheck { // <--- chain
        unitTests {} // <--- step
        uiTests {}
        lint {}
    }
}
```

### Built-in steps

#### UI tests

Runs instrumentation tests.

```groovy
uiTests {
  configurations = ["configurationName"] // list of instrumentation configuration to depends on
  sendStatistics = false // by default
  suppressFailures = false // by default
  useImpactAnalysis = false // by default
  suppressFlaky = false // by default. [игнорирование падений FlakyTest]({{< ref "/docs/test/FlakyTests.md" >}}).
}
```

#### Performance tests

Runs performance tests.

```groovy
performanceTests {
  configuration = "configuration name" // performance configuration from Instrumentation plugin
  enabled = true // true by default
}
```

#### Android lint

Run [Android lint]({{< ref "/docs/checks/AndroidLint.md" >}}) tasks.

```groovy
lint {}
```

#### Compile UI tests

Compile instrumentation tests. It is helpful in local development.

```groovy
compileUiTests {}
```

#### Unit tests

Run unit tests.

```groovy
unitTests {}
```

#### Upload to QApps

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to QApps (internal system)

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToQapps {
    artifacts = ["debugApk"]
}
```

#### Upload to Artifactory

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to Artifactory.

```groovy
artifacts {
    file("myReport", "${project.buildDir}/reports/my_report.json")
}
uploadToArtifactory {
    artifacts = ["myReport"]
}
```

#### Upload to Prosector

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to [Prosector (internal)](http://links.k.avito.ru/cfxrREPBQ).

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToProsector {
    artifacts = ["debugApk"]
}
```

#### Upload build results

{{<avito step>}}

Upload all build results to a deploy service.

```groovy
uploadBuildResult {
    uiTestConfiguration = "regression" // instrumentation configuration
}
```

#### Deploy to Google Play

{{<avito step>}}

Deploy to Google play.

```groovy
deploy {}
```

#### Configuration checks

{{<avito check>}}

Checks a repository configuration. See `:build-script-test` for details.

```groovy
    configuration {}
```

### Using impact analysis in step

Step could use [Impact analysis]({{< ref "/docs/ci/ImpactAnalysis.md" >}})(disabled by default):

```groovy
fastCheck {
    uiTests {
        useImpactAnalysis = true
    }
}
```

### Suppressing errors in step

In different scenarios steps could fail whole build, some can be configured not to.

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

### Collecting artifacts

Artifacts that planned to be used(uploaded somewhere) must be registered:

```groovy
artifacts {
   file("lintReport", "${project.buildDir}/reports/lint-results-release.html")
}
```

There are different types of artifacts:

- apk - gets apk by buildType and checks packageName and signature
- bundle - gets bundle by buildType and checks packageName and signature
- mapping - gets r8 mapping by buildType and checks availability
- file - gets any file by path and checks availability

```groovy
artifacts {
   apk("releaseApk", RELEASE, "com.avito.android", apkPath("release")) { signature = releaseSha1 }
   bundle("releaseBundle", RELEASE, "com.avito.android", bundlePath("release")) { signature = releaseSha1 }
   mapping("releaseMapping", RELEASE, "${project.buildDir}/outputs/mapping/release/mapping.txt")
   file("featureTogglesJson", "${project.buildDir}/reports/feature_toggles.json")
}
```

First argument registers key, that can be used in upload steps

### Writing a custom step

Inherit from `BuildStep`, check available implementations as examples
