package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.avd.internal.command.StopAvdCommand
import com.avito.android.device.manager.internal.storage.AndroidDeviceCoordinates
import com.avito.android.device.manager.internal.storage.AndroidDeviceProcessStorage
import com.avito.cli.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.logging.Logger

@OptIn(ExperimentalCoroutinesApi::class)
internal class StopAndroidDevice(
    private val stopAvdCommand: StopAvdCommand,
    private val storage: AndroidDeviceProcessStorage,
) {

    private val logger = Logger.getLogger("StopAndroidDevice")

    suspend fun execute(device: AndroidDevice) {
        logger.info("Shutting down the emulator...")
        stopAvdCommand.execute(device.serial).collect { notification ->
            when (notification) {
                is Notification.Output -> logger.fine(notification.line)
                is Notification.Exit -> logger.fine(notification.output)
            }
        }
        withContext(Dispatchers.IO) {
            // Waiting for existing emulator shut down, before starting the new one.
            requireNotNull(storage.pop(AndroidDeviceCoordinates(device.sdk, device.type))).awaitTermination()
        }
    }
}
