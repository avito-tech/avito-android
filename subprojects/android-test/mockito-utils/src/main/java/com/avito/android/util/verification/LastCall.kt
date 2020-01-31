package com.avito.android.util.verification

import org.mockito.exceptions.base.MockitoAssertionError
import org.mockito.internal.exceptions.Reporter.wantedButNotInvoked
import org.mockito.internal.invocation.InvocationMarker
import org.mockito.internal.invocation.InvocationMatcher
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.internal.verification.api.VerificationData
import org.mockito.invocation.Invocation
import org.mockito.verification.VerificationMode
import ru.avito.util.invocation.notMatches

class LastCall : VerificationMode {

    override fun verify(data: VerificationData) {
        @Suppress("DEPRECATION")
        val wantedMatcher: InvocationMatcher = data.wanted
        val invocations: List<Invocation> = data.allInvocations

        if (invocations.isEmpty()) {
            throw wantedButNotInvoked(wantedMatcher)
        }
        val lastInvocation = invocations.last()
        if (wantedMatcher.notMatches(lastInvocation)) {
            throw MockitoAssertionError("Expected last call $wantedMatcher but was $lastInvocation}")
        }
        invocations.forEach {
            InvocationMarker.markVerified(it, wantedMatcher)
        }
    }

    override fun description(description: String): VerificationMode {
        return VerificationModeFactory.description(this, description)
    }

}
