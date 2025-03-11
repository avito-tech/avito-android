package com.avito.android.retry

import com.avito.android.Result
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@Suppress("TooGenericExceptionCaught", "LongParameterList")
public inline fun <T> executeWithRetries(
    maxAttempts: Int,
    delay: Duration = Duration.ofSeconds(1),
    onFailedTry: (attempt: Int, throwable: Throwable, lastAttemptDuration: Duration) -> Unit = { _, _, _ -> },
    onFailure: (throwable: Throwable, lastAttemptDuration: Duration) -> Unit = { _, _ -> },
    onSuccess: (attempt: Int, t: T, lastAttemptDuration: Duration) -> Unit = { _, _, _ -> },
    action: () -> T
): Result<T> {
    for (attempt in 1..maxAttempts) {
        val attemptStartTime = Instant.now()
        if (attempt > 1) TimeUnit.MILLISECONDS.sleep(delay.toMillis())
        try {
            val result = action.invoke()

            onSuccess(attempt, result, Duration.between(attemptStartTime, Instant.now()))
            return Result.Success(result)
        } catch (e: Throwable) {
            if (attempt == maxAttempts) {
                onFailure(e, Duration.between(attemptStartTime, Instant.now()))
                return Result.Failure(e)
            } else {
                onFailedTry(attempt, e, Duration.between(attemptStartTime, Instant.now()))
            }
        }
    }
    error("retry must return value or throw exception")
}
