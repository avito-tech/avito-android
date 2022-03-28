package com.avito.http

import com.avito.android.stats.SeriesName
import com.avito.android.stats.TimeMetric
import com.google.common.truth.Truth.assertThat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Disabled("https://github.com/gradle/test-retry-gradle-plugin/issues/128")
internal class FlakyHttpClientProviderMetricsTest : BaseHttpClientProviderMetricsTest() {

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
}
