package com.avito.runner.service.worker.device.adb

import com.avito.time.TimeProvider
import java.util.concurrent.TimeUnit

class RetryAction(private val timeProvider: TimeProvider) {

    fun <T> retry(
        retriesCount: Int,
        delaySeconds: Long = 1,
        onError: (attempt: Int, throwable: Throwable, durationMs: Long) -> Unit = { _, _, _ -> },
        onFailure: (throwable: Throwable, durationMs: Long) -> Unit = { _, _ -> },
        onSuccess: (attempt: Int, t: T, durationMs: Long) -> Unit = { _, _, _ -> },
        action: () -> T
    ): Result<T> {
        for (attempt in 0..retriesCount) {
            val attemptStartTime = timeProvider.nowInMillis()
            if (attempt > 0) TimeUnit.SECONDS.sleep(delaySeconds)
            try {
                val result = action()
                onSuccess(attempt, result, timeProvider.nowInMillis() - attemptStartTime)
                return Result.Success(result)
            } catch (e: Throwable) {
                if (attempt == retriesCount - 1) {
                    onFailure(e, timeProvider.nowInMillis() - attemptStartTime)
                    return Result.Failure(e)
                } else {
                    onError(attempt, e, timeProvider.nowInMillis() - attemptStartTime)
                }
            }
        }
        throw IllegalStateException("retry must return value or throw exception")
    }
}
