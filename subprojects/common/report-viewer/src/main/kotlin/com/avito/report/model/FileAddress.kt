package com.avito.report.model

import okhttp3.HttpUrl

sealed class FileAddress {
    data class File(val fileName: String) : FileAddress()
    data class URL(val url: HttpUrl) : FileAddress()
    data class Error(val error: Throwable) : FileAddress()
}
