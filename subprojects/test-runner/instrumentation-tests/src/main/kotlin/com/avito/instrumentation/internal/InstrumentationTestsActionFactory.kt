package com.avito.instrumentation.internal

import com.avito.android.TestSuiteLoaderImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.devices.KubernetesApiProvider
import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactoryProvider
import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.ReportFactoryImpl
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.finalizer.Finalizer
import com.avito.runner.finalizer.FinalizerFactory
import com.avito.runner.finalizer.FinalizerFactoryImpl
import com.avito.runner.scheduler.TestRunnerFactoryProvider
import com.avito.runner.scheduler.runner.scheduler.TestScheduler
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerFactory
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerFactoryImpl
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider

internal interface InstrumentationTestsActionFactory {

    fun provideFinalizer(): Finalizer

    fun provideScheduler(): TestScheduler

    class Impl(
        params: InstrumentationTestsActionParams,
        metricsConfig: RunnerMetricsConfig
    ) : InstrumentationTestsActionFactory {

        private val schedulerFactory: TestSchedulerFactory

        private val finalizerFactory: FinalizerFactory

        init {
            val timeProvider = DefaultTimeProvider()

            val statsdSender = StatsDSender.Impl(
                config = metricsConfig.statsDConfig,
                loggerFactory = params.loggerFactory
            )

            val httpClientProvider = HttpClientProvider(
                statsDSender = statsdSender,
                timeProvider = timeProvider,
                loggerFactory = params.loggerFactory
            )

            val reportFactory: ReportFactory = ReportFactoryImpl(
                timeProvider = timeProvider,
                useInMemoryReport = params.useInMemoryReport,
                buildId = params.buildId,
                loggerFactory = params.loggerFactory,
                httpClientProvider = httpClientProvider,
                reportViewerConfig = params.reportViewerConfig
            )

            val report = reportFactory.createReport()

            val androidDebugBridgeProvider = AndroidDebugBridgeProvider(
                loggerFactory = params.loggerFactory,
            )
            val emulatorsLogsReporterProvider = EmulatorsLogsReporterProvider(
                logcatTags = params.executionParameters.logcatTags,
                outputDir = params.outputDir
            )
            this.schedulerFactory = TestSchedulerFactoryImpl(
                params = params,
                report = report,
                testSuiteLoader = TestSuiteLoaderImpl(),
                reportFactory = reportFactory,
                testRunnerFactoryProvider = TestRunnerFactoryProvider(
                    params = params,
                    timeProvider = timeProvider,
                    httpClientProvider = httpClientProvider,
                    report = report,
                    devicesProviderFactory = DevicesProviderFactory.create(
                        loggerFactory = params.loggerFactory,
                        timeProvider = timeProvider,
                        deviceType = params.instrumentationConfiguration.requestedDeviceType,
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
                    ),
                    metricsConfig = metricsConfig
                )
            )

            this.finalizerFactory = FinalizerFactoryImpl(
                params = params,
                metricsConfig = metricsConfig,
                reportFactory = reportFactory,
                timeProvider = timeProvider,
                loggerFactory = params.loggerFactory,
            )
        }

        override fun provideScheduler() = schedulerFactory.create()

        override fun provideFinalizer() = finalizerFactory.create()
    }
}
