package com.avito.test.summary.time

import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class TimeMessengerClientTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val client = TimeMessengerClient(
        baseUrl = mockWebServer.url("/").toString(),
        token = "secret-token",
    )

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `create post - sends message to channel with bearer token`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"post-id"}"""))

        val result = client.createPost(channelId = "channel-id", message = "**Report**")

        assertThat(result.isSuccess()).isTrue()
        val request = mockWebServer.takeRequest()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.path).isEqualTo("/api/v4/posts")
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer secret-token")
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertThat(body.get("channel_id").asString).isEqualTo("channel-id")
        assertThat(body.get("message").asString).isEqualTo("**Report**")
    }

    @Test
    fun `create post - retries - when rate limited`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(429).setBody("limit exceeded"))
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"post-id"}"""))

        val result = client.createPost(channelId = "channel-id", message = "message")

        assertThat(result.isSuccess()).isTrue()
        assertThat(mockWebServer.requestCount).isEqualTo(2)
    }

    @Test
    fun `create post - fails with response body - when channel is not allowed`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403).setBody("ctb.policy.channel_not_allowed"))

        val result = client.createPost(channelId = "channel-id", message = "message")

        assertThat(result.isFailure()).isTrue()
        result.onFailure { error ->
            assertThat(error).hasMessageThat().isEqualTo(
                "Time API responded with 403 to create post: ctb.policy.channel_not_allowed"
            )
        }
        assertThat(mockWebServer.requestCount).isEqualTo(1)
    }
}
