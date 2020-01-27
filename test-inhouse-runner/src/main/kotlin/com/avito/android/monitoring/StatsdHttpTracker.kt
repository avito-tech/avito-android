package com.avito.android.monitoring

import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import okhttp3.HttpUrl
import okhttp3.Response

class StatsdHttpTracker(private val statsd: StatsDSender) : HttpTracker {

    override fun trackRequest(response: Response) {
        val durationMs = response.receivedResponseAtMillis() - response.sentRequestAtMillis()

        val urlKey = convertUrlToMetricKey(response.request().url())

        statsd.send(
            "test.http.${urlKey}",
            CountMetric(response.code().toString())
        )
        statsd.send(
            "test.http.duration",
            TimeMetric(urlKey, durationMs)
        )
    }
}

internal fun convertUrlToMetricKey(url: HttpUrl): String {
    val parts = mutableListOf<String>()
    parts.add(url.host().substringBefore('.'))
    parts.addAll(url.pathSegments().filterNot { it.isDigitsOnly() })
    return parts.joinToString(separator = "_").replace(Regex("[^a-z_]+"), "_")
}

private fun CharSequence.isDigitsOnly() = none { !it.isDigit() }
