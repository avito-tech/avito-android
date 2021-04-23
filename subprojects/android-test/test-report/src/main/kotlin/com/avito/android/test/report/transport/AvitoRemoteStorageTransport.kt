package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageRequest
import com.avito.filestorage.map
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.time.TimeProvider

internal class AvitoRemoteStorageTransport(
    private val avitoFileStorage: RemoteStorage,
    private val timeProvider: TimeProvider
) : Transport, TransportMappers {

    override fun sendReport(state: Started) {
        throw IllegalStateException("not implemented")
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorageRequest,
        comment: String
    ): FutureValue<Entry.File> {
        return avitoFileStorage.upload(
            uploadRequest = request
        ).map { result ->
            val fileAddress = result.fold(
                onSuccess = { FileAddress.URL(it) },
                onFailure = { FileAddress.Error(it) }
            )

            Entry.File(
                comment = comment,
                fileAddress = fileAddress,
                timeInSeconds = timeProvider.nowInSeconds(),
                fileType = request.toFileType()
            )
        }
    }
}
