package com.avito.http

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.avito.http.internal.RequestMetadata
import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.Flaky
import com.avito.time.DefaultTimeProvider
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class HttpClientProviderMetricsTest {

    private val loggerFactory = PrintlnLoggerFactory

    private val statsDSender = StubStatsdSender()

    private val mockWebServer = MockWebServer()

    private val metricNamesCorrespondence: Correspondence<StatsMetric, StatsMetric> = Correspondence.from(
        { actual, expected ->
            if (actual != null && expected != null) {
                actual.name == expected.name
            } else false
        },
        "metric names are equal"
    )

    private val doesNotMatter = 111L

    @Test
    fun `timeout metric send - request timed out`() {
        val provider = createClientProvider()

        val httpClient = provider.provide()
            .readTimeout(1, TimeUnit.MILLISECONDS)
            .build()

        mockWebServer.enqueue(MockResponse().setHeadersDelay(10, TimeUnit.MILLISECONDS))

        assertThrows<SocketTimeoutException> {
            httpClient.blockingCall()
        }

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).containsExactly(
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "timeout"), doesNotMatter)
        )
    }

    @Flaky(reason = "MBS-11302")
    @Test
    fun `all metrics send - multiple parallel requests`() {
        val provider = createClientProvider()

        val httpClient = provider.provide().build()

        val count = 3

        repeat(count) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        httpClient.blockingMultipleParallelCalls(count)

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).containsExactly(
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "200"), doesNotMatter),
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "200"), doesNotMatter),
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "200"), doesNotMatter)
        )
    }

    @Test
    fun `response 200 metric send - success request`() {
        val provider = createClientProvider()

        val httpClient = provider.provide().build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        httpClient.blockingCall()

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).contains(
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "200"), doesNotMatter)
        )
    }

    @Test
    fun `response 502 metric - failed request`() {
        val provider = createClientProvider()

        val httpClient = provider.provide().build()

        mockWebServer.enqueue(MockResponse().setResponseCode(502))

        httpClient.blockingCall()

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).contains(
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "502"), doesNotMatter)
        )
    }

    @Test
    fun `all retries metrics send - request retries multiple times`() {
        val provider = createClientProvider()

        val httpClient = provider.provide()
            .addInterceptor(RetryInterceptor(retries = 3))
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(502))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        httpClient.blockingCall()

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).containsExactly(
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "502"), doesNotMatter),
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "500"), doesNotMatter),
            TimeMetric(SeriesName.create("service", "some-service", "some-method", "200"), doesNotMatter)
        ).inOrder()
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    private fun OkHttpClient.createCall(): Call {
        return newCall(
            Request.Builder()
                .url(mockWebServer.url("/"))
                .tag(
                    RequestMetadata::class.java,
                    RequestMetadata("some-service", "some-method")
                )
                .build()
        )
    }

    private fun OkHttpClient.blockingCall() {
        createCall().execute()
    }

    private fun OkHttpClient.blockingMultipleParallelCalls(count: Int) {
        val latch = CountDownLatch(count)
        repeat(count) {
            createCall().enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        latch.countDown()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        latch.countDown()
                    }
                }
            )
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    private fun createClientProvider(): HttpClientProvider {
        return HttpClientProvider(
            statsDSender = statsDSender,
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory
        )
    }
}
