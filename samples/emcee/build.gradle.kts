plugins {
    `kotlin-dsl`
    id("com.android.application") version "7.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.5.31" apply false
    id("com.avito.android.gradle-logger")
}

gradleLogger {
    printlnHandler(false, com.avito.logger.LogLevel.DEBUG)
}
