package com.avito.report.model

public sealed class FileAddress {

    public data class File(val fileName: String) : FileAddress()

    public data class URL(val url: okhttp3.HttpUrl) : FileAddress()

    public data class Error(val error: Throwable) : FileAddress()
}
