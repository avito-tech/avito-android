package com.avito.report

import com.avito.android.Result
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest

interface ReportsFetchApi {

    /**
     * Run.List
     */
    fun getReportsList(planSlug: String, jobSlug: String, pageNumber: Int): Result<List<Report>>

    /**
     * Run.GetByParams
     */
    fun getReport(reportCoordinates: ReportCoordinates): Result<Report>

    /**
     * RunTest.List
     * получение краткого списка результатов тестов по запуску
     */
    fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>>

    fun getTestsForReportId(reportId: String): Result<List<SimpleRunTest>>

    fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Result<CrossDeviceSuite>
}
