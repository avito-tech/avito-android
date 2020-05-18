---
title: Build properties Gradle plugin
type: docs
---

# Build properties Gradle plugin

Sometimes you need to inspect information about the current build in your app code.
The conventional way is to use custom fields in a [BuildConfig](https://developer.android.com/studio/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code) or resource values.

```groovy
android {
  buildTypes {
    release {
      buildConfigField("String", "GIT_COMMIT", "\"${commit}\"")
      resValue("string", "git_commit", "${commit}")
    }
    debug {
      // To avoid rebuilding
      buildConfigField("String", "GIT_COMMIT", "\"_\"")
      resValue("string", "git_commit", "_")
    }
  }
}
```

`BuildConfig` and R classes are used to compile the code. Any changes harm incremental compilation and build caching. 
This is a known problem: [unstable task inputs](https://guides.gradle.org/using-build-cache/#stable_task_inputs).

To mitigate this issue the plugin uses assets to store properties. This approach is less harmful for incremental compilation and build caching.


## Getting started

### 1. Apply the plugin in the Android module's build script

```groovy
plugins {
    id("com.avito.android.build-properties")
}
```

{{%plugins-setup%}}

### 2. Define properties in a build script

{{< tabs "build properties example" >}}
{{< tab "Kotlin" >}}

`build.gradle.kts`

```kotlin
buildProperties {
    buildProperty("GIT_COMMIT", commit)
}
```

{{< /tab >}}
{{< tab "Groovy" >}}

`build.gradle`

```groovy
buildProperties {
    buildProperty("GIT_COMMIT", commit)
}
```

{{< /tab >}}
{{< /tabs >}}

### 3. Add auto-generated file to the `.gitignore` config

Content of this file depends on the build. It's no use to store it in VCS.

```.gitignore
build-info.properties
```

### 4. Read properties in the code

```kotlin
val properties = Properties()
context.assets.open("build-info.properties").use {
    properties.load(it)
}
val gitCommit = properties.getProperty("GIT_COMMIT")
```
