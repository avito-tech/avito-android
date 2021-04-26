package com.avito.report

import com.avito.report.model.TestName

public interface ReportLinkGenerator {

    public fun generateReportLink(
        filterOnlyFailtures: Boolean = false,
        team: String? = null
    ): String

    public fun generateTestLink(testName: TestName): String

    public class NoOp(
        private val reportLink: String = "",
        private val testLink: String = ""
    ) : ReportLinkGenerator {

        override fun generateReportLink(
            filterOnlyFailtures: Boolean,
            team: String?
        ): String = reportLink

        override fun generateTestLink(testName: TestName): String = testLink
    }
}
