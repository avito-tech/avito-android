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

    override fun upload(
        uploadRequest: RemoteStorageRequest,
        comment: String,
        deleteOnUpload: Boolean
    ): FutureValue<RemoteStorageResult> {

        val futureValue = SettableFutureValue<RemoteStorageResult>()

        val timestamp = timeProvider.nowInSeconds()

        logUploading(uploadRequest)

        when (uploadRequest) {
            is RemoteStorageRequest.FileRequest.Image -> storageClient.uploadPng(
                content = uploadRequest.file.asRequestBody(uploadRequest.mediaType)
            )
            is RemoteStorageRequest.FileRequest.Video -> storageClient.uploadMp4(
                content = uploadRequest.file.asRequestBody(uploadRequest.mediaType)
            )
            is RemoteStorageRequest.ContentRequest -> storageClient.upload(
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

                        futureValue.set(RemoteStorageResult.Error(comment, timestamp, uploadRequest, t))
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        val result = when {
                            response.isSuccessful && !response.body().isNullOrEmpty() -> {

                                // responseBody contains only a string with relative file path
                                // example: /static/m/2021-04-23/16-39/6082f85819b1d410fd11714e.png
                                val responseBody = response.body()!!

                                // addEncodedPathSegments accepts path segments separated with '/'
                                // shouldn't start with '/' though
                                val pathSegments = responseBody.trimStart('/')

                                val fullUrl = endpoint.newBuilder()
                                    .addEncodedPathSegments(pathSegments)
                                    .build()

                                logUploaded(
                                    uploadRequest = uploadRequest,
                                    url = fullUrl
                                )

                                RemoteStorageResult.Success(
                                    comment = comment,
                                    url = fullUrl,
                                    timeInSeconds = timestamp,
                                    uploadRequest = uploadRequest
                                )
                            }
                            response.isSuccessful && response.body().isNullOrEmpty() -> {
                                val exception = IllegalStateException("Uploading failed response body is absent")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)
                                RemoteStorageResult.Error(
                                    comment = comment,
                                    timeInSeconds = timestamp,
                                    uploadRequest = uploadRequest,
                                    t = exception
                                )
                            }
                            else -> {
                                val exception = RuntimeException("Uploading failed with response: ${response.body()}")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)
                                RemoteStorageResult.Error(
                                    comment = comment,
                                    timeInSeconds = timestamp,
                                    uploadRequest = uploadRequest,
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
        uploadRequest: RemoteStorageRequest,
        deleteOnUpload: Boolean
    ) {
        if (deleteOnUpload) {
            if (uploadRequest is RemoteStorageRequest.FileRequest) {
                uploadRequest.file.delete()
            }
        }
    }

    private fun logUploading(
        uploadRequest: RemoteStorageRequest
    ) {
        when (uploadRequest) {
            is RemoteStorageRequest.FileRequest ->
                logger.debug(
                    "RemoteStorage: Uploading file: ${uploadRequest.file.absolutePath} " +
                        "with size: ${uploadRequest.file.length()} bytes"
                )

            is RemoteStorageRequest.ContentRequest ->
                logger.debug(
                    "RemoteStorage: Uploading content with size: ${uploadRequest.content.length} " +
                        "with extension: ${uploadRequest.extension}"
                )
        }
    }

    private fun logUploaded(
        uploadRequest: RemoteStorageRequest,
        url: HttpUrl
    ) {
        when (uploadRequest) {
            is RemoteStorageRequest.FileRequest ->
                logger.debug(
                    "RemoteStorage: File: ${uploadRequest.file.absolutePath} uploaded to url: $url"
                )

            is RemoteStorageRequest.ContentRequest ->
                logger.debug(
                    "RemoteStorage: Content with size: ${uploadRequest.content.length} uploaded to url: $url"
                )
        }
    }

    private fun getUploadRequestErrorMessage(
        uploadRequest: RemoteStorageRequest,
        body: String? = null
    ) =
        when (uploadRequest) {
            is RemoteStorageRequest.FileRequest ->
                "RemoteStorage: Failed to upload file: ${uploadRequest.file.absolutePath}" +
                    if (body != null) " with body: $body" else ""
            is RemoteStorageRequest.ContentRequest ->
                "RemoteStorage: Failed to upload content with size: ${uploadRequest.content.length} " +
                    "as ${uploadRequest.extension}" +
                    if (body != null) " with body: $body" else ""
        }
}
