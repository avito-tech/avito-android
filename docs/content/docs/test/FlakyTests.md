---
title: Flaky Tests
type: docs
---

# Flaky Tests

A flaky test is a test that could fail or pass for the same configuration.
One could say this is unstable tests that fail sometimes but not every launch.
Usually, it happens when a test relies on an external environment. Network or real back-end for instance.

## `@Flaky` annotation

Such tests are marked with special annotation - `@Flaky`. You also able to provide an optional reason of flakiness for this test.

```kotlin
@Flaky(reason = "Relies on real back-end")
class MyAwesomeTests {
    // ...
}
```

This annotation can be added both to the whole class and to a separate test:

```kotlin
class MyAwesomeTests {
    @Flaky
    @Test
    fun foobar() {
        // ...
    }
}
```

## Suppress `@Flaky` test failures

To suppress failures of @Flaky tests just add `suppressFlaky = true` to your `uiTests` step.
For more information on configuring build steps for the CI / CD Plugin, please refer to [this document]({{< ref "/docs/ci/CIGradlePlugin.md" >}}).