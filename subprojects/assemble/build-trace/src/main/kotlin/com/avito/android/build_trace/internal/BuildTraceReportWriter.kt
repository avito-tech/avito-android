package com.avito.android.build_trace.internal

import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import java.io.File

internal class BuildTraceReportWriter(
    private val output: File,
) {

    private val lock = Any()

    private var isFinalized = false

    fun write(report: TraceReport) {
        synchronized(lock) {
            if (isFinalized) return

            output.parentFile?.mkdirs()
            TraceReportFileAdapter(output).write(report)
            isFinalized = true
        }
    }
}
