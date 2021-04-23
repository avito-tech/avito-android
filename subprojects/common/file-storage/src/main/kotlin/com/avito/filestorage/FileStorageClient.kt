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
    @Headers(
        "X-Source: $xSourceValue"
    )
    fun upload(
        @Header("X-Extension") extension: String,
        @Body content: String,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-binary-text")
    ): Call<String>

    @POST("/file/addBinary")
    @Headers(
        "X-Extension: png",
        "X-Source: $xSourceValue"
    )
    fun uploadPng(
        @Body content: RequestBody,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-binary-png")
    ): Call<String>

    @POST("/file/addBinary")
    @Headers(
        "X-Extension: mp4",
        "X-Source: $xSourceValue"
    )
    fun uploadMp4(
        @Body content: RequestBody,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-binary-mp4")
    ): Call<String>
}

private const val xSourceValue = "android_ui_tests"

private const val serviceName = "file-storage"
