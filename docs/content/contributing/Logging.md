# Logging

We have common libs those are reused in both environments: Android and Gradle. So we need a logger is not dependent on the concrete environment. \
We've created `com.avito.logger.Logger`. It helps us to reuse code. 

## Logging in Gradle

To obtain a logger for Gradle we create the `GradleLoggerPlugin`. It must be applied to the root project before others plugins dependent on it. \
You create `LoggerFactory` for a `project` or a `task`

```kotlin
val projectLoggerFactory = GradleLoggerPlugin.getLoggerFactory(project)
val taskLoggerFactory = GradleLoggerPlugin.getLoggerFactory(task)
```

### PrintlnLoggerHandler

Gradle uses lifecycle level by default, but to see info or debug level you have to set it for the whole Gradle run
via `--info` or `--debug`, which made console output unreadable and build slow.

There is an issue for
that: [gradle/#1010 Ability to set log level for specific task](https://github.com/gradle/gradle/issues/1010)

`PrintlnLoggerHandler` adds the ability to tune log level for avito plugins separately

## Logging in Android

To obtain a logger for Android create a `LoggerFactory` by `LoggerFactoryBuilder`

## Testing

`StubLoggerFactory` and `StubLogger` can be used in tests.

`StubLogger` will write to stdout only during test runs from IDE.
