package com.avito.emcee.worker.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.manager.AndroidDeviceManager
import com.avito.emcee.queue.DeviceConfiguration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class SingleInstanceAndroidDeviceTestExecutorProvider(
    private val manager: AndroidDeviceManager
) : TestExecutorProvider {

    private var last: AndroidDevice? = null

    override suspend fun provide(
        configuration: DeviceConfiguration
    ): TestExecutor {
        return StatefulAndroidDeviceTestExecutor(device = getDevice(configuration))
    }

    private suspend fun getDevice(configuration: DeviceConfiguration): AndroidDevice {
        val last = last
        return if (last == null || last.sdk == configuration.sdkVersion || last.type == configuration.type) {
            if (last != null) {
                manager.stop(last)
                this.last = null
            }
            val new = manager.findOrStart(configuration.sdkVersion, configuration.type)
            this.last = new
            new
        } else {
            last
        }
    }
}
