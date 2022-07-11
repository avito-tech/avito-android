package com.avito.test.summary

import Slf4jGradleLoggerFactory
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.notification.NotificationClient
import com.avito.notification.NotificationClientFactory
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.ReportsApiFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.provider.Provider

internal class TestSummaryFactory(
    private val statsDConfig: Provider<StatsDConfig>,
) {

    private val serviceName = "test-summary-slack"

    val timeProvider: TimeProvider
        get() = DefaultTimeProvider()

    fun createSlackClient(token: String, workspace: String): NotificationClient {
        return NotificationClientFactory.createSlackClient(
            serviceName = serviceName,
            token = token,
            workspace = workspace,
            httpClientProvider = createHttpClientProvider(statsDConfig)
        )
    }

    fun createReportsApi(reportsHost: String): ReportsApi {
        return ReportsApiFactory.create(
            host = reportsHost,
            httpClientProvider = createHttpClientProvider(statsDConfig)
        )
    }

    private fun createHttpClientProvider(statsDConfig: Provider<StatsDConfig>): HttpClientProvider {
        return HttpClientProvider(
            statsDSender = StatsDSender.create(statsDConfig.get(), Slf4jGradleLoggerFactory),
            timeProvider = timeProvider,
            loggerFactory = Slf4jGradleLoggerFactory
        )
    }
}
