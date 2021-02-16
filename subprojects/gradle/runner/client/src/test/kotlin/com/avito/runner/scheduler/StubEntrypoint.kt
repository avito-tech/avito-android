package com.avito.runner.scheduler

import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.StubReporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.report.SummaryReportMakerImplementation
import com.avito.runner.scheduler.runner.StubTestRunner
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerResult
import kotlinx.coroutines.CoroutineScope

internal fun Entrypoint.Companion.createStubInstance(
    testRunner: TestRunner = StubTestRunner(result = TestRunnerResult(emptyMap())),
    summaryMaker: SummaryReportMaker = SummaryReportMakerImplementation(),
    reporter: Reporter = StubReporter(),
    loggerFactory: LoggerFactory = StubLoggerFactory,
    coroutineScope: CoroutineScope
): Entrypoint {
    return Entrypoint(
        testRunner = testRunner,
        summaryReportMaker = summaryMaker,
        reporter = reporter,
        loggerFactory = loggerFactory,
        scope = coroutineScope
    )
}
