package com.avito.android.mock

import com.google.common.truth.Truth.assertThat
import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Test
import java.net.InetAddress
import java.nio.charset.Charset

class MockDispatcherTest {

    private val dispatcher = MockDispatcher(logger = { println(it) })

    @Test
    fun `dispatcher - dispatch last matching response - if multiple registered conditions matches`() {
        val sameRequest: RequestData.() -> Boolean = { path.contains("xxx") }

        dispatcher.mocks.add(Mock(sameRequest, MockResponse().setBody("First registered")))
        dispatcher.mocks.add(Mock(sameRequest, MockResponse().setBody("Second registered")))

        val response = dispatcher.dispatch(buildRequest(path = "xxx"))

        assertThat(response.getBody()?.readUtf8()).isEqualTo("Second registered")
    }

    @Test
    fun `dispatcher - find matching request - if multiple registered request has same path but different body`() {
        dispatcher.mocks.add(
            Mock(
                { path.contains("xxx") && body.contains("param1485") },
                MockResponse().setBody("First registered")
            )
        )
        dispatcher.mocks.add(
            Mock(
                { path.contains("xxx") && body.contains("category89") },
                MockResponse().setBody("Second registered")
            )
        )

        val response = dispatcher.dispatch(buildRequest(path = "xxx", body = "param1485"))

        assertThat(response.getBody()?.readUtf8()).isEqualTo("First registered")
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
        socket = FakeSocket(
            InetAddress.getByAddress(
                "127.0.0.1",
                byteArrayOf(127, 0, 0, 1)
            ), 80
        )
    )
