package com.avito.test.summary

import Slf4jGradleLoggerFactory
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.ReportsApiFactory
import com.avito.slack.SlackClient
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.provider.Provider

public class TestSummaryFactory(
    private val statsDConfig: Provider<StatsDConfig>,
) {

    private val serviceName = "test-summary-slack"

    public val timeProvider: TimeProvider
        get() = DefaultTimeProvider()

    public fun createSlackClient(extension: TestSummaryExtension): SlackClient {
        return SlackClient.create(
            serviceName = serviceName,
            token = extension.slackToken.get(),
            workspace = extension.slackWorkspace.get(),
            httpClientProvider = createHttpClientProvider(statsDConfig)
        )
    }

    public fun createReportsApi(extension: TestSummaryExtension): ReportsApi {
        return ReportsApiFactory.create(
            host = extension.reportsHost.get(),
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
