package com.avito.runner.test

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import java.time.Duration

fun runBlockingWithTimeout(timeoutSeconds: Long = 15, action: suspend () -> Unit) {
    Assertions.assertTimeoutPreemptively(
        Duration.ofSeconds(timeoutSeconds)
    ) {
        runBlocking {
            action()
        }
    }
}
