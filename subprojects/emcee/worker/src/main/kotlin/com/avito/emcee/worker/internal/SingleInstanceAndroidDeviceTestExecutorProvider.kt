package com.avito.emcee.worker.internal

import com.avito.emcee.device.AndroidDevice
import com.avito.emcee.queue.DeviceConfiguration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class SingleInstanceAndroidDeviceTestExecutorProvider(
    private val adb: AndroidDebugBridgeClient,
) : TestExecutorProvider {

    override suspend fun provide(
        device: DeviceConfiguration
    ): TestExecutor {
        return StatefulAndroidDeviceTestExecutor(
            device = get(device)
        )
    }

    private suspend fun get(device: DeviceConfiguration): AndroidDevice {
        val devices: List<DeviceConfiguration> = adb.execute(request = ListDevicesRequest())
    }
}
