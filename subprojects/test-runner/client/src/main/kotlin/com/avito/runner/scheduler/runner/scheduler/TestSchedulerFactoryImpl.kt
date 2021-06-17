package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.ReportFactory
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.scheduler.TestRunnerFactoryProvider
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter

public class TestSchedulerFactoryImpl(
    private val params: InstrumentationTestsActionParams,
    private val report: Report,
    private val testSuiteLoader: TestSuiteLoader,
    private val reportFactory: ReportFactory,
    private val testRunnerFactoryProvider: TestRunnerFactoryProvider,
) : TestSchedulerFactory {

    override fun create(): TestScheduler {
        val testSuiteProvider: TestSuiteProvider = createTestSuiteProvider()

        return TestSchedulerImpl(
            params = params,
            report = report,
            testSuiteProvider = testSuiteProvider,
            testSuiteLoader = testSuiteLoader,
            filterInfoWriter = FilterInfoWriter.Impl(
                outputDir = params.outputDir,
            ),
            loggerFactory = params.loggerFactory,
            testRunnerFactory = testRunnerFactoryProvider.provide(),
        )
    }

    private fun createTestSuiteProvider(): TestSuiteProvider = TestSuiteProvider.Impl(
        report = report,
        targets = params.instrumentationConfiguration.targets,
        reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
        filterFactory = FilterFactory.create(
            filterData = params.instrumentationConfiguration.filter,
            impactAnalysisResult = params.impactAnalysisResult,
            reportFactory = reportFactory,
            loggerFactory = params.loggerFactory
        )
    )
}
