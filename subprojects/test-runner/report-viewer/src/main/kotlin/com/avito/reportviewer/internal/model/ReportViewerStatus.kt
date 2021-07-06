package com.avito.reportviewer.internal.model

import com.google.gson.annotations.SerializedName

internal enum class ReportViewerStatus(val intValue: Int) {

    @SerializedName("1")
    OK(1),

    @SerializedName("2")
    FAILURE(2),

    @SerializedName("3")
    ERROR(3),

    @SerializedName("4")
    OTHER(4),

    @SerializedName("5")
    PANIC(5),

    @SerializedName("10")
    LOST(10),

    @SerializedName("32")
    MANUAL(32),

    @SerializedName("100")
    SKIP(100);
}
