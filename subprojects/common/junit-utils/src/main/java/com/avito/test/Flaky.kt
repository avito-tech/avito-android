package com.avito.test

/**
 * Unstable test marker.
 * It's used for retry logic.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class Flaky(
    val reason: String
)
