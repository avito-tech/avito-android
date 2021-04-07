package com.avito.android.test.report.transport

import com.avito.android.Result
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

    override fun toUploadPlaceholder(file: File): String {
        return "#upload:${file.name}"
    }

    override fun fromUploadPlaceholder(placeholder: String): String {
        return placeholder.replace("#upload:", "")
    }

    override fun provideReportDir(): Result<File> {
        return try {
            val testFolderName = "$className#$methodName" // todo add deviceName

            val runnerDirectory = File(rootDir.value, runnerOutputFolder)

            val testMetadataDirectory = File(runnerDirectory, testFolderName).apply {
                mkdirs()
            }

            Result.Success(testMetadataDirectory)
        } catch (e: Throwable) {
            Result.Failure(e)
        }
    }

    override fun provideReportFile(): Result<File> {
        return provideReportDir().map { File(it, reportFileName) }
    }

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> {
        return provideReportDir().map {
            val file = File(it, "$name.$extension")
            if (create) {
                file.createNewFile()
            }
            file
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return generateFile(name = randomFileNameGenerator.invoke(), extension = extension, create = create)
    }
}
