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
            /**
             * Official startup options documentation:
             * https://developer.android.com/studio/run/emulator-commandline#startup-options
             */
            args = listOf(
                "-avd ${avdConfig.emulatorFileName}",
                "-sdcard ${avdConfig.sdCardFileName}",
                "-no-window",
                "-no-snapshot",
                "-no-boot-anim",
                "-no-audio",
                /**
                 * Emulator will have 2Gb on the disk. Default is 256Mb
                 */
                "-partition-size 2048",
                /**
                 * Use CPU as GPU
                 */
                "-gpu swiftshader_indirect",
                "-verbose"
            )
        ).executeAsFlow()
    }
}
