package com.avito.http

import com.avito.android.stats.StatsDSender
import com.avito.http.internal.StatsHttpEventListener
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient

public class HttpClientProvider(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider
) {

    private val builder = OkHttpClient.Builder()

    public fun provide(): OkHttpClient.Builder {
        return builder.eventListenerFactory {
            StatsHttpEventListener(
                statsDSender = statsDSender,
                timeProvider = timeProvider
            )
        }
    }

    public companion object
}
