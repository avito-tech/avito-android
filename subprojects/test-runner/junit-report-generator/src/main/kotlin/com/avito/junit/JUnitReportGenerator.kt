package com.avito.junit

import org.apache.commons.text.StringEscapeUtils

public class JUnitReportGenerator(
    private val testSuiteConfig: JunitTestSuiteConfig,
) {

    private val estimatedTestRecordSize = 150

    public fun generateReport(tests: List<JunitReportTestCase>): String {
        val overallDurationSec: Long = tests.sumOf {
            when (val duration = it.duration) {
                is Duration.Executed -> duration.value
                Duration.Unknown -> 0
            }
        }
        val testCountOverall = tests.size
        val testCountFailures = tests.count { it is JunitReportTestCase.Failed }
        val testCountErrors = tests.count { it is JunitReportTestCase.Error }
        val testCountSkipped = tests.count { it is JunitReportTestCase.Skipped }

        val xml = buildString(testCountOverall * estimatedTestRecordSize) {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")

            append("<testsuite ")
            append("""name="${testSuiteConfig.testSuiteName}" """)
            append("""tests="$testCountOverall" """)
            append("""failures="$testCountFailures" """)
            append("""errors="$testCountErrors" """)
            append("""skipped="$testCountSkipped" """)
            append("""time="$overallDurationSec" """)
            appendLine(">")

            appendLine("<properties/>")

            tests.forEach { test -> appendTest(test) }

            appendLine("</testsuite>")
        }

        return xml
    }

    private fun StringBuilder.appendTest(test: JunitReportTestCase) {
        val duration = when (val duration = test.duration) {
            is Duration.Executed -> duration.value
            Duration.Unknown -> "unknown"
        }
        append("<testcase ")
        append("""classname="${test.name.className}" """)
        append("""name="${test.name.methodName}" """)
        append("""caseId="${test.caseId}" """)
        append("""time="$duration"""")

        appendLine(">")

        when (test) {
            is JunitReportTestCase.Skipped -> {
                appendLine("<skipped/>")
                appendLine("<system-out>")
                appendEscapedLine("Тест не запускался: ${test.skipReason}")
                appendLine("</system-out>")
            }

            is JunitReportTestCase.Failed -> {
                appendLine("<failure>")
                appendEscapedLine(test.error)
                appendLine(testSuiteConfig.getTestReportLink(test.name))
                appendLine("</failure>")
            }

            is JunitReportTestCase.Error -> {
                appendLine("<error>")
                appendLine(test.error)
                appendLine(testSuiteConfig.getTestReportLink(test.name))
                appendLine("</error>")
            }

            is JunitReportTestCase.Success -> {
                // empty
            }
        }

        appendLine("</testcase>")
    }

    private fun StringBuilder.appendEscapedLine(line: String) {
        appendLine(StringEscapeUtils.escapeXml10(line))
    }
}
