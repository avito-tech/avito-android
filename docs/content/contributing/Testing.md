# Testing

## Gradle plugins

See [Gradle plugins / Testing](GradlePlugins.md#testing)

## Naming conventions

We use pattern: `state under test - expected behaviour - [when]`:

```kotlin
@Test
fun `success runs - returns number of passed test runs`() {}

@Test
fun `read instrumentation output - completes stream - with failed test`() {}

@Test
fun `create summary - marks test as matched - all tests passed`() {}
```
