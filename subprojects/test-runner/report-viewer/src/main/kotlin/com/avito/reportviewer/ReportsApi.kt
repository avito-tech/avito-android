package com.avito.reportviewer

import com.avito.android.Result
import com.avito.reportviewer.model.ReportCoordinates
import com.google.gson.JsonElement

public interface ReportsApi : ReportsAddApi, ReportsFetchApi {

    /**
     * Run.SetFinished
     */
    public fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit>

    /**
     * RunTest.AddConclusion - Successful
     */
    public fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit>

    /**
     * RunTest.AddConclusion - Failed
     */
    public fun markAsFailed(testRunId: String, author: String, comment: String): Result<Unit>

    /**
     * Run.PushPreparedData
     * Сохранение дополнительной информации в отчёте
     * @param reportId идентификатор запуска в отчетах (run_id)
     * @param analyzerKey имя анализатора
     * @param preparedData произвольные данные анализатора
     */
    public fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Result<Unit>
}
