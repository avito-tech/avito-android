package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.TestStaticData

public interface Report : ReadReport {

    public fun tryCreate(testHost: String, gitBranch: String, gitCommit: String)

    public fun tryGetId(): String?

    public fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    public fun sendCompletedTest(completedTest: AndroidTest.Completed)

    public fun finish()

    public fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit>

    public fun getCrossDeviceTestData(): Result<CrossDeviceSuite>

    public companion object
}
