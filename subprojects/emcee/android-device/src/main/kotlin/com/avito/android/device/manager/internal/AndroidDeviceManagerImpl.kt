package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.manager.AndroidDeviceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.logging.Logger

@ExperimentalCoroutinesApi
internal class AndroidDeviceManagerImpl(
    private val findAndroidDevice: FindAndroidDevice,
    private val stopAndroidDevice: StopAndroidDevice,
    private val startAndroidDevice: StartAndroidDevice,
) : AndroidDeviceManager {

    private val logger = Logger.getLogger("AndroidDeviceManager")

    override suspend fun findOrStart(
        sdk: Int,
        type: String,
    ): AndroidDevice {
        val foundDevice = findAndroidDevice.execute(sdk, type)
        if (foundDevice != null) {
            logger.info("Found an already started device: $foundDevice")
            return foundDevice
        }

        logger.info("Device did not found. Starting a new one with sdk=$sdk and type=$type")
        return startAndroidDevice.execute(sdk, type)
    }

    override suspend fun stop(device: AndroidDevice) {
        logger.info("Stopping device: $device")
        stopAndroidDevice.execute(device)
    }
}
