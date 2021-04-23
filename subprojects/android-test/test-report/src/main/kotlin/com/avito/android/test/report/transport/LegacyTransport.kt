package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorageRequest
import com.avito.report.model.Entry

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
        request: RemoteStorageRequest,
        comment: String
    ): FutureValue<Entry.File> {
        return remoteStorageTransport.sendContent(test, request, comment)
    }
}
