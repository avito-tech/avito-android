package com.avito.report.internal

import com.avito.report.ApplicationDirProvider
import com.avito.report.ReportDirProvider
import com.avito.report.TestDirGenerator
import java.io.File

internal abstract class ReportDirProviderByTest(
    rootDir: ApplicationDirProvider,
    testDirGenerator: TestDirGenerator,
) : ReportDirProvider {

    protected val runnerDirectory: File by lazy {
        File(rootDir.dir, "runner")
    }

    protected val testReportDir: File by lazy {
        File(runnerDirectory, testDirGenerator.generateUniqueDir())
    }
}
