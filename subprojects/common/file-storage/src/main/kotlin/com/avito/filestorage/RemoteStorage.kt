package com.avito.filestorage

import com.avito.android.Result
import okhttp3.HttpUrl

interface RemoteStorage {

    fun upload(
        uploadRequest: RemoteStorageRequest,
        deleteOnUpload: Boolean = true
    ): FutureValue<Result<HttpUrl>>
}
