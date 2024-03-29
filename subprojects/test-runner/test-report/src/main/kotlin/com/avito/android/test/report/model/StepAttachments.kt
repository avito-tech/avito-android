package com.avito.android.test.report.model

import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import java.util.concurrent.CopyOnWriteArrayList

/**
 * We modify [entries] and [uploads] concurrently
 * All collections must be thread-safe
 */
public class StepAttachments {
    public val entries: MutableList<Entry> = CopyOnWriteArrayList()
    public val uploads: MutableList<FutureValue<Entry.File>> = CopyOnWriteArrayList()
}
