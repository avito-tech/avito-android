package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.TestRuntimeData
import java.io.File

internal interface ReportParser {

    fun parse(reportFile: File): Result<TestRuntimeData>
}
