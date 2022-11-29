package com.avito.emcee.worker.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.manager.AndroidDeviceManager
import com.avito.emcee.queue.DeviceConfiguration
import java.util.logging.Logger
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class SingleInstanceAndroidDeviceTestExecutorProvider(
    private val manager: AndroidDeviceManager
) : DeviceTestExecutorProvider {

    private val logger = Logger.getLogger("SingleInstanceAndroidDeviceTestExecutorProvider")

    private var last: AndroidDevice? = null

    override suspend fun provide(
        configuration: DeviceConfiguration
    ): TestExecutor {
        logger.info("Looking for device with $configuration")
        return StatefulAndroidDeviceTestExecutor(device = getDevice(configuration))
    }

    private suspend fun getDevice(configuration: DeviceConfiguration): AndroidDevice {
        if (isDeviceSuitableForConfiguration(last, configuration)) {
            logger.fine("Remembered device is suitable for $configuration")
            return last!!
        }
        return manager.findOrStart(configuration.sdkVersion, configuration.type)
            .also { device -> last = device }
    }

    private suspend fun isDeviceSuitableForConfiguration(
        device: AndroidDevice?,
        configuration: DeviceConfiguration
    ): Boolean {
        return device != null &&
            device.sdk == configuration.sdkVersion &&
            device.type == configuration.type &&
            device.isAlive()
    }
}
