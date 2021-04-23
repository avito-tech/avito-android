package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorageRequest
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress

internal object NoOpTransport : Transport, TransportMappers {

    override fun sendReport(state: Started) {
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorageRequest,
        comment: String
    ): FutureValue<Entry.File> {
        return FutureValue.create(
            Entry.File(
                comment = comment,
                fileAddress = FileAddress.Error(RuntimeException("File not available: NoOpTransport chosen")),
                timeInSeconds = 0,
                fileType = request.toFileType()
            )
        )
    }
}
