package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorageRequest
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.time.TimeProvider
import com.google.gson.Gson

/**
 * Send all to device external storage
 * Test runner will read it and prepare reports
 */
internal class ExternalStorageTransport(
    private val gson: Gson,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory,
    private val testArtifactsProvider: TestArtifactsProvider
) : Transport, TransportMappers {

    private val logger = loggerFactory.create<ExternalStorageTransport>()

    private val testRuntimeDataBuilder = TestRuntimeDataBuilder(timeProvider)

    override fun sendReport(state: Started) {
        testArtifactsProvider.provideReportFile().fold(
            onSuccess = { file ->
                try {
                    val json = gson.toJson(testRuntimeDataBuilder.fromState(state))
                    logger.debug("Write report to file: $file")
                    file.writeText(json)
                } catch (e: Throwable) {
                    logger.critical("Can't write report runtime data; leads to LOST test", e)
                }
            },
            onFailure = { throwable ->
                logger.critical(
                    "Can't create output file for test runtime data; leads to LOST test",
                    throwable
                )
            }
        )
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorageRequest,
        comment: String
    ): FutureValue<Entry.File> {
        val fileName = when (request) {
            is RemoteStorageRequest.ContentRequest ->
                testArtifactsProvider.generateUniqueFile(extension = request.extension).fold(
                    { file ->
                        file.writeText(request.content)
                        file.name
                    },
                    { throwable ->
                        val errorMessage = "no-file"
                        logger.warn(errorMessage, throwable)
                        errorMessage
                    }
                )

            is RemoteStorageRequest.FileRequest ->
                request.file.name
        }
        return FutureValue.create(
            Entry.File(
                comment = comment,
                fileAddress = FileAddress.File(fileName),
                timeInSeconds = timeProvider.nowInSeconds(),
                fileType = request.toFileType()
            )
        )
    }
}
