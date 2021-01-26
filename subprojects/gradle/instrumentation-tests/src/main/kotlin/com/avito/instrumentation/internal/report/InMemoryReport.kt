package com.avito.instrumentation.internal.report

import com.avito.instrumentation.report.Report
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider
import org.funktionale.tries.Try

internal class InMemoryReport(
    private val id: String,
    private val timeProvider: TimeProvider
) : Report {

    private var gitInfo: String? = null
    private val testStatusFinalizer = TestStatusFinalizer.create()
    private val testAttempts = mutableListOf<AndroidTest>()

    override fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String) {
        gitInfo = "$apiUrl;$gitBranch$;$gitCommit"
    }

    override fun tryGetId(): String? {
        val gitInfo = gitInfo
        return if (gitInfo != null) {
            id + gitInfo
        } else {
            null
        }
    }

    @Synchronized
    override fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        this.testAttempts.addAll(
            skippedTests.map { (test, reason) ->
                AndroidTest.Skipped.fromTestMetadata(
                    testStaticData = test,
                    skipReason = reason,
                    reportTime = timeProvider.nowInMillis()
                )
            }
        )
    }

    @Synchronized
    override fun sendLostTests(lostTests: List<AndroidTest.Lost>) {
        this.testAttempts.addAll(lostTests)
    }

    @Synchronized
    override fun sendCompletedTest(completedTest: AndroidTest.Completed) {
        this.testAttempts.add(completedTest)
    }

    @Synchronized
    override fun finish() {
        // empty
    }

    @Synchronized
    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit> {
        TODO("Need to implement")
    }

    @Synchronized
    override fun getCrossDeviceTestData(): Try<CrossDeviceSuite> {
        TODO("Not yet implemented")
    }

    @Synchronized
    override fun getTests(): Try<List<SimpleRunTest>> {
        val result = testAttempts
            .groupBy { testAttempt ->
                "${testAttempt.name};${testAttempt.device}"
            }.map { (testCoordinate, attempts) ->
                val status = testStatusFinalizer.getTestFinalStatus(attempts)
                val lastAttempt = attempts.last()
                SimpleRunTest(
                    id = testCoordinate, // todo unique in one run
                    reportId = requireNotNull(tryGetId()), // todo fix failure
                    name = lastAttempt.name.name,
                    className = lastAttempt.name.className,
                    methodName = lastAttempt.name.methodName,
                    testCaseId = lastAttempt.testCaseId,
                    deviceName = lastAttempt.device.name,
                    status = status.status,
                    stability = status.stability,
                    buildId = null,
                    groupList = emptyList(),
                    startTime = status.startTime,
                    endTime = status.endTime,
                    skipReason = status.skippedReason,
                    isFinished = status.isFinished,
                    lastAttemptDurationInSeconds = status.lastAttemptDurationInSeconds,
                    externalId = lastAttempt.externalId,
                    description = lastAttempt.description,
                    dataSetNumber = lastAttempt.dataSetNumber,
                    featureIds = lastAttempt.featureIds,
                    features = lastAttempt.featureIds.map { it.toString() },
                    tagIds = lastAttempt.tagIds,
                    priority = lastAttempt.priority,
                    behavior = lastAttempt.behavior,
                    kind = lastAttempt.kind,
                    flakiness = lastAttempt.flakiness
                )
            }
        return Try.Success(result)
    }
}
