package com.avito.android.api

import org.mockito.verification.VerificationMode

/**
 * Represents multiple response templates for single api request
 */
public abstract class ApiRequest {

    public var mode: VerificationMode = com.nhaarman.mockitokotlin2.atLeast(1)

    public abstract fun verify()
}

public fun <T : ApiRequest> T.atLeast(numInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.atLeast(numInvocations) }

public fun <T : ApiRequest> T.atMost(maxNumberOfInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.atMost(maxNumberOfInvocations) }

public fun <T : ApiRequest> T.times(wantedNumberOfInvocations: Int): T =
    this.apply { mode = com.nhaarman.mockitokotlin2.times(wantedNumberOfInvocations) }

public fun <T : ApiRequest> T.once(): T = this.apply { mode = com.nhaarman.mockitokotlin2.times(1) }

public fun <T : ApiRequest> T.background(): T = this.apply { mode = com.nhaarman.mockitokotlin2.atLeast(0) }

public fun <T : ApiRequest> T.never(): T = this.apply { mode = com.nhaarman.mockitokotlin2.never() }
