package com.avito.test.summary.compose

import com.avito.report.ReportViewer
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.FailureOnDevice
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackStringFormat
import com.avito.test.summary.analysis.analyzeFailures
import org.funktionale.tries.Try

internal class SlackSummaryComposerImpl(private val reportViewer: ReportViewer) : SlackSummaryComposer {

    private val insightLimitLength = 400

    override fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        mentionOnFailures: Boolean,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Try<String> {
        val reportViewerUrl = Try { reportViewer.generateReportUrl(reportId, team = team) }
        val reportIdentifier = reportCoordinates.runId

        val failures = testData.analyzeFailures()
            .toList()
            .sortedByDescending { it.second.size }
        val topFailures: List<Pair<String, List<FailureOnDevice>>> = failures.take(5)
        val rareFailures = failures.drop(5)
        val rareFailuresCount = rareFailures.sumBy { it.second.size }

        return reportViewerUrl.map { url ->
            StringBuilder().apply {
                appendLine(SlackStringFormat.link(label = "Report: $reportIdentifier", url = url))
                if (team != Team.UNDEFINED) {
                    appendLine("Юнит: ${team.name}\n")
                }
                appendLine("Ручные тесты: ${testData.manualCount}\n")

                appendLine("*Автотесты*: ${testData.automatedCount}")

                // todo адекватная разбивка по flaky
                appendLine(
                    ":green_heart: " +
                            "*Зеленые тесты*: " +
                            "${testData.success} (${testData.percentSuccessOfAutomated})"
                )
                appendLine(
                    ":warning: " +
                            "*Тесты упали только на некоторых девайсах*: " +
                            "${testData.failedOnSomeDevicesCount} (${testData.percentFailedOnSomeDevicesOfAutomated})"
                )
                appendLine(
                    ":red_circle: " +
                            "*Тесты упали на всех девайсах*: " +
                            "${testData.failedOnAllDevicesCount} (${testData.percentFailedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":white_circle: " +
                            "*Пропущенные тесты (например, заигнорен) на всех девайсах*: " +
                            "${testData.skippedOnAllDevicesCount} (${testData.percentSkippedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":black_circle: " +
                            "*Потерянные тесты (например, зависли и не зарепортились) на некоторых девайсах*: " +
                            "${testData.lostOnSomeDevicesCount} (${testData.percentLostOnSomeDevicesOfAutomated})"
                )

                val hasFailures = testData.failedOnSomeDevicesCount + testData.failedOnAllDevicesCount > 0

                if (mentionOnFailures && hasFailures) {
                    appendLine("${SlackStringFormat.mentionChannel}, т.к. есть упавшие тесты")
                }

                if (topFailures.isNotEmpty()) {
                    appendLine("*Причины падений:*")
                    topFailures.forEach {
                        val reason = SlackStringFormat.ellipsize(string = it.first, limit = insightLimitLength)
                        appendLine("*${it.second.size}* из-за ```$reason```")
                    }
                }

                if (rareFailuresCount > 0) {
                    appendLine("И еще *$rareFailuresCount* более редких падений из-за различных причин.")
                }
            }.toString()
        }
    }
}
