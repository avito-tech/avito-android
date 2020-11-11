package com.avito.android.lint

import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.utils.logging.CILogger
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
        val parser = LintResultsParser(File("src/integTest/resources"), logger)

        val reportModels = parser.parse()

        val lintSlackReporter: LintSlackReporter = LintSlackReporter.Impl(slackClient, logger)

        lintSlackReporter.report(reportModels, testChannel)
    }
}
