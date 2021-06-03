package com.avito.runner.scheduler.report.trace

import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.TraceEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.model.SummaryReport
import com.avito.runner.service.model.TestCaseRun
import java.io.File
import java.util.concurrent.TimeUnit

internal class TraceReporter(
    private val runName: String,
    private val outputDirectory: File
) : Reporter {

    override fun report(report: SummaryReport) {
        val traceReport = makeTraceReport(report)

        outputDirectory.mkdirs()
        val outputFile = File(outputDirectory, "report.trace")
        TraceReportFileAdapter(outputFile).write(traceReport)
    }

    private fun makeTraceReport(report: SummaryReport): TraceReport {
        val events: List<TraceEvent> = report.reports
            .flatMap { it.runs }
            .map {
                CompleteEvent(
                    timestampMicroseconds = TimeUnit.MILLISECONDS.toMicros(it.testCaseRun.timestampStartedMilliseconds),
                    durationMicroseconds = TimeUnit.MILLISECONDS.toMicros(it.testCaseRun.durationMilliseconds),
                    processId = runName,
                    threadId = "${it.device.serial} (${it.device.configuration.api})",
                    eventName = it.testCaseRun.test.testName,
                    color = when (it.testCaseRun.result) {
                        is TestCaseRun.Result.Passed -> TraceEvent.COLOR_GOOD
                        is TestCaseRun.Result.Failed -> TraceEvent.COLOR_BAD
                        is TestCaseRun.Result.Ignored -> TraceEvent.COLOR_GREY
                    }
                )
            }
            .toList()

        return TraceReport(traceEvents = events)
    }
}
