package com.avito.emcee.worker.internal.artifacts

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

internal interface FileDownloaderApi {

    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl: HttpUrl): Response<ResponseBody>

    companion object {

        fun Retrofit.Builder.createFileDownloaderApi(client: OkHttpClient, baseUrl: String): FileDownloaderApi {
            return client(client)
                .baseUrl(baseUrl)
                .build()
                .create(FileDownloaderApi::class.java)
        }
    }
}
