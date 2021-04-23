package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorageRequest
import com.avito.report.model.Entry

interface Transport {

    fun sendReport(state: Started)

    fun sendContent(
        test: TestMetadata,
        request: RemoteStorageRequest,
        comment: String
    ): FutureValue<Entry.File>
}
