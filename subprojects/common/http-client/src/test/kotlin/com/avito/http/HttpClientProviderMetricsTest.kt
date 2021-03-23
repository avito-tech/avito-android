package com.avito.http

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.logger.StubLoggerFactory
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

internal class HttpClientProviderMetricsTest {

    private val loggerFactory = StubLoggerFactory

    private val statsDSender = StubStatsdSender()

    private val mockWebServer = MockWebServer()

    private val dispatcher = MockDispatcher(loggerFactory = loggerFactory)
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    private val metricNamesCorrespondence: Correspondence<StatsMetric, StatsMetric> = Correspondence.from(
        { actual, expected ->
            if (actual != null && expected != null) {
                actual.name == expected.name
            } else false
        },
        "metric names are equal"
    )

    private val doesNotMatter = 111

    @Test
    fun `timeout metric`() {
        val provider = createClientProvider()

        val httpClient = provider.provide(
            serviceName = "some-service",
            timeoutMs = 1,
            retryPolicy = null
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setHeadersDelay(10, TimeUnit.MILLISECONDS)
            )
        )

        assertThrows<SocketTimeoutException> {
            httpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()
        }

        assertThat(statsDSender.getSentMetrics()).containsExactly(
            CountMetric(SeriesName.create("service", "some-service", "response", "timeout"))
        )
    }

    @Test
    fun `response 200 metric`() {
        val provider = createClientProvider()

        val httpClient = provider.provide(
            serviceName = "some-service",
            retryPolicy = null
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(200)
            )
        )

        httpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        assertThat(statsDSender.getSentMetrics()).contains(
            CountMetric(SeriesName.create("service", "some-service", "response", "200"))
        )
    }

    @Test
    fun `response 502 metric`() {
        val provider = createClientProvider()

        val httpClient = provider.provide(
            serviceName = "some-service",
            retryPolicy = null
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(502)
            )
        )

        httpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        assertThat(statsDSender.getSentMetrics()).contains(
            CountMetric(SeriesName.create("service", "some-service", "response", "502"))
        )
    }

    @Test
    fun `latency metric`() {
        val provider = createClientProvider()

        val httpClient = provider.provide(
            serviceName = "some-service",
            retryPolicy = null
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(200)
            )
        )

        httpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        assertThat(statsDSender.getSentMetrics()).comparingElementsUsing(metricNamesCorrespondence).contains(
            GaugeMetric(SeriesName.create("service", "some-service", "latency"), doesNotMatter)
        )
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    private fun createClientProvider(): HttpClientProvider {
        return HttpClientProvider(statsDSender)
    }
}
