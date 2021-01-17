# Logging

`com.avito.logger.Logger` used both in Gradle and in Android Runtime

## Logging in Gradle

To obtain a logger for Gradle use `GradleLoggerFactory` methods:

```kotlin
abstract class MyTask: DefaultTask() {
    
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
