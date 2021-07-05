package com.avito.reportviewer

import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team
import com.avito.test.model.TestName
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

public class StubReportViewerLinksGenerator(
    private val reportViewerUrl: String = "http://localhost/",
    public var byReportCoordinatesUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    public var byReportIdUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    public var byTestIdUrl: HttpUrl = reportViewerUrl.toHttpUrl(),
    public var byTestName: HttpUrl = reportViewerUrl.toHttpUrl()
) : ReportViewerLinksGenerator {

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

    override fun generateReportLink(filterOnlyFailures: Boolean, team: String?): String {
        return byReportCoordinatesUrl.toString()
    }

    override fun generateTestLink(testName: TestName): String {
        return byTestName.toString()
    }
}
