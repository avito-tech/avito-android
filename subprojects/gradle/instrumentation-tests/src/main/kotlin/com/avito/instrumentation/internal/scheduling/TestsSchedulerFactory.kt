package com.avito.instrumentation.internal.scheduling

import com.avito.android.TestSuiteLoader
import com.avito.android.TestSuiteLoaderImpl
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.report.listener.ReportViewerTestReporter
import com.avito.instrumentation.internal.suite.TestSuiteProvider
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.FilterInfoWriter
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.instrumentation.report.Report
import com.avito.retrace.ProguardRetracer
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson

internal interface TestsSchedulerFactory {

    fun create(): TestsScheduler

    class Impl : TestsSchedulerFactory {

        private val params: InstrumentationTestsAction.Params
        private val sourceReport: Report
        private val gson: Gson
        private val testExecutorFactory: TestExecutorFactory
        private val testSuiteLoader: TestSuiteLoader
        private val runnerPrefix: SeriesName

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson = InstrumentationTestsActionFactory.gson,
            testExecutorFactory: TestExecutorFactory,
            testSuiteLoader: TestSuiteLoader,
            runnerPrefix: SeriesName
        ) {
            this.params = params
            this.sourceReport = sourceReport
            this.gson = gson
            this.testExecutorFactory = testExecutorFactory
            this.testSuiteLoader = testSuiteLoader
            this.runnerPrefix = runnerPrefix
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson,
            runnerPrefix: SeriesName
        ) : this(
            params = params,
            sourceReport = sourceReport,
            gson = gson,
            testExecutorFactory = TestExecutorFactory.Implementation(),
            testSuiteLoader = TestSuiteLoaderImpl(),
            runnerPrefix = runnerPrefix
        )

        override fun create(): TestsScheduler {
            val testRunner: TestsRunner = createTestRunner()
            val testSuiteProvider: TestSuiteProvider = createTestSuiteProvider()

            return InstrumentationTestsScheduler(
                testsRunner = testRunner,
                params = params,
                reportCoordinates = params.reportCoordinates,
                sourceReport = sourceReport,
                testSuiteProvider = testSuiteProvider,
                testSuiteLoader = testSuiteLoader,
                gson = gson,
                filterInfoWriter = FilterInfoWriter.Impl(
                    outputDir = params.outputDir,
                    gson = gson
                ),
                loggerFactory = params.loggerFactory
            )
        }

        private fun createTestSuiteProvider(): TestSuiteProvider.Impl {
            return TestSuiteProvider.Impl(
                report = sourceReport,
                targets = params.instrumentationConfiguration.targets,
                filterFactory = FilterFactory.create(
                    filterData = params.instrumentationConfiguration.filter,
                    impactAnalysisResult = params.impactAnalysisResult,
                    factory = params.reportFactory,
                    reportConfig = params.reportConfig
                ),
                reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests
            )
        }

        private fun createTestRunner(): TestsRunnerImplementation {
            // todo pass though constructor but needs to be serializable
            val timeProvider = DefaultTimeProvider()

            return TestsRunnerImplementation(
                testExecutorFactory = testExecutorFactory,
                kubernetesCredentials = params.kubernetesCredentials,
                testReporterFactory = { testSuite, outputDir, report ->
                    ReportViewerTestReporter(
                        loggerFactory = params.loggerFactory,
                        timeProvider = timeProvider,
                        testSuite = testSuite,
                        report = report,
                        fileStorageUrl = params.fileStorageUrl,
                        logcatDir = outputDir,
                        retracer = ProguardRetracer.Impl(params.proguardMappings),
                        metricsSender = InstrumentationMetricsSender(
                            statsDSender = StatsDSender.Impl(
                                config = params.statsDConfig,
                                loggerFactory = params.loggerFactory
                            ),
                            runnerPrefix = runnerPrefix
                        )
                    )
                },
                buildId = params.buildId,
                buildType = params.buildType,
                projectName = params.projectName,
                executionParameters = params.executionParameters,
                outputDirectory = params.outputDir,
                instrumentationConfiguration = params.instrumentationConfiguration,
                loggerFactory = params.loggerFactory,
                registry = params.registry,
                metricsConfig = RunnerMetricsConfig(
                    statsDConfig = params.statsDConfig,
                    runnerPrefix = runnerPrefix
                ),
                timeProvider = timeProvider
            )
        }
    }
}
