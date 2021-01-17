# Testing a minimized build

About minimization: [link](https://developer.android.com/studio/build/shrink-code)

We want to run UI tests against build as close as possible to a production one.\
It's quite a challenging task, mostly because of tooling problems: [#126429384](https://issuetracker.google.com/issues/126429384)\
Developers should maintain a list of keep rules of code referenced from a test app manually.

We work around these problems by using [keeper](https://slackhq.github.io/keeper/).

## Build type

Android Gradle plugin uses `testBuildType` property to determine which build type to test.

We chose `stagingAutoTest` as a type to test against in CI environment.\
However local builds defaults to `debug` type for faster test writing cycle.

Developer who wants to reproduce test problem specific to CI locally, should temporary change `<app name>.testBuildType` property
to `stagingAutoTest` in `~/.gradle/gradle.properties`.

## Sample

You can check an example in the `:test-app` module.

## Known issues

### Dynamically referenced resources

You can see in `PageObjectTest`, that we referenced `R.layout.page_object_1`, dynamically creating layout.\
These layouts referenced nowhere in app code, so if `shrinkResources` enabled you will face strange error:

```text
error inflating class x
Caused by: java.lang.ClassNotFoundException: Didn't find class "android.view.x" on path: DexPathList
```

What it really hides, `R.layout.page_object_1` got shrinked to:

```xml
<x />
```

If this is your case, add these resources to `res/raw/keep.xml` like this:

```xml
<resources xmlns:tools="http://schemas.android.com/tools"
           tools:keep="@layout/page_object*"/>
```

### X already has a mapping

[Issue #122924648](https://issuetracker.google.com/issues/122924648) - Solved in a 1.6.x

Some issues could be solved in a new version of r8, you should consider upgrading before hacking further.

By default, r8 bundled with Android Gradle plugin, but you can override it.

```kotlin
buildscript {
    val r8Version: String by project

    repositories {
        maven { setUrl("http://storage.googleapis.com/r8-releases/raw") }
    }
    dependencies {
        classpath("com.android.tools:r8:$r8Version") // < it should be added before android Gradle plugin
    }
}
```

???+ info For versions check tags here: [https://r8.googlesource.com/r8/](https://r8.googlesource.com/r8/)

    Seems like 1.5 versions bundled with AGP 3.5.x\
    1.6 -> 3.6.x\
    and 2.0 -> 4.0.x

???+ warning Don't forget to tell `keeper`, you are using different r8 version:

    ```kotlin
    dependencies {
        keeperR8("com.android.tools:r8:$r8Version")
    }
    ```

### Still NoSuchMethodError

Take a look at module `:subprojects:android-test:keep-for-testing` for additional keep annotations.\
It's a workaround described here: [Issue #143419092](https://issuetracker.google.com/issues/143419092#comment4)

You still need to add proguard rules as pointed in annotation classes comments.

> Why not package proguard rules?

In this case, rules go into all variants, and you only want a tested one.

> Why not use original @Keep?

Just to distinguish an intent, it's for testing and only in a tested variant
