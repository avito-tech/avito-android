plugins {
    `kotlin-dsl`
    id("com.android.application") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    id("com.avito.android.gradle-logger")
}

gradleLogger {
    printlnHandler(false, com.avito.logger.LogLevel.DEBUG)
}
