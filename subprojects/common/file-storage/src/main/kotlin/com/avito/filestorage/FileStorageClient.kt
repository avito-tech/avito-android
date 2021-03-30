package com.avito.filestorage

import com.avito.http.internal.RequestMetadata
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Tag
import java.util.concurrent.Executors

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

    companion object {

        fun create(
            endpoint: HttpUrl,
            httpClient: OkHttpClient
        ): FileStorageClient = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(httpClient)
            /**
             * Когда мы вызываем метод апи асинхронно, мы регистрируем коллбэк, в котором
             * обрабатываем результаты. По дефолту на андроиде этот коллбэк выполняется на
             * main thread приложения. Делает он это через handler.post(...).
             *
             * Почему это проблема тут?
             * Мейн тред приложения может упасть в любой момент, это значит, что возможна следующая ситуация:
             *
             * 1) Приложение упало.
             * 2) Мы пошли это репортить выполняя запрос через Retrofit клиент.
             * 3) Когда Retrofit получает результат он пытается вызвать наши каллбэки на мейнтреде
             * (который уже погиб и его лупер не работает).
             * 4) Мы зависаем навсегда.
             */
            .callbackExecutor(Executors.newSingleThreadExecutor())
            .addConverterFactory(ToStringConverterFactory())
            .build()
            .create()
    }
}

private const val xSourceValue = "android_ui_tests"

private const val serviceName = "file-storage"
