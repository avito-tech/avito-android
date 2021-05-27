package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest

/**
 * Legacy way of interacting with report model; internal ReportViewer service domain leaking here
 */
public interface LegacyReport {

    public fun finish()

    public fun sendLostTests(lostTests: Collection<AndroidTest.Lost>)

    /**
     * getTestsForRunId will fetch all tests, even not relatable to current run
     * Avito Report suite can contain tests from all instrumentation configurations and apps/libraries
     */
    public fun getTests(): Result<List<SimpleRunTest>>

    public companion object
}
