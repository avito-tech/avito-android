package com.avito.android.test.report.transport

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.Result
import java.io.File

internal class OutputFileProvider {

    // todo should be passed with instrumentation params, see [ArtifactsTestListener]
    private val runnerOutputFolder = "runner"

    // todo should be passed with instrumentation params, see [ReportViewerTestReporter]
    private val reportFileName = "report.json"

    /**
     * special string, that indicates that file should be uploaded
     * and string should be changed with url later by test runner
     */
    fun toUploadPlaceholder(file: File): String {
        return "#upload:${file.absolutePath}"
    }

    fun provideReportDir(className: String, methodName: String): Result<File> {
        return try {
            val testFolderName = "$className#$methodName"
            val externalStorage = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
                ?: throw RuntimeException("External storage is not available")

            val runnerDirectory = File(externalStorage, runnerOutputFolder)

            val testMetadataDirectory = File(runnerDirectory, testFolderName).apply {
                mkdirs()
            }

            Result.Success(testMetadataDirectory)
        } catch (e: Throwable) {
            Result.Failure(e)
        }
    }

    fun provideReportFile(className: String, methodName: String): Result<File> {
        return provideReportDir(className, methodName).map { File(it, reportFileName) }
    }
}
