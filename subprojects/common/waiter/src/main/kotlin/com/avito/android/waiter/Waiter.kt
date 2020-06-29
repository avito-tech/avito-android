package com.avito.android.waiter

import java.util.concurrent.TimeUnit

fun <T> waitFor(
    frequencyMs: Long = 50L,
    timeoutMs: Long = TimeUnit.SECONDS.toMillis(2),
    allowedExceptions: Set<Class<out Any>> = setOf(AssertionError::class.java),
    sleepAction: (frequencyMs: Long) -> Unit = { Thread.sleep(it) },
    onWaiterRetry: (e: Throwable) -> Unit = { },
    action: () -> T
): T {
    var timer = 0L
    var caughtAllowedException: Throwable

    val startTime = System.currentTimeMillis()

    do {
        try {
            return action.invoke()
        } catch (e: Throwable) {
            val isExceptionAllowed =
                allowedExceptions.find { it.isAssignableFrom(e.javaClass) } != null

            onWaiterRetry(e)

            when {
                isExceptionAllowed -> {
                    sleepAction(frequencyMs)
                    timer += frequencyMs
                    caughtAllowedException = e
                }
                else -> throw e
            }
        }
    } while (timer <= timeoutMs && System.currentTimeMillis() - startTime <= timeoutMs)

    throw caughtAllowedException
}
