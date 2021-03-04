package com.avito.android.runner.report.factory

import com.avito.time.StubTimeProvider
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

internal class InMemoryReportFactoryTest {

    @Test
    fun `factory is serializable`() {
        val factory = InMemoryReportFactory(timeProvider = StubTimeProvider())

        assertSerializable(factory)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
