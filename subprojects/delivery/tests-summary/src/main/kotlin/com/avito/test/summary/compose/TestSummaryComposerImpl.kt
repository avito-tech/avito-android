package com.avito.test.summary.compose

import com.avito.alertino.AlertinoStringFormat
import com.avito.android.Result
import com.avito.report.ReportLinksGenerator
import com.avito.report.model.Team
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.analysis.analyzeFailures
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.FailureOnDevice

internal class TestSummaryComposerImpl(private val reportViewerUrl: String) : TestSummaryComposer {

    private val insightLimitLength = 400

    override fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Result<String> {
        val reportLinksGenerator: ReportLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl = reportViewerUrl,
            reportCoordinates = reportCoordinates,
            reportViewerQuery = ReportViewerQuery.createForJvm(),
        )
        val reportViewerUrl = Result.tryCatch {
            reportLinksGenerator.generateReportLink(team = team.name, filterOnlyFailures = true)
        }
        val reportIdentifier = reportCoordinates.runId

        val failures = testData.analyzeFailures()
            .toList()
            .sortedByDescending { it.second.size }
        val topFailures: List<Pair<String, List<FailureOnDevice>>> = failures.take(5)
        val rareFailures = failures.drop(5)
        val rareFailuresCount = rareFailures.sumOf { it.second.size }

        return reportViewerUrl.map { url ->
            StringBuilder().apply {
                appendLine(AlertinoStringFormat.link(label = "Report: $reportIdentifier", url = url))
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
                    ":red_circle: " +
                        "*Тесты упали на всех девайсах*: " +
                        "${testData.failedOnAllDevicesCount} (${testData.percentFailedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":warning: " +
                        "*Тесты упали только на некоторых девайсах*: " +
                        "${testData.failedOnSomeDevicesCount} (${testData.percentFailedOnSomeDevicesOfAutomated})"
                )
                appendLine(
                    ":white_circle: " +
                        "*Пропущенные тесты на всех девайсах (например, заигнорены)*: " +
                        "${testData.skippedOnAllDevicesCount} (${testData.percentSkippedOnAllDevicesOfAutomated})"
                )
                appendLine(
                    ":black_circle: " +
                        "*Потерянные тесты на некоторых девайсах (например, зависли и не зарепортились)*: " +
                        "${testData.lostOnSomeDevicesCount} (${testData.percentLostOnSomeDevicesOfAutomated})"
                )

                if (topFailures.isNotEmpty()) {
                    appendLine("*Причины падений:*")
                    topFailures.forEach {
                        val reason = AlertinoStringFormat.ellipsize(string = it.first, limit = insightLimitLength)
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
