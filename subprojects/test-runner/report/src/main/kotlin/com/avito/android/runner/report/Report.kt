package com.avito.android.runner.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

public interface Report {

    public fun addTest(testAttempt: TestAttempt)

    /**
     * Skipped tests available right after initial filtering, so it's added even before test runner started
     */
    public fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    /**
     * Optionally report about tests, lost during run
     */
    public fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>)

    /**
     * single result for each test, where attempts aggregated by
     * [com.avito.android.runner.report.internal.TestAttemptsAggregateStrategy]
     */
    public fun getTestResults(): Collection<AndroidTest>

    public companion object
}
