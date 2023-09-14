package com.avito.runner.service.worker.device.adb.request.adb

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class PullAdbCommandTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `adb pull command - serialized correctly`() {
        val request = PullAdbRequest(
            from = Path.of("/storage/sdcard/path/to/test/directory"),
            to = Path.of("/mnt/project/output")
        )
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub pull /storage/sdcard/path/to/test/directory /mnt/project/output")
    }
}
