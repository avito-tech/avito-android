package com.avito.android.lint

import com.avito.android.lint.internal.slack.LintSlackReporter
import com.avito.logger.StubLoggerFactory
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import java.io.File

internal class LintSlackAlertIntegrationTest {

    private val testChannelId = SlackChannel(
        id = requireNotNull(System.getProperty("avito.slack.test.channelId")),
        name = requireNotNull(System.getProperty("avito.slack.test.channel"))
    )
    private val testToken = requireNotNull(System.getProperty("avito.slack.test.token"))
    private val workspace = requireNotNull(System.getProperty("avito.slack.test.workspace"))
    private val slackClient: SlackClient = SlackClient.Impl(testToken, workspace)
    private val loggerFactory = StubLoggerFactory

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

inline fun <reified C> fileFromJarResources(name: String): File {
    val file = C::class.java.classLoader
        ?.getResource(name)
        ?.file
        ?.let { File(it) }

    return requireNotNull(file) { "$name not found in resources" }
}
