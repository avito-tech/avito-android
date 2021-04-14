package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.TestStaticData

/**
 * Legacy way of interacting with report model; internal ReportViewer service domain leaking here
 */
public interface LegacyReport {

    public fun tryCreate(testHost: String, gitBranch: String, gitCommit: String)

    public fun tryGetId(): String?

    public fun finish()

    public fun getCrossDeviceTestData(): Result<CrossDeviceSuite>

    public fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)
}
