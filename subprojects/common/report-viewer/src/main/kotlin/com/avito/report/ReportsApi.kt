package com.avito.report

import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
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
            loggerFactory: LoggerFactory,
            readTimeout: Long = 60,
            writeTimeout: Long = 10,
            verboseHttp: Boolean = false
        ): ReportsApi {

            val logger = loggerFactory.create<ReportsApi>()

            return ReportsApiImpl(
                loggerFactory = loggerFactory,
                requestProvider = JsonRpcRequestProvider(
                    host = host,
                    httpClient = getHttpClient(
                        verbose = verboseHttp,
                        logger = logger,
                        readTimeoutSec = readTimeout,
                        writeTimeoutSec = writeTimeout,
                        retryInterceptor = RetryInterceptor(
                            logger = logger,
                            allowedMethods = listOf("POST")
                        )
                    ),
                    gson = gson
                )
            )
        }
    }
}
