package com.avito.filestorage

import com.avito.android.Result
import okhttp3.HttpUrl
import java.io.File

interface RemoteStorage {

    fun upload(file: File, type: ContentType): FutureValue<Result<HttpUrl>>

    fun upload(content: String, type: ContentType): FutureValue<Result<HttpUrl>>
}
