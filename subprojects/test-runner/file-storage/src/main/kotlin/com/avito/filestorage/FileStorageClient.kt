package com.avito.filestorage

import com.avito.http.internal.RequestMetadata
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Tag

internal interface FileStorageClient {

    @POST("/file/addBinary")
    @Headers("X-Source: $xSourceValue")
    fun upload(
        @Body content: RequestBody,
        @Header("X-Extension") extension: String,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-binary-$extension")
    ): Call<String>
}

private const val xSourceValue = "android_ui_tests"

private const val serviceName = "file-storage"
