package com.avito.instrumentation.internal

import com.avito.android.TestSuiteLoaderImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.ReportFactoryImpl
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.finalizer.FinalizerFactory
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.instrumentation.internal.scheduling.TestsSchedulerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.DefaultTimeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal interface InstrumentationTestsActionFactory {

    fun provideFinalizer(): InstrumentationTestActionFinalizer

    fun provideScheduler(devicesProviderFactory: DevicesProviderFactory): TestsScheduler

    class Impl(
        params: InstrumentationTestsAction.Params,
        metricsConfig: RunnerMetricsConfig
    ) : InstrumentationTestsActionFactory {

        private val gson: Gson = Companion.gson

        private val schedulerFactory: TestsSchedulerFactory

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

            this.schedulerFactory = TestsSchedulerFactory.Impl(
                params = params,
                report = report,
                gson = gson,
                metricsConfig = metricsConfig,
                testExecutorFactory = TestExecutorFactory.Implementation(),
                testSuiteLoader = TestSuiteLoaderImpl(),
                timeProvider = timeProvider,
                httpClientProvider = httpClientProvider,
                reportFactory = reportFactory
            )

            this.finalizerFactory = FinalizerFactory.Impl(
                params = params,
                gson = gson,
                metricsConfig = metricsConfig,
                reportFactory = reportFactory,
                timeProvider = timeProvider
            )
        }

        override fun provideScheduler(devicesProviderFactory: DevicesProviderFactory) =
            schedulerFactory.create(devicesProviderFactory)

        override fun provideFinalizer() = finalizerFactory.create()
    }

    companion object {
        val gson: Gson by lazy {
            GsonBuilder()
                .setPrettyPrinting()
                .create()
        }
    }
}
