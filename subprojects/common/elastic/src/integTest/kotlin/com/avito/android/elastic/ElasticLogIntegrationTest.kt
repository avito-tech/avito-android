package com.avito.android.elastic

import com.avito.time.DefaultTimeProvider
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ElasticLogIntegrationTest {

    @Disabled
    @Test
    fun test() {
        val elastic: Elastic = MultipleEndpointsElastic(
            okHttpClient = OkHttpClient(),
            timeProvider = DefaultTimeProvider(),
            endpoints = listOf(requireNotNull(System.getProperty("avito.elastic.endpoints"))),
            indexPattern = requireNotNull(System.getProperty("avito.elastic.indexpattern")),
            buildId = "12345",
            onError = { msg, e ->
                println(msg)
                e?.printStackTrace()
            }
        )

        elastic.sendMessage(
            tag = "Test",
            level = "WARNING",
            message = "Test",
            throwable = null
        )
    }
}
