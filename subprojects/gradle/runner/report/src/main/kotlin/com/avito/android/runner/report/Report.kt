package com.avito.android.runner.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.TestStaticData
import org.funktionale.tries.Try

public interface Report : ReadReport {

    public fun tryCreate(testHost: String, gitBranch: String, gitCommit: String)

    public fun tryGetId(): String?

    public fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    public fun sendCompletedTest(completedTest: AndroidTest.Completed)

    public fun finish()

    public fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit>

    public fun getCrossDeviceTestData(): Try<CrossDeviceSuite>

    public companion object
}
