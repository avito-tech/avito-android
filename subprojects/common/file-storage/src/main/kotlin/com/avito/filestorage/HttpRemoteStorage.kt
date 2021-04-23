package com.avito.filestorage

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import okhttp3.HttpUrl
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class HttpRemoteStorage(
    private val endpoint: HttpUrl,
    private val storageClient: FileStorageClient,
    loggerFactory: LoggerFactory,
) : RemoteStorage {

    private val logger = loggerFactory.create<HttpRemoteStorage>()

    override fun upload(
        uploadRequest: RemoteStorageRequest,
        deleteOnUpload: Boolean
    ): FutureValue<Result<HttpUrl>> {

        val futureValue = SettableFutureValue<Result<HttpUrl>>()

        when (uploadRequest) {
            is RemoteStorageRequest.FileRequest -> storageClient.upload(
                extension = uploadRequest.mediaType.subtype,
                content = uploadRequest.file.asRequestBody(uploadRequest.mediaType)
            )
            is RemoteStorageRequest.ContentRequest -> storageClient.upload(
                extension = uploadRequest.extension,
                content = uploadRequest.content.toRequestBody()
            )
        }
            .enqueue(
                object : Callback<String> {

                    override fun onFailure(call: Call<String>, throwable: Throwable) {
                        logger.warn(getUploadRequestErrorMessage(uploadRequest), throwable)

                        deleteUploadedFile(
                            uploadRequest = uploadRequest,
                            deleteOnUpload = deleteOnUpload
                        )

                        futureValue.set(Result.Failure(throwable))
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

                                Result.Success(fullUrl)
                            }
                            response.isSuccessful && response.body().isNullOrEmpty() -> {
                                val exception = IllegalStateException("Uploading failed response body is absent")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)

                                Result.Failure(exception)
                            }
                            else -> {
                                val exception = RuntimeException("Uploading failed with response: ${response.body()}")
                                logger.warn(getUploadRequestErrorMessage(uploadRequest, response.body()), exception)

                                Result.Failure(exception)
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
