package com.avito.reportviewer

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.reportviewer.model.Report
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest
import com.google.gson.JsonElement
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class StubReportsApi : ReportsApi {

    private val markAsSuccessfulRequests = mutableListOf<MarkAsRequest>()

    private val testsForRunCoordinates = mutableMapOf<ReportCoordinates, Result<List<SimpleRunTest>>>()

    private val testsForRunId = mutableMapOf<String, Result<List<SimpleRunTest>>>()

    lateinit var getReportResult: Result<Report>

    lateinit var finished: Result<Unit>

    val addTestsRequests: Queue<AddTestsRequest> = ConcurrentLinkedQueue()

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
    override fun getReport(reportCoordinates: ReportCoordinates): Result<Report> = getReportResult

    fun enqueueTestsForRunCoordinates(reportCoordinates: ReportCoordinates, value: Result<List<SimpleRunTest>>) {
        testsForRunCoordinates[reportCoordinates] = value
    }

    fun enqueueTestsForRunId(reportId: String, value: Result<List<SimpleRunTest>>) {
        testsForRunId[reportId] = value
    }

    @Synchronized
    override fun getTestsForRunCoordinates(reportCoordinates: ReportCoordinates): Result<List<SimpleRunTest>> {
        return testsForRunCoordinates[reportCoordinates] ?: error("no stub ready for $reportCoordinates")
    }

    @Synchronized
    override fun getTestsForRunId(reportId: String): Result<List<SimpleRunTest>> {
        return testsForRunId[reportId] ?: error("no stub ready for id $reportId")
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
