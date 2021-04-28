package com.avito.report

import com.avito.report.model.TestName

public interface ReportLinkGenerator {

    public fun generateReportLink(
        filterOnlyFailtures: Boolean = false,
        team: String? = null
    ): String

    public fun generateTestLink(testName: TestName): String
}
