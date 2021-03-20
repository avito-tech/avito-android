package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.ReportsFetchApi
import com.avito.report.model.SimpleRunTest

public interface ReadReport {

    public fun getTests(): Result<List<SimpleRunTest>>

    public class Id(
        private val reportsFetchApi: ReportsFetchApi,
        private val id: String
    ) : ReadReport {

        override fun getTests(): Result<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForReportId(id)
        }
    }

    public class ReportCoordinates(
        private val reportsFetchApi: ReportsFetchApi,
        private val coordinates: com.avito.report.model.ReportCoordinates
    ) : ReadReport {

        override fun getTests(): Result<List<SimpleRunTest>> {
            return reportsFetchApi.getTestsForRunId(coordinates)
        }
    }
}
