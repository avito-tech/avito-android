package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage

internal class AvitoRemoteStorageTransport(
    private val avitoFileStorage: RemoteStorage
) : Transport {

    override fun sendReport(state: ReportState.Initialized.Started) {
        throw IllegalStateException("not implemented")
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorage.Request,
        comment: String
    ): FutureValue<RemoteStorage.Result> {
        return avitoFileStorage.upload(
            uploadRequest = request,
            comment = comment
        )
    }
}
