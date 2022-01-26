package com.avito.test

/**
 * Unstable test marker.
 * It's used for retry logic.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS) // member functions are not supported by 'org.gradle.test-retry' plugin
public annotation class Flaky(
    val reason: String
)
