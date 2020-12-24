package com.avito.android.elastic

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

    private val dispatcher = MockDispatcher(unmockedResponse = MockResponse().setResponseCode(200))
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    @Test
    fun testRequestParams() {
        val timeProvider = StubTimeProvider()

        timeProvider.now = Date(1606922084000)

        val elasticClient: ElasticClient = HttpElasticClient(
            okHttpClient = OkHttpClient(),
            timeProvider = timeProvider,
            endpoint = mockWebServer.url("/").toUrl(),
            indexPattern = "doesnt-matter",
            buildId = "12345",
            onError = { msg, error ->
                println(msg)
                error?.printStackTrace()
            }
        )

        val capturedRequest = dispatcher.captureRequest { true }

        elasticClient.sendMessage(
            metadata = mapOf("some_key" to "SomeValue"),
            level = "WARNING",
            message = "SomeMessage",
            throwable = Exception("SomeException")
        )

        capturedRequest.checks.singleRequestCaptured().apply {
            pathContains("doesnt-matter-2020-12-02/_doc")
            bodyContains(
                "{\"@timestamp\":\"2020-12-02T18:14:44.000000000+0300\"," +
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
