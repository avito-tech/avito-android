package com.avito.android.elastic

import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.time.FakeTimeProvider
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Date

internal class MultipleEndpointsElasticTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val dispatcher = MockDispatcher(unmockedResponse = MockResponse().setResponseCode(200))
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    @Test
    fun testRequestParams() {
        val timeProvider = FakeTimeProvider()

        timeProvider.now = Date(1606922084000)

        val elastic: Elastic = MultipleEndpointsElastic(
            timeProvider = timeProvider,
            endpoints = listOf(mockWebServer.url("/").toString()),
            indexPattern = "doesnt-matter",
            buildId = "12345",
            verboseHttpLog = { println(it) },
            onError = { msg, error ->
                println(msg)
                error?.printStackTrace()
            }
        )

        val capturedRequest = dispatcher.captureRequest { true }

        elastic.sendMessage(
            tag = "SomeTag",
            level = "WARNING",
            message = "SomeMessage",
            throwable = Exception("SomeException")
        )

        capturedRequest.checks.singleRequestCaptured().apply {
            pathContains("doesnt-matter-2020-12-02/_doc")
            bodyContains(
                "{\"@timestamp\":\"2020-12-02T18:14:44.000000000+0300\"," +
                    "\"tag\":\"SomeTag\"," +
                    "\"level\":\"WARNING\"," +
                    "\"build_id\":\"12345\"," +
                    "\"message\":\"SomeMessage\"," +
                    "\"error_message\":\"SomeException\"}"
            )
        }
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
