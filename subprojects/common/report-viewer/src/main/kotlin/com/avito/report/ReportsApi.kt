package com.avito.report

import com.avito.report.model.CreateResult
import com.avito.report.model.ReportCoordinates
import com.google.gson.JsonElement
import org.funktionale.tries.Try

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
    fun setFinished(reportCoordinates: ReportCoordinates): Try<Unit>

    /**
     * RunTest.AddConclusion - Successful
     */
    fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit>

    /**
     * RunTest.AddConclusion - Failed
     */
    fun markAsFailed(testRunId: String, author: String, comment: String): Try<Unit>

    /**
     * Run.PushPreparedData
     * Сохранение дополнительной информации в отчёте
     * @param reportId идентификатор запуска в отчетах (run_id)
     * @param analyzerKey имя анализатора
     * @param preparedData произвольные данные анализатора
     */
    fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Try<Unit>
}
