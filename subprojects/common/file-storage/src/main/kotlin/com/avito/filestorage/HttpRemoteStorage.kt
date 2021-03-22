package com.avito.filestorage

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HttpRemoteStorage(
    private val endpoint: HttpUrl,
    httpClient: OkHttpClient,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : RemoteStorage {

    private val logger = loggerFactory.create<HttpRemoteStorage>()

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

    override fun upload(
        uploadRequest: RemoteStorage.Request,
        comment: String,
        deleteOnUpload: Boolean
    ): FutureValue<RemoteStorage.Result> {

        val futureValue = SettableFutureValue<RemoteStorage.Result>()

        val timestamp = timeProvider.nowInSeconds()

        logUploading(uploadRequest)

        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest.Image -> storageClient.uploadPng(
                content = uploadRequest.file.asRequestBody(uploadRequest.mediaType)
            )
            is RemoteStorage.Request.FileRequest.Video -> storageClient.uploadMp4(
                content = uploadRequest.file.asRequestBody(uploadRequest.mediaType)
            )
            is RemoteStorage.Request.ContentRequest -> storageClient.upload(
                extension = uploadRequest.extension,
                content = uploadRequest.content
            )
        }
            .enqueue(
                object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        logger.warn(getUploadRequestErrorMessage(uploadRequest), t)

                        deleteUploadedFile(
                            uploadRequest = uploadRequest,
                            deleteOnUpload = deleteOnUpload
                        )

                        futureValue.set(RemoteStorage.Result.Error(comment, timestamp, t))
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        val result = when {
                            response.isSuccessful && !response.body().isNullOrEmpty() -> {
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
                            }
                            response.isSuccessful && response.body().isNullOrEmpty() -> {
                                val exception = IllegalStateException("Uploading failed response body is absent")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)
                                RemoteStorage.Result.Error(
                                    comment = comment,
                                    timeInSeconds = timestamp,
                                    t = exception
                                )
                            }
                            else -> {
                                val exception = RuntimeException("Uploading failed with response: ${response.body()}")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)
                                RemoteStorage.Result.Error(
                                    comment = comment,
                                    timeInSeconds = timestamp,
                                    t = exception
                                )
                            }
                        }

                        deleteUploadedFile(
                            uploadRequest = uploadRequest,
                            deleteOnUpload = deleteOnUpload
                        )

                        futureValue.set(result)
                    }
                }
            )

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
                logger.debug(
                    "RemoteStorage: Uploading file: ${uploadRequest.file.absolutePath} " +
                        "with size: ${uploadRequest.file.length()} bytes"
                )

            is RemoteStorage.Request.ContentRequest ->
                logger.debug(
                    "RemoteStorage: Uploading content with size: ${uploadRequest.content.length} " +
                        "with extension: ${uploadRequest.extension}"
                )
        }
    }

    private fun logUploaded(
        uploadRequest: RemoteStorage.Request,
        url: String
    ) {
        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest ->
                logger.debug(
                    "RemoteStorage: File: ${uploadRequest.file.absolutePath} uploaded to url: $url"
                )

            is RemoteStorage.Request.ContentRequest ->
                logger.debug(
                    "RemoteStorage: Content with size: ${uploadRequest.content.length} uploaded to url: $url"
                )
        }
    }

    private fun getUploadRequestErrorMessage(
        uploadRequest: RemoteStorage.Request,
        body: String? = null
    ) =
        when (uploadRequest) {
            is RemoteStorage.Request.FileRequest ->
                "RemoteStorage: Failed to upload file: ${uploadRequest.file.absolutePath}" +
                    if (body != null) " with body: $body" else ""
            is RemoteStorage.Request.ContentRequest ->
                "RemoteStorage: Failed to upload content with size: ${uploadRequest.content.length} " +
                    "as ${uploadRequest.extension}" +
                    if (body != null) " with body: $body" else ""
        }
}
