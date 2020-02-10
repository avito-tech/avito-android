package com.avito.report

import com.avito.logger.Logger
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.getHttpClient
import com.avito.report.model.HistoryListResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.funktionale.tries.Try

// Не используется сейчас нигде. Понадобится в задаче PER-783
interface ReportsHistoryApi {

    fun getTestHistory(testName: String, branch: String, depth: Int): Try<HistoryListResult>

    companion object {
        fun create(
            host: String,
            fallbackUrl: String,
            logger: Logger,
            verboseHttp: Boolean = false,
            gson: Gson = GsonBuilder().create()
        ): ReportsHistoryApi {
            return ReportsHistoryApiImpl(
                requestProvider = JsonRpcRequestProvider(
                    host = host,
                    httpClient = getHttpClient(
                        verbose = verboseHttp,
                        fallbackUrl = fallbackUrl,
                        logger = logger
                    ),
                    gson = gson
                )
            )
        }
    }
}
