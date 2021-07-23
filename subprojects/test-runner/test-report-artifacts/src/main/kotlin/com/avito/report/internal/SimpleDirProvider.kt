package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ReportDirProvider
import java.io.File

internal class SimpleDirProvider(reportDir: File) : ReportDirProvider {
    override val reportDir: Result<File> = Result.Success(reportDir)
}
