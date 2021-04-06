package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage

internal object StubTransport : Transport {

    override fun sendReport(state: ReportState.Initialized.Started) {
    }

    override fun sendContent(
        test: TestMetadata,
        request: RemoteStorage.Request,
        comment: String
    ): FutureValue<RemoteStorage.Result> {
        return FutureValue.create(stubError(request))
    }

    private fun stubError(request: RemoteStorage.Request) = RemoteStorage.Result.Error(
        comment = "stub",
        timeInSeconds = 0,
        uploadRequest = request,
        t = IllegalStateException("stub")
    )
}
