package com.avito.reportviewer.internal.model

import com.avito.report.model.FileAddress
import com.google.gson.annotations.SerializedName

internal data class Video(
    @SerializedName("link") val link: FileAddress,
    @SerializedName("format") val format: String = "video/mp4"
)
