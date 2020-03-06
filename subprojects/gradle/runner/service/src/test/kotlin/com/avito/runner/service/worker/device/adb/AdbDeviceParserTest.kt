package com.avito.runner.service.worker.device.adb

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class AdbDeviceParserTest {

    val parser = AdbDeviceParser()

    @Test
    fun `parse correctly`() {
        val givenadbs = parser.parse(givenAdbOutput)

        assertThat(givenadbs).hasSize(2)
        assertThat(givenadbs).hasSize(2)
        assertThat(givenadbs.first()).isEqualTo(
            AdbDeviceParams(
                "ce11182b8452d0070b",
                "SM_G950F",
                true
            )
        )

        assertThat(givenadbs.last()).isEqualTo(
            AdbDeviceParams(
                "10.21.100.53:5555",
                "Android_SDK_built_for_x86",
                true
            )
        )
    }

    private val givenAdbOutput = """List of devices attached
ce11182b8452d0070b     device usb:337641472X product:dreamltexx model:SM_G950F device:dreamlte transport_id:6
10.21.100.53:5555         device product:sdk_gphone_x86 model:Android_SDK_built_for_x86 device:generic_x86 transport_id:2

"""
}
