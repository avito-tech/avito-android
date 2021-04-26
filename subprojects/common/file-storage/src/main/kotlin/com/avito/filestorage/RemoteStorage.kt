package com.avito.filestorage

import com.avito.android.Result
import okhttp3.HttpUrl
import java.io.File

public interface RemoteStorage {

    public fun upload(file: File, type: ContentType): FutureValue<Result<HttpUrl>>

    public fun upload(content: String, type: ContentType): FutureValue<Result<HttpUrl>>
}
