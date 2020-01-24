package com.avito.android.test.report.model

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry

/**
 * @param number step number; must be same as in test case
 * @param title in http://links.k.avito.ru/9O: "Step Action"; value must be the same (can be trimmed)
 */
data class StepResult(
    var timestamp: Long? = null,
    var number: Int? = null,
    var title: String? = null,
    var entryList: MutableList<Entry> = mutableListOf(),
    val futureUploads: MutableList<FutureValue<RemoteStorage.Result>> = mutableListOf()
)
