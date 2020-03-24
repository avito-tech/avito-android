package com.avito.android.api

import org.mockito.verification.VerificationMode

/**
 * Represents multiple response templates for single api request
 */
abstract class ApiRequest {

    var mode: VerificationMode = com.nhaarman.mockitokotlin2.atLeast(1)

    abstract fun verify()
}

fun <T : ApiRequest> T.atLeast(numInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.atLeast(numInvocations) }

fun <T : ApiRequest> T.atMost(maxNumberOfInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.atMost(maxNumberOfInvocations) }

fun <T : ApiRequest> T.times(wantedNumberOfInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.times(wantedNumberOfInvocations) }

fun <T : ApiRequest> T.once(): T = this.apply { mode = com.nhaarman.mockitokotlin2.times(1) }

fun <T : ApiRequest> T.background(): T = this.apply { mode = com.nhaarman.mockitokotlin2.atLeast(0) }

fun <T : ApiRequest> T.never(): T = this.apply { mode = com.nhaarman.mockitokotlin2.never() }
