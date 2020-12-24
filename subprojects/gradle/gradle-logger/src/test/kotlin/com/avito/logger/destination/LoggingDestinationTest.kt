package com.avito.logger.destination

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.logger.LoggerMetadata
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.URL

class LoggingDestinationTest {

    @TestFactory
    fun `destinations are serializable`(): List<DynamicTest> {
        val metadata = LoggerMetadata("LoggingDestinationTest")
        return listOf(
            Slf4jDestination("name"),
            SentryDestination(
                config = SentryConfig.Enabled(
                    dsn = "stub",
                    environment = "stub",
                    serverName = "stub",
                    release = "stub",
                    tags = emptyMap()
                ),
                metadata = metadata
            ),
            ElasticDestination(
                config = ElasticConfig.Enabled(
                    endpoint = URL("http://stub"),
                    indexPattern = "pattern",
                    buildId = "id"
                ),
                metadata = metadata
            )
        ).map { destination ->
            dynamicTest("${destination.javaClass.simpleName} should be serializable") {
                assertSerializable(destination)
            }
        }
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
