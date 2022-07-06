package com.avito.android.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.transport.ExternalStorageTransport
import com.avito.android.test.report.transport.Transport
import com.avito.android.test.report.transport.TransportMappers
import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import java.io.File

/**
 * Save report to extrnal storage
 * Send all data to remote storage
 *
 * Legacy way; see [ExternalStorageTransport]
 */
internal class LegacyTransport(
    private val remoteStorageTransport: AvitoRemoteStorageTransport,
    private val externalStorageTransport: ExternalStorageTransport
) : Transport, TransportMappers {

    override fun sendReport(state: Started) {
        // todo handle result
        externalStorageTransport.sendReport(state)
    }

    override fun sendContent(
        test: TestMetadata,
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return remoteStorageTransport.sendContent(test, file, type, comment)
    }

    override fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File> {
        return remoteStorageTransport.sendContent(test, content, type, comment)
    }
}
