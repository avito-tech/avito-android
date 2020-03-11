package com.avito.report

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.logger.Logger
import com.avito.report.internal.JsonRpcRequestProvider
import com.avito.report.internal.model.ConclusionStatus
import com.avito.report.internal.model.ListResult
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.internal.model.RpcResult
import com.avito.report.internal.model.Run
import com.avito.report.internal.model.RunTest
import com.avito.report.internal.model.TestStatus
import com.avito.report.model.CrossDeviceRunTest
import com.avito.report.model.CrossDeviceStatus
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.FailureOnDevice
import com.avito.report.model.Flakiness
import com.avito.report.model.GetReportResult
import com.avito.report.model.Kind
import com.avito.report.model.PerformanceTest
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Stability
import com.avito.report.model.Status
import com.avito.report.model.TestName
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.funktionale.tries.Try

internal class ReportsFetchApiImpl(
    private val requestProvider: JsonRpcRequestProvider,
    private val gson: Gson,
    private val logger: Logger
) : ReportsFetchApi {

    override fun getReportsList(
        planSlug: String,
        jobSlug: String,
        pageNumber: Int
    ): Try<List<Report>> {
        return Try {
            requestProvider.jsonRpcRequest<RpcResult<List<Run>>>(
                RfcRpcRequest(
                    method = "Run.List",
                    params = mapOf(
                        "plan_slug" to planSlug,
                        "job_slug" to jobSlug,
                        "page_num" to pageNumber
                    )
                )
            ).result.map { run ->
                Report(
                    id = run.id,
                    planSlug = run.planSlug,
                    jobSlug = run.jobSlug,
                    runId = run.runId,
                    isFinished = run.isFinished,
                    buildBranch = tryToGetBuildBranch(run)
                )
            }
        }
    }

    override fun getReport(reportCoordinates: ReportCoordinates): GetReportResult {
        return Try {
            requestProvider.jsonRpcRequest<RpcResult<Report>>(
                RfcRpcRequest(
                    method = "Run.GetByParams",
                    params = mapOf(
                        "plan_slug" to reportCoordinates.planSlug,
                        "job_slug" to reportCoordinates.jobSlug,
                        "run_id" to reportCoordinates.runId
                    )
                )
            ).result
        }.fold<GetReportResult>(
            { report -> GetReportResult.Found(report) },
            { exception ->
                val isNotFoundError = exception.message?.contains("\"data\":\"not found\"") ?: false
                if (isNotFoundError) {
                    GetReportResult.NotFound
                } else {
                    GetReportResult.Error(exception)
                }
            }
        )
    }

    override fun getPerformanceTest(id: String): Try<PerformanceTest> {
        return try {
            val test = getTest(id)
            val performanceAttempts = test.runTestResult
                .mapNotNull {
                    it.preparedData["performance"]?.toString()
                }.flatMap {
                    gson.fromJson<List<Map<String, Double>>>(it)
                }
            Try.Success(
                PerformanceTest(
                    test.testName,
                    test.id,
                    convert(performanceAttempts)
                )
            )
        } catch (e: Exception) {
            Try.Failure(e)
        }
    }

    override fun getTestsForRunId(reportCoordinates: ReportCoordinates): Try<List<SimpleRunTest>> {
        return when (val getReportResult = getReport(reportCoordinates)) {
            is GetReportResult.Found -> getTestData(getReportResult.report.id)
            GetReportResult.NotFound -> Try.Failure(Exception("Report not found $reportCoordinates"))
            is GetReportResult.Error -> Try.Failure(getReportResult.exception)
        }
    }

    override fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Try<CrossDeviceSuite> {
        return when (val getReportResult = getReport(reportCoordinates)) {
            is GetReportResult.Found -> getTestData(getReportResult.report.id)
                .map { testData -> toCrossDeviceTestData(testData) }
            GetReportResult.NotFound -> Try.Failure(Exception("Report not found $reportCoordinates"))
            is GetReportResult.Error -> Try.Failure(getReportResult.exception)
        }
    }

    private fun toCrossDeviceTestData(testData: List<SimpleRunTest>): CrossDeviceSuite {
        return testData
            .groupBy { it.name }
            .map { (testName, runs) ->
                val status: CrossDeviceStatus = when {
                    runs.any { it.status is Status.Lost } -> CrossDeviceStatus.LostOnSomeDevices
                    runs.all { it.status is Status.Skipped } -> CrossDeviceStatus.SkippedOnAllDevices
                    runs.all { it.status is Status.Manual } -> CrossDeviceStatus.Manual

                    /**
                     * Успешным прогоном является при соблюдении 2 условий:
                     *  - Все тесты прошли (имеют Success статус)
                     *  - Есть пропущенные тесты (скипнули на каком-то SDK например), но все остальные являются успешными
                     *    (как минимум 1)
                     */
                    runs.any { it.status is Status.Success } &&
                        runs.all { it.status is Status.Success || it.status is Status.Skipped } -> CrossDeviceStatus.Success

                    runs.all { it.status is Status.Failure } -> CrossDeviceStatus.FailedOnAllDevices(
                        runs.deviceFailures()
                    )
                    runs.any { it.status is Status.Failure } -> CrossDeviceStatus.FailedOnSomeDevices(
                        runs.deviceFailures()
                    )


                    else -> CrossDeviceStatus.Inconsistent
                }
                CrossDeviceRunTest(TestName(testName), status)
            }
            .let { CrossDeviceSuite(it) }
    }

    /**
     * todo выпилить и падать если не находим прямо тут после того как история уедет вперед достаточно чтобы не ловить эти падения
     */
    private fun tryToGetBuildBranch(run: Run): String? {
        return run.reportData?.appBranch ?: run.reportData?.tags
            ?.find { it.startsWith("buildBranch:") }
            ?.substringAfter(':')
    }

    private fun getTestData(reportId: String): Try<List<SimpleRunTest>> {
        return Try {
            requestProvider.jsonRpcRequest<RpcResult<List<ListResult>?>>(
                RfcRpcRequest(
                    method = "RunTest.List",
                    params = mapOf("run_id" to reportId)
                )
            ).result?.map { listResult ->
                val testName = "${listResult.className}.${listResult.methodName}"
                SimpleRunTest(
                    id = listResult.id,
                    reportId = reportId,
                    name = testName,
                    deviceName = requireNotNull(listResult.environment) { "deviceName(environment) is null for test $testName, that's illegal!" },
                    testCaseId = getTestCaseId(listResult),
                    className = listResult.className,
                    methodName = listResult.methodName,
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
                    kind = listResult.kind?.let { Kind.fromTmdId(it) } ?: Kind.UNKNOWN,
                    flakiness = getFlakiness(listResult)
                )
            } ?: emptyList()
        }
    }

    /**
     * RunTest.Get
     * получение результата конкретного теста по id
     */
    private fun getTest(id: String): RunTest {
        return requestProvider.jsonRpcRequest<RpcResult<RunTest>>(
            RfcRpcRequest(
                method = "RunTest.Get",
                params = mapOf(
                    "id" to id
                )
            )
        ).result
    }

    private fun deserializeStatus(reportModel: ListResult): Status {
        return when (reportModel.status) {
            TestStatus.OK -> Status.Success
            TestStatus.FAILURE, TestStatus.ERROR -> {
                if (reportModel.lastConclusion == ConclusionStatus.OK) {
                    Status.Success
                } else {
                    val verdict = reportModel.preparedData?.lastOrNull()?.verdict
                    if (verdict.isNullOrBlank()) {
                        //todo fallback
                        logger.debug("Can't get verdict for test: $reportModel")
                        Status.Failure(
                            "Can't get verdict",
                            reportModel.lastErrorHash ?: "hash unavailable"
                        )
                    } else {
                        Status.Failure(verdict, reportModel.lastErrorHash ?: "hash unavailable")
                    }
                }
            }
            TestStatus.OTHER, TestStatus.PANIC, TestStatus.LOST, null -> Status.Lost
            TestStatus.MANUAL -> Status.Manual
            TestStatus.SKIP -> Status.Skipped("test ignored") //todo нужен более подробный reason
        }
    }

    private fun determineStability(reportModel: ListResult): Stability {
        return when {
            reportModel.attemptsCount == null || reportModel.successCount == null -> {
                logger.debug("should not be here $reportModel")
                Stability.Stable
            }
            reportModel.attemptsCount < 1 -> {
                logger.debug("test without attempts? $reportModel")
                Stability.Failing(reportModel.attemptsCount) // на самом деле не совсем, репортим эту ситуацию как невероятную
            }
            reportModel.successCount > reportModel.attemptsCount -> {
                logger.debug("success count > attempts count?? $reportModel")
                Stability.Stable // на самом деле не совсем, репортим эту ситуацию как невероятную
            }
            reportModel.successCount == 0 -> Stability.Failing(reportModel.attemptsCount)
            reportModel.successCount == reportModel.attemptsCount -> Stability.Stable
            // FIXME тут может быть ошибка, т.к. attempt может быть skipped или какой-то другой не-success статус
            reportModel.successCount < reportModel.attemptsCount -> Stability.Flaky(
                reportModel.attemptsCount,
                reportModel.successCount
            )
            else -> {
                logger.debug("should not be here $reportModel")
                Stability.Stable
            }
        }
    }

    private fun getDescription(listResult: ListResult): String? {
        return if (listResult.description.isNullOrBlank()) {
            listResult.preparedData?.lastOrNull()?.testCase?.description
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

    private fun convert(performanceAttempts: List<Map<String, Double>>): Map<String, List<Double>> {
        return performanceAttempts.flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
    }

    private fun List<SimpleRunTest>.deviceFailures(): List<FailureOnDevice> {
        return this.filter { it.status is Status.Failure }
            .map {
                FailureOnDevice(it.deviceName, (it.status as Status.Failure).verdict)
            }
    }
}
