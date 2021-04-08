package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.Report
import com.avito.android.stats.StatsDSender
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.report.WriteJUnitReportAction
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.report.ReportViewer
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.utils.BuildFailer
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson
import java.io.File

internal interface FinalizerFactory {

    fun create(): InstrumentationTestActionFinalizer

    class Impl : FinalizerFactory {
        private val params: InstrumentationTestsAction.Params
        private val sourceReport: Report
        private val gson: Gson
        private val buildFailer: BuildFailer

        // todo Make generic. Need two realization for InMemory and ReportViewer
        private val reportViewer: ReportViewer
        private val loggerFactory: LoggerFactory
        private val metricsConfig: RunnerMetricsConfig

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson = InstrumentationTestsActionFactory.gson,
            buildFailer: BuildFailer,
            metricsConfig: RunnerMetricsConfig
        ) {
            this.params = params
            this.sourceReport = sourceReport
            this.gson = gson
            this.reportViewer = ReportViewer.Impl(params.reportViewerUrl)
            this.loggerFactory = params.loggerFactory
            this.buildFailer = buildFailer
            this.metricsConfig = metricsConfig
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson,
            metricsConfig: RunnerMetricsConfig
        ) : this(
            params = params,
            sourceReport = sourceReport,
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

            val actions = listOf(
                SendMetricsAction(metricsSender),
                WriteJUnitReportAction(
                    reportViewer = reportViewer,
                    reportCoordinates = params.reportCoordinates,
                    // For Teamcity XML report processing
                    destination = File(params.outputDir, "junit-report.xml")
                ),
                ReportFinishAction(sourceReport),
                WriteReportViewerLinkFile(
                    reportViewer = reportViewer,
                    reportCoordinates = params.reportCoordinates,
                    outputDir = params.outputDir
                ),
                WriteTaskVerdictAction(
                    coordinates = params.reportCoordinates,
                    verdictDestination = params.verdictFile,
                    reportViewer = reportViewer,
                    gson = gson,
                )
            )
            return InstrumentationTestActionFinalizer.Impl(
                hasFailedTestDeterminer = hasFailedTestDeterminer,
                hasNotReportedTestsDeterminer = HasNotReportedTestsDeterminer.Impl(),
                params = params,
                buildFailer = buildFailer,
                actions = actions
            )
        }
    }
}
