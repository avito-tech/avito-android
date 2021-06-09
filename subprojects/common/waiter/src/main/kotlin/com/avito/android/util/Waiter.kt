package com.avito.android.util

import com.avito.android.waiter.repeatFor
import com.avito.android.waiter.waitFor

fun <T> waitForAssertion(
    timeoutMilliseconds: Long = DEFAULT_TIMEOUT_MS,
    frequencyMilliseconds: Long = DEFAULT_FREQUENCY_MS,
    action: () -> T
): T = waitFor(
    timeoutMs = timeoutMilliseconds,
    frequencyMs = frequencyMilliseconds,
    allowedExceptions = setOf(AssertionError::class.java),
    action = action
)

fun continuousAssertion(
    timeoutMilliseconds: Long = DEFAULT_TIMEOUT_MS,
    frequencyMilliseconds: Long = DEFAULT_FREQUENCY_MS,
    action: () -> Unit
) = repeatFor(
    timeoutMs = timeoutMilliseconds,
    frequencyMs = frequencyMilliseconds,
    action = action
)

private const val DEFAULT_TIMEOUT_MS = 5000L
private const val DEFAULT_FREQUENCY_MS = 50L
