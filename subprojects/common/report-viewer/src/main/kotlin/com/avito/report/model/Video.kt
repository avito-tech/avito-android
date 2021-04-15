package com.avito.report.model

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("link") val fileAddress: FileAddress,
    @SerializedName("format") val format: String = "video/mp4"
)
