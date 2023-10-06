package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.Result
import com.avito.report.Report
import com.avito.report.model.TestStatus
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.runner.scheduler.suite.filter.RunResultsProvider
import com.avito.test.model.TestCase

internal class RunResultsProviderImpl(private val report: Report) : RunResultsProvider {
    override fun getPreviousRunsResults(): Result<Map<TestCase, RunStatus>> {
        return report.getPreviousRunsResults().mapToRunStatus()
    }

    override fun getRunResultsById(id: String): Result<Map<TestCase, RunStatus>> {
        return report.getRunResultsById(id).mapToRunStatus()
    }

    private fun Result<Map<TestCase, TestStatus>>.mapToRunStatus(): Result<Map<TestCase, RunStatus>> =
        map {
            it.mapValues { (_, testStatus) ->
                when (testStatus) {
                    is TestStatus.Success -> RunStatus.Success
                    is TestStatus.Failure -> RunStatus.Failed
                    TestStatus.Lost -> RunStatus.Lost
                    TestStatus.Manual -> RunStatus.Manual
                    is TestStatus.Skipped -> RunStatus.Skipped
                }
            }
        }
}
