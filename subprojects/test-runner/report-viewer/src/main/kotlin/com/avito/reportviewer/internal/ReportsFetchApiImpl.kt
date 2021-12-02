package com.avito.reportviewer.internal

import com.avito.android.Result
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.jsonrpc.JsonRpcClient
import com.avito.jsonrpc.RfcRpcRequest
import com.avito.jsonrpc.RpcResult
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.Stability
import com.avito.report.model.TestStatus
import com.avito.reportviewer.ReportsFetchApi
import com.avito.reportviewer.internal.model.ConclusionStatus
import com.avito.reportviewer.internal.model.GetReportResult
import com.avito.reportviewer.internal.model.ListResult
import com.avito.reportviewer.internal.model.ReportViewerStatus
import com.avito.reportviewer.model.Report
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest
import com.avito.test.model.TestName

internal class ReportsFetchApiImpl(
    private val client: JsonRpcClient,
) : ReportsFetchApi {

    override fun getReport(reportCoordinates: ReportCoordinates): Result<Report> {
        return when (val result = getReportInternal(reportCoordinates)) {
            is GetReportResult.Error -> Result.Failure(result.exception)
            is GetReportResult.Found -> Result.Success(result.report)
            is GetReportResult.NotFound -> Result.Failure(
                Exception("Report not found $reportCoordinates", result.exception)
            )
        }
    }

    override fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>> {
        return when (val result = getReportInternal(reportCoordinates)) {
            is GetReportResult.Found -> getTestData(result.report.id)
            is GetReportResult.NotFound -> Result.Failure(
                Exception("Report not found $reportCoordinates", result.exception)
            )
            is GetReportResult.Error -> Result.Failure(result.exception)
        }
    }

    private fun getReportInternal(reportCoordinates: ReportCoordinates): GetReportResult {
        return Result.tryCatch {
            client.jsonRpcRequest<RpcResult<Report>>(
                RfcRpcRequest(
                    method = "Run.GetByParams",
                    params = mapOf(
                        "plan_slug" to reportCoordinates.planSlug,
                        "job_slug" to reportCoordinates.jobSlug,
                        "run_id" to reportCoordinates.runId
                    )
                )
            ).result
        }.fold(
            { report -> GetReportResult.Found(report) },
            { exception ->
                val isNotFoundError = exception.message?.contains("\"data\":\"not found\"") ?: false
                if (isNotFoundError) {
                    GetReportResult.NotFound(exception)
                } else {
                    GetReportResult.Error(exception)
                }
            }
        )
    }

    private fun getTestData(reportId: String): Result<List<SimpleRunTest>> {
        return Result.tryCatch {
            client.jsonRpcRequest<RpcResult<List<ListResult>?>>(
                RfcRpcRequest(
                    method = "RunTest.List",
                    params = mapOf("run_id" to reportId)
                )
            ).result?.map { listResult ->
                val testName = TestName(listResult.className, listResult.methodName)
                SimpleRunTest(
                    id = listResult.id,
                    reportId = reportId,
                    deviceName = requireNotNull(listResult.environment) {
                        "deviceName(environment) is null for test $testName, that's illegal!"
                    },
                    testCaseId = getTestCaseId(listResult),
                    name = testName,
                    status = deserializeStatus(listResult),
                    stability = determineStability(listResult),
                    groupList = listResult.groupList ?: emptyList(),
                    startTime = listResult.startTime ?: 0,
                    endTime = listResult.endTime ?: 0,
                    buildId = listResult.preparedData?.lastOrNull()?.tcBuild,
                    skipReason = listResult.preparedData?.lastOrNull()?.skipReason,
                    isFinished = listResult.isFinished ?: false,
                    lastAttemptDurationInSeconds = listResult.preparedData?.lastOrNull()?.runDuration
                        ?: -1,
                    externalId = listResult.preparedData?.lastOrNull()?.externalId,
                    description = getDescription(listResult),
                    dataSetNumber = getDataSetNumber(listResult),
                    features = listResult.preparedData?.lastOrNull()?.features ?: emptyList(),
                    tagIds = listResult.preparedData?.lastOrNull()?.tagId ?: emptyList(),
                    featureIds = listResult.preparedData?.lastOrNull()?.featureIds ?: emptyList(),
                    priority = getPriority(listResult),
                    behavior = getBehavior(listResult),
                    kind = listResult.kind?.let { Kind.fromTmsId(it) } ?: Kind.UNKNOWN,
                    flakiness = getFlakiness(listResult)
                )
            } ?: emptyList()
        }
    }

    private fun deserializeStatus(reportModel: ListResult): TestStatus {
        return when (reportModel.status) {
            ReportViewerStatus.OK -> TestStatus.Success
            ReportViewerStatus.FAILURE, ReportViewerStatus.ERROR -> {
                if (reportModel.lastConclusion == ConclusionStatus.OK) {
                    TestStatus.Success
                } else {
                    val verdict = reportModel.preparedData?.lastOrNull()?.verdict
                    if (verdict.isNullOrBlank()) {
                        // todo fallback
                        TestStatus.Failure("Can't get verdict")
                    } else {
                        TestStatus.Failure(verdict)
                    }
                }
            }
            ReportViewerStatus.OTHER, ReportViewerStatus.PANIC, ReportViewerStatus.LOST, null -> TestStatus.Lost
            ReportViewerStatus.MANUAL -> TestStatus.Manual
            ReportViewerStatus.SKIP -> TestStatus.Skipped("test ignored") // todo нужен более подробный reason
        }
    }

    private fun determineStability(reportModel: ListResult): Stability {
        return when {
            reportModel.attemptsCount == null || reportModel.successCount == null -> Stability.Stable(0, 0)
            reportModel.attemptsCount < 1 -> {
                // на самом деле не совсем, репортим эту ситуацию как невероятную
                Stability.Failing(reportModel.attemptsCount)
            }
            reportModel.successCount > reportModel.attemptsCount -> {
                // на самом деле не совсем, репортим эту ситуацию как невероятную
                Stability.Stable(
                    reportModel.attemptsCount,
                    reportModel.successCount
                )
            }
            reportModel.successCount == 0 -> Stability.Failing(reportModel.attemptsCount)
            reportModel.successCount == reportModel.attemptsCount -> Stability.Stable(
                reportModel.attemptsCount,
                reportModel.successCount
            )
            // FIXME тут может быть ошибка, т.к. attempt может быть skipped или какой-то другой не-success статус
            reportModel.successCount < reportModel.attemptsCount -> Stability.Flaky(
                reportModel.attemptsCount,
                reportModel.successCount
            )
            else -> Stability.Unknown(reportModel.attemptsCount, reportModel.successCount)
        }
    }

    private fun getDescription(listResult: ListResult): String? {
        return if (listResult.description.isNullOrBlank()) {
            listResult.preparedData?.lastOrNull()?.cthulhuTestCase?.description
        } else {
            listResult.description
        }
    }

    private fun getDataSetNumber(listResult: ListResult): Int? {
        return if (listResult.dataSetNumber != null && listResult.dataSetNumber > 0) {
            listResult.dataSetNumber
        } else {
            null
        }
    }

    private fun getTestCaseId(listResult: ListResult): Int? {
        return try {
            listResult.testCaseId?.toInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun getPriority(listResult: ListResult): TestCasePriority? {
        return try {
            listResult.preparedData?.lastOrNull()?.priorityId?.let { TestCasePriority.fromId(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun getBehavior(listResult: ListResult): TestCaseBehavior? {
        return try {
            listResult.preparedData?.lastOrNull()?.behaviorId?.let { TestCaseBehavior.fromId(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFlakiness(listResult: ListResult) = listResult.preparedData?.lastOrNull()?.let {
        if (it.isFlaky == true) {
            Flakiness.Flaky(it.flakyReason ?: "")
        } else {
            Flakiness.Stable
        }
    } ?: Flakiness.Stable
}
