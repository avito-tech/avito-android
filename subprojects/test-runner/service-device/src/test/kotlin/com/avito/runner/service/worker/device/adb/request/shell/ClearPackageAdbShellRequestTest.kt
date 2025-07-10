package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ClearPackageAdbShellRequestTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `'pm clear' command - serialized correctly`() {
        val request = ClearPackageAdbShellRequest("com.avito.android")
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub shell pm clear com.avito.android")
    }
}
