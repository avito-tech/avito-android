package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import okhttp3.OkHttpClient

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    loggerFactory: LoggerFactory = StubLoggerFactory,
    builderTransform: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this }
): HttpClientProvider {
    return HttpClientProvider(statsdSender, loggerFactory, builderTransform)
}
