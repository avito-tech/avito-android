package com.avito.reportviewer

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.reportviewer.model.CrossDeviceSuite
import com.avito.reportviewer.model.Report
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest
import com.google.gson.JsonElement
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

public class StubReportsApi : ReportsApi {

    private val markAsSuccessfulRequests = mutableListOf<MarkAsRequest>()

    private val testsForRunId = mutableMapOf<ReportCoordinates, Result<List<SimpleRunTest>>>()

    public lateinit var getReportResult: Result<Report>

    public lateinit var finished: Result<Unit>

    public lateinit var crossDeviceTestData: Result<CrossDeviceSuite>

    public val addTestsRequests: Queue<AddTestsRequest> = ConcurrentLinkedQueue()

    override fun addTest(reportCoordinates: ReportCoordinates, buildId: String?, test: AndroidTest): Result<String> {
        TODO("not implemented")
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
    override fun getCrossDeviceTestData(reportCoordinates: ReportCoordinates): Result<CrossDeviceSuite> =
        crossDeviceTestData

    @Synchronized
    override fun getReport(reportCoordinates: ReportCoordinates): Result<Report> = getReportResult

    public fun enqueueTestsForRunId(reportCoordinates: ReportCoordinates, value: Result<List<SimpleRunTest>>) {
        testsForRunId[reportCoordinates] = value
    }

    @Synchronized
    override fun getTestsForRunId(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>> {
        return testsForRunId[reportCoordinates] ?: error("no stub ready for $reportCoordinates")
    }

    @Synchronized
    override fun setFinished(reportCoordinates: ReportCoordinates): Result<Unit> = finished

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

    public data class MarkAsRequest(
        val testRunId: String,
        val author: String,
        val comment: String
    )

    public data class AddTestsRequest(
        val reportCoordinates: ReportCoordinates,
        val buildId: String?,
        val tests: Collection<AndroidTest>
    )
}
