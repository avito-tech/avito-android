package com.avito.runner.config

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

internal class TestsActionParamsSerializationTest {

    @Test
    fun `params is serializable`() {
        val params = RunnerInputParams.createStubInstance()

        assertSerializable(params)
    }

    private fun assertSerializable(value: Any) {
        ObjectOutputStream(ByteArrayOutputStream()).writeObject(value)
    }
}
