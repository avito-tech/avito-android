package com.avito.android.elastic

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ElasticLogIntegrationTest {

    @Disabled
    @Test
    fun test() {
        val elasticLog = ElasticLog(
            endpoints = listOf(requireNotNull(System.getProperty("avito.elastic.endpoints"))),
            indexPattern = requireNotNull(System.getProperty("avito.elastic.indexpattern")),
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
