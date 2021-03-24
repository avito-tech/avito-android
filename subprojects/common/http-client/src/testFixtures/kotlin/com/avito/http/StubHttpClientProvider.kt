package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    loggerFactory: LoggerFactory = StubLoggerFactory,
    timeProvider: TimeProvider = StubTimeProvider(),
    builderTransform: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this }
): HttpClientProvider {
    return HttpClientProvider(statsdSender, loggerFactory, timeProvider, builderTransform)
}
