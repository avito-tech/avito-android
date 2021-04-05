package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ReportFileProvider
import java.io.File
import java.util.UUID

class ReportFileProviderImpl(
    override val rootDir: Lazy<File>,
    private val className: String,
    private val methodName: String,
    private val randomFileNameGenerator: () -> String = { UUID.randomUUID().toString() }
) : ReportFileProvider {

    // todo should be passed with instrumentation params, see [ArtifactsTestListener]
    private val runnerOutputFolder = "runner"

    // todo should be passed with instrumentation params, see [ReportViewerTestReporter]
    private val reportFileName = "report.json"

    override fun provideReportDir(): File {
        val testFolderName = "$className#$methodName" // todo add deviceName

        val runnerDirectory = File(rootDir.value, runnerOutputFolder)

        return File(runnerDirectory, testFolderName)
    }

    override fun provideReportFile(): File {
        return File(provideReportDir(), reportFileName)
    }

    override fun getFile(relativePath: String): File {
        return File(provideReportDir(), relativePath)
    }

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> {
        return Result.tryCatch {
            File(provideReportDir(), "$name.$extension").apply {
                parentFile?.mkdirs()
                createNewFile()
            }
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return generateFile(name = randomFileNameGenerator.invoke(), extension = extension, create = create)
    }
}
