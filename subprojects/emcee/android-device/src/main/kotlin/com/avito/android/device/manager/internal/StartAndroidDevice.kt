package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.avd.internal.StartAvd
import com.avito.android.device.internal.AndroidDeviceImpl
import com.avito.cli.Notification
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

@ExperimentalCoroutinesApi
internal class StartAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val startAvd: StartAvd,
    private val maximumRunningDevices: Int = 1
) {

    private val logger = Logger.getLogger("StartAndroidDevice")

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
                                is Notification.Exit -> {
                                    logger.info("start avd exits: \n ${notification.output}")
                                    cancel()
                                }

                                is Notification.Output -> {
                                    logger.info(notification.line)
                                }
                            }
                        }
                }
                activeDevice.await()
            } finally {
                logger.info("finally")
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
        val activeDevice = deviceEventsChannel.receiveAsFlow().first { currentDeviceList ->
            require(currentDeviceList.size <= maximumRunningDevices) {
                "Must be maximum $maximumRunningDevices running devices per worker"
            }
            currentDeviceList.singleOrNull { it.state == DeviceState.DEVICE } != null
        }[0]
        return AndroidDeviceImpl(
            sdk = sdk,
            type = type,
            serial = DeviceSerial(activeDevice.serial),
            adb
        )
    }
}
