package com.avito.report

import com.avito.android.Result
import java.io.File

public interface ReportDirProvider {
    public val reportDir: Result<File>
}

internal class SimpleDirProvider(reportDir: File) : ReportDirProvider {
    override val reportDir: Result<File> = Result.Success(reportDir)
}

internal abstract class ReportDirProviderByTest(
    rootDir: Lazy<File>,
    testDirGenerator: TestDirGenerator,
) : ReportDirProvider {

    protected val runnerDirectory: File by lazy {
        File(rootDir.value, "runner")
    }

    protected val testReportDir: File by lazy {
        File(runnerDirectory, testDirGenerator.generateUniqueDir())
    }
}

internal class ReportDirProviderForAdb(
    rootDir: Lazy<File>,
    testDirGenerator: TestDirGenerator,
) : ReportDirProviderByTest(rootDir, testDirGenerator) {

    override val reportDir: Result<File> = Result.tryCatch { testReportDir }
}

internal class ReportDirProviderWithCreation(
    rootDir: Lazy<File>,
    testDirGenerator: TestDirGenerator,
) : ReportDirProviderByTest(rootDir, testDirGenerator) {

    override val reportDir by lazy {
        Result.tryCatch {
            val root = rootDir.value
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
