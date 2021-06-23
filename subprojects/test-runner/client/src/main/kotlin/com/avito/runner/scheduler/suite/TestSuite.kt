package com.avito.runner.scheduler.suite

import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.suite.filter.TestsFilter

public data class TestSuite(
    val appliedFilter: TestsFilter,
    val testsToRun: List<TestStaticData>,
    val skippedTests: List<Pair<TestStaticData, TestsFilter.Result.Excluded>>
)
