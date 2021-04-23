package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import com.avito.time.TimeProvider
import java.io.File

internal class AvitoRemoteStorageTransport(
    private val avitoFileStorage: RemoteStorage,
    private val timeProvider: TimeProvider
) : Transport, TransportMappers {

    override fun sendReport(state: Started) {
        throw IllegalStateException("not implemented")
    }

    override fun sendContent(
        test: TestMetadata,
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return avitoFileStorage.upload(
            file = file,
            type = type.toContentType()
        ).toEntry(comment, timeProvider, type)
    }

    override fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return avitoFileStorage.upload(
            content = content,
            type = type.toContentType()
        ).toEntry(comment, timeProvider, type)
    }
}
