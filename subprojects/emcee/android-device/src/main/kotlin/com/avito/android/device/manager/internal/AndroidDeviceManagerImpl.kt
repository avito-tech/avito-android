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
        logger.info("Start findOrStart sdk:$sdk, type:$type")
        return findAndroidDevice.execute(sdk, type)
            ?: startAndroidDevice.execute(sdk, type)
    }

    override suspend fun stop(device: AndroidDevice) {
        stopAndroidDevice.execute(device.serial)
    }
}
