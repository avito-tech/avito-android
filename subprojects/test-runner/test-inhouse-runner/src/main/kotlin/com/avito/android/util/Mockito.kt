package com.avito.android.util

import com.avito.android.test.waitFor
import com.avito.android.test.waitForCrt
import com.nhaarman.mockitokotlin2.verify
import org.mockito.verification.VerificationMode

fun <T> waitForVerify(mock: T, that: T.() -> Unit) {
    waitFor(
        timeoutMs = DEFAULT_VERIFY_TIMEOUT_MS,
        allowedExceptions = setOf(Throwable::class.java)
    ) { verify(mock).that() }
}

fun <T> waitForVerify(mock: T, mode: VerificationMode, that: T.() -> Unit) {
    waitFor(
        timeoutMs = DEFAULT_VERIFY_TIMEOUT_MS,
        allowedExceptions = setOf(Throwable::class.java)
    ) { verify(mock, mode).that() }
}

suspend fun <T> waitForVerifyCrt(mock: T, mode: VerificationMode, that: suspend T.() -> Unit) {
    waitForCrt(
        timeoutMs = DEFAULT_VERIFY_TIMEOUT_MS,
        allowedExceptions = setOf(Throwable::class.java)
    ) { verify(mock, mode).that() }
}

private const val DEFAULT_VERIFY_TIMEOUT_MS = 5000L
