package com.avito.report

import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class StubReportViewer(
    private val reportViewerUrl: String = "http://localhost/",
    var byReportCoordinatesUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    var byReportIdUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    var byTestIdUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    var byTestName: HttpUrl = reportViewerUrl.toHttpUrl()
) : ReportViewer {

    override fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean,
        team: Team
    ): HttpUrl = byReportCoordinatesUrl

    override fun generateReportUrl(reportId: String, onlyFailures: Boolean, team: Team): HttpUrl = byReportIdUrl

    override fun generateSingleTestRunUrl(testRunId: String): HttpUrl = byTestIdUrl

    override fun generateSingleTestRunUrl(
        reportCoordinates: ReportCoordinates,
        testClass: String,
        testMethod: String
    ): HttpUrl = byTestName
}
