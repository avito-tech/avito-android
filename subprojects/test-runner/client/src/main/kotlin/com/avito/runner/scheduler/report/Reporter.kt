package com.avito.runner.scheduler.report

import com.avito.runner.scheduler.report.model.SummaryReport

internal interface Reporter {

    fun report(report: SummaryReport)
}
