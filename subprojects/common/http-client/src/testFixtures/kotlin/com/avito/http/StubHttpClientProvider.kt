package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.logger.LoggerFactory
import com.avito.logger.PrintlnLoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    timeProvider: TimeProvider = DefaultTimeProvider(),
    loggerFactory: LoggerFactory = PrintlnLoggerFactory
): HttpClientProvider {
    return HttpClientProvider(statsdSender, timeProvider, loggerFactory)
}
