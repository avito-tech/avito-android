# Logging

`com.avito.logger.Logger` used both in Gradle and in Android Runtime

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

To obtain logger for android create `AndroidLoggerFactory`

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

`StubLoggerFactory` and `StubLogger` can be used in tests

`StubLogger` will write to stdout only during test runs from IDE

## Verbose mode

Use gradle property `avito.logging.verbose` to override gradle logging level and send avito logs with gradle's error
level.

Value defines which levels to override.

For example: `-Pavito.logging.verbose=INFO` makes `INFO` and `WARNING` levels act like error

`CRITICAL`, which is mapped to gradle's `error` level, always acts as an error

Possible values are in `DEBUG`, `INFO`, `WARNING`, `CRITICAL` (see `com.avito.logger.LogLevel`)

Default is `CRITICAL`

### Why is it needed?

Gradle use lifecycle level by default, but to see info or debug level you have to set it for whole gradle run
via `--info` or `--debug`, which made console output unreadable and build slow.

There is an issue for
that: [gradle/#1010 Ability to set log level for specific task](https://github.com/gradle/gradle/issues/1010)

With verbose flag you are able to tune log level for avito plugins separately. 
