package com.avito.instrumentation.report

import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.InsightData
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackMessageSender
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

interface FlakyTestReporter {
    fun reportSummary(
        info: List<FlakyInfo>
    ): Try<Unit>
}

class FlakyTestReporterImpl(
    private val slackClient: SlackMessageSender,
    private val summaryChannel: SlackChannel,
    private val messageAuthor: String,
    private val bitbucket: Bitbucket,
    private val sourceCommitHash: String,
    private val reportViewer: ReportViewer,
    private val buildUrl: String,
    private val currentBranch: String,
    private val reportCoordinates: ReportCoordinates,
    private val logger: CILogger
) : FlakyTestReporter {

    private val emoji = ":open-eye-laugh-crying:"

    override fun reportSummary(
        info: List<FlakyInfo>
    ): Try<Unit> = Try {

        if (info.isNotEmpty()) {

            val topBadTests = determineBadTests(info)

            if (topBadTests.isEmpty()) {
                return@Try
            }

            val slackMessage = sendMessage(
                badTests = topBadTests,
                channel = summaryChannel,
                buildUrl = buildUrl,
                currentBranch = currentBranch,
                reportUrl = reportViewer.generateReportUrl(
                    reportCoordinates = reportCoordinates,
                    onlyFailures = true,
                    team = Team.UNDEFINED
                ).toString()
            )

            slackMessage.map { message ->
                bitbucket.addInsightReport(
                    sourceCommitHash = sourceCommitHash,
                    key = "flaky-test-reporter",
                    title = "Flaky tests report in slack",
                    details = "Билд шел дольше чем нужно из-за flaky тестов, отчет по ссылке",
                    link = message.link,
                    data = listOf(
                        InsightData.Number(
                            title = "Кол-во flaky тестов",
                            value = info.size
                        ),
                        InsightData.Number(
                            title = "Суммарное кол-во перезапусков",
                            value = info.sumBy { it.attempts }),
                        InsightData.Duration(
                            title = "Суммарное потраченное время в сек",
                            value = info.sumBy { it.wastedTimeEstimateInSec } * 1000L)
                    )
                )
            }.onFailure { logger.critical("Can't send flaky report", it) }
        }
    }

    private fun sendMessage(
        badTests: List<FlakyInfo>,
        channel: SlackChannel,
        reportUrl: String,
        buildUrl: String,
        currentBranch: String
    ): Try<SlackMessage> {

        //language=TEXT
        return slackClient.sendMessage(
            SlackSendMessageRequest(
                channel = channel,
                text = """<$buildUrl|Билд на ветке $currentBranch> шел дольше чем нужно, и виноваты эти тесты:
```
${badTests.stringify()}
```

Почините или заигнорьте их :stalin:

<$reportUrl|Отчет о запусках на $currentBranch>
""".trimIndent(),
                emoji = emoji,
                author = messageAuthor
            )
        )
    }

    private fun List<FlakyInfo>.stringify(): String = joinToString(
        separator = "\n",
        transform = { "${it.testName} пришлось запустить ${it.attempts} раз; занял суммарно ${formatTime(it.wastedTimeEstimateInSec)}" })

    private fun determineBadTests(info: List<FlakyInfo>): List<FlakyInfo> {
        return info
            .filter { (it.attempts > 2 && it.wastedTimeEstimateInSec > 240) || it.attempts > 5 }
            .sortedByDescending { it.wastedTimeEstimateInSec }
            .take(10)
    }

    private fun formatTime(seconds: Int): String {
        val result = StringBuilder()
        if (seconds > 59) {
            result.append("${seconds / 60}м ")
        }
        result.append("${seconds % 60}с")
        return result.toString()
    }
}
