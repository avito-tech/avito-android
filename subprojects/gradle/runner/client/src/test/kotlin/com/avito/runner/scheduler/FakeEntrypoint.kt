package com.avito.runner.scheduler

import com.avito.logger.Logger
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.scheduler.report.FakeReporter
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.report.SummaryReportMakerImplementation
import com.avito.runner.scheduler.runner.FakeTestRunner
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerResult

internal fun Entrypoint.Companion.createStubInstance(
    testRunner: TestRunner = FakeTestRunner(result = TestRunnerResult(emptyMap())),
    summaryMaker: SummaryReportMaker = SummaryReportMakerImplementation(),
    reporter: Reporter = FakeReporter(),
    logger: Logger = StdOutLogger()
): Entrypoint {
    return Entrypoint(
        testRunner = testRunner,
        summaryReportMaker = summaryMaker,
        reporter = reporter,
        logger = logger
    )
}
