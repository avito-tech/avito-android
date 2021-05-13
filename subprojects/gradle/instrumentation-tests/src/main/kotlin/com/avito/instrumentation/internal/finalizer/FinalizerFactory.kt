package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.ReportFactory
import com.avito.android.stats.StatsDSender
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.finalizer.action.AvitoReportViewerFinishAction
import com.avito.instrumentation.internal.finalizer.action.FinalizeAction
import com.avito.instrumentation.internal.finalizer.action.LegacyAvitoReportViewerFinishAction
import com.avito.instrumentation.internal.finalizer.action.LegacyFinalizeAction
import com.avito.instrumentation.internal.finalizer.action.LegacySendMetricsAction
import com.avito.instrumentation.internal.finalizer.action.LegacyWriteJUnitReportAction
import com.avito.instrumentation.internal.finalizer.action.LegacyWriteReportViewerLinkFile
import com.avito.instrumentation.internal.finalizer.action.LegacyWriteTaskVerdictAction
import com.avito.instrumentation.internal.finalizer.action.SendMetricsAction
import com.avito.instrumentation.internal.finalizer.action.WriteJUnitReportAction
import com.avito.instrumentation.internal.finalizer.action.WriteReportViewerLinkFile
import com.avito.instrumentation.internal.finalizer.action.WriteTaskVerdictAction
import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.LegacyFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.LegacyNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdictDeterminerFactory
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminerImpl
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.logger.LoggerFactory
import com.avito.report.ReportLinkGenerator
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
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
        private val timeProvider: TimeProvider

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            reportFactory: ReportFactory,
            gson: Gson = InstrumentationTestsActionFactory.gson,
            buildFailer: BuildFailer,
            metricsConfig: RunnerMetricsConfig,
            timeProvider: TimeProvider,
        ) {
            this.params = params
            this.reportFactory = reportFactory
            this.gson = gson
            this.loggerFactory = params.loggerFactory
            this.buildFailer = buildFailer
            this.metricsConfig = metricsConfig
            this.timeProvider = timeProvider
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            reportFactory: ReportFactory,
            gson: Gson,
            metricsConfig: RunnerMetricsConfig,
            timeProvider: TimeProvider,
        ) : this(
            params = params,
            reportFactory = reportFactory,
            gson = gson,
            buildFailer = BuildFailer.RealFailer(),
            metricsConfig = metricsConfig,
            timeProvider = timeProvider
        )

        override fun create(): InstrumentationTestActionFinalizer {

            val metricsSender = InstrumentationMetricsSender(
                statsDSender = StatsDSender.Impl(metricsConfig.statsDConfig, loggerFactory),
                runnerPrefix = metricsConfig.runnerPrefix
            )

            val reportLinkGenerator = reportFactory.createReportLinkGenerator()

            return if (params.useInMemoryReport) {
                createFinalizer(
                    reportLinkGenerator = reportLinkGenerator,
                    metricsSender = metricsSender
                )
            } else {
                createLegacyFinalizer(
                    params = params,
                    timeProvider = timeProvider,
                    reportLinkGenerator = reportLinkGenerator,
                    metricsSender = metricsSender
                )
            }
        }

        private fun createFinalizer(
            reportLinkGenerator: ReportLinkGenerator,
            metricsSender: InstrumentationMetricsSender,
        ): InstrumentationTestActionFinalizerImpl {

            val verdictDeterminer: VerdictDeterminer = VerdictDeterminerImpl(
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky,
                timeProvider = timeProvider
            )

            val actions = mutableListOf<FinalizeAction>()

            actions += SendMetricsAction(metricsSender)

            actions += WriteTaskVerdictAction(
                verdictDestination = params.verdictFile,
                gson = gson,
                reportLinkGenerator = reportLinkGenerator
            )

            actions += WriteJUnitReportAction(
                destination = File(params.outputDir, "junit-report.xml"),
                reportLinkGenerator = reportLinkGenerator,
                testSuiteNameProvider = reportFactory.createTestSuiteNameGenerator()
            )

            if (params.reportViewerConfig != null) {

                actions += AvitoReportViewerFinishAction(legacyReport = reportFactory.createAvitoReport())

                actions += WriteReportViewerLinkFile(
                    outputDir = params.outputDir,
                    reportLinkGenerator = reportLinkGenerator
                )
            }

            return InstrumentationTestActionFinalizerImpl(
                actions = actions,
                buildFailer = buildFailer,
                verdictFile = params.verdictFile,
                verdictDeterminer = verdictDeterminer
            )
        }

        private fun createLegacyFinalizer(
            params: InstrumentationTestsAction.Params,
            timeProvider: TimeProvider,
            reportLinkGenerator: ReportLinkGenerator,
            metricsSender: InstrumentationMetricsSender,
        ): LegacyFinalizer {
            val hasFailedTestDeterminer: HasFailedTestDeterminer = LegacyFailedTestDeterminer(
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky
            )

            val verdictDeterminer = LegacyVerdictDeterminerFactory.create()

            val actions = mutableListOf<LegacyFinalizeAction>()

            actions += LegacySendMetricsAction(metricsSender)

            actions += LegacyWriteTaskVerdictAction(
                verdictDestination = params.verdictFile,
                gson = gson,
                reportLinkGenerator = reportLinkGenerator
            )

            actions += LegacyWriteJUnitReportAction(
                destination = File(params.outputDir, "junit-report.xml"),
                testSuiteNameProvider = reportFactory.createTestSuiteNameGenerator(),
                reportLinkGenerator = reportLinkGenerator,
            )

            if (params.reportViewerConfig != null) {

                actions += LegacyAvitoReportViewerFinishAction(legacyReport = reportFactory.createAvitoReport())

                actions += LegacyWriteReportViewerLinkFile(
                    outputDir = params.outputDir,
                    reportLinkGenerator = reportLinkGenerator
                )
            }

            return LegacyFinalizer(
                hasFailedTestDeterminer = hasFailedTestDeterminer,
                hasNotReportedTestsDeterminer = LegacyNotReportedTestsDeterminer(
                    timeProvider = timeProvider
                ),
                legacyVerdictDeterminer = verdictDeterminer,
                actions = actions,
                buildFailer = buildFailer,
                params = params,
                loggerFactory = params.loggerFactory,
                report = reportFactory.createAvitoReport()
            )
        }
    }
}
