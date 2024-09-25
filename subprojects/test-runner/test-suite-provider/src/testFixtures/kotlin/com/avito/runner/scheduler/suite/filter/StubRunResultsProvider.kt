package com.avito.runner.scheduler.suite.filter

import com.avito.android.Result
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.runner.scheduler.suite.filter.run_results_provider.RunResultsProvider
import com.avito.test.model.TestCase

class StubRunResultsProvider : RunResultsProvider {

    var previousRunResults: Result<Map<TestCase, RunStatus>> = Result.Success(emptyMap())

    var reportIdToRunResults: Result<Map<String, Map<TestCase, RunStatus>>> = Result.Success(emptyMap())

    override fun getPreviousRunsResults(): Result<Map<TestCase, RunStatus>> {
        return previousRunResults
    }

    override fun getRunResultsById(id: String): Result<Map<TestCase, RunStatus>> {
        return reportIdToRunResults.map {
            it.getOrElse(id) {
                throw NoSuchElementException("No stub for report id: $id")
            }
        }
    }
}
