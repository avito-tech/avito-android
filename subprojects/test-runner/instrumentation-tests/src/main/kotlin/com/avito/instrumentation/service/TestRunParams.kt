package com.avito.instrumentation.service

import com.avito.android.stats.SeriesName

/**
 * Parameters for a specific single test suite run
 * Don't put global test runner parameters here
 * "global" - means that it is available on a build start
 */
internal data class TestRunParams(
    val projectName: String,
    val instrumentationConfigName: String
) {

    val metricsPrefix = SeriesName.create(
        "testrunner",
        projectName,
        instrumentationConfigName
    )
}
