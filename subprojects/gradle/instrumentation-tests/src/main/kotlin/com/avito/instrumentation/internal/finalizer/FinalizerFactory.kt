package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.ReportFactory
import com.avito.android.stats.StatsDSender
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.report.WriteJUnitReportAction
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.utils.BuildFailer
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson
import java.io.File

internal interface FinalizerFactory {

    fun create(): InstrumentationTestActionFinalizer

    class Impl : FinalizerFactory {
        private val params: InstrumentationTestsAction.Params
        private val reportFactory: ReportFactory
        private val gson: Gson
        private val buildFailer: BuildFailer
        private val loggerFactory: LoggerFactory
        private val metricsConfig: RunnerMetricsConfig

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            reportFactory: ReportFactory,
            gson: Gson = InstrumentationTestsActionFactory.gson,
            buildFailer: BuildFailer,
            metricsConfig: RunnerMetricsConfig
        ) {
            this.params = params
            this.reportFactory = reportFactory
            this.gson = gson
            this.loggerFactory = params.loggerFactory
            this.buildFailer = buildFailer
            this.metricsConfig = metricsConfig
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            reportFactory: ReportFactory,
            gson: Gson,
            metricsConfig: RunnerMetricsConfig
        ) : this(
            params = params,
            reportFactory = reportFactory,
            gson = gson,
            buildFailer = BuildFailer.RealFailer(),
            metricsConfig = metricsConfig
        )

        override fun create(): InstrumentationTestActionFinalizer {

            val hasFailedTestDeterminer: HasFailedTestDeterminer = HasFailedTestDeterminer.Impl(
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky
            )

            val metricsSender = InstrumentationMetricsSender(
                statsDSender = StatsDSender.Impl(metricsConfig.statsDConfig, loggerFactory),
                runnerPrefix = metricsConfig.runnerPrefix
            )

            val reportLinkGenerator = reportFactory.createReportLinkGenerator()

            val actions = mutableListOf(

                SendMetricsAction(metricsSender),

                WriteJUnitReportAction(
                    // For Teamcity XML report processing
                    destination = File(params.outputDir, "junit-report.xml"),
                    reportLinkGenerator = reportLinkGenerator,
                    testSuiteNameProvider = reportFactory.createTestSuiteNameGenerator()
                ),

                WriteTaskVerdictAction(
                    verdictDestination = params.verdictFile,
                    gson = gson,
                    reportLinkGenerator = reportLinkGenerator
                )
            )

            if (params.reportViewerConfig != null) {

                actions += AvitoReportViewerFinishAction(avitoReport = reportFactory.createAvitoReport())

                actions += WriteReportViewerLinkFile(
                    outputDir = params.outputDir,
                    reportLinkGenerator = reportLinkGenerator
                )
            }

            return InstrumentationTestActionFinalizer.Impl(
                hasFailedTestDeterminer = hasFailedTestDeterminer,
                hasNotReportedTestsDeterminer = HasNotReportedTestsDeterminer.Impl(),
                params = params,
                buildFailer = buildFailer,
                actions = actions,
                report = reportFactory.createReport() // todo pass report from constructor
            )
        }
    }
}
