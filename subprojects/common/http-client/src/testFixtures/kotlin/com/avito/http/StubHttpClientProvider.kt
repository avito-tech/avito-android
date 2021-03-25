package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    timeProvider: TimeProvider = StubTimeProvider(),
    loggerFactory: LoggerFactory = StubLoggerFactory
): HttpClientProvider {
    return HttpClientProvider(statsdSender, timeProvider, loggerFactory)
}
