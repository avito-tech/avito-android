package com.avito.report.model

import okhttp3.HttpUrl

public sealed class FileAddress {
    public data class File(val fileName: String) : FileAddress()
    public data class URL(val url: HttpUrl) : FileAddress()
    public data class Error(val error: Throwable) : FileAddress()
}
