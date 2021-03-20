package com.avito.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.CreateResult
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.google.gson.JsonElement
import java.util.ArrayDeque
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class StubReportsApi(
    reportListResults: List<Result<List<Report>>> = emptyList()
) : ReportsApi {

    private val reportListResultsQueue: Queue<Result<List<Report>>> = ArrayDeque(reportListResults)

    private val testsForReportId = mutableMapOf<String, Result<List<SimpleRunTest>>>()

    private val markAsSuccessfulRequests = mutableListOf<MarkAsRequest>()

    private val testsForRunId = mutableMapOf<ReportCoordinates, Result<List<SimpleRunTest>>>()

    lateinit var createResult: CreateResult

    lateinit var getReportResult: GetReportResult

    lateinit var finished: Result<Unit>

    lateinit var crossDeviceTestData: Result<CrossDeviceSuite>

    val addTestsRequests: Queue<AddTestsRequest> = ConcurrentLinkedQueue()

    @Synchronized
    override fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        testHost: String,
        gitBranch: String,
        gitCommit: String,
        tmsBranch: String
    ): CreateResult = createResult

    override fun addTest(reportCoordinates: ReportCoordinates, buildId: String?, test: AndroidTest): Result<String> {
        TODO("not implemented")
    }

    fun enqueueTestsForReportId(reportId: String, value: Result<List<SimpleRunTest>>) {
        testsForReportId[reportId] = value
    }

    override fun getTestsForReportId(reportId: String): Result<List<SimpleRunTest>> {
        return testsForReportId[reportId] ?: error("you need to enqueue result by reportId: $reportId")
    }

    override fun addTests(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        tests: Collection<AndroidTest>
    ): Result<List<String>> {
        addTestsRequests.add(AddTestsRequest(reportCoordinates, buildId, tests))
        return Result.Success(emptyList())
    }

    @Synchronized
    override fun getReportsList(planSlug: String, jobSlug: String, pageNumber: Int): Result<List<Report>> {
        if (reportListResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "getReportsList results queue is empty in StubReportsApi"
            )
        }
        return reportListResultsQueue.poll()
    }

    @Synchronized
    override fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Result<CrossDeviceSuite> =
        crossDeviceTestData

    @Synchronized
    override fun getReport(reportCoordinates: ReportCoordinates): GetReportResult = getReportResult

    fun enqueueTestsForRunId(reportCoordinates: ReportCoordinates, value: Result<List<SimpleRunTest>>) {
        testsForRunId[reportCoordinates] = value
    }

    @Synchronized
    override fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>> {
        return testsForRunId[reportCoordinates] ?: error("no stub ready for $reportCoordinates")
    }

    @Synchronized
    override fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit> = finished

    fun getLastMarkAsSuccessfulRequest(): MarkAsRequest? = markAsSuccessfulRequests.lastOrNull()

    @Synchronized
    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit> {
        markAsSuccessfulRequests.add(
            MarkAsRequest(
                testRunId,
                author,
                comment
            )
        )
        return Result.Success(Unit)
    }

    @Synchronized
    override fun markAsFailed(testRunId: String, author: String, comment: String): Result<Unit> {
        TODO("not implemented")
    }

    override fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Result<Unit> {
        TODO("not implemented")
    }

    data class MarkAsRequest(
        val testRunId: String,
        val author: String,
        val comment: String
    )

    data class AddTestsRequest(
        val reportCoordinates: ReportCoordinates,
        val buildId: String?,
        val tests: Collection<AndroidTest>
    )
}
