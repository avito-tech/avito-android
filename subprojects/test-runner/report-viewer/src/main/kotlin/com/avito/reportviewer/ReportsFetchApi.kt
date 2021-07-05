package com.avito.reportviewer

import com.avito.android.Result
import com.avito.reportviewer.model.CrossDeviceSuite
import com.avito.reportviewer.model.Report
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest

public interface ReportsFetchApi {

    /**
     * Run.GetByParams
     */
    public fun getReport(reportCoordinates: ReportCoordinates): Result<Report>

    /**
     * RunTest.List
     * получение краткого списка результатов тестов по запуску
     */
    public fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>>

    public fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Result<CrossDeviceSuite>
}
