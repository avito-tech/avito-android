---
title: Build checks Gradle plugin
type: docs
---

# Build checks Gradle plugin

This plugin verifies [common build problems]({{< relref "#checks">}}) with environment and project configuration.

## Getting started

Apply the plugin in the root `build.gradle` file:

```groovy
plugins {
    id("com.avito.android.buildchecks")
}

buildChecks {
    enableByDefault = true
}
```

In the `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            String pluginId = requested.id.id
            if (pluginId.startsWith("com.avito.android")) {
                def artifact = pluginId.replace("com.avito.android.", "")
                useModule("com.avito.android:$artifact:$avitoToolsVersion")
            }
        }
    }
}
```

Some checks require settings to work properly. 
You'll get an error about missing settings:

```text
> ./gradlew

A problem occurred configuring root project
> buildChecks.androidSdk.compileSdkVersion must be set
```

After adding missing properties you will get something like this:

```groovy
buildChecks {
    enableByDefault = true

    androidSdk {
        it.compileSdkVersion = 29
        it.revision = 4
    }
    javaVersion {
        it.version = JavaVersion.VERSION_1_8
    }
}
```

That's all for a configuration. Run it manually to verify that it works:

```text
> ./gradlew checkBuildEnvironment
```

The plugin will run it automatically on every build.

## Configuration

### Enable all checks

```groovy
buildChecks {
    enableByDefault = true // false by default
}
```

### Disable all checks

```groovy
buildChecks {
    enableByDefault = false
}
```

### Disable single check

```groovy
buildChecks {
    enableByDefault = true

    androidSdk {
        it.enabled = false
    }
}
```

### Enable single check

```groovy
buildChecks {
    enableByDefault = false

    androidSdk {
        it.compileSdkVersion = 29
        it.revision = 4
    }
}
```

## Checks

### Java version

The Java version can influence the output of the Java compiler. 
It leads to Gradle [cache misses](https://guides.gradle.org/using-build-cache/#diagnosing_cache_miss).\
This check forces the same version for all builds.

```groovy
buildChecks {
    javaVersion {
        it.version = JavaVersion.VERSION_1_8
    }   
}
```

### Android SDK version

Android build tools uses android.jar (`$ANDROID_HOME/platforms/android-<compileSdkVersion>/android.jar`).\ 
We can specify only a version without a revision ([#117789774](https://issuetracker.google.com/issues/117789774)).

```groovy
android {
    compileSdkVersion 29
}
```

Different revisions lead to Gradle cache misses. This check forces the same revision:

```groovy
buildChecks {
    androidSdk {
        it.compileSdkVersion = 29
        it.revision = 4
    }
}
```

### macOS localhost lookup

On macOs `java.net.InetAddress#getLocalHost()` invocation can last up to 5 seconds instead of milliseconds.
Gradle has a [workaround](https://github.com/gradle/gradle/pull/11134) but it works only inside Gradle's code.

To diagnose the problem manually use [thoeni/inetTester](https://github.com/thoeni/inetTester).

To fix the problem use this workaround:

1. Find your computer's name - [Find your computer's name and network address](https://support.apple.com/en-us/guide/mac-help/find-your-computers-name-and-network-address-mchlp1177/mac)
2. Add it to `/etc/hosts` config:

```text
127.0.0.1        localhost <your_host_name>.local
::1              localhost <your_host_name>.local
```

3. Reboot the computer
4. Check again by [thoeni/inetTester](https://github.com/thoeni/inetTester)

See an original post [thoeni.io/post/macos-sierra-java](https://thoeni.io/post/macos-sierra-java/).

This check automatically detects the issue:

```groovy
buildChecks {
    macOSLocalhost { } // enabled by default
}
```

### Dynamic dependency version

Dynamic versions, such as "2.+", and snapshot versions force Gradle to check them on a remote server. 
It slows down a [configuration time](https://guides.gradle.org/performance/#minimize_dynamic_and_snapshot_versions) 
and makes build [less reproducible](https://reproducible-builds.org/). 

This check forbids dynamic dependency versions.

```groovy
buildChecks {
    dynamicDependencies { } // enabled by default
}
```

### Gradle daemon reusage

Gradle can run multiple daemons for [many reasons](https://docs.gradle.org/5.0/userguide/gradle_daemon.html#sec:why_is_there_more_than_one_daemon_process_on_my_machine).

If you use `buildSrc` in the project with standalone Gradle wrapper, this check will verify common problems to reuse it.

```groovy
buildChecks {
    gradleDaemon { } // enabled by default
}
```

### Kotlin API dependency

Gradle `api` dependencies are not working properly in kotlin plugin. 
We have to use `compile` dependency as a workaround.

```groovy
buildChecks {
    kotlinApiDependencies { } // enabled by default
}
```

This check detects the issue as early as possible and provides helpful information:

```text
> ./gradlew assemble

There were errors:
 - project ':module' uses api dependencies <dependency> in the 'configuration ':module:api' configuration. 
It's not working correctly in the kotlin plugin. 
Use 'compile' instead.

```

### Module types

This check force to apply [`module-types`](https://github.com/avito-tech/avito-android/blob/develop/subprojects/gradle/module-types) Gradle plugin in all modules.
It prevents modules go to wrong configurations (android-test module as an app’s implementation dependency for example).

```groovy
buildChecks {
    moduleTypes { 
        enabled = true // disabled by default
    } 
}
```

### Gradle properties

{{<avito check>}}

This check detects if you override [Gradle project property](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties) by command-line.
It sends mismatches to [statsd]({{< ref "/docs/analytics/Statsd.md" >}}). 
This information helps to see frequently changed propeties that can lead to cache misses.

```groovy
buildChecks {
    gradleProperties { 
        enabled = true // disabled by default
    } 
}
```
