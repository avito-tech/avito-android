package com.avito.runner.service.worker.device.adb.request.adb

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class LogcatAdbCommandTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `adb logcat command - serialized correctly - when lines count is null`() {
        val request = LogcatAdbRequest(null)
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub logcat -d")
    }

    @Test
    fun `adb logcat command - serialized correctly - when lines count is not null`() {
        val request = LogcatAdbRequest(5)
        val serialized = serializer.serialize(request)
        assertThat(serialized).isEqualTo("adb -s stub logcat -t 5")
    }
}
