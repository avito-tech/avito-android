# CI steps plugin

???+ warning 
    Plugin wasn't tested outside Avito yet, so expect difficulties, or even blockers. 
    However, if you interested, please contact us.

Plugin creates chains of tasks for CI, encapsulating it under single gradle task.

## Getting started

Apply the plugin in the app's `build.gradle` file:

```groovy
plugins {
    id("com.avito.android.cd")
}
```

The plugin can be applied to a root project or any module.

--8<--
plugins-setup.md
--8<--

## Builds

First, name your chain:

=== "Kotlin"

    ```kotlin
    builds {
        register("myChain") {
     
           //optional description for generated task
           taskDescription.set("This chain does something useful")
        }
    }
    ```

=== "Groovy"

    ```groovy
    builds {
        myChain {
    
           //optional description for generated task
           taskDescription.set("This chain does something useful")
        }
    }
    ```

### Avito example chains

- `localCheck` - compilation checks for local run
- `prCheck` - as fast as possible checks for Pull Request
- `fullCheck` - as full as possible checks to be run after merges, non-blocking, could be slow
- `release` - chain to release our app

## Steps

Step is a declaration to run some logic. It works inside a chain:

=== "Kotlin"

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

=== "Groovy"

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

Now when you invoke `./gradlew fastCheck` gradle will run unitTests, uiTests and lint of corresponding project

### Built-in steps

#### UI tests

Runs instrumentation tests.

=== "Kotlin"

    ```kotlin
    uiTests {
      configurations("configurationName") // list of instrumentation configuration to depends on
      sendStatistics = false // by default
      suppressFailures = false // by default
      useImpactAnalysis = true // by default
      suppressFlaky = false // by default. [игнорирование падений FlakyTest](../test/FlakyAnnotation.md).
    }
    ```

=== "Groovy"

    ```groovy
    uiTests {
      configurations = ["configurationName"] // list of instrumentation configuration to depends on
      sendStatistics = false // by default
      suppressFailures = false // by default
      useImpactAnalysis = true // by default
      suppressFlaky = false // by default. [игнорирование падений FlakyTest](../test/FlakyAnnotation.md).
    }
    ```

#### Android lint

Runs Android lint tasks.

=== "Kotlin"

    ```kotlin
    lint {}
    ```

=== "Groovy"

    ```groovy
    lint {}
    ```

#### Compile UI tests

Compile instrumentation tests. It is helpful in local development.

=== "Kotlin"

    ```kotlin
    compileUiTests {}
    ```

=== "Groovy"

    ```groovy
    compileUiTests {}
    ```

#### Unit tests

Run unit tests.

=== "Kotlin"

    ```kotlin
    unitTests {}
    ```

=== "Groovy"

    ```groovy
    unitTests {}
    ```

#### Upload to QApps

--8<--
avito-disclaimer.md
--8<--

Upload [artifacts](#collecting-artifacts) to QApps (internal system)

=== "Kotlin"

    ```kotlin
    artifacts {
        apk("debugApk", ...)
    }
    uploadToQapps {
        artifacts = setOf("debugApk")
    }
    ```

=== "Groovy"

    ```groovy
    artifacts {
        apk("debugApk", ...)
    }
    uploadToQapps {
        artifacts = ["debugApk"]
    }
    ```

#### Upload to Artifactory

Upload [artifacts](#collecting-artifacts) to Artifactory.

=== "Kotlin"

    ```kotlin
    artifacts {
        file("myReport", "${project.buildDir}/reports/my_report.json")
    }
    uploadToArtifactory {
        artifacts = setOf("myReport")
    }
    ```

=== "Groovy"

    ```groovy
    artifacts {
        file("myReport", "${project.buildDir}/reports/my_report.json")
    }
    uploadToArtifactory {
        artifacts = ["myReport"]
    }
    ```

#### Upload to Prosector

--8<--
avito-disclaimer.md
--8<--

Upload [artifacts](#collecting-artifacts) to [Prosector (internal)](http://links.k.avito.ru/cfxrREPBQ).

=== "Kotlin"

    ```kotlin
    artifacts {
        apk("debugApk", ...)
    }
    uploadToProsector {
        artifacts = setOf("debugApk")
    }
    ```

=== "Groovy"

    ```groovy
    artifacts {
        apk("debugApk", ...)
    }
    uploadToProsector {
        artifacts = ["debugApk"]
    }
    ```

#### Upload build results

--8<--
avito-disclaimer.md
--8<--

Upload all build results to a deploy service.

=== "Kotlin"

    ```kotlin
    uploadBuildResult {
        uiTestConfiguration = "regression" // instrumentation configuration
    }
    ```

=== "Groovy"

    ```groovy
    uploadBuildResult {
        uiTestConfiguration = "regression" // instrumentation configuration
    }
    ```

#### Deploy to Google Play

--8<--
avito-disclaimer.md
--8<--

Deploy to Google play.

=== "Kotlin"

    ```kotlin
    deploy {}
    ```

=== "Groovy"

    ```groovy
    deploy {}
    ```

#### Mark report as source of truth for TMS

See [Test case in code](http://links.k.avito.ru/Wg)

#### Configuration checks

--8<--
avito-disclaimer.md
--8<--

Checks a repository configuration. See `:build-script-test` for details.

=== "Kotlin"

    ```kotlin
    configuration {}
    ```

=== "Groovy"

    ```groovy
    configuration {}
    ```

### Using impact analysis in step

Step can use [Impact analysis](../ci/ImpactAnalysis.md). It is enabled by default.

=== "Kotlin"

    ```kotlin
    fastCheck {
        uiTests {
        }
    }
    ```

=== "Groovy"

    ```groovy
    fastCheck {
        uiTests {
        }
    }
    ```

### Suppressing errors in step

In different scenarios steps could fail whole build, some can be configured not to.

=== "Kotlin"

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

=== "Groovy"

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

=== "Kotlin"

    ```kotlin
    artifacts {
       file("lintReport", "${project.buildDir}/reports/lint-results-release.html")
    }
    ```

=== "Groovy"

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

=== "Kotlin"

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

=== "Groovy"

    ```groovy
    def releaseSha1 = "my sha" // it's public info, so safe to share
    
    artifacts {
       apk("releaseApk", RELEASE, "com.avito.android", "${project.buildDir}/outputs/apk/release/avito.apk") { signature = releaseSha1 }
       bundle("releaseBundle", RELEASE, "com.avito.android", "${project.buildDir}/outputs/bundle/release/avito.aab") { signature = releaseSha1 }
       mapping("releaseMapping", RELEASE, "${project.buildDir}/outputs/mapping/release/mapping.txt")
       file("featureTogglesJson", "${project.buildDir}/reports/feature_toggles.json")
    }
    ```

The first argument is a key for upload steps.

### Writing a custom step

Inherit from `BuildStep`, check available implementations as examples
