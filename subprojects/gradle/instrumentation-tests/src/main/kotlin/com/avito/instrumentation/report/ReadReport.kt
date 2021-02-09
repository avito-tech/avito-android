package com.avito.instrumentation.report

import com.avito.report.ReportsFetchApi
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

public interface ReadReport {

    public fun getTests(): Try<List<SimpleRunTest>>

    public class Id(
        private val reportsFetchApi: ReportsFetchApi,
        private val id: String
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForReportId(id)
        }
    }

    public class ReportCoordinates(
        private val reportsFetchApi: ReportsFetchApi,
        private val coordinates: com.avito.report.model.ReportCoordinates
    ) : ReadReport {

        override fun getTests(): Try<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForRunId(coordinates)
        }
    }
}
