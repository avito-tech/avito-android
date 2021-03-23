package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import okhttp3.OkHttpClient

public fun HttpClientProvider.Companion.createStubInstance(
    statsdSender: StatsDSender = StubStatsdSender(),
    builderTransform: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it }
): HttpClientProvider {
    return HttpClientProvider(statsdSender, builderTransform)
}
