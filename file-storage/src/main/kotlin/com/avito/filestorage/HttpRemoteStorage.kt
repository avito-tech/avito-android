package com.avito.filestorage

import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HttpRemoteStorage(
    private val endpoint: HttpUrl,
    httpClient: OkHttpClient,
    private val timeSource: TimeProvider = DefaultTimeProvider(),
    private val logger: (String, Throwable?) -> Unit
) : RemoteStorage {

    private val storageClient: FileStorageClient =
        FileStorageClient.create(
            endpoint = endpoint,
            httpClient = httpClient
        )

    /**
     * если нужен полный путь до файла
     * при отрисовки Entity в ReportViewer это не требуется, т.к. это делает фронт
     */
    fun fullUrl(result: RemoteStorage.Result.Success): String = "$endpoint${result.url}"

    fun isFileStorageHost(url: HttpUrl): Boolean = url.host() == endpoint.host()

    override fun upload(
        uploadRequest: RemoteStorage.Request,
        comment: String,
        deleteOnUpload: Boolean
    ): FutureValue<RemoteStorage.Result> {

        val futureValue = SettableFutureValue<RemoteStorage.Result>()

        val timestamp = timeSource.nowInSeconds()

        logUploading(uploadRequest)

        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest.Image -> storageClient.uploadPng(
                content = RequestBody.create(uploadRequest.mediaType, uploadRequest.file)
            )
            is RemoteStorage.Request.FileRequest.Video -> storageClient.uploadMp4(
                content = RequestBody.create(uploadRequest.mediaType, uploadRequest.file)
            )
            is RemoteStorage.Request.ContentRequest -> storageClient.upload(
                extension = uploadRequest.extension,
                content = uploadRequest.content
            )
        }
            .enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    logUploadingError(uploadRequest)

                    deleteUploadedFile(
                        uploadRequest = uploadRequest,
                        deleteOnUpload = deleteOnUpload
                    )

                    futureValue.set(RemoteStorage.Result.Error(t))
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val result = if (response.isSuccessful) {
                        val url = response.body()!!

                        logUploaded(
                            uploadRequest = uploadRequest,
                            url = url
                        )

                        RemoteStorage.Result.Success(
                            comment = comment,
                            url = url,
                            timeInSeconds = timestamp,
                            uploadRequest = uploadRequest
                        )
                    } else {
                        logUploadingError(
                            uploadRequest = uploadRequest,
                            body = response.body()
                        )

                        RemoteStorage.Result.Error(
                            t = RuntimeException("Uploading failed with response: ${response.body()}")
                        )
                    }

                    deleteUploadedFile(
                        uploadRequest = uploadRequest,
                        deleteOnUpload = deleteOnUpload
                    )

                    futureValue.set(result)
                }
            })

        return futureValue
    }

    private fun deleteUploadedFile(
        uploadRequest: RemoteStorage.Request,
        deleteOnUpload: Boolean
    ) {
        if (deleteOnUpload) {
            if (uploadRequest is RemoteStorage.Request.FileRequest) {
                uploadRequest.file.delete()
            }
        }
    }

    private fun logUploading(
        uploadRequest: RemoteStorage.Request
    ) {
        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest ->
                logger(
                    "RemoteStorage: Uploading file: ${uploadRequest.file.absolutePath} " +
                        "with size: ${uploadRequest.file.length()} bytes",
                    null
                )

            is RemoteStorage.Request.ContentRequest ->
                logger(
                    "RemoteStorage: Uploading content with size: ${uploadRequest.content.length} " +
                        "with extension: ${uploadRequest.extension}",
                    null
                )
        }
    }

    private fun logUploaded(
        uploadRequest: RemoteStorage.Request,
        url: String
    ) {
        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest ->
                logger(
                    "RemoteStorage: File: ${uploadRequest.file.absolutePath} uploaded to url: $url",
                    null
                )

            is RemoteStorage.Request.ContentRequest ->
                logger(
                    "RemoteStorage: Content with size: ${uploadRequest.content.length} uploaded to url: $url",
                    null
                )
        }
    }

    private fun logUploadingError(
        uploadRequest: RemoteStorage.Request,
        t: Throwable? = null,
        body: String? = null
    ) {
        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest ->
                logger(
                    "RemoteStorage: Filed to upload file: ${uploadRequest.file.absolutePath}" +
                        if (body != null) " with body: $body" else "",
                    t
                )

            is RemoteStorage.Request.ContentRequest ->
                logger(
                    "RemoteStorage: Filed to upload content with size: ${uploadRequest.content.length} " +
                        "as ${uploadRequest.extension}" +
                        if (body != null) " with body: $body" else "",
                    t
                )
        }
    }
}
