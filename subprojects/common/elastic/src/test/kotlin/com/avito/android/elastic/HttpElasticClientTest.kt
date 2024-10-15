package com.avito.android.elastic

import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.time.TimeMachineProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class HttpElasticClientTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val loggerFactory = PrintlnLoggerFactory

    private val timeProvider = TimeMachineProvider()

    private val dispatcher = MockDispatcher(
        unmockedResponse = MockResponse().setResponseCode(200),
    )
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    @Test
    fun testRequestParams() {

        timeProvider.now = 1609858594000

        val elasticClient: ElasticClient = HttpElasticClient(
            timeProvider = timeProvider,
            elasticApi = ElasticApiProvider(
                gson = GsonBuilder(),
                okHttpClientBuilder = OkHttpClient.Builder(),
            ).provide(listOf(mockWebServer.url("/").toUrl())),
            indexName = "stub-index",
            sourceType = "gradle-build",
            sourceId = "12345",
            authApiKey = "testkey",
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
            containsHeader("Authorization", "ApiKey testkey")
            pathContains("stub-index-2021.01.05/_doc")
            bodyContains(
                "{\"@timestamp\":\"2021-01-05T14:56:34.000Z\"," +
                    "\"level\":\"WARNING\"," +
                    "\"source.type\":\"gradle-build\"," +
                    "\"source.id\":\"12345\"," +
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
