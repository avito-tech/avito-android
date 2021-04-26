package com.avito.filestorage

import com.avito.android.Result
import okhttp3.HttpUrl
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

internal class HttpRemoteStorage(
    private val endpoint: HttpUrl,
    private val storageClient: FileStorageClient,
) : RemoteStorage {

    override fun upload(file: File, type: ContentType): FutureValue<Result<HttpUrl>> {
        val mediaType = type.toMediaType()
        return uploadInternal(
            storageClient.upload(
                content = file.asRequestBody(mediaType),
                extension = type.toExtension()
            )
        )
    }

    override fun upload(content: String, type: ContentType): FutureValue<Result<HttpUrl>> {
        val mediaType = type.toMediaType()
        return uploadInternal(
            storageClient.upload(
                content = content.toRequestBody(mediaType),
                extension = type.toExtension()
            )
        )
    }

    private fun uploadInternal(call: Call<String>): FutureValue<Result<HttpUrl>> {
        val futureValue = SettableFutureValue<Result<HttpUrl>>()
        call.enqueue(
            object : Callback<String> {

                override fun onFailure(call: Call<String>, throwable: Throwable) {
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
                        response.isSuccessful && response.body().isNullOrEmpty() ->
                            Result.Failure(IllegalStateException("Uploading failed response body is absent"))

                        else -> Result.Failure(RuntimeException("Uploading failed with response: ${response.body()}"))
                    }

                    futureValue.set(result)
                }
            }
        )
        return futureValue
    }
}
