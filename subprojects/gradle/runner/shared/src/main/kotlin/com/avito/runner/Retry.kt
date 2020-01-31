package com.avito.runner

import java.util.concurrent.TimeUnit

fun <T> retry(
    retriesCount: Int,
    delaySeconds: Long = 1,
    attemptFailedHandler: (attempt: Int, throwable: Throwable) -> Unit = { _, _ -> },
    actionFailedHandler: (throwable: Throwable) -> Unit = { },
    block: (attempt: Int) -> T
): T {
    var throwable: Throwable? = null

    (0 until retriesCount).forEach { attempt ->
        try {
            return block(attempt)
        } catch (e: Throwable) {
            throwable = e
            attemptFailedHandler(attempt, e)
            TimeUnit.SECONDS.sleep(delaySeconds)
        }
    }

    actionFailedHandler(throwable!!)

    throw throwable!!
}
