package com.avito.filestorage

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

public enum class ContentType {

    PNG, MP4, TXT, HTML;

    internal fun toMediaType(): MediaType = when (this) {
        PNG -> "image/png"
        MP4 -> "video/mp4"
        TXT -> "text/plain"
        HTML -> "text/html"
    }.toMediaType()

    internal fun toExtension(): String {
        return when (this) {
            PNG -> "png"
            MP4 -> "mp4"
            TXT -> "txt"
            HTML -> "txt"
        }
    }
}
