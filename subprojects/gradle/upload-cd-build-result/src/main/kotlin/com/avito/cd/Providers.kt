package com.avito.cd

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object Providers {
    val gson: Gson by lazy {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
            .registerTypeAdapter(CdBuildConfig.Deployment::class.java, DeploymentDeserializer)
            .create()
    }

    private val clientBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
    }

    fun client(
        user: String,
        password: String,
        logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT
    ): OkHttpClient {
        return clientBuilder
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .addHeader("Authorization", Credentials.basic(user, password))
                        .build()
                )
            }
            .addInterceptor(HttpLoggingInterceptor(logger).apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
}
