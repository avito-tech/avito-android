package com.avito.test.http

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class NothingCapturedTest {

    private val dispatcher = MockDispatcher()

    @Test
    fun `mock dispatcher - nothing captured - no requests`() {
        val request = dispatcher.captureRequest(requestMatcher = { path.contains("xxx") })

        request.checks.nothingCaptured()
    }

    @Test
    fun `mock dispatcher - nothing captured - un matching request`() {
        val request = dispatcher.captureRequest(requestMatcher = { path.contains("xxx") })

        dispatcher.dispatch(buildRequest(path = "yyy"))

        request.checks.nothingCaptured()
    }

    @Test
    fun `mock dispatcher - nothing captured fails - matching request`() {
        val request = dispatcher.captureRequest(requestMatcher = { path.contains("xxx") })

        dispatcher.dispatch(buildRequest(path = "xxx"))

        val error = assertThrows<AssertionError> {
            request.checks.nothingCaptured()
        }

        assertThat(error.message).contains("No requests should be captured")
    }
}
