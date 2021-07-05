package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.devices.DeviceProviderFactoryProvider
import com.avito.android.runner.devices.KubernetesApiProvider
import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactoryProvider
import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.ReportFactoryImpl
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.finalizer.FinalizerFactoryImpl
import com.avito.runner.scheduler.TestRunnerFactoryProvider
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.ProcessRunner

public class TestSchedulerFactoryProvider {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    public fun provide(params: RunnerInputParams): TestSchedulerFactory {

        val httpClientProvider = HttpClientProvider(
            statsDSender = StatsDSender.create(
                config = params.statsDConfig,
                loggerFactory = params.loggerFactory
            ),
            timeProvider = timeProvider,
            loggerFactory = params.loggerFactory
        )

        val metricsConfig = createRunnerMetricsConfig(params)

        val reportFactory = createReportFactory(
            params = params,
            timeProvider = timeProvider,
            httpClientProvider = httpClientProvider
        )

        val report = reportFactory.createReport()
        val processRunner = ProcessRunner.create(null)
        val androidDebugBridgeProvider = AndroidDebugBridgeProvider(params.loggerFactory, processRunner)

        val emulatorsLogsReporterProvider = EmulatorsLogsReporterProvider(
            logcatTags = params.executionParameters.logcatTags,
            outputDir = params.outputDir
        )

        return TestSchedulerFactoryImpl(
            finalizerFactory = FinalizerFactoryImpl(
                report = report,
                reportFactory = reportFactory,
                metricsConfig = metricsConfig,
                timeProvider = timeProvider,
                loggerFactory = params.loggerFactory,
                reportViewerConfig = params.reportViewerConfig,
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky,
                verdictFile = params.verdictFile,
                outputDir = params.outputDir,
            ),
            report = report,
            testSuiteProvider = TestSuiteProvider.Impl(
                report = report,
                targets = params.instrumentationConfiguration.targets,
                reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
                filterFactory = FilterFactory.create(
                    filterData = params.instrumentationConfiguration.filter,
                    impactAnalysisResult = params.impactAnalysisResult,
                    reportFactory = reportFactory,
                    loggerFactory = params.loggerFactory
                )
            ),
            testRunnerFactoryProvider = TestRunnerFactoryProvider(
                params = params,
                timeProvider = timeProvider,
                httpClientProvider = httpClientProvider,
                report = report,
                devicesProviderFactory = DeviceProviderFactoryProvider(
                    loggerFactory = params.loggerFactory,
                    timeProvider = timeProvider,
                    deviceType = params.instrumentationConfiguration.requestedDeviceType,
                    processRunner = processRunner,
                    kubernetesReservationClientProvider = KubernetesReservationClientProvider(
                        loggerFactory = params.loggerFactory,
                        kubernetesApiProvider = KubernetesApiProvider(
                            timeProvider = timeProvider,
                            kubernetesNamespace = params.executionParameters.namespace,
                            kubernetesCredentials = params.kubernetesCredentials,
                            loggerFactory = params.loggerFactory,
                            statsDConfig = params.statsDConfig
                        ),
                        androidDebugBridgeProvider = androidDebugBridgeProvider,
                        reservationDeploymentFactoryProvider = ReservationDeploymentFactoryProvider(
                            configurationName = params.instrumentationConfiguration.name,
                            projectName = params.projectName,
                            buildId = params.buildId,
                            buildType = params.buildType,
                            loggerFactory = params.loggerFactory
                        ),
                        emulatorsLogsReporterProvider = emulatorsLogsReporterProvider
                    ),
                    androidDebugBridgeProvider = androidDebugBridgeProvider,
                    emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
                    metricsConfig = metricsConfig
                ).provide(),
                metricsConfig = metricsConfig
            ),
            testSuiteLoader = TestSuiteLoader.create(),
            loggerFactory = params.loggerFactory,
            fileInfoWriter = FilterInfoWriter.Impl(
                outputDir = params.outputDir,
            ),
            filter = params.instrumentationConfiguration.filter,
            testApk = params.testApk,
            outputDir = params.outputDir,
        )
    }

    private fun createRunnerMetricsConfig(params: RunnerInputParams): RunnerMetricsConfig {
        return RunnerMetricsConfig(
            statsDConfig = params.statsDConfig,
            runnerPrefix = SeriesName.create(
                "testrunner",
                params.projectName,
                params.instrumentationConfiguration.name
            )
        )
    }

    private fun createReportFactory(
        params: RunnerInputParams,
        timeProvider: TimeProvider,
        httpClientProvider: HttpClientProvider,
    ): ReportFactory {
        return ReportFactoryImpl(
            timeProvider = timeProvider,
            buildId = params.buildId,
            loggerFactory = params.loggerFactory,
            httpClientProvider = httpClientProvider,
            reportViewerConfig = params.reportViewerConfig
        )
    }
}
