---
title: Android Lint
type: docs
---

# Android Lint

{{<avito page>}}

We use [Android lint](https://developer.android.com/studio/write/lint) to check applications in CI.

By default, we check only applications. It's no use checking libraries in isolation.

## How to run lint locally

`./gradlew :<app module>:lintRelease`

## Configure lint to suppress warnings

You can use default capabilities of Android lint ([Configure lint to suppress warnings](https://developer.android.com/studio/write/lint.html#config)). 

- `@SuppressLint` annotation in the code
- `tools:ignore` attribute in XML files
- `lint.xml` config file in an application module

Try to minimize a scope of suppressing. It reduces the risk of suppressing other problems accidentally.

## Configure lint to run in CI

Add a `lint` [build step]({{< ref "/docs/ci/CIGradlePlugin.md#android-lint-step" >}}) to a build in `build.gradle`

```groovy
fastCheck {
    lint {}
}
```

## Writing a custom lint check

{{< hint info>}} This section contains Avito specific information {{< /hint >}}

All customs android lint checks are in `lint-checks` (internal) module.

How to start:

- [Static Analysis with Android Lint by Tor Norbye (mDevCamp 2019)](https://slideslive.com/38916502) 
- [Sample project](https://github.com/googlesamples/android-custom-lint-rules)
