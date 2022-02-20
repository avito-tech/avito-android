package com.avito.emcee.worker.internal

import com.avito.emcee.device.AndroidDevice
import com.avito.emcee.queue.Device

internal class SingleInstanceAndroidDeviceTestExecutorProvider : TestExecutorProvider {

    override suspend fun provide(
        device: Device
    ): TestExecutor {
        return StatefulAndroidDeviceTestExecutor(
            device = get(device)
        )
    }

    private suspend fun get(device: Device): AndroidDevice {
        TODO("Unused $device")
    }
}
