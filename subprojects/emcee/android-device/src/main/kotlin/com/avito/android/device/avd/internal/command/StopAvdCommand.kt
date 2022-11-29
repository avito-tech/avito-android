package com.avito.android.device.avd.internal.command

import com.avito.android.device.DeviceSerial
import com.avito.cli.FlowCommandLine
import com.avito.cli.Notification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@ExperimentalCoroutinesApi
internal class StopAvdCommand(
    private val androidSdk: Path
) {

    fun execute(serial: DeviceSerial): Flow<Notification> {
        val adbCommand = androidSdk.resolve("platform-tools").resolve("adb").absolutePathString()
        return FlowCommandLine(
            command = adbCommand,
            args = listOf(
                "-s", serial.value,
                "emu", "kill"
            )
        ).start()
    }
}
