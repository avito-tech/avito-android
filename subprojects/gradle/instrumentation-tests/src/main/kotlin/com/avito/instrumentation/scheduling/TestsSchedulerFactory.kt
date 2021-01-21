package com.avito.instrumentation.scheduling

import com.avito.android.TestSuiteLoader
import com.avito.android.TestSuiteLoaderImpl
import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.InstrumentationTestsActionFactory
import com.avito.instrumentation.executing.TestExecutorFactory
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.listener.ReportViewerTestReporter
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.instrumentation.suite.filter.FilterInfoWriter
import com.avito.retrace.ProguardRetracer
import com.avito.time.DefaultTimeProvider
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson

interface TestsSchedulerFactory {

    fun create(): TestsScheduler

    class Impl : TestsSchedulerFactory {

        private val params: InstrumentationTestsAction.Params
        private val sourceReport: Report
        private val gson: Gson
        private val testExecutorFactory: TestExecutorFactory
        private val testSuiteLoader: TestSuiteLoader

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson = InstrumentationTestsActionFactory.gson,
            testExecutorFactory: TestExecutorFactory,
            testSuiteLoader: TestSuiteLoader
        ) {
            this.params = params
            this.sourceReport = sourceReport
            this.gson = gson
            this.testExecutorFactory = testExecutorFactory
            this.testSuiteLoader = testSuiteLoader
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson
        ) : this(
            params = params,
            sourceReport = sourceReport,
            gson = gson,
            testExecutorFactory = TestExecutorFactory.Implementation(),
            testSuiteLoader = TestSuiteLoaderImpl()
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
                )
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
            return TestsRunnerImplementation(
                testExecutorFactory = testExecutorFactory,
                kubernetesCredentials = params.kubernetesCredentials,
                testReporterFactory = { testSuite, outputDir, report ->
                    ReportViewerTestReporter(
                        loggerFactory = params.loggerFactory,
                        // todo pass though constructor but needs to be serializable
                        timeProvider = DefaultTimeProvider(params.loggerFactory),
                        testSuite = testSuite,
                        report = report,
                        fileStorageUrl = params.fileStorageUrl,
                        logcatDir = outputDir,
                        retracer = ProguardRetracer.Impl(params.proguardMappings)
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
                statsDConfig = params.statsDConfig
            )
        }
    }
}
