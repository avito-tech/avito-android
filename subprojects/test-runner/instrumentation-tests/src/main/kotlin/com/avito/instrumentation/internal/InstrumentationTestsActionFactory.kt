package com.avito.instrumentation.internal

import com.avito.android.TestSuiteLoaderImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.ReportFactoryImpl
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.finalizer.Finalizer
import com.avito.runner.finalizer.FinalizerFactory
import com.avito.runner.finalizer.FinalizerFactoryImpl
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

        private val devicesProviderFactory: DevicesProviderFactory

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

            this.devicesProviderFactory = DevicesProviderFactory.create(
                loggerFactory = params.loggerFactory,
                timeProvider = timeProvider,
                deviceType = params.instrumentationConfiguration.requestedDeviceType,
                kubernetesNamespace = params.executionParameters.namespace,
                credentials = params.kubernetesCredentials,
                statsDConfig = params.statsDConfig,
                configurationName = params.instrumentationConfiguration.name,
                projectName = params.projectName,
                buildId = params.buildId,
                buildType = params.buildType,
                logcatTags = params.executionParameters.logcatTags,
                outputDir = params.outputDir,
                metricsConfig = metricsConfig
            )
            this.schedulerFactory = TestSchedulerFactoryImpl(
                params = params,
                report = report,
                timeProvider = timeProvider,
                httpClientProvider = httpClientProvider,
                metricsConfig = metricsConfig,
                testSuiteLoader = TestSuiteLoaderImpl(),
                reportFactory = reportFactory,
                devicesProviderFactory = devicesProviderFactory
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
