package com.avito.filestorage

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.Executors

internal interface FileStorageClient {

    @POST("/file/addBinary")
    fun upload(@Header("X-Extension") extension: String, @Body content: String): Call<String>

    @POST("/file/addBinary")
    @Headers("X-Extension: png")
    fun uploadPng(@Body content: RequestBody): Call<String>

    @POST("/file/addBinary")
    @Headers("X-Extension: mp4")
    fun uploadMp4(@Body content: RequestBody): Call<String>

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
            .create(FileStorageClient::class.java)
    }
}
