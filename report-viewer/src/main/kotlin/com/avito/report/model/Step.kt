package com.avito.report.model

import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("number") val number: Int,
    @SerializedName("title") val title: String,
    @SerializedName("entry_list") val entryList: List<Entry>
)
