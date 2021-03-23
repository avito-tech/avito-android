package com.avito.android.test.report.transport

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.Result
import java.io.File

internal class OutputFileProvider {

    // todo should be passed with instrumentation params, see [ArtifactsTestListener]
    private val runnerOutputFolder = "runner"

    // todo should be passed with instrumentation params, see [ReportViewerTestReporter]
    private val reportFileName = "report.json"

    fun provideReportFile(className: String, methodName: String): Result<File> {
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
            Result.Failure(e)
        }
    }
}
