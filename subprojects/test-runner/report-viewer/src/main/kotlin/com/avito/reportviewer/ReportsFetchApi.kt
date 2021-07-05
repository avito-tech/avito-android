package com.avito.reportviewer

import com.avito.android.Result
import com.avito.reportviewer.model.Report
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest

public interface ReportsFetchApi {

    /**
     * Run.GetByParams
     *
     * todo remove Report from public api
     * todo remove ReportCoordinates from public api
     */
    public fun getReport(reportCoordinates: ReportCoordinates): Result<Report>

    /**
     * RunTest.List
     * получение краткого списка результатов тестов по запуску
     */
    public fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>>
}
