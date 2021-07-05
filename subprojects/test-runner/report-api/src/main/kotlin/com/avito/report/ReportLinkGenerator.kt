package com.avito.report

import com.avito.test.model.TestName

public interface ReportLinkGenerator {

    public fun generateReportLink(
        filterOnlyFailures: Boolean = false,
        team: String? = null
    ): String

    public fun generateTestLink(testName: TestName): String
}
