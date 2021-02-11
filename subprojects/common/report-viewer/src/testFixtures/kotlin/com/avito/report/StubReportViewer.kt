package com.avito.report

import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class StubReportViewer(
    private val reportViewerUrl: String = "http://localhost/",
    var byReportCoordinatesUrl: HttpUrl = reportViewerUrl.toHttpUrlOrNull()!!,
    var byReportIdUrl: HttpUrl = reportViewerUrl.toHttpUrlOrNull()!!,
    var byTestIdUrl: HttpUrl = reportViewerUrl.toHttpUrlOrNull()!!
) : ReportViewer {

    override fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean,
        team: Team
    ): HttpUrl = byReportCoordinatesUrl

    override fun generateReportUrl(reportId: String, onlyFailures: Boolean, team: Team): HttpUrl = byReportIdUrl

    override fun generateSingleTestRunUrl(testRunId: String): HttpUrl = byTestIdUrl
}
