package com.avito.runner.scheduler.runner.model

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal data class TestRunnerResults(
    val testsToRun: Collection<TestStaticData>,
    val testResults: Collection<AndroidTest>
)
