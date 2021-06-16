package com.avito.runner.scheduler.runner.model

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

public data class TestSchedulerResult(
    val testsToRun: Collection<TestStaticData>,
    val testResults: Collection<AndroidTest>
)
