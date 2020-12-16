package com.avito.utils.logging

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.net.URL

class CILoggingDestinationTest {

    @Test
    fun `destinations are serializable`() {
        assertSerializable(StdoutDestination)
        assertSerializable(StderrDestination)
        assertSerializable(OnlyMessageStdoutDestination)
        assertSerializable(
            SentryDestination(
                config = SentryConfig.Enabled(
                    dsn = "stub",
                    environment = "stub",
                    serverName = "stub",
                    release = "stub",
                    tags = emptyMap()
                )
            )
        )
        assertSerializable(
            FileDestination(
                file = File(".")
            )
        )
        assertSerializable(
            ElasticDestination(
                config = ElasticConfig.Enabled(
                    endpoint = URL("http://stub"),
                    indexPattern = "pattern",
                    buildId = "id"
                ),
                tag = "tag",
                level = "DEBUG"
            )
        )
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
