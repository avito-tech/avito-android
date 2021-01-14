package com.avito.android.elastic

import com.avito.logger.StubLoggerFactory
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.time.StubTimeProvider
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Date

internal class HttpElasticClientTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val loggerFactory = StubLoggerFactory

    private val dispatcher = MockDispatcher(
        unmockedResponse = MockResponse().setResponseCode(200),
        loggerFactory = loggerFactory
    )
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    @Test
    fun testRequestParams() {
        val timeProvider = StubTimeProvider()

        timeProvider.now = Date(1609858594000)

        val elasticClient: ElasticClient = HttpElasticClient(
            okHttpClient = OkHttpClient(),
            timeProvider = timeProvider,
            endpoints = listOf(mockWebServer.url("/").toUrl()),
            indexPattern = "doesnt-matter",
            buildId = "12345",
            loggerFactory = loggerFactory
        )

        val capturedRequest = dispatcher.captureRequest { true }

        elasticClient.sendMessage(
            metadata = mapOf("some_key" to "SomeValue"),
            level = "WARNING",
            message = "SomeMessage",
            throwable = Exception("SomeException")
        )

        capturedRequest.checks.singleRequestCaptured().apply {
            pathContains("doesnt-matter-2021-01-05/_doc")
            bodyContains(
                "{\"@timestamp\":\"2021-01-05T17:56:34.000000000+0300\"," +
                    "\"level\":\"WARNING\"," +
                    "\"build_id\":\"12345\"," +
                    "\"message\":\"SomeMessage\"," +
                    "\"error_message\":\"SomeException\"," +
                    "\"some_key\":\"SomeValue\"}"
            )
        }
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
