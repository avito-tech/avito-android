package com.avito.filestorage

interface RemoteStorage {

    fun upload(
        uploadRequest: RemoteStorageRequest,
        comment: String,
        deleteOnUpload: Boolean = true
    ): FutureValue<RemoteStorageResult>
}
