package com.avito.runner.service.worker.device.adb

import com.avito.runner.service.worker.device.Serial
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AdbDeviceParserTest {

    private val parser = AdbDeviceParser()

    @Test
    fun `parse correct output`() {
        val output =
            """List of devices attached
ce11182b8452d0070b     device usb:337641472X product:dreamltexx model:SM_G950F device:dreamlte transport_id:6
10.21.100.53:5555      device product:sdk_gphone_x86 model:Android_SDK_built_for_x86 device:generic_x86 transport_id:2
"""
        val devices = parser.parse(output)

        assertThat(devices).hasSize(2)
        assertThat(devices).hasSize(2)
        assertThat(devices.first()).isEqualTo(
            AdbDeviceParams(
                Serial.Local("ce11182b8452d0070b"),
                "SM_G950F",
                true
            )
        )

        assertThat(devices.last()).isEqualTo(
            AdbDeviceParams(
                Serial.Remote("10.21.100.53:5555"),
                "Android_SDK_built_for_x86",
                true
            )
        )
    }

    @Test
    fun `failed to connect`() {
        val output = """List of devices attached
adb server version (41) doesn't match this client (39); killing...
ADB server didn't ACK
Full server startup log: /tmp/adb.1000.log
Server had pid: 23164
--- adb starting (pid 23164) ---
adb I 03-20 19:05:57 23164 23164 main.cpp:57] Android Debug Bridge version 1.0.39
adb I 03-20 19:05:57 23164 23164 main.cpp:57] Version 1:8.1.0+r23-5~18.04
adb I 03-20 19:05:57 23164 23164 main.cpp:57] Installed as /usr/lib/android-sdk/platform-tools/adb
adb I 03-20 19:05:57 23164 23164 main.cpp:57] 
adb I 03-20 19:05:57 23164 23164 adb_auth_host.cpp:416] adb_auth_init...
adb I 03-20 19:05:57 23164 23164 adb_auth_host.cpp:174] read_key_file '/home/user/.android/adbkey'...
adb I 03-20 19:05:57 23164 23164 adb_auth_host.cpp:391] adb_auth_inotify_init...
adb server killed by remote request

* failed to start daemon
error: cannot connect to daemon"""

        val error = assertThrows<RuntimeException> {
            parser.parse(output)
        }
        assertThat(error).hasMessageThat().contains("cannot connect to daemon")
    }
}
