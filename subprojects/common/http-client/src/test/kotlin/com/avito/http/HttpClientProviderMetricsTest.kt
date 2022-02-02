package com.avito.http

import com.avito.android.stats.SeriesName
import com.avito.android.stats.TimeMetric
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

internal class HttpClientProviderMetricsTest : BaseHttpClientProviderMetricsTest() {

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

    private fun OkHttpClient.blockingCall() {
        createCall().execute()
    }
}
