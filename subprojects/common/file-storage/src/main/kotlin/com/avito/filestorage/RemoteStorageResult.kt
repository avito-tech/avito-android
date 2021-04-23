package com.avito.filestorage

import okhttp3.HttpUrl

sealed class RemoteStorageResult {

    abstract val comment: String
    abstract val timeInSeconds: Long
    abstract val uploadRequest: RemoteStorageRequest

    /**
     * @param url relative to the host
     */
    class Success(
        override val comment: String,
        override val timeInSeconds: Long,
        override val uploadRequest: RemoteStorageRequest,
        val url: HttpUrl,
    ) : RemoteStorageResult()

    class Error(
        override val comment: String,
        override val timeInSeconds: Long,
        override val uploadRequest: RemoteStorageRequest,
        val t: Throwable,
    ) : RemoteStorageResult()
}
