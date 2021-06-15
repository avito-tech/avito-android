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
                    caughtAllowedException = e
                }
                else -> throw e
            }
        }
    } while (System.currentTimeMillis() - startTime <= timeoutMs)

    throw caughtAllowedException
}

fun repeatFor(
    frequencyMs: Long = 50L,
    timeoutMs: Long = TimeUnit.SECONDS.toMillis(2),
    sleepAction: (frequencyMs: Long) -> Unit = { Thread.sleep(it) },
    action: () -> Unit
) {
    val startTime = System.currentTimeMillis()

    do {
        action.invoke()
        sleepAction(frequencyMs)
    } while (System.currentTimeMillis() - startTime <= timeoutMs)
}
