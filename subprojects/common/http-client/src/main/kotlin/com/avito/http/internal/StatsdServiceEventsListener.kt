package com.avito.http.internal

import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import java.io.IOException
import java.net.SocketTimeoutException

internal class StatsdServiceEventsListener(
    private val statsDSender: StatsDSender,
    private val prefix: SeriesName
) : ServiceEventsListener {

    override fun onResponse(code: Int, latencyMs: Long) {
        statsDSender.send(CountMetric(prefix.append("response", "$code")))
        statsDSender.send(TimeMetric(prefix.append("latency"), latencyMs))
    }

    override fun onTimeout(e: SocketTimeoutException) {
        statsDSender.send(CountMetric(prefix.append("response", "timeout")))
    }

    override fun onUnknownException(e: IOException) {
        statsDSender.send(CountMetric(prefix.append("response", "unknown")))
    }
}
