# `@Flaky` Annotation

## What is flaky test

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

## Annotation

Mark flaky tests with special annotation - `@Flaky`

```kotlin
@Flaky(reason = "Relies on real back-end", onSdks = [22, 28])
class MyAwesomeTests {
    // ...
}
```

- `reason` - description of flakiness. You will see this reason in ReportViewer
- `onSdks` - specify if your test is flaking only on concrete sdk versions. When it's empty all sdk versions will be marked flaky.

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

By default `@Flaky` only adds flaky sign to ReportViewer.
There are two options for changing `@Flaky` tests behavior:
- [Add `suppressFlaky = true` to the `uiTests` step to suppress step failure because of `@Flaky` tests](../projects/CiSteps.md#ui-tests)
- [Add `excludeFlaky = true` to the filter for excluding `@Flaky` tests from execution](md#filter-flaky-tests)
