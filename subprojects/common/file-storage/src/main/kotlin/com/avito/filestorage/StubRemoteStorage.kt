package com.avito.filestorage

class StubRemoteStorage(private val logger: (String) -> Unit) : RemoteStorage {

    override fun upload(
        uploadRequest: RemoteStorage.Request,
        comment: String,
        deleteOnUpload: Boolean
    ): FutureValue<RemoteStorage.Result> {

        logger("upload $uploadRequest")

        return SettableFutureValue<RemoteStorage.Result>().apply {
            set(
                RemoteStorage.Result.Error(
                    comment = comment,
                    timeInSeconds = 0,
                    uploadRequest = uploadRequest,
                    t = RuntimeException("stub")
                )
            )
        }
    }
}
