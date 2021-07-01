package com.avito.instrumentation.internal.reservation.adb

import com.avito.android.Result
import com.avito.android.Result.Failure
import com.avito.android.Result.Success
import com.avito.logger.Logger
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

internal suspend fun <T> waitForCondition(
    logger: Logger,
    conditionName: String,
    successMessage: String? = null,
    maxAttempts: Int = WAIT_FOR_COMMAND_MAX_ATTEMPTS,
    frequencySeconds: Long = WAIT_FOR_COMMAND_FREQUENCY_SECONDS,
    timeoutSec: Long = WAIT_FOR_COMMAND_TIMEOUT,
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
                logger.debug("${successMessage ?: "$conditionName succeed"} in $durationMs at attempt=$attempt")
                return result
            }
            is Failure -> {
                val lastAttempt = attempt == maxAttempts - 1
                if (lastAttempt) {
                    return result
                } else {
                    attempt++
                    lastAttemptTime = System.currentTimeMillis()
                    delay(TimeUnit.SECONDS.toMillis(frequencySeconds))
                }
            }
        }
    }
    return Failure(IllegalStateException("Condition $conditionName must end in for loop"))
}

private const val WAIT_FOR_COMMAND_MAX_ATTEMPTS = 30
private const val WAIT_FOR_COMMAND_FREQUENCY_SECONDS = 2L
private const val WAIT_FOR_COMMAND_TIMEOUT = 60L
