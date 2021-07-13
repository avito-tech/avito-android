package com.avito.android.waiter

import com.avito.android.Result
import com.avito.android.Result.Failure
import com.avito.android.Result.Success
import java.util.concurrent.TimeUnit

public suspend fun <T> waitForCondition(
    conditionName: String,
    maxAttempts: Int = WAIT_FOR_COMMAND_MAX_ATTEMPTS,
    frequencySeconds: Long = WAIT_FOR_COMMAND_FREQUENCY_SECONDS,
    timeoutSec: Long = WAIT_FOR_COMMAND_TIMEOUT,
    onSuccess: (conditionName: String, durationMs: Long, attempt: Int) -> Unit,
    sleepAction: suspend (frequencyMs: Long) -> Unit,
    condition: suspend () -> Result<T>
): Result<T> {
    val timeoutMs = TimeUnit.SECONDS.toMillis(timeoutSec)
    val startTime = System.currentTimeMillis()
    var lastAttemptTime = startTime
    var attempt = 1
    while (lastAttemptTime - startTime < timeoutMs && attempt <= maxAttempts) {
        when (val result = condition()) {
            is Success -> {
                val durationMs = System.currentTimeMillis() - startTime
                onSuccess(conditionName, durationMs, attempt)
                return result
            }
            is Failure -> {
                val lastAttempt = attempt == maxAttempts - 1
                if (lastAttempt) {
                    return result
                } else {
                    attempt++
                    lastAttemptTime = System.currentTimeMillis()
                    sleepAction(TimeUnit.SECONDS.toMillis(frequencySeconds))
                }
            }
        }
    }
    return Failure(IllegalStateException("Condition $conditionName must end in for loop"))
}

private const val WAIT_FOR_COMMAND_MAX_ATTEMPTS = 30
private const val WAIT_FOR_COMMAND_FREQUENCY_SECONDS = 2L
private const val WAIT_FOR_COMMAND_TIMEOUT = 60L
