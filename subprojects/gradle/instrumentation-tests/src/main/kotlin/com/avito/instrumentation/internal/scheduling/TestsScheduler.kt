package com.avito.instrumentation.internal.scheduling

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal interface TestsScheduler {

    fun schedule(): Result

    data class Result(
        val initialTestSuite: Collection<TestStaticData>,
        val testResults: Collection<AndroidTest>
    )
}
