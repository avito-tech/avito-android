package com.avito.reportviewer

import com.avito.report.ReportLinksGenerator
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team
import okhttp3.HttpUrl

public interface ReportViewerLinksGenerator : ReportLinksGenerator {

    public fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean = true,
        team: Team = Team.UNDEFINED
    ): HttpUrl

    public fun generateReportUrl(
        reportId: String,
        onlyFailures: Boolean = true,
        team: Team = Team.UNDEFINED
    ): HttpUrl

    public fun generateSingleTestRunUrl(testRunId: String): HttpUrl

    /**
     * Create url with applied [test] String to the `search field`
     *
     * Use when you don't have a testRunId
     */
    public fun generateSingleTestRunUrl(
        reportCoordinates: ReportCoordinates,
        testClass: String,
        testMethod: String
    ): HttpUrl
}
