package com.avito.test

/**
 * Unstable test marker.
 * It's used for retry logic.
 */
annotation class Flaky(
    val reason: String
)
