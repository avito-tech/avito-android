package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    timeProvider: TimeProvider = StubTimeProvider()
): HttpClientProvider {
    return HttpClientProvider(statsdSender, timeProvider)
}
