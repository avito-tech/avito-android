package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import java.io.File

internal object NoOpTransport : Transport, TransportMappers {

    override fun sendReport(state: Started) {
    }

    override fun sendContent(
        test: TestMetadata,
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return sendContentInternal(comment, type)
    }

    override fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return sendContentInternal(comment, type)
    }

    private fun sendContentInternal(
        comment: String,
        type: Entry.File.Type
    ): FutureValue<Entry.File> {
        return FutureValue.create(
            Entry.File(
                comment = comment,
                fileAddress = FileAddress.Error(RuntimeException("File is not available: NoOpTransport chosen")),
                timeInSeconds = 0,
                fileType = type
            )
        )
    }
}
