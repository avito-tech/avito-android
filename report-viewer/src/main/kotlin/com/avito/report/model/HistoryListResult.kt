package com.avito.report.model

import com.google.gson.annotations.SerializedName

data class HistoryListResult(
    @SerializedName("test_name") val testName: String,
    val records: List<HistoryTest>?
)
