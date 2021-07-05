package com.avito.runner.finalizer

import com.avito.android.stats.StatsDSender
import com.avito.logger.LoggerFactory
import com.avito.report.Report
import com.avito.runner.finalizer.action.FinalizeAction
import com.avito.runner.finalizer.action.ReportLostTestsAction
import com.avito.runner.finalizer.action.SendMetricsAction
import com.avito.runner.finalizer.action.WriteJUnitReportAction
import com.avito.runner.finalizer.action.WriteReportViewerLinkFile
import com.avito.runner.finalizer.action.WriteTaskVerdictAction
import com.avito.runner.finalizer.verdict.VerdictDeterminer
import com.avito.runner.finalizer.verdict.VerdictDeterminerImpl
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.report.ReportViewerConfig
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import java.io.File

internal class FinalizerFactoryImpl(
    private val report: Report,
    private val metricsConfig: RunnerMetricsConfig,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val reportViewerConfig: ReportViewerConfig?,
    private val suppressFailure: Boolean,
    private val suppressFlaky: Boolean,
    private val verdictFile: File,
    private val outputDir: File,
) : FinalizerFactory {

    override fun create(): Finalizer {

        val metricsSender = InstrumentationMetricsSender(
            statsDSender = StatsDSender.create(metricsConfig.statsDConfig, loggerFactory),
            runnerPrefix = metricsConfig.runnerPrefix
        )

        return createFinalizer(
            metricsSender = metricsSender
        )
    }

    private fun createFinalizer(
        metricsSender: InstrumentationMetricsSender,
    ): FinalizerImpl {

        val verdictDeterminer: VerdictDeterminer = VerdictDeterminerImpl(
            suppressFailure = suppressFailure,
            suppressFlaky = suppressFlaky,
            timeProvider = timeProvider
        )

        val actions = mutableListOf<FinalizeAction>()

        actions += SendMetricsAction(metricsSender)

        actions += WriteTaskVerdictAction(
            verdictDestination = verdictFile,
            reportLinksGenerator = report.reportLinksGenerator
        )

        actions += WriteJUnitReportAction(
            destination = File(outputDir, "junit-report.xml"),
            reportLinksGenerator = report.reportLinksGenerator,
            testSuiteNameProvider = report.testSuiteNameProvider
        )

        if (reportViewerConfig != null) {

            actions += ReportLostTestsAction(report = report)

            actions += WriteReportViewerLinkFile(
                outputDir = outputDir,
                reportLinksGenerator = report.reportLinksGenerator
            )
        }

        return FinalizerImpl(
            actions = actions,
            verdictFile = verdictFile,
            verdictDeterminer = verdictDeterminer,
            finalizerFileDumper = FinalizerFileDumperImpl(
                outputDir = outputDir,
                loggerFactory = loggerFactory
            )
        )
    }
}
