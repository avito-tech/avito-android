package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GetPropAdbShellRequestTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `getprop adb command - serialized correctly`() {
        val request = GetPropAdbShellRequest("sys.boot_completed")
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub shell getprop sys.boot_completed")
    }
}
