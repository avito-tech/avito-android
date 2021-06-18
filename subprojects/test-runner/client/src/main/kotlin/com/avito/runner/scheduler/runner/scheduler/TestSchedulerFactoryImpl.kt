package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.finalizer.FinalizerFactory
import com.avito.runner.scheduler.TestRunnerFactoryProvider
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import java.io.File

internal class TestSchedulerFactoryImpl(
    private val finalizerFactory: FinalizerFactory,
    private val report: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testRunnerFactoryProvider: TestRunnerFactoryProvider,
    private val testSuiteLoader: TestSuiteLoader,
    private val loggerFactory: LoggerFactory,
    private val fileInfoWriter: FilterInfoWriter,
    private val filter: InstrumentationFilterData,
    private val testApk: File,
    private val outputDir: File,
) : TestSchedulerFactory {

    override fun create(): TestScheduler {
        return TestSchedulerImpl(
            report = report,
            testSuiteProvider = testSuiteProvider,
            testSuiteLoader = testSuiteLoader,
            filterInfoWriter = fileInfoWriter,
            loggerFactory = loggerFactory,
            testRunnerFactory = testRunnerFactoryProvider.provide(),
            finalizer = finalizerFactory.create(),
            filter = filter,
            testApk = testApk,
            outputDir = outputDir,
        )
    }
}
