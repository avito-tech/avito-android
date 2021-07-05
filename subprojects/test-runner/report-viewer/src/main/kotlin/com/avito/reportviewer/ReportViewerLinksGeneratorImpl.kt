package com.avito.reportviewer

import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team
import com.avito.test.model.TestName
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

public class ReportViewerLinksGeneratorImpl(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val reportViewerQuery: ReportViewerQuery = ReportViewerQuery()
) : ReportLinksGenerator, TestSuiteNameProvider {

    private val host = reportViewerUrl.removeSuffix("/")

    override fun getName(): String = "${reportCoordinates.planSlug}_${reportCoordinates.jobSlug}"

    override fun generateReportLink(filterOnlyFailures: Boolean, team: String?): String {
        return generateReportUrl(
            reportCoordinates,
            onlyFailures = filterOnlyFailures,
            team = team?.let { Team(it) } ?: Team.UNDEFINED
        ).toString()
    }

    override fun generateTestLink(testName: TestName): String {
        return generateSingleTestRunUrl(
            reportCoordinates = reportCoordinates,
            testClass = testName.className,
            testMethod = testName.methodName
        ).toString()
    }

    private fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean,
        team: Team
    ): HttpUrl {
        val url =
            "$host/report/${reportCoordinates.planSlug}/${reportCoordinates.jobSlug}/${reportCoordinates.runId}" +
                reportViewerQuery.createQuery(onlyFailures, team)

        return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
    }

    private fun generateSingleTestRunUrl(
        reportCoordinates: ReportCoordinates,
        testClass: String,
        testMethod: String
    ): HttpUrl {
        val url =
            "$host/report/${reportCoordinates.planSlug}/${reportCoordinates.jobSlug}/${reportCoordinates.runId}" +
                reportViewerQuery.createQuery(testClass, testMethod)

        return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
    }
}
