package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider

internal class InMemoryReport(
    private val id: String,
    private val timeProvider: TimeProvider
) : Report, LegacyReport, ReadReport {

    private var gitInfo: String? = null
    private val testStatusFinalizer = TestStatusFinalizer.create()
    private val testAttempts = mutableListOf<AndroidTest>()

    @Synchronized
    override fun addTest(test: AndroidTest) {
        this.testAttempts.add(test)
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
    override fun finish() {
        // empty
    }

    @Synchronized
    override fun getTests(): Result<List<SimpleRunTest>> {
        val result = testAttempts
            .groupBy { testAttempt ->
                "${testAttempt.name};${testAttempt.device}"
            }.map { (testCoordinate, attempts) ->
                val status = testStatusFinalizer.getTestFinalStatus(attempts)
                val lastAttempt = attempts.last()
                SimpleRunTest(
                    id = testCoordinate, // todo unique in one run
                    reportId = requireNotNull(tryGetId()) {
                        "InMemoryReport reportId must be present"
                    },
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
        return Result.Success(result)
    }

    private fun tryGetId(): String? {
        val gitInfo = gitInfo
        return if (gitInfo != null) {
            id + gitInfo
        } else {
            null
        }
    }
}
