package com.avito.report

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.report.internal.JsonRpcClient
import com.avito.report.internal.model.ConclusionStatus
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.model.ReportCoordinates
import com.google.gson.JsonElement

internal class ReportsApiImpl(
    private val loggerFactory: LoggerFactory,
    private val client: JsonRpcClient
) : ReportsApi,
    ReportsAddApi by ReportsAddApiImpl(client),
    ReportsFetchApi by ReportsFetchApiImpl(client, loggerFactory) {

    override fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit> {
        return getReport(reportCoordinates).fold(
            onSuccess = { report ->
                Result.tryCatch {
                    client.jsonRpcRequest<Unit>(
                        RfcRpcRequest(
                            method = "Run.SetFinished",
                            params = mapOf(
                                "id" to report.id
                            )
                        )
                    )
                }
            },
            onFailure = { throwable ->
                Result.Failure(throwable)
            }
        )
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.OK,
            comment
        )
    }

    override fun markAsFailed(testRunId: String, author: String, comment: String): Result<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.FAIL,
            comment
        )
    }

    override fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Result<Unit> {
        return Result.tryCatch {
            client.jsonRpcRequest<Unit>(
                RfcRpcRequest(
                    method = "Run.PushPreparedData",
                    params = mapOf(
                        "id" to reportId,
                        "analyzer_key" to analyzerKey,
                        "prepared_data" to preparedData
                    )
                )
            )
        }
    }

    /**
     * RunTest.AddConclusion
     * @param status 'ok','fail','irrelevant' ('irrelevant' допустим только для тестов в статусе 32 - testcase)
     */
    private fun addConclusion(id: String, author: String, status: ConclusionStatus, comment: String): Result<Unit> {
        return Result.tryCatch {
            client.jsonRpcRequest<Unit>(
                RfcRpcRequest(
                    method = "RunTest.AddConclusion",
                    params = mapOf(
                        "id" to id,
                        "author" to author,
                        "status" to status,
                        "comment" to comment
                    )
                )
            )
        }
    }
}
