package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.avd.internal.StartAvd
import com.avito.android.device.internal.AndroidDeviceImpl
import com.avito.cli.CommandLine.Notification.Public
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
internal class StartAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val startAvd: StartAvd,
    private val maximumRunningDevices: Int = 1
) {

    suspend fun execute(sdk: Int, type: String): AndroidDevice {
        return coroutineScope {
            val deviceEventsChannel: ReceiveChannel<List<Device>> = adb.execute(
                request = AsyncDeviceMonitorRequest(),
                scope = this
            )
            try {
                val activeDevice = async {
                    waitActiveDevice(deviceEventsChannel, sdk, type)
                }
                launch {
                    startAvd.execute(sdk, type)
                        .collect { notification ->
                            when (notification) {
                                is Public.Exit -> cancel()
                                is Public.Output -> {
                                    // TODO add logging
                                }
                            }
                        }
                }
                activeDevice.await()
            } finally {
                withContext(NonCancellable) {
                    if (!deviceEventsChannel.isClosedForReceive) {
                        deviceEventsChannel.cancel()
                    }
                }
            }
        }
    }

    private suspend fun waitActiveDevice(
        deviceEventsChannel: ReceiveChannel<List<Device>>,
        sdk: Int,
        type: String
    ): AndroidDeviceImpl {
        @Suppress("DEPRECATION")
        val activeDevice = deviceEventsChannel.first { currentDeviceList ->
            require(currentDeviceList.size <= maximumRunningDevices) {
                "Must be maximum $maximumRunningDevices running devices per worker"
            }
            currentDeviceList.singleOrNull() { it.state == DeviceState.DEVICE } != null
        }[0]
        return AndroidDeviceImpl(
            sdk = sdk,
            type = type,
            serial = DeviceSerial(activeDevice.serial),
            adb
        )
    }
}
