package com.avito.report

import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

interface ReportViewer {

    fun generateReportUrl(
        reportCoordinates: ReportCoordinates,
        onlyFailures: Boolean = true,
        team: Team = Team.UNDEFINED
    ): HttpUrl

    fun generateReportUrl(
        reportId: String,
        onlyFailures: Boolean = true,
        team: Team = Team.UNDEFINED
    ): HttpUrl

    fun generateSingleTestRunUrl(testRunId: String): HttpUrl

    /**
     * Create url with applied [test] String to the `search field`
     *
     * Use when you don't have a testRunId
     */
    fun generateSingleTestRunUrl(
        reportCoordinates: ReportCoordinates,
        testClass: String,
        testMethod: String
    ): HttpUrl

    class Impl(
        host: String,
        private val reportViewerQuery: ReportViewerQuery = ReportViewerQuery()
    ) : ReportViewer {

        private val host = host.removeSuffix("/")

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
