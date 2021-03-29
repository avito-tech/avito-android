package com.avito.test.summary

import com.avito.android.Result
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackMessageSender
import com.avito.slack.model.SlackChannelId
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

internal class FlakyTestReporterImpl(
    private val slackClient: SlackMessageSender,
    private val summaryChannel: SlackChannelId,
    private val messageAuthor: String,
    private val reportViewer: ReportViewer,
    private val buildUrl: String,
    private val currentBranch: String,
    private val reportCoordinates: ReportCoordinates
) : FlakyTestReporter {

    private val emoji = ":open-eye-laugh-crying:"

    override fun reportSummary(info: List<FlakyInfo>): Result<Unit> = Result.tryCatch {

        if (info.isNotEmpty()) {

            val topBadTests = determineBadTests(info)

            if (topBadTests.isEmpty()) {
                return@tryCatch
            }

            sendMessage(
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
        }
    }

    private fun sendMessage(
        badTests: List<FlakyInfo>,
        channel: SlackChannelId,
        reportUrl: String,
        buildUrl: String,
        currentBranch: String
    ): Result<SlackMessage> {

        //language=TEXT
        return slackClient.sendMessage(
            SlackSendMessageRequest(
                id = channel,
                text =
                """<$buildUrl|Билд на ветке $currentBranch> шел дольше чем нужно, и виноваты эти тесты:
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
        transform = {
            "${it.testName} пришлось запустить ${it.attempts} раз; " +
                "занял суммарно ${formatTime(it.wastedTimeEstimateInSec)}"
        }
    )

    private fun determineBadTests(info: List<FlakyInfo>): List<FlakyInfo> {
        return info
            .filter { it.attempts > 2 && it.wastedTimeEstimateInSec > 240 || it.attempts > 5 }
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
