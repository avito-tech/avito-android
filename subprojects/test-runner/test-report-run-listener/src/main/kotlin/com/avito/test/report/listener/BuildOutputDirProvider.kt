package com.avito.test.report.listener

import com.avito.android.Result
import com.avito.report.ReportDirProvider
import java.io.File

internal class BuildOutputDirProvider(file: File) : ReportDirProvider {
    override val reportDir: Result<File> = Result.Success(file)
}
