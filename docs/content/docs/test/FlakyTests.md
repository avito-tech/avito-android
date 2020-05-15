---
title: Flaky Tests
type: docs
---

# Flaky Tests

A flaky test is a test that could fail or pass without project changes.\
One could say this is unstable tests that fail sometimes but not every launch.\
It happens when: 
- test relies on an external environment: back-end, database, environment variables etc.
- test had written incorrect. Mistakes easy to do in async jobs.
- test framework has bugs

Flaky tests make hard:
- find real failures
- trust to test failures
- to react on failures as soon as possible \

That's why we introduce annotion to mark your test flaky. 

We want:
- Minimize flakiness
- Launch as many tests as we can on pull request checks
- Help to fix common flaky problems

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
For more information on configuring build steps for the CI Steps plugin, please refer to [this document]({{< ref "/docs/projects/CISteps.md" >}}).
