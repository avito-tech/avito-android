# Build checks Gradle plugin

This plugin verifies [common build problems](#checks) with environment and project configuration.

## Getting started

Apply the plugin in a root build script or in an Android application module:

```groovy
plugins {
    id("com.avito.android.build-checks")
}
```

--8<--
plugins-setup.md
--8<--

=== "Kotlin"

    `build.gradle.kts`
    
    ```kotlin
    buildChecks {
        androidSdk {
            version(
                compileSdkVersion = 29,
                revision = 5
            )
        }
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }
    }
    ```

=== "Groovy"

    `build.gradle`
    
    ```groovy
    buildChecks {
        androidSdk {
            version(29, 5)
        }
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }
        uniqueRClasses {
            enabled = false
        }
    }
    ```

That's all for a configuration. Run it manually to verify that it works:

```text
./gradlew checkBuildEnvironment
```

The plugin will run it automatically on every build.

## Configuration

### Enable single check

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        enableByDefault = false
    
        androidSdk {
            version(
                compileSdkVersion = 29,
                revision = 5
            )
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        enableByDefault = false
    
        androidSdk {
            version(29, 5)
        }
    }
    ```

### Disable single check

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        androidSdk {
            enabled = false
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        androidSdk {
            enabled = false
        }
    }
    ```

### Disable all checks

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        enableByDefault = false
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        enableByDefault = false
    }
    ```

### Disable plugin

To completely disable the plugin add a Gradle property:

```properties
avito.build-checks.enabled=false
```

## Checks

### Common build checks

These checks are available in a root project's buildscript.  
See also [Android application checks](#android-application-checks).

#### Java version

The Java version can influence the output of the Java compiler. It leads to
Gradle [remote cache misses](https://guides.gradle.org/using-build-cache/#diagnosing_cache_miss)
due
to [Java version tracking](https://docs.gradle.org/nightly/userguide/common_caching_problems.html#java_version_tracking).  
This check forces the same major version for all builds.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }   
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }   
    }
    ```

#### Android SDK version

Android build tools uses android.jar (`$ANDROID_HOME/platforms/android-<compileSdkVersion>/android.jar`).  
The version can be specified only without a revision ([#117789774](https://issuetracker.google.com/issues/117789774)).
Different revisions lead to Gradle remote cache misses. 
This check forces the same revision:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        androidSdk {
            version(
                compileSdkVersion = 29,
                revision = 5
            )
            // You can define multiple versions if modules use them
            version(
                compileSdkVersion = 30,
                revision = 3
            )
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        androidSdk {
            version(29, 5)
        }
    }
    ```

#### macOS localhost lookup

On macOs `java.net.InetAddress#getLocalHost()` invocation can last up to 5 seconds instead of milliseconds
([thoeni.io/post/macos-sierra-java](https://thoeni.io/post/macos-sierra-java/)). Gradle has
a [workaround](https://github.com/gradle/gradle/pull/11134) but it works only inside Gradle's code.

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

This check automatically detects the issue:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        macOSLocalhost { }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        macOSLocalhost { }
    }
    ```

#### Prevent Kotlin daemon fallback strategy 

Sometimes Kotlin daemon fails and can't recover on its own. 
It has incredible impact on build performance and continuing build is not worth it.
This is a workaround for [KT-48843](https://youtrack.jetbrains.com/issue/KT-48843). See there more details.

=== "Kotlin"
`build.gradle.kts`

    ```kotlin
    buildChecks {
        preventKotlinDaemonFallback { 
            enabled = true // disabled by default
        } 
    }
    ```

=== "Groovy"
`build.gradle`

    ```groovy
    buildChecks {
        preventKotlinDaemonFallback { 
            enabled = true // disabled by default
        } 
    }
    ```

#### Gradle properties

???+ warning
    This check is deprecated and will be removed.

--8<--
avito-disclaimer.md
--8<--

This check detects if you
override [Gradle project property](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)
by command-line. It sends mismatches to statsd. This information helps to see frequently changed propeties that can lead
to remote cache misses.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        gradleProperties { 
            enabled = true // disabled by default
        } 
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        gradleProperties { 
            enabled = true // disabled by default
        } 
    }
    ```

### Android application checks

These checks are available in an Android application's buildscript.  
Each application can have specific settings.

#### Unique R classes

If two Android libraries use the same package, their R classes will be merged. While merging, it can unexpectedly override
resources ([#175316324](https://issuetracker.google.com/issues/175316324)). It happens even
with `android.namespacedRClass`.

To forbid merged R files use this check:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    plugins {
        id("com.avito.android.build-checks")
    }
    
    buildChecks {
        uniqueRClasses { } // enabled by default
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    plugins {
        id("com.avito.android.build-checks")
    }
    
    buildChecks {
        uniqueRClasses { } // enabled by default
    }
    ```

See also `android.uniquePackageNames` check. In AGP 4.1 it provides similar but less complete contract.

You can suppress errors for a specific package:

`build.gradle.kts`

```kotlin
buildChecks {
    uniqueRClasses {
        allowedNonUniquePackageNames.addAll(listOf(
            "androidx.test", // Default from ManifestMerger #151171905
            "androidx.test.espresso", // Won't fix: https://issuetracker.google.com/issues/176002058
            "androidx.navigation.ktx" // Fixed in 2.2.1: https://developer.android.com/jetpack/androidx/releases/navigation#2.2.1
        ))
    }
}
```

#### Unique application resources

From [Android library - considerations](https://developer.android.com/studio/projects/android-library#Considerations):

    The build tools merge resources from a library module with those of a dependent app module. 
    If a given resource ID is defined in both modules, the resource from the app is used.

    If conflicts occur between multiple AAR libraries, then the resource from the library listed first in the dependencies list is used.

`uniqueAppResources` ensures that resources in application are unique and won't be overridden implicitly.

=== "Kotlin"
    `build.gradle.kts`

    Root module:

    ```kotlin
    plugins {
        id("com.avito.android.impact")
    }
    ```

    Application module:

    ```kotlin
    plugins {
        id("com.avito.android.build-checks")
    }
    
    buildChecks {
        uniqueAppResources {} // disabled by default
    }
    ```

=== "Groovy"
    `build.gradle`

    Root module:

    ```groovy
    plugins {
        id("com.avito.android.impact")
    }
    ```

    Application module:

    ```groovy
    plugins {
        id("com.avito.android.build-checks")
    }
    
    buildChecks {
        uniqueAppResources {} // disabled by default
    }
    ```

To avoid resource conflicts, consider using a prefix (`android.resourcePrefix`) or other consistent naming scheme.

##### Ignoring duplicates

```kotlin
buildChecks {
    uniqueAppResources {
        // Resource types: string, dimen, bool, layout, drawable, ...
        ignoredResourceTypes.add("string")
        // Specific resources
        ignoredResource("string", "title")
        ignoredResource("dimen", "max_height")
    }
}
```

##### Known issues

- Requires impact analysis that slows project configuration
- Disabled by default due to possible false positive cases. Usually, it requires to configure ignored resources.
- Don't compare values, only resource identifiers. Reported duplicates can have the same content.
- Detects only project modules without binary dependencies. 
  Don't know how to deal with massive false positive duplicates for widely used libraries (androidx and similar ones). 
- Some resource types are not supported because the issue is not confirmed for them. These are `id`, `attr`, `styleable`.
