package com.avito.report.model

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("link") val link: String,
    @SerializedName("format") val format: String = "video/mp4"
)
