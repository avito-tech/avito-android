package com.avito.instrumentation.report

import com.avito.report.ReportsFetchApi
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface ReadReport {

    fun getTests(): Try<List<SimpleRunTest>>

    class Id(
        private val reportsFetchApi: ReportsFetchApi,
        private val id: String
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForReportId(id)
        }
    }

    class ReportCoordinates(
        private val reportsFetchApi: ReportsFetchApi,
        private val coordinates: com.avito.report.model.ReportCoordinates
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForRunId(coordinates)
        }
    }
}
