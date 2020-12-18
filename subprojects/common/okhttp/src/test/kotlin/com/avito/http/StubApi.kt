package com.avito.http

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

internal interface StubApi {

    @POST("/")
    fun request(): Call<Void>

    @POST("/fallback")
    fun requestFallback(): Call<Void>
}

internal fun createApi(
    baseUrl: HttpUrl,
    modifyHttpClient: OkHttpClient.Builder.() -> OkHttpClient.Builder
): StubApi =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(OkHttpClient.Builder().modifyHttpClient().build())
        .validateEagerly(true)
        .build()
        .create(StubApi::class.java)
