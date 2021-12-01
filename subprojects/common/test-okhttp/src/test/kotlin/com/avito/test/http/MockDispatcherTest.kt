package com.avito.test.http

import com.google.common.truth.Truth.assertThat
import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.nio.charset.Charset

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

private fun buildRequest(
    method: String = "GET",
    path: String = "",
    body: String? = null
): RecordedRequest =
    RecordedRequest(
        requestLine = "$method /$path HTTP/1.1",
        headers = Headers.Builder().build(),
        chunkSizes = emptyList(),
        bodySize = if (body == null) -1 else Buffer().writeString(body, Charset.forName("UTF-8")).size,
        body = if (body == null) Buffer() else Buffer().writeString(body, Charset.forName("UTF-8")),
        sequenceNumber = -1,
        socket = StubSocket(
            inetAddress = InetAddress.getByAddress(
                "127.0.0.1",
                byteArrayOf(127, 0, 0, 1)
            ),
            localPort = 80
        )
    )
