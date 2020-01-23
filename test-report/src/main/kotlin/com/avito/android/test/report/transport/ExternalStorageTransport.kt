package com.avito.android.test.report.transport

import android.annotation.SuppressLint
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.report.ReportState
import com.avito.report.model.TestRuntimeDataPackage
import com.google.gson.Gson
import java.io.File

/**
 * Сохраняем репорт в json, чтобы потом достать из раннера
 */
class ExternalStorageTransport(private val gson: Gson) : Transport, PreTransportMappers {

    @SuppressLint("LogNotTimber")
    override fun send(state: ReportState.Initialized.Started) {
        val testFolderName = "${state.testMetadata.className}#${state.testMetadata.methodName}"

        val testMetadataDirectory = testMetadataDirectory(testFolderName = testFolderName)

        val reportMetadataJson = File(testMetadataDirectory, REPORT_FILE_NAME)

        val testRuntimeDataPackage = TestRuntimeDataPackage(
            incident = state.incident,
            dataSetData = state.dataSet?.serialize() ?: emptyMap(),
            performanceJson = state.performanceJson,
            video = state.video,
            preconditions = transformStepList(state.preconditionStepList),
            steps = transformStepList(state.testCaseStepList),
            startTime = state.startTime,
            endTime = state.endTime
        )

        Log.v(TAG, "Write report for test $testFolderName to file: ${reportMetadataJson.absolutePath}")

        reportMetadataJson.writeText(gson.toJson(testRuntimeDataPackage))
    }

    private fun testMetadataDirectory(testFolderName: String): File {
        val externalStorage = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
            ?: throw RuntimeException("External storage is not available")

        val runnerDirectory = File(
            externalStorage,
            RUNNER_OUTPUT_FOLDER
        )

        return File(runnerDirectory, testFolderName).apply {
            mkdirs()
        }
    }

    companion object {
        //todo наверное можно прокинуть в instrumentation params
        private const val RUNNER_OUTPUT_FOLDER = "runner"
        private const val REPORT_FILE_NAME = "report.json"
        private const val TAG = "StorageJsonTransport"
    }
}
