---
title: Mocking in tests
type: docs
---

# Mocking in tests

## Mocks (internal)

### Mocking network

TBD

### Mocking analytics

There are different options:

- `MockAnalyticsRule`: mocks analytics completely 
- `AnalyticsRule`: only intercepts events and tries to not interfere with real implementation
- `<test without rule>`: real implementation

```kotlin
@get:Rule
val analytics = AnalyticsRule()

analytics.checks.assertEventTracked<ShowSearchEvent>()
```

### Mocking location

`LocationRule` serves for mocking location in tests.

```kotlin
@get:Rule
val locationRule = LocationRule()

locationRule.setLocation(getTestLocation())
```

**KEEP IN MIND** that above rule does not mock location for the whole device but rather replace the best known location
in `BaseGeoProvider`. That is technically possible that device location providers would give better location that was mocked. 
To avoid that mock location with high accuracy.

### Mocking User-Agent

It is possible to mock User-Agent for test suite or for specific test with `@UserAgent` annotation. Here's an example:

```kotlin
@UserAgent("AVITO 59.0 (Android 8.1.0; ru)")
class MyAwesomeTests {}
```

This annotation is available in all types of tests but make sense the mostly for functional tests.

## Writing custom mocks for tests

Different tests need different mocks. We have to adapt an application state for these requirements.\
We can define different types of tests:

- With mocked network
- With real network
- With mocked analytics
- ...

Trouble is, it leads to a combinatorial explosion.\
We want to be sure that our changes in mocks for one test won't break other tests.
Therefore, it is better to have a straightforward and explicit relationship between a test and an application state.

How do we adapt an application state exclusively for one test?

1. Test Runner starts before the application.\
See `InHouseInstrumentationTestRunner`.
1. Test Runner finds a test for running in arguments.\
It parses test class (annotations, methods, rules) and saves this information into a Bundle.\
See `TestMetadataInjector`
1. The Test App reads parameters from `InstrumentationRegistry.getArguments()`.\
Now we know requirements for the exact test and configure an application state with minimum side effects.
1. The test is starting, and the application is already in the desired state.
