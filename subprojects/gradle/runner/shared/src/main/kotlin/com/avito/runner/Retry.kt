package com.avito.runner

import java.util.concurrent.TimeUnit

fun <T> retry(
    retriesCount: Int,
    delaySeconds: Long = 1,
    attemptFailedHandler: (attempt: Int, throwable: Throwable) -> Unit = { _, _ -> },
    actionFailedHandler: (throwable: Throwable) -> Unit = { },
    block: (attempt: Int) -> T
): T {
    for (attempt in 0..retriesCount) {
        if (attempt > 0) TimeUnit.SECONDS.sleep(delaySeconds)
        try {
            return block(attempt)
        } catch (e: Throwable) {
            if (attempt == retriesCount - 1) {
                actionFailedHandler(e)
                throw e
            } else {
                attemptFailedHandler(attempt, e)
            }
        }
    }
    throw IllegalStateException("retry must return value or throw exception")
}
