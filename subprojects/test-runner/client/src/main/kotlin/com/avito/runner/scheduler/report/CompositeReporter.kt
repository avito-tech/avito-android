package com.avito.runner.scheduler.report

import com.avito.runner.scheduler.report.model.SummaryReport

internal class CompositeReporter(
    private val reporters: Collection<Reporter>
) : Reporter {

    override fun report(report: SummaryReport) {
        reporters.forEach { it.report(report) }
    }
}
