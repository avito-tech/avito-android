package com.avito.instrumentation.report

import com.avito.report.ReportsFetchApi
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface ReadReport {

    fun getTests(): Try<List<SimpleRunTest>>

    interface Factory {
        fun create(coordinates: com.avito.report.model.ReportCoordinates): ReadReport
        fun create(id: String): ReadReport

        companion object {
            fun create(reportsFetchApi: ReportsFetchApi): Factory {
                return Impl(reportsFetchApi)
            }
        }

        private class Impl(
            private val reportsFetchApi: ReportsFetchApi
        ) : Factory {

            override fun create(coordinates: com.avito.report.model.ReportCoordinates): ReadReport {
                return ReadReport.ReportCoordinates(
                    reportsFetchApi = reportsFetchApi,
                    coordinates = coordinates
                )
            }

            override fun create(id: String): ReadReport {
                return ReadReport.Id(
                    reportsFetchApi = reportsFetchApi,
                    id = id
                )
            }

        }
    }

    private class Id(
        private val reportsFetchApi: ReportsFetchApi,
        private val id: String
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForReportId(id)
        }

    }

    private class ReportCoordinates(
        private val reportsFetchApi: ReportsFetchApi,
        private val coordinates: com.avito.report.model.ReportCoordinates
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForRunId(coordinates)
        }

    }
}