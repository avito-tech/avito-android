package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.http.internal.RequestMetadataProvider
import com.avito.http.internal.StatsHttpEventListener
import com.avito.http.internal.TagRequestMetadataProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient

public class HttpClientProvider(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
) {

    private val sharedBuilder = OkHttpClient.Builder()

    public fun provide(
        requestMetadataProvider: RequestMetadataProvider = defaultRequestMetadataProvider()
    ): OkHttpClient.Builder {
        return modifyExisting(sharedBuilder, requestMetadataProvider)
    }

    public fun modifyExisting(
        builder: OkHttpClient.Builder,
        requestMetadataProvider: RequestMetadataProvider = defaultRequestMetadataProvider()
    ): OkHttpClient.Builder {
        return builder.eventListenerFactory {
            StatsHttpEventListener(
                statsDSender = statsDSender,
                timeProvider = timeProvider,
                requestMetadataProvider = requestMetadataProvider,
                loggerFactory = loggerFactory
            )
        }
    }

    private fun defaultRequestMetadataProvider(): RequestMetadataProvider {
        return TagRequestMetadataProvider()
    }

    public companion object
}
