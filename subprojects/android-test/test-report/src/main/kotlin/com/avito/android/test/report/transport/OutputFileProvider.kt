package com.avito.android.test.report.transport

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal class OutputFileProvider {

    sealed class Result {
        data class Success(val file: File) : Result()
        data class Error(val e: Throwable) : Result()
    }

    // todo should be passed with instrumentation params, see [ArtifactsTestListener]
    private val runnerOutputFolder = "runner"

    // todo should be passed with instrumentation params, see [ReportViewerTestReporter]
    private val reportFileName = "report.json"

    fun provideReportFile(className: String, methodName: String): Result {
        return try {
            val testFolderName = "$className#$methodName"
            val externalStorage = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
                ?: throw RuntimeException("External storage is not available")

            val runnerDirectory = File(externalStorage, runnerOutputFolder)

            val testMetadataDirectory = File(runnerDirectory, testFolderName).apply {
                mkdirs()
            }

            Result.Success(File(testMetadataDirectory, reportFileName))
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }
}
