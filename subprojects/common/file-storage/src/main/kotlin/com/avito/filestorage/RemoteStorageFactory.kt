package com.avito.filestorage

import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object RemoteStorageFactory {

    fun create(
        endpoint: String,
        httpClientProvider: HttpClientProvider,
        loggerFactory: LoggerFactory,
        isAndroidRuntime: Boolean,
    ): RemoteStorage = HttpRemoteStorage(
        endpoint = requireNotNull(endpoint.toHttpUrlOrNull()) { "Can't parse endpoint: $endpoint" },
        httpClient = httpClientProvider.provide()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .build(),
        loggerFactory = loggerFactory,
        isAndroidRuntime = isAndroidRuntime
    )

    internal fun createClient(
        endpoint: HttpUrl,
        httpClient: OkHttpClient,
        isAndroidRuntime: Boolean,
    ): FileStorageClient {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(httpClient)
            .addConverterFactory(ToStringConverterFactory())
            .apply {
                if (isAndroidRuntime) {
                    /**
                     * By default [retrofit2.Callback] called on android main thread via handler.post()
                     *
                     * Why is this a problem here?
                     * App's main thread could crash at any moment, which will cause a scenario:
                     *
                     * 1) app's main thread crashed
                     * 2) InstrumentationTestRunner intercepts a crash and reports test crash
                     * 3) During report this client called to upload some files
                     * 4) Trying to call Callback via already dead main looper
                     * 5) Test thread hangs
                     */
                    callbackExecutor(Executors.newSingleThreadExecutor())
                }
            }

        return retrofitBuilder.build().create()
    }
}
