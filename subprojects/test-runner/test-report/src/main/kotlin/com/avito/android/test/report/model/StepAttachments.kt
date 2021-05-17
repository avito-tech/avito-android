package com.avito.android.test.report.model

import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import java.util.concurrent.CopyOnWriteArrayList

class StepAttachments {
    val entries: MutableList<Entry> = CopyOnWriteArrayList()
    val uploads: MutableList<FutureValue<Entry.File>> = CopyOnWriteArrayList()
}
