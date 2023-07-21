package com.avito.android.elastic

import com.avito.http.RetryInterceptor
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

internal class ElasticRoundRobinHttpServiceFactory(
    private val endpoints: List<HttpUrl>,
    private val okHttpClientBuilder: OkHttpClient.Builder,
    private val gson: Gson
) : ElasticServiceFactory {

    private val roundRobin = object : ThreadLocal<RoundRobinIterable<HttpUrl>>() {

        override fun initialValue(): RoundRobinIterable<HttpUrl> = RoundRobinIterable(endpoints)
    }

    override fun provide(): ElasticService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(
                okHttpClientBuilder
                    .addInterceptor(
                        RoundRobinInterceptor(
                            roundRobin = roundRobin,
                        )
                    )
                    .addInterceptor(
                        RetryInterceptor(
                            retries = 6,
                            allowedMethods = listOf("GET", "POST"),
                            delayMs = 500
                        ) { originalRequest ->
                            RoundRobinInterceptor.requestOnNextUrl(
                                originalRequest = originalRequest,
                                nextUrl = roundRobin.get().next()
                            )
                        }
                    )
                    .build()
            )
            .baseUrl("http://localhost") // real url will be set in [RoundRobinInterceptor]
            .build()
            .create()
}
