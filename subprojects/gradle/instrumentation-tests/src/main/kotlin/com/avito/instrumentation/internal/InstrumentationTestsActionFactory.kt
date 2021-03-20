package com.avito.instrumentation.internal

import com.avito.android.TestSuiteLoaderImpl
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
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

        private val sourceReport: Report = params.reportFactory.createReport(params.reportConfig)

        private val schedulerFactory: TestsSchedulerFactory

        private val finalizerFactory: FinalizerFactory

        init {
            val timeProvider = DefaultTimeProvider()

            this.schedulerFactory = TestsSchedulerFactory.Impl(
                params = params,
                sourceReport = sourceReport,
                gson = gson,
                timeProvider = timeProvider,
                metricsConfig = metricsConfig,
                testExecutorFactory = TestExecutorFactory.Implementation(),
                testSuiteLoader = TestSuiteLoaderImpl()
            )

            this.finalizerFactory = FinalizerFactory.Impl(
                params = params,
                sourceReport = sourceReport,
                gson = gson,
                metricsConfig = metricsConfig
            )
        }

        override fun provideScheduler(devicesProviderFactory: DevicesProviderFactory) =
            schedulerFactory.create(devicesProviderFactory)

        override fun provideFinalizer() = finalizerFactory.create()
    }

    companion object {
        val gson: Gson by lazy {
            GsonBuilder()
                .registerTypeHierarchyAdapter(TestRunResult.Verdict::class.java, TestRunResultVerdictJsonSerializer)
                .setPrettyPrinting()
                .create()
        }
    }
}
