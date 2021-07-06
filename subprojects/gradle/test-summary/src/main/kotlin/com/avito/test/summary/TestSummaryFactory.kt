package com.avito.test.summary

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.ReportsApiFactory
import com.avito.slack.SlackClient
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.provider.Provider

public class TestSummaryFactory(
    private val loggerFactory: LoggerFactory,
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
            loggerFactory = loggerFactory,
            httpClientProvider = createHttpClientProvider(statsDConfig)
        )
    }

    private fun createHttpClientProvider(statsDConfig: Provider<StatsDConfig>): HttpClientProvider {
        return HttpClientProvider(
            statsDSender = StatsDSender.create(statsDConfig.get(), loggerFactory),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )
    }
}
