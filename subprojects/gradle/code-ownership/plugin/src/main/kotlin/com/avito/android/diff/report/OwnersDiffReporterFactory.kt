package com.avito.android.diff.report

import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.report.file.FileOwnersDiffReporter

public class OwnersDiffReporterFactory(private val messageFormatter: OwnersDiffMessageFormatter) {

    public fun create(destination: OwnersDiffReportDestination): OwnersDiffReporter {
        return when (destination) {
            is OwnersDiffReportDestination.Custom -> destination.reporter
            is OwnersDiffReportDestination.File -> FileOwnersDiffReporter(destination.parentDir, messageFormatter)
        }
    }
}
