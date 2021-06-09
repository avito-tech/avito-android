package com.avito.runner.finalizer.action

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.Stability
import com.avito.report.model.Status
import com.avito.report.model.TestName
import com.avito.runner.finalizer.TestRunResult
import com.avito.runner.finalizer.verdict.LegacyVerdict
import com.avito.runner.finalizer.verdict.TestStatisticsCounterFactory
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class LegacyWriteJUnitReportAction(
    private val testSuiteNameProvider: TestSuiteNameProvider,
    private val reportLinkGenerator: ReportLinkGenerator,
    private val destination: File
) : LegacyFinalizeAction {

    private val estimatedTestRecordSize = 150

    override fun action(
        testRunResult: TestRunResult,
        verdict: LegacyVerdict,
    ) {

        val testStatisticsCounter = TestStatisticsCounterFactory.createLegacy(
            reportedTests = testRunResult.reportedTests,
            failedTestDeterminer = testRunResult.failed,
            notReportedTestsDeterminer = testRunResult.notReported
        )

        val testCountOverall = testStatisticsCounter.overallCount()
        val testCountSuccess = testStatisticsCounter.successCount()
        val testCountFailures = testStatisticsCounter.failureCount()
        val testCountErrors = testStatisticsCounter.notReportedCount()
        val testCountSkipped = testStatisticsCounter.skippedCount()

        require(testCountOverall == testCountSuccess + testCountFailures + testCountSkipped + testCountErrors)

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${testSuiteNameProvider.getName()}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""errors="$testCountErrors" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="${testStatisticsCounter.overallDurationSec()}" """)
            appendLine(">")

            appendLine("<properties/>")

            testRunResult.reportedTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.className}" """)
                append("""name="${test.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="${test.lastAttemptDurationInSeconds}"""")
                appendLine(">")

                if (test.stability is Stability.Flaky) {
                    appendLine("<system-out>")
                    appendEscapedLine(
                        "Flaky test. " +
                            "Success runs: ${(test.stability as Stability.Flaky).successCount} " +
                            "out of ${(test.stability as Stability.Flaky).attemptsCount}"
                    )
                    appendLine("</system-out>")
                }

                when (test.status) {
                    is Status.Skipped -> {
                        appendLine("<skipped/>")
                        if (test.skipReason != null) {
                            appendLine("<system-out>")
                            appendEscapedLine("Тест не запускался: ${test.skipReason}")
                            appendLine("</system-out>")
                        }
                    }
                    is Status.Failure -> {
                        appendLine("<failure>")
                        appendEscapedLine((test.status as Status.Failure).verdict)
                        appendLine(reportLinkGenerator.generateTestLink(TestName(test.className, test.methodName)))
                        appendLine("</failure>")
                    }
                    is Status.Lost -> {
                        appendLine("<error>")
                        appendLine("LOST (no info in report)")
                        appendLine(reportLinkGenerator.generateTestLink(TestName(test.className, test.methodName)))
                        appendLine("</error>")
                    }
                    Status.Success -> { /* do nothing */
                    }
                    Status.Manual -> { /* do nothing */
                    }
                }

                appendLine("</testcase>")
            }

            testRunResult.notReported.lostTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.name.className}" """)
                append("""name="${test.name.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="unknown"""")
                appendLine(">")

                appendLine("<error>")
                appendLine("Not reported ${reportLinkGenerator.generateTestLink(test.name)}")
                appendLine("</error>")

                appendLine("</testcase>")
            }

            appendLine("</testsuite>")
        }

        destination.writeText(xml)
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendLine(StringEscapeUtils.escapeXml10(line))
    }
}
