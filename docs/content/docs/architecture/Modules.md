---
title: Modules
type: docs
---

# Modules

{{<avito page>}}

This page describes common structure of modules.

## Modules grouping

### By application

On the top-level all modules are grouped by application:

{{<mermaid>}}
flowchart TB
    subgraph avito-app-modules[ ]
        avito-->avito-modules[...]
        demo-app-->avito-modules
    end
    subgraph domofond-app-modules[ ]
        domofond-->domofond-modules[...]
    end
    subgraph common-modules[ ]
        common
    end
    avito-modules-->common-modules
    domofond-modules-->common-modules
{{</mermaid>}}

- `avito`, `domofond`: application modules. They only consume and wire features into an Android application.
- common modules: utilities, design components and so on. They are reused between different applications.

### By layers

Within one application modules are grouped by logical layers.
It reduces coupling and helps to build faster.

{{<mermaid>}}
flowchart LR
    avito-->avito-app
    avito-app-->avito-libs
    avito-app-->avito-api
    avito-libs-->avito-api
    avito-libs-->persistence
    avito-libs-->core
    avito-app-->persistence
    avito-app-->core
{{</mermaid>}}

{{< hint info >}}
This scheme illustrates general principles and reasons for a decoupling.\
You can find violations in the code. There are no excuses, it's a tech debt.
{{< /hint >}}

- `avito-app`: feature modules. They are used in application directly.
- `avito-libs`: smaller libraries. They are exists to reuse code between features.
- `core`: common code for all features.

## Special cases

These types of modules exist only due to technical limitations.

### Modules for build types

Android libraries have only a release ([build variant](https://developer.android.com/studio/build#build-config)).
It helps to configure and build applications faster. 

```kotlin
plugins.withType<LibraryPlugin> {
    variantFilter {
        if (name != "release") {
            setIgnore(true)
        }
    }
}
```

Application modules have all [build types]({{< ref "/docs/assemble/BuildTypes.md" >}}). 
To consume release libraries we have to use special modules for build types.

{{<mermaid>}}
flowchart LR
    avito-->|debugImplementation| avito-debug
    avito-->|releaseImplementation| avito-release
{{</mermaid>}}

### Modules for test fixtures

[Test fixture](https://en.wikipedia.org/wiki/Test_fixture#Software) is the common practice in testing. They are auxiliary classes for tests.
 
Gradle has [java test fixtures](https://docs.gradle.org/5.6/userguide/java_testing.html#sec:java_test_fixtures) plugin,
and it hasn't supported in AGP yet ([#139438142](https://issuetracker.google.com/issues/139438142)).

The workaround is to extract test fixtures to special "test" modules:

{{<mermaid>}}
flowchart TB
    application-->|implementation| feature
    feature-->|androidTestImplementation| test:feature
    test:feature-->|implementation| feature
    application-->|androidTestImplementation| test:feature
{{</mermaid>}}
