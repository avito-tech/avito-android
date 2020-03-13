package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.CreateResult
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.PerformanceTest
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.google.gson.JsonElement
import org.funktionale.tries.Try
import java.util.ArrayDeque
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class FakeReportsApi(
    reportListResults: List<Try<List<Report>>> = emptyList()
) : ReportsApi {

    lateinit var createResult: CreateResult

    lateinit var getReportResult: GetReportResult

    @Synchronized
    override fun create(
        reportCoordinates: ReportCoordinates,
        buildId: String,
        apiUrl: String,
        gitBranch: String,
        gitCommit: String,
        tmsBranch: String
    ): CreateResult = createResult

    private val reportListResultsQueue: Queue<Try<List<Report>>> = ArrayDeque(reportListResults)

    override fun addTest(reportCoordinates: ReportCoordinates, buildId: String?, test: AndroidTest): Try<String> {
        TODO("not implemented")
    }

    val addTestsRequests: Queue<AddTestsRequest> = ConcurrentLinkedQueue()

    override fun addTests(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        tests: Collection<AndroidTest>
    ): Try<List<String>> {
        addTestsRequests.add(AddTestsRequest(reportCoordinates, buildId, tests))
        return Try.Success(emptyList())
    }

    data class AddTestsRequest(
        val reportCoordinates: ReportCoordinates,
        val buildId: String?,
        val tests: Collection<AndroidTest>
    )

    @Synchronized
    override fun getReportsList(planSlug: String, jobSlug: String, pageNumber: Int): Try<List<Report>> {
        if (reportListResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "getReportsList results queue is empty in FakeReportsApi"
            )
        }
        return reportListResultsQueue.poll()
    }

    lateinit var crossDeviceTestData: Try<CrossDeviceSuite>

    @Synchronized
    override fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Try<CrossDeviceSuite> =
        crossDeviceTestData

    @Synchronized
    override fun getReport(reportCoordinates: ReportCoordinates): GetReportResult = getReportResult

    private val testsForRunId = mutableMapOf<ReportCoordinates, Try<List<SimpleRunTest>>>()

    fun enqueueTestsForRunId(reportCoordinates: ReportCoordinates, value: Try<List<SimpleRunTest>>) {
        testsForRunId[reportCoordinates] = value
    }

    @Synchronized
    override fun getTestsForRunId(reportCoordinates: ReportCoordinates): Try<List<SimpleRunTest>> {
        return testsForRunId[reportCoordinates] ?: error("no stub ready for $reportCoordinates")
    }

    @Synchronized
    override fun getPerformanceTest(id: String): Try<PerformanceTest> {
        TODO("not implemented")
    }

    lateinit var finished: Try<Unit>

    @Synchronized
    override fun setFinished(reportCoordinates: ReportCoordinates): Try<Unit> = finished

    private val markAsSuccessfulRequests = mutableListOf<MarkAsRequest>()

    data class MarkAsRequest(val testRunId: String, val author: String, val comment: String)

    fun getLastMarkAsSuccessfulRequest(): MarkAsRequest? = markAsSuccessfulRequests.lastOrNull()

    @Synchronized
    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit> {
        markAsSuccessfulRequests.add(
            MarkAsRequest(
                testRunId,
                author,
                comment
            )
        )
        return Try.Success(Unit)
    }

    @Synchronized
    override fun markAsFailed(testRunId: String, author: String, comment: String): Try<Unit> {
        TODO("not implemented")
    }

    override fun pushPreparedData(reportId: String, analyzerKey: String, preparedData: JsonElement): Try<Unit> {
        TODO("not implemented")
    }
}
