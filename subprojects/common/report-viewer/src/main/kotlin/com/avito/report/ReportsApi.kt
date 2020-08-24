package com.avito.report

import com.avito.logger.Logger
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.getHttpClient
import com.avito.report.model.CreateResult
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.ReportCoordinates
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.funktionale.tries.Try

interface ReportsApi : ReportsAddApi, ReportsFetchApi {

    /**
     * Run.Create
     */
    fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        apiUrl: String,
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

    companion object {
        /**
         * Для доступа в тестах
         */
        val gson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(EntryTypeAdapterFactory())
            .create()

        fun create(
            host: String,
            fallbackUrl: String,
            logger: Logger,
            readTimeout: Long = 60,
            writeTimeout: Long = 10,
            verboseHttp: Boolean = false
        ): ReportsApi {

            return ReportsApiImpl(
                gson = gson,
                logger = logger,
                requestProvider = JsonRpcRequestProvider(
                    host = host,
                    httpClient = getHttpClient(
                        verbose = verboseHttp,
                        fallbackUrl = fallbackUrl,
                        logger = logger,
                        readTimeout = readTimeout,
                        writeTimeout = writeTimeout
                    ),
                    gson = gson
                )
            )
        }
    }
}
