package com.avito.report

import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface ReportsFetchApi {

    /**
     * Run.List
     */
    fun getReportsList(planSlug: String, jobSlug: String, pageNumber: Int): Try<List<Report>>

    /**
     * Run.GetByParams
     */
    fun getReport(reportCoordinates: ReportCoordinates): GetReportResult

    /**
     * RunTest.List
     * получение краткого списка результатов тестов по запуску
     */
    fun getTestsForRunId(reportCoordinates: ReportCoordinates): Try<List<SimpleRunTest>>

    fun getTestsForReportId(reportId: String): Try<List<SimpleRunTest>>

    fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Try<CrossDeviceSuite>
}
