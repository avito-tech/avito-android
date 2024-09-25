package com.avito.runner.scheduler.suite.filter.run_results_provider

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.test.model.TestCase

public class NoHistoryRunResultsProvider(loggerFactory: LoggerFactory) : RunResultsProvider {

    private val logger = loggerFactory.create<NoHistoryRunResultsProvider>()

    override fun getPreviousRunsResults(): Result<Map<TestCase, RunStatus>> {
        logger.warn("NoHistoryRunResultsProvider is used, other run's results are not available")
        return Result.Success(emptyMap())
    }

    override fun getRunResultsById(id: String): Result<Map<TestCase, RunStatus>> {
        logger.warn("NoHistoryRunResultsProvider is used, other run's results are not available")
        return Result.Success(emptyMap())
    }
}
