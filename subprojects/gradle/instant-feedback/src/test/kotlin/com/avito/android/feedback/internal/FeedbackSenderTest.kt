package com.avito.android.feedback.internal

import com.avito.android.Result
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FeedbackSenderTest {

    private lateinit var server: MockWebServer

    @BeforeEach
    fun setup() {
        server = MockWebServerFactory.create()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sends expected payload`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val result = sender().send(
            RequestFeedbackData(
                scenario = "after_android_update_translations",
                user = "testuser",
                arguments = mapOf("target" to ":some:module", "status" to "success"),
                date = "2026-09-18T10:15:30.000Z",
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)

        val request = server.takeRequest()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.getHeader("Content-Type")).contains("application/json")
        assertThat(request.body.readUtf8()).isEqualTo(
            """{"scenario":"after_android_update_translations","user":"testuser",""" +
                """"arguments":{"target":":some:module","status":"success"},""" +
                """"date":"2026-09-18T10:15:30.000Z"}"""
        )
    }

    @Test
    fun `server error - failure without throwing`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("nope"))

        val result = sender().send(
            RequestFeedbackData(
                scenario = "any",
                user = "testuser",
                arguments = emptyMap(),
                date = "2026-09-18T10:15:30.000Z",
            )
        )

        assertThat(result).isInstanceOf(Result.Failure::class.java)
    }

    private fun sender() = FeedbackSender(endpoint = server.url("/sendEvent/").toString())
}
