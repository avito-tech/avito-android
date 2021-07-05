package com.avito.reportviewer

import com.avito.report.ReportLinkGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team
import com.avito.test.model.TestName
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

public interface ReportViewer {

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

    public class Impl(
        host: String,
        private val reportCoordinates: ReportCoordinates,
        private val reportViewerQuery: ReportViewerQuery = ReportViewerQuery()
    ) : ReportViewer, ReportLinkGenerator, TestSuiteNameProvider {

        private val host = host.removeSuffix("/")

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

        override fun generateReportUrl(
            reportCoordinates: ReportCoordinates,
            onlyFailures: Boolean,
            team: Team
        ): HttpUrl {
            val url =
                "$host/report/${reportCoordinates.planSlug}/${reportCoordinates.jobSlug}/${reportCoordinates.runId}" +
                    reportViewerQuery.createQuery(onlyFailures, team)

            return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
        }

        override fun generateReportUrl(
            reportId: String,
            onlyFailures: Boolean,
            team: Team
        ): HttpUrl {
            val url =
                "$host/run/$reportId" +
                    reportViewerQuery.createQuery(onlyFailures, team)

            return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
        }

        override fun generateSingleTestRunUrl(
            reportCoordinates: ReportCoordinates,
            testClass: String,
            testMethod: String
        ): HttpUrl {
            val url =
                "$host/report/${reportCoordinates.planSlug}/${reportCoordinates.jobSlug}/${reportCoordinates.runId}" +
                    reportViewerQuery.createQuery(testClass, testMethod)

            return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
        }

        override fun generateSingleTestRunUrl(testRunId: String): HttpUrl {
            val url = "$host/test/$testRunId"
            return requireNotNull(url.toHttpUrl()) { "Invalid url: $url" }
        }
    }
}
