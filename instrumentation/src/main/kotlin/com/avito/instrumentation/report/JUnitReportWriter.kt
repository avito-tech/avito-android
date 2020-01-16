package com.avito.instrumentation.report

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
        testData: List<SimpleRunTest>,
        destination: File
    ) {
        val testCountOverall = testData.size
        val testCountSuccess = testData.filter { it.status is Status.Success || it.status is Status.Manual }.size
        val testCountFailures = testData.filter { it.status is Status.Failure || it.status is Status.Lost }.size
        val testCountSkipped = testData.filter { it.status is Status.Skipped }.size

        require(testCountOverall == testCountSuccess + testCountFailures + testCountSkipped)

        val testsDuration: Int = testData.sumBy { it.lastAttemptDurationInSeconds }

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendln("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${reportCoordinates.planSlug}_${reportCoordinates.jobSlug}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="$testsDuration" """)
            appendln(">")

            appendln("<properties/>")

            testData.forEach { test ->
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
                        appendln("<failure>")
                        appendln("LOST (no info in report)")
                        appendln("Report Viewer: ${reportViewer.generateSingleTestRunUrl(test.id)}")
                        appendln("</failure>")
                    }
                }

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
