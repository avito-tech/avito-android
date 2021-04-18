package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ReportFileProvider
import com.avito.report.TestDirGenerator
import java.io.File
import java.util.UUID

class ReportFileProviderImpl(
    override val rootDir: Lazy<File>,
    private val testDirGenerator: TestDirGenerator,
    private val uniqueFileNameGenerator: () -> String = { UUID.randomUUID().toString() }
) : ReportFileProvider {

    private val runnerOutputFolder = "runner"

    private val reportFileName = "report.json"

    override fun provideReportDir(): Result<File> {
        val testFolderName = testDirGenerator.generateUniqueDir()

        val runnerDirectory = File(rootDir.value, runnerOutputFolder)

        return Result.tryCatch {
            File(runnerDirectory, testFolderName).apply {
                parentFile?.mkdirs()
                mkdir()
            }
        }
    }

    override fun provideReportFile(): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, reportFileName)
        }
    }

    override fun getFile(relativePath: String): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, relativePath)
        }
    }

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, "$name.$extension")
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return generateFile(name = uniqueFileNameGenerator.invoke(), extension = extension, create = create)
    }
}
