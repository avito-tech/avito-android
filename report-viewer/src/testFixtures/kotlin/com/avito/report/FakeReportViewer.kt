package com.avito.report

import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import okhttp3.HttpUrl

class FakeReportViewer(
    var byReportCoordinatesUrl: HttpUrl = HttpUrl.parse("http://localhost/")!!,
    var byReportIdUrl: HttpUrl = HttpUrl.parse("http://localhost/")!!,
    var byTestIdUrl: HttpUrl = HttpUrl.parse("http://localhost/")!!
) : ReportViewer {

    override fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean,
        team: Team
    ): HttpUrl = byReportCoordinatesUrl

    override fun generateReportUrl(reportId: String, onlyFailures: Boolean, team: Team): HttpUrl = byReportIdUrl

    override fun generateSingleTestRunUrl(testRunId: String): HttpUrl = byTestIdUrl
}
