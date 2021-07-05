package com.avito.report

import com.avito.test.model.TestName

public class NoOpReportLinkGenerator(
    private val reportLink: String = "",
    private val testLink: String = ""
) : ReportLinkGenerator {

    override fun generateReportLink(
        filterOnlyFailures: Boolean,
        team: String?
    ): String = reportLink

    override fun generateTestLink(testName: TestName): String = testLink
}
