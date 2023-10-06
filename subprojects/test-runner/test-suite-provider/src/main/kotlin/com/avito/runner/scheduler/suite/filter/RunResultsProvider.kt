package com.avito.runner.scheduler.suite.filter

import com.avito.android.Result
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.test.model.TestCase

public interface RunResultsProvider {
    public fun getPreviousRunsResults(): Result<Map<TestCase, RunStatus>>
    public fun getRunResultsById(id: String): Result<Map<TestCase, RunStatus>>
}
