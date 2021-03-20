package com.avito.report

import com.avito.android.Result
import com.avito.report.model.CreateResult
import com.avito.report.model.ReportCoordinates
import com.google.gson.JsonElement

interface ReportsApi : ReportsAddApi, ReportsFetchApi {

    /**
     * Run.Create
     */
    fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        testHost: String,
        gitBranch: String,
        gitCommit: String,
        tmsBranch: String = "master"
    ): CreateResult

    /**
     * Run.SetFinished
     */
    fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit>

    /**
     * RunTest.AddConclusion - Successful
     */
    fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit>

    /**
     * RunTest.AddConclusion - Failed
     */
    fun markAsFailed(testRunId: String, author: String, comment: String): Result<Unit>

    /**
     * Run.PushPreparedData
     * Сохранение дополнительной информации в отчёте
     * @param reportId идентификатор запуска в отчетах (run_id)
     * @param analyzerKey имя анализатора
     * @param preparedData произвольные данные анализатора
     */
    fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Result<Unit>
}
