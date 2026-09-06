package com.avito.android.api

import org.mockito.verification.VerificationMode

/**
 * Represents multiple response templates for single api request
 */
public abstract class ApiRequest {

    public var mode: VerificationMode = org.mockito.kotlin.atLeast(1)

    public abstract fun verify()
}

public fun <T : ApiRequest> T.atLeast(numInvocations: Int): T =
    this.apply { mode = org.mockito.kotlin.atLeast(numInvocations) }

public fun <T : ApiRequest> T.atMost(maxNumberOfInvocations: Int): T =
    this.apply { mode = org.mockito.kotlin.atMost(maxNumberOfInvocations) }

public fun <T : ApiRequest> T.times(wantedNumberOfInvocations: Int): T =
    this.apply { mode = org.mockito.kotlin.times(wantedNumberOfInvocations) }

public fun <T : ApiRequest> T.once(): T = this.apply { mode = org.mockito.kotlin.times(1) }

public fun <T : ApiRequest> T.background(): T = this.apply { mode = org.mockito.kotlin.atLeast(0) }

public fun <T : ApiRequest> T.never(): T = this.apply { mode = org.mockito.kotlin.never() }
