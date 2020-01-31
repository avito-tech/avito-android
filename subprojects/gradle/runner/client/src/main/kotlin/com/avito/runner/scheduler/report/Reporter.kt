package com.avito.runner.scheduler.report

import com.avito.runner.scheduler.report.model.SummaryReport

interface Reporter {
    fun report(report: SummaryReport)
}
