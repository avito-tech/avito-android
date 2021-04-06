package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import com.google.gson.Gson
import java.io.File
import java.util.UUID

/**
 * Send all to device external storage
 * Test runner will read it and prepare reports
 */
internal class ExternalStorageTransport(
    private val gson: Gson,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : Transport {

    private val logger = loggerFactory.create<ExternalStorageTransport>()

    private val outputFileProvider = OutputFileProvider()
    private val testRuntimeDataBuilder = TestRuntimeDataBuilder(timeProvider)

    override fun sendReport(state: ReportState.Initialized.Started) {
        outputFileProvider.provideReportFile(
            className = state.testMetadata.className,
            methodName = state.testMetadata.methodName!!
        ).fold(
            onSuccess = { file ->
                try {
                    val json = gson.toJson(testRuntimeDataBuilder.fromState(state))
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
        request: RemoteStorage.Request,
        comment: String
    ): FutureValue<RemoteStorage.Result> {
        return outputFileProvider.provideReportDir(
            className = test.className,
            methodName = test.methodName!!
        ).fold(
            onSuccess = { dir ->
                val url = when (request) {
                    is RemoteStorage.Request.ContentRequest -> {
                        val filename = "${UUID.randomUUID()}.${request.extension}"
                        val file = File(dir, filename)
                        file.writeText(request.content)
                        outputFileProvider.toUploadPlaceholder(file)
                    }

                    is RemoteStorage.Request.FileRequest -> {
                        // todo copy files to dir, or save it there in first place (later preferred)
                        outputFileProvider.toUploadPlaceholder(request.file)
                    }
                }

                FutureValue.create(
                    RemoteStorage.Result.Success(
                        comment = comment,
                        timeInSeconds = timeProvider.nowInSeconds(),
                        uploadRequest = request,
                        url = url
                    )
                )
            },
            onFailure = { throwable ->
                FutureValue.create(
                    RemoteStorage.Result.Error(
                        comment = comment,
                        timeInSeconds = timeProvider.nowInSeconds(),
                        uploadRequest = request,
                        t = throwable
                    )
                )
            }
        )
    }
}
