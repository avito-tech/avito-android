package com.avito.logger.destination

import com.avito.android.elastic.ElasticConfig
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.URL

internal class ElasticDestinationTest {

    @Test
    fun `destination is serializable`() {
        val destination = ElasticDestination(
            config = ElasticConfig.Enabled(
                endpoints = listOf(URL("http://stub")),
                indexPattern = "pattern",
                buildId = "id"
            ),
            metadata = mapOf("tag" to "LoggingDestinationTest")
        )

        assertSerializable(destination)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
