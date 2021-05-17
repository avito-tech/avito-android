package com.avito.instrumentation.internal

import com.avito.instrumentation.stub.createStubInstance
import com.avito.logger.StubLoggerFactory
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

internal class TestsActionParamsSerializationTest {

    private val loggerFactory = StubLoggerFactory

    @Test
    fun `params is serializable`() {
        val params = InstrumentationTestsAction.Params.createStubInstance(loggerFactory = loggerFactory)

        assertSerializable(params)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
