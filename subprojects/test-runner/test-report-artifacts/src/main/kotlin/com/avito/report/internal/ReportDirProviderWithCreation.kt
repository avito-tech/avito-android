package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ApplicationDirProvider
import com.avito.report.TestDirGenerator

internal class ReportDirProviderWithCreation(
    rootDir: ApplicationDirProvider,
    testDirGenerator: TestDirGenerator,
) : ReportDirProviderByTest(rootDir, testDirGenerator) {

    override val reportDir by lazy {
        Result.tryCatch {
            val root = rootDir.dir
            require(root.exists()) {
                "Root dir $root doesn't exist"
            }
            if (!runnerDirectory.exists()) {
                require(runnerDirectory.mkdir()) {
                    "Failed to create runner dir $runnerDirectory"
                }
            }
            if (!testReportDir.exists()) {
                require(testReportDir.mkdir()) {
                    "Failed to create a test report dir $testReportDir"
                }
            }
            testReportDir
        }
    }
}
