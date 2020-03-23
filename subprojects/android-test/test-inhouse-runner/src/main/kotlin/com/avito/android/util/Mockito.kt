package com.avito.android.util

import com.avito.android.test.waitFor
import com.nhaarman.mockitokotlin2.verify
import org.mockito.verification.VerificationMode
import java.util.concurrent.TimeUnit

fun <T> waitForVerify(mock: T, that: T.() -> Unit) {
    waitFor(
        timeoutMs = TimeUnit.SECONDS.toMillis(5),
        allowedExceptions = setOf(Throwable::class.java)
    ) { verify(mock).that() }
}

fun <T> waitForVerify(mock: T, mode: VerificationMode, that: T.() -> Unit) {
    waitFor(
        timeoutMs = TimeUnit.SECONDS.toMillis(5),
        allowedExceptions = setOf(Throwable::class.java)
    ) { verify(mock, mode).that() }
}
