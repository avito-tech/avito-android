package com.avito.android.util

import com.avito.android.test.waitFor
import java.lang.AssertionError
import java.util.concurrent.TimeUnit

fun <T> waitForAssertion(
    timeoutMilliseconds: Long = TimeUnit.SECONDS.toMillis(5),
    frequencyMilliseconds: Long = 50,
    action: () -> T
): T = waitFor(
    timeoutMs = timeoutMilliseconds,
    allowedExceptions = setOf(AssertionError::class.java),
    action = action
)
