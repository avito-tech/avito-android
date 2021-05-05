# Logging

`com.avito.logger.Logger` is used both in Gradle and in Android runtime.

## Logging in Gradle

To obtain a logger for Gradle use `GradleLoggerFactory` methods:

```kotlin
abstract class MyTask : DefaultTask() {

    @TaskAction
    fun doWork() {
        val loggerFactory: LoggerFactory = GradleLoggerFactory.fromTask(this)
    }
}
```

## Logging in Android

To obtain a logger for Android create `AndroidLoggerFactory`

```kotlin
val loggerFactory: LoggerFactory = AndroidLoggerFactory(args)
```

## How to get logger instance

Use logger factory suited for your case:

```kotlin
class MyClass {

    val logger = loggerFactory.create<MyClass>()
}
```

If you need custom tag, not associated with class name:

```kotlin
val logger = loggerFactory.create("MyCustomTag")
```

## Testing

`StubLoggerFactory` and `StubLogger` can be used in tests.

`StubLogger` will write to stdout only during test runs from IDE.

## Verbose mode

Use Gradle property `avito.logging.verbosity` to override Gradle logging level and send avito logs to stdout 
(e.g. with Gradle's `quiet` level.)

Value defines which levels to override.

For example: `-Pavito.logging.verbosity=INFO` makes `INFO` and higher (`WARNING`) levels act like level quiet

`CRITICAL`, which is mapped to Gradle's `error` level is visible already on quiet level

Possible values are in `DEBUG`, `INFO`, `WARNING`, `CRITICAL` (see `com.avito.logger.LogLevel`)

Default is not defined.

### Stacktrace

Add Gradle's `--stacktrace` to also print stacktraces in verbose mode if available.

### Why is it needed?

Gradle use lifecycle level by default, but to see info or debug level you have to set it for whole Gradle run
via `--info` or `--debug`, which made console output unreadable and build slow.

There is an issue for
that: [gradle/#1010 Ability to set log level for specific task](https://github.com/gradle/gradle/issues/1010)

With verbose flag you are able to tune log level for avito plugins separately.

It only affects console output, and not affecting custom loggers like elastic.
