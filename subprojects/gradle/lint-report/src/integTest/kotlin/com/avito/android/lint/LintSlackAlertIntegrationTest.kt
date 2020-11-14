package com.avito.android.lint

import com.avito.android.lint.slack.LintSlackReporter
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.CILogger
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import java.io.File

internal class LintSlackAlertIntegrationTest {

    private val testChannel = SlackChannel(requireNotNull(System.getProperty("avito.slack.test.channel")))
    private val testToken = requireNotNull(System.getProperty("avito.slack.test.token"))
    private val workspace = requireNotNull(System.getProperty("avito.slack.test.workspace"))
    private val slackClient: SlackClient = SlackClient.Impl(testToken, workspace)
    private val logger: CILogger = CILogger.allToStdout

    @Test
    fun integrationTest() {
        val parser = LintResultsParser(logger)

        val reportModels = parser.parse(
            projectPath = ":app",
            lintXml = fileFromJarResources<LintSlackAlertIntegrationTest>("lint-results.xml"),
            lintHtml = fileFromJarResources<LintSlackAlertIntegrationTest>("lint-results.html")
        )

        val lintSlackReporter: LintSlackReporter = LintSlackReporter.Impl(slackClient, logger)

        lintSlackReporter.report(
            lintReport = reportModels,
            channel = testChannel,
            buildUrl = "https://stubbuildurl".toHttpUrl()
        )
    }
}

inline fun <reified C> fileFromJarResources(name: String) = File(C::class.java.classLoader.getResource(name).file)
