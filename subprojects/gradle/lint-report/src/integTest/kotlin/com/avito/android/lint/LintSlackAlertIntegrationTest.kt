package com.avito.android.lint

import com.avito.android.lint.internal.model.LintResultsParser
import com.avito.android.lint.internal.slack.LintSlackReporter
import com.avito.android.stats.StubStatsdSender
import com.avito.http.HttpClientProvider
import com.avito.logger.StubLoggerFactory
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.time.StubTimeProvider
import com.avito.utils.fileFromJarResources
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

internal class LintSlackAlertIntegrationTest {

    private val loggerFactory = StubLoggerFactory

    private val timeProvider = StubTimeProvider()

    private val statsdSender = StubStatsdSender()

    private val testChannelId = SlackChannel(
        id = requireNotNull(System.getProperty("avito.slack.test.channelId")),
        name = requireNotNull(System.getProperty("avito.slack.test.channel"))
    )

    private val testToken = requireNotNull(System.getProperty("avito.slack.test.token"))

    private val workspace = requireNotNull(System.getProperty("avito.slack.test.workspace"))

    private val slackClient: SlackClient = SlackClient.Impl(
        serviceName = "lint-report-slack-test",
        token = testToken,
        workspace = workspace,
        httpClientProvider = HttpClientProvider(
            statsDSender = statsdSender,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        )
    )

    @Test
    fun integrationTest() {
        val parser = LintResultsParser(loggerFactory)

        val reportModels = parser.parse(
            projectPath = ":app",
            lintXml = fileFromJarResources<LintSlackAlertIntegrationTest>("lint-results.xml"),
            lintHtml = fileFromJarResources<LintSlackAlertIntegrationTest>("lint-results.html")
        )

        val lintSlackReporter: LintSlackReporter = LintSlackReporter.Impl(slackClient, loggerFactory)

        lintSlackReporter.report(
            lintReport = reportModels,
            channel = testChannelId,
            channelForLintBugs = testChannelId,
            buildUrl = "https://stubbuildurl".toHttpUrl()
        )
    }
}
