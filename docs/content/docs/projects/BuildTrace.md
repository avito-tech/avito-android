---
title: Build trace Gradle plugin
type: docs
---

# Build trace Gradle plugin

This plugin is a primitive analog of [Gradle build scan](https://scans.gradle.com/). 
Use it if you can't use a build scan for any reason.

This plugin collects tasks execution time in a trace event format.

![](https://user-images.githubusercontent.com/1104540/80872574-63d68e80-8cbb-11ea-9333-c7f5f8c9e557.png)

## Getting started

Apply the plugin in the root `build.gradle` file:

```groovy
plugins {
    id("com.avito.android.build-trace")
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

Run a build with `--profile` argument. You will get a message in a log:

```log
Build trace: <path to the project>/outputs/trace/build.trace
```

Open the file in `chrome://tracing`. 

## Known issues

- Tasks' completion time is long after a real time ([#8630](https://github.com/gradle/gradle/issues/8630)). 
In a trace it looks like a task is completed right after the another from the same module.
