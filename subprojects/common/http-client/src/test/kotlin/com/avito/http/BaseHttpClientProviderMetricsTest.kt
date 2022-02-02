package com.avito.http

import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.http.internal.RequestMetadata
import com.avito.logger.PrintlnLoggerFactory
import com.avito.time.DefaultTimeProvider
import com.google.common.truth.Correspondence
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockWebServer

internal open class BaseHttpClientProviderMetricsTest {

    protected val statsDSender = StubStatsdSender()
    protected val mockWebServer = MockWebServer()
    protected val doesNotMatter = 111L
    private val loggerFactory = PrintlnLoggerFactory

    protected val metricNamesCorrespondence: Correspondence<StatsMetric, StatsMetric> = Correspondence.from(
        { actual, expected ->
            if (actual != null && expected != null) {
                actual.name == expected.name
            } else false
        },
        "metric names are equal"
    )

    protected fun OkHttpClient.createCall(): Call {
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

    protected fun createClientProvider(): HttpClientProvider {
        return HttpClientProvider(
            statsDSender = statsDSender,
            timeProvider = DefaultTimeProvider(),
            loggerFactory = loggerFactory
        )
    }
}
