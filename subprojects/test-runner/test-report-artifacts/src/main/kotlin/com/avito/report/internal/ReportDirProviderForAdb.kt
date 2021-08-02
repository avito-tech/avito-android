package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ApplicationDirProvider
import com.avito.report.TestDirGenerator
import java.io.File

internal class ReportDirProviderForAdb(
    rootDir: ApplicationDirProvider,
    testDirGenerator: TestDirGenerator,
) : ReportDirProviderByTest(rootDir, testDirGenerator) {

    override val reportDir: Result<File> = Result.tryCatch { testReportDir }
}
