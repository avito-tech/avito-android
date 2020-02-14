package com.avito.instrumentation.report

import com.avito.instrumentation.TestRunResult
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Stability
import com.avito.report.model.Status
import org.apache.commons.text.StringEscapeUtils
import java.io.File

class JUnitReportWriter(private val reportViewer: ReportViewer) {

    private val estimatedTestRecordSize = 150

    fun write(
        reportCoordinates: ReportCoordinates,
        testRunResult: TestRunResult,
        destination: File
    ) {
        val testCountOverall = testRunResult.testCount()
        val testCountSuccess = testRunResult.successCount()
        val testCountFailures = testRunResult.failureCount()
        val testCountErrors = testRunResult.notReportedCount()
        val testCountSkipped = testRunResult.skippedCount()

        require(testCountOverall == testCountSuccess + testCountFailures + testCountSkipped + testCountErrors)

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendln("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${reportCoordinates.planSlug}_${reportCoordinates.jobSlug}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""errors="$testCountErrors" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="${testRunResult.testsDuration}" """)
            appendln(">")

            appendln("<properties/>")

            testRunResult.reportedTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.className}" """)
                append("""name="${test.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="${test.lastAttemptDurationInSeconds}"""")
                appendln(">")

                if (test.stability is Stability.Flaky) {
                    appendln("<system-out>")
                    appendEscapedLine("Flaky test. Success runs: ${(test.stability as Stability.Flaky).successCount} out of ${(test.stability as Stability.Flaky).attemptsCount}")
                    appendln("</system-out>")
                }

                when {
                    test.status is Status.Skipped -> {
                        appendln("<skipped/>")
                        if (test.skipReason != null) {
                            appendln("<system-out>")
                            appendEscapedLine("Тест не запускался: ${test.skipReason}")
                            appendln("</system-out>")
                        }
                    }
                    test.status is Status.Failure -> {
                        appendln("<failure>")
                        appendEscapedLine((test.status as Status.Failure).verdict)
                        appendln("Report Viewer: ${reportViewer.generateSingleTestRunUrl(test.id)}")
                        appendln("</failure>")
                    }
                    test.status is Status.Lost -> {
                        appendln("<error>")
                        appendln("LOST (no info in report)")
                        appendln("Report Viewer: ${reportViewer.generateSingleTestRunUrl(test.id)}")
                        appendln("</error>")
                    }
                }

                appendln("</testcase>")
            }

            testRunResult.notReported.lostTests.forEach { test ->
                append("<testcase ")
                append("""classname="${test.name.className}" """)
                append("""name="${test.name.methodName}" """)
                append("""caseId="${test.testCaseId}" """)
                append("""time="unknown"""")
                appendln(">")

                appendln("<error>")
                appendln("Not reported")
                appendln("</error>")

                appendln("</testcase>")
            }


            appendln("</testsuite>")
        }

        destination.writeText(xml)
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendln(StringEscapeUtils.escapeXml10(line))
    }
}
