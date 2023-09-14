package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ClearDirectoryAdbShellRequestTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `'rm -rf' command - serialized correctly`() {
        val request = ClearDirectoryAdbShellRequest(Path.of("/storage/sdcard/path/to/test/directory"))
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub shell rm -rf /storage/sdcard/path/to/test/directory")
    }
}
