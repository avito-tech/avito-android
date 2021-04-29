package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName

/**
 * Legacy way of interacting with report model; internal ReportViewer service domain leaking here
 */
public interface LegacyReport {

    public fun finish()

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    /**
     * getTestsForRunId will fetch all tests, even not relatable to current run
     * Avito Report suite can contain tests from all instrumentation configurations and apps/libraries
     *
     * todo initialSuiteFilter is avito report implementation detail
     */
    public fun getTests(initialSuiteFilter: List<TestName> = emptyList()): Result<List<SimpleRunTest>>

    public companion object
}
