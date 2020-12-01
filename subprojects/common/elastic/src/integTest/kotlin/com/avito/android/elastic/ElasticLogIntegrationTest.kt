package com.avito.android.elastic

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ElasticLogIntegrationTest {

    private val endpoint = requireNotNull(System.getProperty("avito.elastic.endpoints"))
    private val indexPattern = requireNotNull(System.getProperty("avito.elastic.indexpattern"))

    @Disabled
    @Test
    fun test() {
        val elasticLog = ElasticLog(
            endpoints = listOf(endpoint),
            indexPattern = indexPattern,
            buildId = "12345",
            verboseHttpLog = { println(it) },
            onError = { msg, e ->
                println(msg)
                e?.printStackTrace()
            }
        )

        elasticLog.sendMessage(
            tag = "Test",
            level = "WARNING",
            message = "Test",
            throwable = null
        )
    }
}
