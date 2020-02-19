package com.avito.report

import com.avito.logger.Logger
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.model.ConclusionStatus
import com.avito.report.internal.model.CreateResponse
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.internal.model.RpcResult
import com.avito.report.model.CreateResult
import com.avito.report.model.CreateResult.AlreadyCreated
import com.avito.report.model.CreateResult.Created
import com.avito.report.model.CreateResult.Failed
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.funktionale.tries.Try

internal class ReportsApiImpl(
    private val gson: Gson,
    private val logger: Logger,
    private val requestProvider: JsonRpcRequestProvider
) : ReportsApi,
    ReportsAddApi by ReportsAddApiImpl(requestProvider),
    ReportsFetchApi by ReportsFetchApiImpl(requestProvider, gson, logger) {

    override fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        apiUrl: String,
        gitBranch: String,
        gitCommit: String,
        tmsBranch: String
    ): CreateResult {
        return try {
            val result = requestProvider.jsonRpcRequest<RpcResult<CreateResponse>>(
                RfcRpcRequest(
                    method = "Run.Create",
                    params = mapOf(
                        "plan_slug" to reportCoordinates.planSlug,
                        "job_slug" to reportCoordinates.jobSlug,
                        "run_id" to reportCoordinates.runId,
                        "report_data" to mapOf(
                            "build" to buildId,
                            "testHost" to apiUrl,
                            "testsBranch" to tmsBranch,
                            "appBranch" to gitBranch,

                            /**
                             * Секретный массив, в который добавляются параметры в таком формате, чтобы потом по ним фильтровать в бекенде
                             * истории
                             */
                            "tags" to listOf(
                                "buildBranch:$gitBranch",
                                "buildCommit:$gitCommit"
                            )
                        )
                    )
                )
            )
            Created(result.result.id)
        } catch (e: Throwable) {
            val isDuplicateKeyError = e.message?.contains("duplicate key error collection") ?: false
            if (isDuplicateKeyError) {
                AlreadyCreated
            } else {
                Failed(e)
            }
        }
    }

    override fun setFinished(reportCoordinates: ReportCoordinates): Try<Unit> {
        return when (val getReportResult = getReport(reportCoordinates)) {
            is GetReportResult.Found -> Try {
                requestProvider.jsonRpcRequest<Unit>(
                    RfcRpcRequest(
                        method = "Run.SetFinished",
                        params = mapOf(
                            "id" to getReportResult.report.id
                        )
                    )
                )
            }
            GetReportResult.NotFound -> Try.Failure(Exception("Report not found $reportCoordinates"))
            is GetReportResult.Error -> Try.Failure(getReportResult.exception)
        }
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.OK,
            comment
        )
    }

    override fun markAsFailed(testRunId: String, author: String, comment: String): Try<Unit> {
        return addConclusion(
            testRunId,
            author,
            ConclusionStatus.FAIL,
            comment
        )
    }

    override fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Try<Unit> {
        return Try {
            requestProvider.jsonRpcRequest<Unit>(
                RfcRpcRequest(
                    method = "Run.PushPreparedData", params = mapOf(
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
    private fun addConclusion(id: String, author: String, status: ConclusionStatus, comment: String): Try<Unit> {
        return Try {
            requestProvider.jsonRpcRequest<Unit>(
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
