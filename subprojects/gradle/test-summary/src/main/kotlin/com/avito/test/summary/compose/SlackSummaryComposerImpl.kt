package com.avito.test.summary.compose

import com.avito.android.Result
import com.avito.report.ReportLinksGenerator
import com.avito.report.model.Team
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.slack.SlackStringFormat
import com.avito.test.summary.analysis.analyzeFailures
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.FailureOnDevice

internal class SlackSummaryComposerImpl(private val reportViewerUrl: String) : SlackSummaryComposer {

    private val insightLimitLength = 400

    override fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        mentionOnFailures: Boolean,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Result<String> {
        val reportLinksGenerator: ReportLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl = reportViewerUrl,
            reportCoordinates = reportCoordinates
        )
        val reportViewerUrl = Result.tryCatch { reportLinksGenerator.generateReportLink(team = team.name) }
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
                        "${testData.success} (${testData.successOfAutomated})"
                )
                appendLine(
                    ":warning: " +
                        "*Тесты упали только на некоторых девайсах*: " +
                        "${testData.failedOnAnyDeviceCount} (${testData.failedOnAnyDeviceOfAutomated})"
                )
                appendLine(
                    ":red_circle: " +
                        "*Тесты упали на всех девайсах*: " +
                        "${testData.failedOnAllDevicesCount} (${testData.failedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":white_circle: " +
                        "*Пропущенные тесты (например, заигнорен) на всех девайсах*: " +
                        "${testData.skippedOnAllDevicesCount} (${testData.skippedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":black_circle: " +
                        "*Потерянные тесты (например, зависли и не зарепортились) на некоторых девайсах*: " +
                        "${testData.lostOnAnyDeviceCount} (${testData.lostOnAnyDeviceOfAutomated})"
                )

                val hasFailures = testData.failedOnAnyDeviceCount + testData.failedOnAllDevicesCount > 0

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
