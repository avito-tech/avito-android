package com.avito.reportviewer.internal.model

import com.avito.report.model.Entry
import com.google.gson.annotations.SerializedName

internal data class Step(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("number") val number: Int,
    @SerializedName("title") val title: String,
    @SerializedName("entry_list") val entryList: List<Entry>
)
