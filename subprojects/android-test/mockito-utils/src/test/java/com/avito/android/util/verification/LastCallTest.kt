package com.avito.android.util.verification

import com.avito.android.util.verification.VerificationModes.lastCall
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test

@Suppress("IllegalIdentifier")
class LastCallTest {

    @Test(expected = AssertionError::class)
    fun `verify - should fail - no invocation`() {
        val stub = mock<Dummy>()

        verify(stub, lastCall()).foo()
    }

    @Test(expected = AssertionError::class)
    fun `verify - should fail - last invocation not matched`() {
        val stub = mock<Dummy>()

        stub.foo()
        stub.bar()

        verify(stub, lastCall()).foo()
    }

    @Test
    fun `verify - should pass - expected last invocation`() {
        val stub = mock<Dummy>()

        stub.bar()
        stub.foo()

        verify(stub, lastCall()).foo()
    }

    @Test
    fun `verify - mark all invocation as verified - expected last invocation`() {
        val stub = mock<Dummy>()

        stub.bar()
        stub.foo()

        verify(stub, lastCall()).foo()
        verifyNoMoreInteractions(stub)
    }

    open class Dummy {
        open fun foo() = {}
        open fun bar() = {}
    }

}