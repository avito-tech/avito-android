package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.serialize.ReportSerializer
import com.avito.time.TimeProvider
import java.io.File

/**
 * Send all to device external storage
 * Test runner will read it and prepare reports
 */
class ExternalStorageTransport(
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory,
    private val testArtifactsProvider: TestArtifactsProvider,
    private val reportSerializer: ReportSerializer,
) : Transport, TransportMappers {

    private val logger = loggerFactory.create<ExternalStorageTransport>()

    private val testRuntimeDataBuilder = TestRuntimeDataBuilder(timeProvider)

    override fun sendReport(state: Started) {
        testArtifactsProvider.provideReportFile()
            .flatMap { file ->
                val testRuntimeData = testRuntimeDataBuilder.fromState(state)
                reportSerializer.serialize(testRuntimeData, file)
            }
            .fold(
                onSuccess = { file ->
                    logger.debug("Write report to file: $file")
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
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return FutureValue.create(
            Entry.File(
                comment = comment,
                fileAddress = FileAddress.File(file.name),
                timeInSeconds = timeProvider.nowInSeconds(),
                fileType = type
            )
        )
    }

    override fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        val fileName = testArtifactsProvider.generateUniqueFile(extension = type.extension()).fold(
            { file ->
                file.writeText(content)
                file.name
            },
            { throwable ->
                val errorMessage = "no-file"
                logger.warn(errorMessage, throwable)
                errorMessage
            }
        )
        return FutureValue.create(
            Entry.File(
                comment = comment,
                fileAddress = FileAddress.File(fileName),
                timeInSeconds = timeProvider.nowInSeconds(),
                fileType = type
            )
        )
    }
}
