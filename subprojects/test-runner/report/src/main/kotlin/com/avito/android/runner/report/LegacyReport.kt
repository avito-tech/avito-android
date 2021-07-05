package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.test.model.TestCase
import com.avito.test.model.TestStatus

/**
 * Legacy way of interacting with report model; internal ReportViewer service domain leaking here
 * TODO remove from core report module
 */
public interface LegacyReport {

    public fun finish()

    public fun sendLostTests(lostTests: Collection<AndroidTest.Lost>)

    /**
     * getTestsForRunId will fetch all tests, even not relatable to current run
     * Avito Report suite can contain tests from all instrumentation configurations and apps/libraries
     */
    public fun getTests(): Result<Map<TestCase, TestStatus>>

    public companion object
}
