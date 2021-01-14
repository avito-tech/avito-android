package com.avito.logger.destination

import com.avito.android.sentry.SentryConfig
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class SentryDestinationTest {

    @Test
    fun `destination is serializable`() {
        val destination = SentryDestination(
            config = SentryConfig.Enabled(
                dsn = "stub",
                environment = "stub",
                serverName = "stub",
                release = "stub",
                tags = emptyMap()
            ),
            metadata = mapOf("tag" to "LoggingDestinationTest")
        )

        assertSerializable(destination)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
