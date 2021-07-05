package com.avito.reportviewer.internal.model

import com.google.gson.annotations.SerializedName

internal enum class ConclusionStatus {

    @SerializedName("ok")
    OK,

    @SerializedName("fail")
    FAIL,

    @SerializedName("irrelevant")
    IRRELEVANT,
}
