package com.avito.test.http

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class MockDispatcherTest {

    private val dispatcher = MockDispatcher()

    @Test
    fun `dispatcher - dispatch last matching response - if multiple registered conditions matches`() {
        val sameRequest: RequestData.() -> Boolean = { path.contains("xxx") }

        dispatcher.registerMock(
            Mock(
                requestMatcher = sameRequest,
                response = MockResponse().setBody("First registered")
            )
        )
        dispatcher.registerMock(
            Mock(
                requestMatcher = sameRequest,
                response = MockResponse().setBody("Second registered")
            )
        )

        val response = dispatcher.dispatch(buildRequest(path = "xxx"))

        assertThat(response.getBody()?.readUtf8()).isEqualTo("Second registered")
    }

    @Test
    fun `dispatcher - find matching request - if multiple registered request has same path but different body`() {
        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("xxx") && body.contains("param1485") },
                response = MockResponse().setBody("First registered")
            )
        )
        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("xxx") && body.contains("category89") },
                response = MockResponse().setBody("Second registered")
            )
        )

        val response = dispatcher.dispatch(buildRequest(path = "xxx", body = "param1485"))

        assertThat(response.getBody()?.readUtf8()).isEqualTo("First registered")
    }

    @Test
    fun `dispatcher - captures request - also sending response`() {
        val request = dispatcher.captureRequest(
            Mock(
                requestMatcher = { path.contains("xxx") },
                response = MockResponse().setResponseCode(200).setBody("Capturer response")
            )
        )

        val response = dispatcher.dispatch(buildRequest(path = "xxx", body = "2222"))

        assertThat(response.getBody()?.readUtf8()).isEqualTo("Capturer response")
        request.checks.singleRequestCaptured().bodyContains("2222")
    }

    /**
     * Run with "Repeat: Until failure" option in IDEA run configuration
     * see MBS-7636
     */
    @Disabled("Used only for manual debugging of concurrency problems on dispatcher internal collections")
    @Test
    fun `dispatcher - register and dispatching mocks - accessed concurrently`() {
        var throwable: Throwable? = null

        val registers = Thread {
            repeat(100) {
                try {
                    dispatcher.registerMock(Mock({ true }, MockResponse(), true))
                } catch (e: Throwable) {
                    throwable = e
                }
            }
        }

        val dispatchers = Thread {
            repeat(100) {
                try {
                    dispatcher.dispatch(buildRequest())
                } catch (e: Throwable) {
                    throwable = e
                }
            }
        }

        registers.start()
        dispatchers.start()

        registers.join()
        dispatchers.join()

        assertThat(throwable).isNull()
    }
}
