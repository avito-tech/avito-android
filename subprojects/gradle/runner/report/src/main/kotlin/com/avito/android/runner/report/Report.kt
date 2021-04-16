package com.avito.android.runner.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

public interface Report : ReadReport {

    public fun addTest(test: AndroidTest)

    /**
     * Skipped tests available right after initial filtering, so it's added even before test runner started
     *
     * Pair<TestStaticData, String> instead of AndroidTest.Skipped in interface because client don't know
     */
    public fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    public companion object
}
