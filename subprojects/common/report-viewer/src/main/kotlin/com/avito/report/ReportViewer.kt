package com.avito.report

import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import okhttp3.HttpUrl

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

    class Impl(
        private val host: String,
        private val reportViewerQuery: ReportViewerQuery = ReportViewerQuery()
    ) : ReportViewer {

        override fun generateReportUrl(
            reportCoordinates: ReportCoordinates,
            onlyFailures: Boolean,
            team: Team
        ): HttpUrl {
            val url =
                "$host/report/${reportCoordinates.planSlug}/${reportCoordinates.jobSlug}/${reportCoordinates.runId}" +
                    reportViewerQuery.createQuery(onlyFailures, team)

            return requireNotNull(HttpUrl.parse(url)) { "Invalid url: $url" }
        }

        override fun generateReportUrl(
            reportId: String,
            onlyFailures: Boolean,
            team: Team
        ): HttpUrl {
            val url =
                "$host/run/$reportId" +
                    reportViewerQuery.createQuery(onlyFailures, team)

            return requireNotNull(HttpUrl.parse(url)) { "Invalid url: $url" }
        }

        override fun generateSingleTestRunUrl(testRunId: String): HttpUrl {
            val url = "$host/test/$testRunId"
            return requireNotNull(HttpUrl.parse(url)) { "Invalid url: $url" }
        }
    }
}
