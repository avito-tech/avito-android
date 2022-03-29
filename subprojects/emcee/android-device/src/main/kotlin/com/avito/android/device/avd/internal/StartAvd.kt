package com.avito.android.device.avd.internal

import com.avito.android.device.avd.internal.AvdConfigurationProvider.ConfigurationKey
import com.avito.cli.CommandLine
import com.avito.cli.executeAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@ExperimentalCoroutinesApi
internal class StartAvd(
    private val configurationProvider: AvdConfigurationProvider,
    private val androidSdk: Path
) {

    fun execute(sdk: Int, type: String): Flow<CommandLine.Notification.Public> {
        val avdConfig = configurationProvider.provide(ConfigurationKey(sdk, type))
        return CommandLine.create(
            command = androidSdk.resolveSibling("emulator").absolutePathString(),
            args = listOf(
                "-avd ${avdConfig.emulatorFileName}",
                "-sdcard ${avdConfig.sdCardFileName}",
                "-no-window",
                "-no-snapshot",
                "-no-boot-anim",
                "-no-audio",
                "-partition-size 2048",
                "-gpu swiftshader_indirect",
                "-verbose"
            )
        ).executeAsFlow()
    }
}
