package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import java.io.File

public interface Transport {

    public fun sendReport(state: Started)

    public fun sendContent(
        test: TestMetadata,
        file: File,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File>

    public fun sendContent(
        test: TestMetadata,
        content: String,
        type: Entry.File.Type,
        comment: String
    ): FutureValue<Entry.File>
}
