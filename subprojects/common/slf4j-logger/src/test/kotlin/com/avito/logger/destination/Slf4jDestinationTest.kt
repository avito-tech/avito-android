package com.avito.logger.destination

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

internal class Slf4jDestinationTest {

    @Test
    fun `destination is serializable`() {
        val destination = Slf4jDestination("name")

        assertSerializable(destination)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
